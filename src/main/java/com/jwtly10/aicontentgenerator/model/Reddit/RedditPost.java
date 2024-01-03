package com.jwtly10.aicontentgenerator.model.Reddit;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RedditPost {
    private String title;
    private String subreddit;
    private String description;
}
