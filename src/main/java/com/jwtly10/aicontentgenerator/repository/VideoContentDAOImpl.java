package com.jwtly10.aicontentgenerator.repository;

import com.jwtly10.aicontentgenerator.exceptions.DatabaseException;
import com.jwtly10.aicontentgenerator.model.VideoContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class VideoContentDAOImpl implements VideoContentDAO<VideoContent> {

    private final JdbcTemplate jdbcTemplate;

    final RowMapper<VideoContent> rowMapper = (rs, rowNum) -> {
        VideoContent videoContent = new VideoContent();
        videoContent.setVideoId(rs.getString("video_id"));
        videoContent.setTitle(rs.getString("title"));
        videoContent.setSubreddit(rs.getString("subreddit"));
        videoContent.setContent(rs.getString("content"));
        videoContent.setBackgroundVideo(rs.getString("backgroundVideo"));
        return videoContent;
    };

    public VideoContentDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<VideoContent> list() {
        return null;
    }

    @Override
    public void create(VideoContent videoContent) throws DatabaseException {
        String sql = "INSERT INTO dev.video_content_tb (video_id, title, subreddit, content, backgroundVideo) VALUES (?, ?, ?, ?, ?)";

        try {
            jdbcTemplate.update(sql, videoContent.getVideoId(), videoContent.getTitle().trim(), videoContent.getSubreddit().trim(), videoContent.getContent().trim(), videoContent.getBackgroundVideo().trim());
        } catch (Exception e) {
            log.error("Error creating video content: {}", e.getMessage());
            throw new DatabaseException("Error creating video content record");
        }
    }

    @Override
    public Optional<VideoContent> get(String videoId) {
        String sql = "SELECT * FROM dev.video_content_tb WHERE video_id = ?";
        VideoContent videoContent = null;
        try {
            videoContent = jdbcTemplate.queryForObject(sql, rowMapper, videoId);
        } catch (Exception e) {
            log.error("Video content not found");
        }

        return Optional.ofNullable(videoContent);
    }

    @Override
    public int update(VideoContent videoContent, String videoId) {
        return 0;
    }

    @Override
    public int delete(String videoId) {
        return 0;
    }
}
