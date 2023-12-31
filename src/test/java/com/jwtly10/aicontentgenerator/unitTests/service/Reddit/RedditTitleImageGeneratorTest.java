package com.jwtly10.aicontentgenerator.unitTests.service.Reddit;

import com.jwtly10.aicontentgenerator.BaseFileTest;
import com.jwtly10.aicontentgenerator.exceptions.RedditTitleImageGeneratorException;
import com.jwtly10.aicontentgenerator.model.Reddit.RedditTitle;
import com.jwtly10.aicontentgenerator.service.Reddit.RedditTitleImageGenerator;
import com.jwtly10.aicontentgenerator.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Slf4j
class RedditTitleImageGeneratorTest extends BaseFileTest {

    @Autowired
    private RedditTitleImageGenerator redditTitleImageGenerator;

    @Test
    void generateImage() {
        String fileId = FileUtils.getUUID();
        RedditTitle title = new RedditTitle();
        title.setTitle("This is a short title which should fit on the image.");
        redditTitleImageGenerator.generateImage(title, fileId);
        assertFileExists(ffmpegTmpPath + fileId + ".png");

        cleanUp(fileId);
    }

    @Test
    void generateImageWithLongTitle() {
        String fileId = FileUtils.getUUID();
        RedditTitle title = new RedditTitle();
        title.setTitle("AITA if my title is too long and I don't care? I don't think I am but I want to know what you think?");
        redditTitleImageGenerator.generateImage(title, fileId);
        assertFileExists(ffmpegTmpPath + fileId + ".png");

        cleanUp(fileId);
    }

    @Test
    void titleIsTooLongForImage() {
        String fileId = FileUtils.getUUID();
        RedditTitle title = new RedditTitle();
        title.setTitle("AITA if my title is too long and I don't care? I don't think I am but I want to know what you think? " +
                "I don't think I am but I want to know what you think? I don't think I am but I want to know what you think?");
        assertThrows(RedditTitleImageGeneratorException.class, () -> redditTitleImageGenerator.generateImage(title, fileId));
    }
}