package com.jwtly10.aicontentgenerator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVideo {
    private int id;
    private int userId;
    private String videoId;
    private VideoProcessingState state;
    private String error;
}