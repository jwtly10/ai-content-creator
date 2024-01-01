package com.jwtly10.aicontentgenerator.unitTests.service;

import com.jwtly10.aicontentgenerator.IntegrationTestBase;
import com.jwtly10.aicontentgenerator.repository.UserVideoDAOImpl;
import com.jwtly10.aicontentgenerator.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class UserServiceTest extends IntegrationTestBase {
    // "Unit Test" For integration tests

    @Mock
    private UserVideoDAOImpl userVideoDAOImpl;

    @InjectMocks
    private UserService userService;

    @Test
    void getLoggedInUser() {
        setupAuthentication();

        int userId = userService.getLoggedInUserId();

        assertEquals(6, userId);
    }

    @Test
    void logUserVideo() {
        setupAuthentication();
        userService.logUserVideo(userService.getLoggedInUserId(), "test_uuid", "test_filepath");

        Mockito.verify(userVideoDAOImpl, Mockito.times(1)).create(Mockito.any());
    }
}
