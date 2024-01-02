package com.jwtly10.aicontentgenerator.service;

import com.jwtly10.aicontentgenerator.model.User;
import com.jwtly10.aicontentgenerator.model.UserVideo;
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
     * Log user video to DB
     *
     * @param userId   User ID
     * @param fileName File name
     */
    public void logUserVideo(int userId, String fileName) {
        log.info("Logging user video to DB");
        userVideoDAOImpl.create(
                UserVideo.builder()
                        .user_id(userId)
                        .file_name(fileName)
                        .upload_date(new Date())
                        .build());
    }
}
