package com.jwtly10.aicontentgenerator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVideo {
    private int id;
    private int user_id;
    private String title;
    private String file_path;
    private Date upload_date;
}