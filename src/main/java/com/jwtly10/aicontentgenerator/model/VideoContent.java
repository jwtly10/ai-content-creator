package com.jwtly10.aicontentgenerator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoContent {
    private String videoId;
    private String title;
    private String subreddit;
    private String content;
    private String backgroundVideo;
}
