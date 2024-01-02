package com.jwtly10.aicontentgenerator.model.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jwtly10.aicontentgenerator.model.VideoProcessingState;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VideoGenResponse {
    private String processId;
    private VideoProcessingState status;
    private String error;
}
