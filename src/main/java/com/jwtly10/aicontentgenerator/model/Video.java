package com.jwtly10.aicontentgenerator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Video {
    private String videoId;
    private String title;
    private String fileUrl;
    private String fileName;
    private Long length;
    private Timestamp uploadDate;
    private Timestamp created;
}
