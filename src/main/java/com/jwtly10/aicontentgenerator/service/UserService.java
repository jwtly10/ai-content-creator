package com.jwtly10.aicontentgenerator.service;

import com.jwtly10.aicontentgenerator.model.User;
import com.jwtly10.aicontentgenerator.model.UserVideo;
import com.jwtly10.aicontentgenerator.model.VideoProcessingState;
import com.jwtly10.aicontentgenerator.repository.UserVideoDAOImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class UserService {

    private final UserVideoDAOImpl userVideoDAOImpl;

    public UserService(UserVideoDAOImpl userVideoDAOImpl) {
        this.userVideoDAOImpl = userVideoDAOImpl;
    }

    /**
     * Get logged in user ID
     *
     * @return User ID
     */
    public int getLoggedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        if (authentication.getPrincipal() instanceof User userDetails) {
            return userDetails.getId();
        }

        throw new RuntimeException("User not authenticated");
    }

    /**
     * Log new video process to DB
     *
     * @param userId   User ID
     * @param fileUuid File UUID
     */
    public void logNewVideoProcess(int userId, String fileUuid) {
        log.info("Logging new video process to DB");
        userVideoDAOImpl.create(
                UserVideo.builder()
                        .user_id(userId)
                        .file_uuid(fileUuid)
                        .state(VideoProcessingState.PENDING)
                        .upload_date(new Date())
                        .build());
    }

    /**
     * Update video process state
     *
     * @param userId   User ID
     * @param fileUuid File UUID
     */
    public void updateVideoProcessLog(int userId, String fileUuid, String fileName, VideoProcessingState state) {
        log.info("Logging video process update");
        userVideoDAOImpl.update(
                UserVideo.builder()
                        .state(state)
                        .file_name(fileName)
                        .build(), userId, fileUuid);
    }

    /**
     * Update video process state
     *
     * @param userId   User ID
     * @param fileUuid File UUID
     * @param state    Video processing state
     * @param error    Error message
     */
    public void updateVideoProcessLog(int userId, String fileUuid, VideoProcessingState state, String error) {
        log.info("Logging video process update");
        userVideoDAOImpl.update(
                UserVideo.builder()
                        .state(state)
                        .error(error)
                        .build(), userId, fileUuid);
    }
}
