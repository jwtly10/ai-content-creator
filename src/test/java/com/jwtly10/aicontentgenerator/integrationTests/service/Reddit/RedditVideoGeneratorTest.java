package com.jwtly10.aicontentgenerator.integrationTests.service.Reddit;

import com.jwtly10.aicontentgenerator.IntegrationTestBase;
import com.jwtly10.aicontentgenerator.model.Reddit.RedditTitle;
import com.jwtly10.aicontentgenerator.service.Reddit.RedditVideoGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@Slf4j
class RedditVideoGeneratorTest extends IntegrationTestBase {

    @Autowired
    private RedditVideoGenerator redditVideoGenerator;

    @Test
    @Transactional
    @Rollback
        // Rollback the log record from DB
    void generateContent() {

        RedditTitle title = new RedditTitle();
        title.setTitle("AITA for expecting my date to cover the cleaning cost of a dress he ruined?");
        String content = """
                I've been seeing this guy for a couple months, and we decided to go to a nice high-end restaurant for a date. 
                He spilt his drink all over it, and it was ruined. I asked him to cover the cost of the dress, and he refused.
                I told him that I would not be seeing him again, and he called me a gold digger. I don't think I'm in the wrong here, but I'm curious what others think.
                """;

        String test_video_loc =
                getFileLocally("test_short_video.mp4").orElseThrow();

        try {
            setupAuthentication();
            String videoID = redditVideoGenerator.generateContent(title, content, test_video_loc);
        } catch (Exception e) {
            log.error(e.getMessage());
            fail();
        }
    }
}