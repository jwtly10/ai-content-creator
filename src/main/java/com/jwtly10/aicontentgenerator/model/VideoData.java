package com.jwtly10.aicontentgenerator.model;

import lombok.Data;

@Data
public class VideoData {
    private Video video;
    private String state;
    private String error;
    private int userId;
}
