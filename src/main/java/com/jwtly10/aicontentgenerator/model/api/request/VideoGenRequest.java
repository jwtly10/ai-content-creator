package com.jwtly10.aicontentgenerator.model.api.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VideoGenRequest {
    private String title;
    private String subreddit;
    private String content;
    private String backgroundVideo;
}
