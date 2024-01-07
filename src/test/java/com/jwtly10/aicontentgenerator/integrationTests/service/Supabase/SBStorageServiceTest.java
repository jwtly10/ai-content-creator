package com.jwtly10.aicontentgenerator.integrationTests.service.Supabase;

import com.jwtly10.aicontentgenerator.IntegrationTestBase;
import com.jwtly10.aicontentgenerator.exceptions.StorageException;
import com.jwtly10.aicontentgenerator.service.Supabase.SBStorageService;
import com.jwtly10.aicontentgenerator.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
public class SBStorageServiceTest extends IntegrationTestBase {

    @Autowired
    private SBStorageService sbStorageService;

    @Test
    public void uploadVideo() {
        String filePath =
                getFileLocally("test_short_video.mp4").orElseThrow();

        log.info(filePath);
        String fileUuid = FileUtils.generateUUID();

        // this file already exists, assert that it failed due to duplicate
        Exception exception = assertThrows(StorageException.class, () -> sbStorageService.uploadVideo(fileUuid, filePath));
        String expectedMessage = "Failed to save file: 400 Bad Request: \"{\"statusCode\":\"409\",\"error\":\"Duplicate\",\"message\":\"The resource already exists\"}\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);

        cleanUpFiles(filePath);
    }

    @Test
    public void downloadVideo() {
        String out = sbStorageService.downloadVideo("2343ebe6-e9b4-4853-9837-7bc1808900e2_final.mp4");

        assertFalse(out.isEmpty(), "Downloaded video is empty");
        assertEquals(downloadPath + "2343ebe6-e9b4-4853-9837-7bc1808900e2_final.mp4", out);

        cleanUpFiles(out);
    }
}
