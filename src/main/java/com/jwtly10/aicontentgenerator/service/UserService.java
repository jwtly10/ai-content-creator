package com.jwtly10.aicontentgenerator.service;

import com.jwtly10.aicontentgenerator.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {

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
}
