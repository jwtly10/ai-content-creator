package com.jwtly10.aicontentgenerator.model;

import lombok.Builder;
import lombok.Data;

/** VideoGen */
@Data
@Builder
public class VideoGen {

    private String backgroundVideoPath;

    private String titleImgPath;

    private String titleAudioPath;

    private String titleTextPath;

    private String contentAudioPath;

    private String contentTextPath;
}
