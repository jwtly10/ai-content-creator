package com.jwtly10.aicontentgenerator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@Slf4j
public class NewsletterService {

    private final JdbcTemplate jdbcTemplate;

    public NewsletterService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Subscribe a user to the newsletter
     *
     * @param email The email address to subscribe
     * @return A response entity with a message
     */
    public ResponseEntity<String> subscribe(String email) {
        log.info("New newsletter subscription");

        if (!patternMatches(email) || email.length() > 255 || email.isEmpty()) {
            log.info("Invalid email address: {}", email);
            return ResponseEntity.badRequest().body("Invalid email address.");
        }

        String sql = "INSERT INTO newsletter_tb (email) VALUES (?)";

        try {
            jdbcTemplate.update(sql, email);
            return ResponseEntity.ok("You have successfully subscribed to our newsletter!");
        } catch (Exception e) {
            if (e.getMessage().contains("Duplicate entry")) {
                log.info("Email address already subscribed: {}", email);
                return ResponseEntity.badRequest().body("Email address already subscribed.");
            }
            log.error("Error subscribing to the newsletter.", e);
            return ResponseEntity.status(500).body("Error subscribing to the newsletter.");
        }
    }

    private boolean patternMatches(String emailAddress) {
        // Basic regex. Eventually we will use more robust libraries
        String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

        return Pattern.compile(regexPattern)
                .matcher(emailAddress)
                .matches();
    }
}