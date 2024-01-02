package com.jwtly10.aicontentgenerator.controller.Generation;

import com.jwtly10.aicontentgenerator.model.api.request.VideoGenRequest;
import com.jwtly10.aicontentgenerator.model.api.response.VideoGenResponse;
import com.jwtly10.aicontentgenerator.service.VideoGenRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
