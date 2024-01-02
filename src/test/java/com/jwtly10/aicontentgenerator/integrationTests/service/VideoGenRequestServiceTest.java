package com.jwtly10.aicontentgenerator.integrationTests.service;

import com.jwtly10.aicontentgenerator.IntegrationTestBase;
import com.jwtly10.aicontentgenerator.exceptions.UserServiceException;
import com.jwtly10.aicontentgenerator.service.VideoGenRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class VideoGenRequestServiceTest extends IntegrationTestBase {

    @Autowired
    private VideoGenRequestService videoGenRequestService;

    @BeforeEach
    public void setup() {
        setupAuthentication();
    }

    @Test
    public void testDownloadingInvalidProcessIdThrows() {
        Exception exception = assertThrows(UserServiceException.class, () -> {
            videoGenRequestService.proxyDownload("invalid_process");
        });

        String expectedMessage = "Process ID not found";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage), "Expected message to contain: " + expectedMessage + ", but was: " + actualMessage);
    }

    @Test
    public void testValidProcessWrongUser() {
        String processUUID = "7db783d8-a68a-4e3e-b390-81d7e7cf3941";

        Exception exception = assertThrows(UserServiceException.class, () -> {
            videoGenRequestService.proxyDownload(processUUID);
        });

        String expectedMessage = "Process ID not found";
        String actualMessage = exception.getMessage();
        assert (actualMessage.contains(expectedMessage));
    }

    @Test
    public void testValidProcessWrongState() {
        String processUUID = "b6d03230-96a2-455d-9c8d-afdsgfd07363";

        Exception exception = assertThrows(UserServiceException.class, () -> {
            videoGenRequestService.proxyDownload(processUUID);
        });

        String expectedMessage = "Video processing not completed";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage), "Expected message to contain: " + expectedMessage + ", but was: " + actualMessage);
    }

    @Test
    public void testMissingFileName() {
        String processUUID = "b6d03230-96a2-455d-9c8d-fsdsgfd07363";

        Exception exception = assertThrows(UserServiceException.class, () -> {
            videoGenRequestService.proxyDownload(processUUID);
        });

        String expectedMessage = "Video file name not found";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage), "Expected message to contain: " + expectedMessage + ", but was: " + actualMessage);
    }
}
