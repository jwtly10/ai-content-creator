package com.jwtly10.aicontentgenerator.integrationTests.service;

import com.jwtly10.aicontentgenerator.baseTests.IntegrationTestBase;
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
        String processUUID = "2343ebe6-e9b4-4853-9837-7bc1808900e2";

        VideoGenResponse res = videoService.checkStatus(processUUID);

        assertNotNull(res);
        assertEquals("2343ebe6-e9b4-4853-9837-7bc1808900e2", res.getProcessId());
        assertEquals("COMPLETED", res.getStatus().toString());
    }

    @Test
    public void testFailedCheckStatus() {
        String processUUID = "9d94fcb2-3e16-4201-bff8-4f46b8c34291";

        VideoGenResponse res = videoService.checkStatus(processUUID);

        assertNotNull(res);
        assertEquals("9d94fcb2-3e16-4201-bff8-4f46b8c34291", res.getProcessId());
        assertEquals("FAILED", res.getStatus().toString());
        assertEquals("Error while aligning text with audio: No route to host", res.getError());
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