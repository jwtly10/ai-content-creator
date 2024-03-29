package com.jwtly10.aicontentgenerator.controller.Videos;

import com.jwtly10.aicontentgenerator.model.api.request.VideoGenFromRedditRequest;
import com.jwtly10.aicontentgenerator.model.api.request.VideoGenRequest;
import com.jwtly10.aicontentgenerator.model.api.response.VideoGenResponse;
import com.jwtly10.aicontentgenerator.model.api.response.VideoListResponse;
import com.jwtly10.aicontentgenerator.service.VideoGenRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/video")
public class VideoGenController {

    private final VideoGenRequestService videoGenRequestService;

    public VideoGenController(VideoGenRequestService videoGenRequestService) {
        this.videoGenRequestService = videoGenRequestService;
    }

    @PostMapping("/generate")
    public ResponseEntity<VideoGenResponse> generateVideo(@RequestBody VideoGenRequest req) {
        return videoGenRequestService.requestVideoGeneration(req);
    }

    @PostMapping("/generate/reddit")
    public ResponseEntity<VideoGenResponse> generateVideoFromRedditUrl(@RequestBody VideoGenFromRedditRequest req) {
        return videoGenRequestService.requestVideoGenFromRedditURL(req);
    }

    @GetMapping("/getVideos")
    public ResponseEntity<VideoListResponse> getVideos() {
        return videoGenRequestService.getVideos();
    }


    @GetMapping("/status/{processId}")
    public ResponseEntity<VideoGenResponse> checkStatus(@PathVariable String processId) {
        return videoGenRequestService.checkStatus(processId);
    }

    @DeleteMapping("/delete/{processId}")
    public ResponseEntity<VideoGenResponse> deleteVideo(@PathVariable String processId) {
        return videoGenRequestService.deleteVideo(processId);
    }

    @GetMapping("download/{processId}")
    public ResponseEntity<?> downloadVideo(@PathVariable String processId) {
        return videoGenRequestService.downloadVideo(processId);
    }

}
