package com.jwtly10.aicontentgenerator.model.api.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VideoGenResponse {
    private String videoUUID;
    private String error;
}
