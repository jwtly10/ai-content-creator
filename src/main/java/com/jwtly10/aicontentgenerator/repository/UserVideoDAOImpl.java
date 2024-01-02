package com.jwtly10.aicontentgenerator.repository;

import com.jwtly10.aicontentgenerator.model.UserVideo;
import com.jwtly10.aicontentgenerator.model.VideoProcessingState;
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
        userVideo.setFile_uuid((rs.getString("file_uuid")));
        userVideo.setState(VideoProcessingState.valueOf((rs.getString("state"))));
        userVideo.setError((rs.getString("error")));
        userVideo.setFile_name((rs.getString("file_name")));
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
        String sql = "INSERT INTO user_video_tb (user_id, file_uuid, state, file_name, upload_date) VALUES (?, ?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql, userVideo.getUser_id(), userVideo.getFile_uuid(), userVideo.getState().toString(), userVideo.getFile_name(), userVideo.getUpload_date());
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

    public int update(UserVideo userVideo, int userId, String fileUuid) {
        if (userVideo.getFile_name() != null) {
            String sql = "UPDATE user_video_tb SET state = ?, error_msg = ?, file_name = ? WHERE user_id = ? AND file_uuid = ?";
            try {
                return jdbcTemplate.update(sql, userVideo.getState().toString(), userVideo.getError(), userVideo.getFile_name(), userId, fileUuid);
            } catch (Exception e) {
                log.error("Error updating user video record: {}", e.getMessage());
            }
        }

        String sql = "UPDATE user_video_tb SET state = ?, error_msg = ? WHERE user_id = ? AND file_uuid = ?";
        try {
            return jdbcTemplate.update(sql, userVideo.getState().toString(), userVideo.getError(), userId, fileUuid);
        } catch (Exception e) {
            log.error("Error updating user video record: {}", e.getMessage());
        }
        return 0;
    }


    @Override
    public int delete(int id) {
        return 0;
    }
}