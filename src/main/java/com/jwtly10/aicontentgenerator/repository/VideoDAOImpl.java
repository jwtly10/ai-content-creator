package com.jwtly10.aicontentgenerator.repository;

import com.jwtly10.aicontentgenerator.model.Video;
import com.jwtly10.aicontentgenerator.model.VideoData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class VideoDAOImpl implements VideoDAO<Video> {
    private final JdbcTemplate jdbcTemplate;
    RowMapper<Video> rowMapper = (rs, rowNum) -> {
        Video video = new Video();
        video.setVideoId((rs.getString("video_id")));
        video.setTitle((rs.getString("title")));
        video.setFileName((rs.getString("file_name")));
        video.setFileUrl((rs.getString("file_url")));
        video.setLength((rs.getLong("length")));
        video.setUploadDate((rs.getTimestamp("upload_date")));
        video.setCreated((rs.getTimestamp("created_at")));
        return video;
    };

    RowMapper<VideoData> customRowMapper = (rs, rowNum) -> {
        Video video = new Video();
        video.setVideoId((rs.getString("video_id")));
        video.setTitle((rs.getString("title")));
        video.setFileName((rs.getString("file_name")));
        video.setFileUrl((rs.getString("file_url")));
        video.setLength((rs.getLong("length")));
        video.setUploadDate((rs.getTimestamp("upload_date")));
        video.setCreated((rs.getTimestamp("created_at")));

        VideoData videoData = new VideoData();
        videoData.setVideo(video);
        videoData.setState(rs.getString("state"));
        videoData.setError(rs.getString("error_msg"));
        videoData.setUserId(rs.getInt("user_id"));
        return videoData;
    };

    public VideoDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Video> list() {
        return null;
    }

    @Override
    public void create(Video video) {
        String sql = """
                INSERT INTO dev.video_tb (video_id, title, file_name, file_url, length)
                VALUES (?, ?, ?, ?, ?);
                """;
        try {
            jdbcTemplate.update(sql, video.getVideoId(), video.getTitle(), video.getFileName(), video.getFileUrl(), video.getLength());
        } catch (Exception e) {
            log.error("Error creating video: {}", e.getMessage());
        }
    }

    @Override
    public Optional<Video> get(String processId) {
        String sql = """
                SELECT video_id, title, file_name, file_url, length, upload_date, created_at
                FROM dev.video_tb
                WHERE video_id = ?;
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, processId));
        } catch (Exception e) {
            log.error("Error getting video: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public List<VideoData> getAllVideoData(int userId) {
        String sql = """
                    SELECT v.video_id, v.title, v.file_name, v.file_url, v.length, v.upload_date, v.created_at, uvt.state, uvt.error_msg, uvt.user_id
                        FROM dev.video_tb v
                    JOIN dev.user_video_tb uvt on v.video_id = uvt.video_id
                    WHERE uvt.user_id = ? AND uvt.state != 'DELETED'
                    ORDER by v.created_at DESC;
                """;
        try {
            return jdbcTemplate.query(sql, customRowMapper, userId);
        } catch (Exception e) {
            log.error("Error getting videos for user: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public int update(Video video) {
        String sql = """
                UPDATE dev.video_tb
                SET title = ?, file_name = ?, file_url = ?, length = ?, upload_date = ? 
                WHERE video_id = ?;
                """;

        try {
            return jdbcTemplate.update(sql, video.getTitle(), video.getFileName(), video.getFileUrl(), video.getLength(), video.getUploadDate(), video.getVideoId());

        } catch (Exception e) {
            log.error("Error updating video: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public int delete(int id) {
        return 0;
    }
}
