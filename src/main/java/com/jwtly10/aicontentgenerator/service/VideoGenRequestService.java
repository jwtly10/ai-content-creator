package com.jwtly10.aicontentgenerator.service;


import com.jwtly10.aicontentgenerator.model.Reddit.RedditTitle;
import com.jwtly10.aicontentgenerator.model.VideoProcessingState;
import com.jwtly10.aicontentgenerator.model.api.request.VideoGenRequest;
import com.jwtly10.aicontentgenerator.model.api.response.VideoGenResponse;
import com.jwtly10.aicontentgenerator.service.Reddit.RedditVideoGenerator;
import com.jwtly10.aicontentgenerator.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

        String test_video_loc = storageService.downloadVideo("test_short_video.mp4", "test-media/").orElseThrow();

        String processUUID = FileUtils.getUUID();
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
                .videoUUID(videoUUID)
                .build());
    }

    public VideoGenResponse downloadVideo() {
        //TODO: download video from storage service
        return null;
    }


}
