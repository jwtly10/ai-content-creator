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

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
public class SBStorageServiceTest extends IntegrationTestBase {

    @Autowired
    private SBStorageService sbStorageService;

    @Test
    public void uploadVideo() throws IOException {
        String filePath =
                new ClassPathResource("test_files/test_short_video.mp4")
                        .getFile()
                        .getAbsolutePath();

        log.info(filePath);
        String fileUuid = FileUtils.getUUID();

        // this file already exists, assert that it failed due to duplicate
        Exception exception = assertThrows(StorageException.class, () -> sbStorageService.uploadVideo(fileUuid, filePath));
        String expectedMessage = "400 Bad Request: \"{\"statusCode\":\"409\",\"error\":\"Duplicate\",\"message\":\"The resource already exists\"}\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void downloadVideo() {
        Optional<String> out = sbStorageService.downloadVideo("5cef329c-17d0-4b57-9b0c-887b3d650aa3.mp4");

        assertFalse(out.isEmpty(), "Downloaded video is empty");
        assertEquals(downloadPath + "/" + "5cef329c-17d0-4b57-9b0c-887b3d650aa3.mp4", out.get());

    }
}
