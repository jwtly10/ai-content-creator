package com.jwtly10.aicontentgenerator.service;


import com.jwtly10.aicontentgenerator.model.Reddit.RedditTitle;
import com.jwtly10.aicontentgenerator.model.api.request.VideoGenRequest;
import com.jwtly10.aicontentgenerator.model.api.response.VideoGenResponse;
import com.jwtly10.aicontentgenerator.service.Reddit.RedditVideoGenerator;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class VideoGenRequestService {

    private final RedditVideoGenerator redditVideoGenerator;

    private final StorageService storageService;

    public VideoGenRequestService(RedditVideoGenerator redditVideoGenerator, StorageService storageService) {
        this.redditVideoGenerator = redditVideoGenerator;
        this.storageService = storageService;
    }

    public ResponseEntity<VideoGenResponse> requestVideoGeneration(VideoGenRequest req) {
        RedditTitle redditTitle = new RedditTitle();
        redditTitle.setTitle(req.getTitle());
        redditTitle.setSubreddit(req.getSubreddit());

        // TODO: get background video URL from id

        String test_video_loc = storageService.downloadVideo("test_short_video.mp4", "test-media/").orElseThrow();

        String videoUUID;
        try {
            videoUUID = redditVideoGenerator.generateContent(redditTitle, req.getContent(), test_video_loc);
        } catch (Exception e) {
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
