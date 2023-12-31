package com.jwtly10.aicontentgenerator.service.Reddit;

import com.jwtly10.aicontentgenerator.model.Reddit.RedditTitle;
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
    // TODO: TURN INTO INTEGRATION TEST
    // FOR NOW, DEV TESTING ONLY

    @Autowired
    private RedditVideoGenerator redditVideoGenerator;

    @Test
    void generateContent() {

        RedditTitle title = new RedditTitle();
        title.setTitle("This is a test title");
        String content = """
                Once upon a time, in the heart of the Enchanted Forest, there existed a quaint village named Eldoria. 
                This mystical realm was known for its vibrant colors and magical creatures that roamed freely among the 
                ancient trees. At the edge of the village stood an old, wise oak tree named Eldor, which was said to hold 
                the secrets of the forest.
                One day, a curious young girl named Luna discovered a hidden path leading to the base of Eldor. 
                Intrigued by the whispers of the wind and the soft glow surrounding the tree, Luna felt a connection 
                with the ancient oak. To her amazement, Eldor spoke to her in a gentle, melodic voice that resonated 
                through the air
                """;

        try {
            String test_video_loc =
                    new ClassPathResource("test_files/resized_example_video.mp4")
                            .getFile()
                            .getAbsolutePath();

            Optional<String> video = redditVideoGenerator.generateContent(title, content, test_video_loc);
            if (video.isEmpty()) {
                fail();
                log.error("Failed to generate video");
            }
        } catch (Exception e) {
            fail();
            log.error("Failed to generate video {}", e.getMessage());
        }

    }
}