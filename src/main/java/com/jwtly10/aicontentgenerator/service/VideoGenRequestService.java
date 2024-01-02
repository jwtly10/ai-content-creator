package com.jwtly10.aicontentgenerator.service;


import com.jwtly10.aicontentgenerator.exceptions.UserServiceException;
import com.jwtly10.aicontentgenerator.model.Reddit.RedditTitle;
import com.jwtly10.aicontentgenerator.model.UserVideo;
import com.jwtly10.aicontentgenerator.model.VideoProcessingState;
import com.jwtly10.aicontentgenerator.model.api.request.VideoGenRequest;
import com.jwtly10.aicontentgenerator.model.api.response.VideoGenResponse;
import com.jwtly10.aicontentgenerator.service.Reddit.RedditVideoGenerator;
import com.jwtly10.aicontentgenerator.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class VideoGenRequestService {

    private final RedditVideoGenerator redditVideoGenerator;

    private final StorageService storageService;

    private final UserService userService;

    public VideoGenRequestService(RedditVideoGenerator redditVideoGenerator, StorageService storageService, UserService userService) {
        this.redditVideoGenerator = redditVideoGenerator;
        this.storageService = storageService;
        this.userService = userService;
    }

    public ResponseEntity<VideoGenResponse> requestVideoGeneration(VideoGenRequest req) {
        RedditTitle redditTitle = new RedditTitle();
        redditTitle.setTitle(req.getTitle());
        redditTitle.setSubreddit(req.getSubreddit());

        // TODO: get background video URL from id

        String test_video_loc = storageService.downloadVideo("test_short_video.mp4", "test-media/");

        String processUUID = FileUtils.generateUUID();
        userService.logNewVideoProcess(userService.getLoggedInUserId(), processUUID);

        String videoUUID;
        try {
            videoUUID = redditVideoGenerator.generateContent(processUUID, redditTitle, req.getContent(), test_video_loc);
        } catch (Exception e) {
            log.error("Error generating video: {}", e.getMessage());
            userService.updateVideoProcessLog(userService.getLoggedInUserId(), processUUID, VideoProcessingState.FAILED, e.getMessage());
            return ResponseEntity.ok(VideoGenResponse.builder()
                    .error(e.getMessage())
                    .build());
        }

        return ResponseEntity.ok(VideoGenResponse.builder()
                .processId(videoUUID)
                .build());
    }

    public ResponseEntity<VideoGenResponse> checkStatus(String processId) {
        VideoGenResponse res;
        try {
            res = userService.checkStatus(processId);
        } catch (Exception e) {
            log.error("Error checking status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(VideoGenResponse.builder()
                    .error(e.getMessage())
                    .build());
        }

        return ResponseEntity.ok(res);
    }

    public ResponseEntity<?> downloadVideo(String processId) {
        try {
            return proxyDownload(processId);
        } catch (Exception e) {
            log.error("Error downloading video: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(VideoGenResponse.builder()
                    .error(e.getMessage())
                    .build());
        }
    }

    /**
     * Download video
     *
     * @param processId Process ID
     * @throws UserServiceException if unable to download video for authenticated user
     */
    public ResponseEntity<byte[]> proxyDownload(String processId) throws UserServiceException {
        Optional<UserVideo> userVideo = userService.getVideo(processId);
        if (userVideo.isEmpty()) {
            throw new UserServiceException("Process ID not found for authenticated user");
        } else if (userVideo.get().getState() != VideoProcessingState.COMPLETED) {
            throw new UserServiceException("Video processing not completed");
        } else if (userVideo.get().getFile_name() == null) {
            throw new UserServiceException("Video file name not found");
        }

        // This should be secure, as we will have already validated the user ID and process ID
        try {
            byte[] fileContent = storageService.proxyDownload(userVideo.get().getFile_name());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", userVideo.get().getFile_name());

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            throw new UserServiceException("Unable to download video for authenticated user");
        }
    }

}
