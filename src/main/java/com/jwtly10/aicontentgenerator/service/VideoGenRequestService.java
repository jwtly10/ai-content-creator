package com.jwtly10.aicontentgenerator.service;


import com.jwtly10.aicontentgenerator.exceptions.UserServiceException;
import com.jwtly10.aicontentgenerator.model.Reddit.RedditPost;
import com.jwtly10.aicontentgenerator.model.Reddit.RedditTitle;
import com.jwtly10.aicontentgenerator.model.UserVideo;
import com.jwtly10.aicontentgenerator.model.Video;
import com.jwtly10.aicontentgenerator.model.VideoProcessingState;
import com.jwtly10.aicontentgenerator.model.api.request.VideoGenFromRedditRequest;
import com.jwtly10.aicontentgenerator.model.api.request.VideoGenRequest;
import com.jwtly10.aicontentgenerator.model.api.response.VideoGenResponse;
import com.jwtly10.aicontentgenerator.model.api.response.VideoListResponse;
import com.jwtly10.aicontentgenerator.service.Reddit.RedditPostParserService;
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
    private final RedditPostParserService redditPostParserService;

    private final StorageService storageService;

    private final VideoService videoService;

    private final UserService userService;

    public VideoGenRequestService(RedditVideoGenerator redditVideoGenerator, RedditPostParserService redditPostParserService, StorageService storageService, VideoService videoService, UserService userService) {
        this.redditVideoGenerator = redditVideoGenerator;
        this.redditPostParserService = redditPostParserService;
        this.storageService = storageService;
        this.videoService = videoService;
        this.userService = userService;
    }

    /**
     * Request video generation
     *
     * @param req Video generation request
     * @return Video generation response
     */
    public ResponseEntity<VideoGenResponse> requestVideoGeneration(VideoGenRequest req) {
        RedditTitle redditTitle = new RedditTitle();
        redditTitle.setTitle(req.getTitle());
        redditTitle.setSubreddit(req.getSubreddit());

        // TODO: get background video URL from id
        String test_video_loc = storageService.downloadVideo("test_short_video.mp4", "test-media/");

        String processUUID = FileUtils.generateUUID();
        videoService.logNewVideoProcess(processUUID, redditTitle);

        try {
            redditVideoGenerator.generateContent(processUUID, redditTitle, req.getContent(), test_video_loc);
        } catch (Exception e) {
            log.error("Error generating video: {}", e.getMessage());
            videoService.updateVideoProcessLog(processUUID, VideoProcessingState.FAILED, e.getMessage());
            return ResponseEntity.ok(VideoGenResponse.builder()
                    .processId(processUUID)
                    .error("Error while generating video: " + e.getMessage())
                    .build());
        }

        return ResponseEntity.ok(VideoGenResponse.builder()
                .processId(processUUID)
                .build());
    }

    public ResponseEntity<VideoGenResponse> requestVideoGenFromRedditURL(VideoGenFromRedditRequest req) {
        try {
            RedditPost redditPost = redditPostParserService.parseRedditPost(req.getUrl());

            RedditTitle redditTitle = new RedditTitle();
            redditTitle.setTitle(redditPost.getTitle());
            redditTitle.setSubreddit(redditPost.getSubreddit());

            VideoGenRequest videoGenRequest = VideoGenRequest.builder()
                    .title(redditPost.getTitle())
                    .subreddit(redditPost.getSubreddit())
                    .content(redditPost.getDescription())
                    .backgroundVideo(req.getBackgroundVideo())
                    .build();

            return requestVideoGeneration(videoGenRequest);
        } catch (Exception e) {
            log.error("Error generating video from Reddit URL: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(VideoGenResponse.builder()
                    .error("Error getting post data from reddit: " + e.getMessage())
                    .build());
        }
    }


    public ResponseEntity<VideoGenResponse> checkStatus(String processId) {
        VideoGenResponse res;
        try {
            res = videoService.checkStatus(processId);
        } catch (Exception e) {
            log.error("Error checking status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(VideoGenResponse.builder()
                    .error("Error getting status: " + e.getMessage())
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
                    .error("Error downloading video: " + e.getMessage())
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
        Optional<UserVideo> userVideo = userService.getUserVideoForProcess(processId);
        if (userVideo.isEmpty()) {
            throw new UserServiceException("Process ID not found for authenticated user");
        } else if (userVideo.get().getState() != VideoProcessingState.COMPLETED) {
            throw new UserServiceException("Video processing not completed");
        }

        // This should be secure, as we will have already validated the user ID and process ID
        try {
            // Get Video Data
            Optional<Video> video = videoService.getVideo(processId);

            byte[] fileContent = storageService.proxyDownload(video.orElseThrow(
                    () -> new UserServiceException("Unable to find video for authenticated user")
            ).getFileName());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", video.get().getFileName());

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            throw new UserServiceException("Unable to download video for authenticated user");
        }
    }

    /**
     * Get videos
     *
     * @return VideoListResponse
     */
    public ResponseEntity<VideoListResponse> getVideos() {
        try {
            return ResponseEntity.ok(videoService.getVideos());
        } catch (Exception e) {
            log.error("Error getting videos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(VideoListResponse.builder()
                    .error("Error getting videos: " + e.getMessage())
                    .build());
        }
    }

}
