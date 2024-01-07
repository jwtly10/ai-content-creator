package com.jwtly10.aicontentgenerator.integrationTests.service.Supabase;

import com.jwtly10.aicontentgenerator.IntegrationTestBase;
import com.jwtly10.aicontentgenerator.exceptions.StorageException;
import com.jwtly10.aicontentgenerator.service.Supabase.SBStorageService;
import com.jwtly10.aicontentgenerator.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
public class SBStorageServiceTest extends IntegrationTestBase {

    @Autowired
    private SBStorageService sbStorageService;

    @Test
    public void uploadVideo() {
        String filePath =
                getTestFileLocally("test_short_video.mp4").orElseThrow();

        log.info(filePath);
        String fileUuid = FileUtils.generateUUID();

        // this file already exists, assert that it failed due to duplicate
        Exception exception = assertThrows(StorageException.class, () -> sbStorageService.uploadVideo(fileUuid, filePath, "test-media/"));
        String expectedMessage = "Failed to upload video: Failed to save file: 400 Bad Request: \"{\"statusCode\":\"409\",\"error\":\"Duplicate\",\"message\":\"The resource already exists\"}\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);

        cleanUpFiles(filePath);
    }

    @Test
    public void downloadVideo() {
        String out = sbStorageService.downloadVideo("0b9159cd-5c7c-46a0-9a59-c58f0de17b65_final.mp4", "test-media/");

        assertFalse(out.isEmpty(), "Downloaded video is empty");
        assertEquals(downloadPath + "0b9159cd-5c7c-46a0-9a59-c58f0de17b65_final.mp4", out);

        cleanUpFiles(out);
    }

    @Test
    public void deleteVideo() {
        String filePath = "";
        try {
            filePath = new ClassPathResource("local_media/test_deleting_video.mp4").getFile().getAbsolutePath();
        } catch (Exception e) {
            log.error("Failed to get file path: {}", e.getMessage());
            fail();
        }

        // Upload a test file
        String fileUuid = FileUtils.generateUUID();
        sbStorageService.uploadVideo(fileUuid, filePath);

        sbStorageService.deleteVideo("test_deleting_video.mp4");

        cleanUpFiles(filePath);
    }

    @Test
    public void deleteVideoThatDoesntExist() {
        assertThrows(StorageException.class, () -> sbStorageService.deleteVideo("invalid_video"));
    }
}
