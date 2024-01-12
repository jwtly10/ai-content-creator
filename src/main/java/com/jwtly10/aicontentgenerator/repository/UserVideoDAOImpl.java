package com.jwtly10.aicontentgenerator.repository;

import com.jwtly10.aicontentgenerator.exceptions.DatabaseException;
import com.jwtly10.aicontentgenerator.model.UserVideo;
import com.jwtly10.aicontentgenerator.model.VideoProcessingState;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class UserVideoDAOImpl implements UserVideoDAO<UserVideo> {
    @Value("${schema}")
    private String schema;

    private final JdbcTemplate jdbcTemplate;
    RowMapper<UserVideo> rowMapper = (rs, rowNum) -> {
        UserVideo userVideo = new UserVideo();
        userVideo.setId(rs.getInt("id"));
        userVideo.setUserId(rs.getInt("user_id"));
        userVideo.setVideoId(rs.getString("video_id"));
        userVideo.setState(VideoProcessingState.valueOf(rs.getString("state")));
        userVideo.setError(rs.getString("error_msg"));
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
    public long create(UserVideo userVideo) {
        String sql = "INSERT INTO " + schema + ".user_video_tb (user_id, video_id, state, error_msg) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(new PreparedStatementCreator() {
                @NotNull
                @Override
                public PreparedStatement createPreparedStatement(@NotNull Connection connection) throws SQLException {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                    ps.setInt(1, userVideo.getUserId());
                    ps.setString(2, userVideo.getVideoId());
                    ps.setString(3, userVideo.getState().toString());
                    ps.setString(4, userVideo.getError());
                    return ps;
                }
            }, keyHolder);

            Number generatedId = keyHolder.getKey();
            if (generatedId != null) {
                return generatedId.longValue();
            } else {
                throw new SQLException("ID not generated for the user video record");
            }
        } catch (Exception e) {
            log.error("Error creating user video record: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public Optional<UserVideo> get(String processId, int userId) {
        String sql = "SELECT * FROM " + schema + ".user_video_tb WHERE video_id = ? AND user_id = ?";
        try {
            UserVideo userVideo = jdbcTemplate.queryForObject(sql, rowMapper, processId, userId);
            return Optional.ofNullable(userVideo);
        } catch (Exception e) {
            log.error("Error getting user video record: {}", e.getMessage());
        }
        return Optional.empty();
    }


    public Optional<UserVideo> get(String processId) {
        String sql = "SELECT * FROM " + schema + ".user_video_tb WHERE video_id = ?";
        try {
            UserVideo userVideo = jdbcTemplate.queryForObject(sql, rowMapper, processId);
            return Optional.ofNullable(userVideo);
        } catch (Exception e) {
            log.error("Error getting user video record: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public int update(UserVideo userVideo, String processId) throws DatabaseException {
        String sql = "UPDATE " + schema + ".user_video_tb SET "
                + "state = COALESCE(?, state), "
                + "error_msg = COALESCE(?, error_msg)"
                + " WHERE video_id = ?";

        try {
            return jdbcTemplate.update(
                    sql,
                    userVideo.getState() != null ? userVideo.getState().toString() : null,
                    userVideo.getError() != null ? userVideo.getError() : null,
                    processId
            );
        } catch (Exception e) {
            log.error("Error updating user video record: ", e);
            throw new DatabaseException("Error updating user video record");
        }
    }

    @Override
    public int delete(int id) {
        return 0;
    }

    @Override
    public List<UserVideo> getPending(int limit) throws DatabaseException {
        String sql = "SELECT * FROM " + schema + ".user_video_tb WHERE state = 'PENDING' LIMIT ?";
        try {
            return jdbcTemplate.query(sql, rowMapper, limit);
        } catch (Exception e) {
            log.error("Error getting pending user video records: {}", e.getMessage());
            throw new DatabaseException("Error getting pending user video records");
        }
    }
}
