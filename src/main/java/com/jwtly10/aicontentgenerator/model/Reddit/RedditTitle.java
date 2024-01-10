package com.jwtly10.aicontentgenerator.model.Reddit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedditTitle {
    private String title;
    private String subreddit;
}
