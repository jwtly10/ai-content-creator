package com.jwtly10.aicontentgenerator.integrationTests.service;

import com.jwtly10.aicontentgenerator.IntegrationTestBase;
import com.jwtly10.aicontentgenerator.exceptions.UserServiceException;
import com.jwtly10.aicontentgenerator.model.api.response.VideoGenResponse;
import com.jwtly10.aicontentgenerator.model.api.response.VideoListResponse;
import com.jwtly10.aicontentgenerator.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class VideoServiceTest extends IntegrationTestBase {

    @Autowired
    private VideoService videoService;

    @BeforeEach
    public void setup() {
        setupAuthentication();
    }

    @Test
    public void testValidCheckStatus() {
        String processUUID = "7e9d1e21-ebf5-45ba-8237-366c8a0d85c7";

        VideoGenResponse res = videoService.checkStatus(processUUID);

        assertNotNull(res);
        assertEquals("7e9d1e21-ebf5-45ba-8237-366c8a0d85c7", res.getProcessId());
        assertEquals("COMPLETED", res.getStatus().toString());
    }

    @Test
    public void testFailedCheckStatus() {
        String processUUID = "b6d03230-96a2-455d-9c8d-c6ac05d07363";

        VideoGenResponse res = videoService.checkStatus(processUUID);

        assertNotNull(res);
        assertEquals("b6d03230-96a2-455d-9c8d-c6ac05d07363", res.getProcessId());
        assertEquals("FAILED", res.getStatus().toString());
        assertEquals("Error connecting to service", res.getError());
    }

    @Test
    public void testInvalidProcessIDThrowsWhenCheckingStatus() {
        String processUUID = "74cf7bea-931c-49d3-a803-44c1771baa3d";

        Exception exception = assertThrows(UserServiceException.class, () -> {
            videoService.checkStatus(processUUID);
        });

        String expectedMessage = "Process ID not found for authenticated user";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void testGettingAllVideosForAuthUser() {
        VideoListResponse res = videoService.getVideos();

        assertNotNull(res);
        assertEquals(3, res.getVideos().size());
    }

}