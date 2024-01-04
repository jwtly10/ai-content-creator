package com.jwtly10.aicontentgenerator.model.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jwtly10.aicontentgenerator.model.VideoData;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VideoListResponse {
    private List<VideoData> videos;
    private String error;
}
