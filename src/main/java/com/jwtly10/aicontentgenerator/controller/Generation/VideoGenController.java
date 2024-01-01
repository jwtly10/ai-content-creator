package com.jwtly10.aicontentgenerator.controller.Generation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/video")
public class VideoGenController {
    @GetMapping("/test")
    public String test() {
        return "test";
    }
}
