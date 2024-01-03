package com.jwtly10.aicontentgenerator.model.api.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VideoGenFromRedditRequest {
    private String url;
    private String backgroundVideo;
}
