package com.jwtly10.aicontentgenerator.integrationTests.service.Reddit;

import com.jwtly10.aicontentgenerator.model.Reddit.RedditTitle;
import com.jwtly10.aicontentgenerator.service.Reddit.RedditVideoGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@Slf4j
class RedditVideoGeneratorTest {
    @Autowired
    private RedditVideoGenerator redditVideoGenerator;

    @Test
    void generateContent() {

        RedditTitle title = new RedditTitle();
        title.setTitle("AITA for expecting my date to cover the cleaning cost of a dress he ruined?");
        String content = """
                I've been seeing this guy for a couple months, and we decided to go to a nice high-end restaurant for a date. 
                He spilt his drink all over it, and it was ruined. I asked him to cover the cost of the dress, and he refused.
                I told him that I would not be seeing him again, and he called me a gold digger. I don't think I'm in the wrong here, but I'm curious what others think.
                """;

        try {
            String test_video_loc =
                    new ClassPathResource("test_files/test_short_video.mp4")
                            .getFile()
                            .getAbsolutePath();

            Optional<String> video = redditVideoGenerator.generateContent(title, content, test_video_loc);
            if (video.isEmpty()) {
                log.error("Failed to generate video");
                fail();
            }
        } catch (Exception e) {
            log.error("Failed to generate video {}", e.getMessage());
            fail();
        }

    }
}