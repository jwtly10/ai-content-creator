package com.jwtly10.aicontentgenerator.repository;

import com.jwtly10.aicontentgenerator.model.UserVideo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class UserVideoDAOImpl implements UserVideoDAO<UserVideo> {

    private final JdbcTemplate jdbcTemplate;
    RowMapper<UserVideo> rowMapper = (rs, rowNum) -> {
        UserVideo userVideo = new UserVideo();
        userVideo.setId((rs.getInt("id")));
        userVideo.setUser_id((rs.getInt("user_id")));
        userVideo.setTitle((rs.getString("title")));
        userVideo.setFile_path((rs.getString("file_path")));
        userVideo.setUpload_date((rs.getDate("upload_date")));
        return userVideo;
    };

    public UserVideoDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<UserVideo> list() {
        return null;
    }

    @Override
    public void create(UserVideo userVideo) {
        String sql = "INSERT INTO user_video_tb (user_id, title, file_path, upload_date) VALUES (?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql, userVideo.getUser_id(), userVideo.getTitle(), userVideo.getFile_path(), userVideo.getUpload_date());
        } catch (Exception e) {
            log.error("Error creating user video record: {}", e.getMessage());
        }
    }

    @Override
    public Optional<UserVideo> get(int userId) {
        return Optional.empty();
    }

    @Override
    public Optional<UserVideo> get(String videoName) {
        return Optional.empty();
    }

    @Override
    public int update(UserVideo userVideo, int id) {
        return 0;
    }

    @Override
    public int delete(int id) {
        return 0;
    }
}