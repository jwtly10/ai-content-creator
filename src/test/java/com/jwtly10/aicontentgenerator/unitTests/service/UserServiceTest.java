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
    void logNewProcess() {
        setupAuthentication();
        userService.logNewVideoProcess(userService.getLoggedInUserId(), "test_uuid.mp4");

        Mockito.verify(userVideoDAOImpl, Mockito.times(1)).create(Mockito.any());
    }
}
