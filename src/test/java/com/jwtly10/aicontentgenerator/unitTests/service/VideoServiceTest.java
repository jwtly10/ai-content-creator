package com.jwtly10.aicontentgenerator.unitTests.service;

import com.jwtly10.aicontentgenerator.IntegrationTestBase;
import com.jwtly10.aicontentgenerator.repository.UserVideoDAOImpl;
import com.jwtly10.aicontentgenerator.repository.VideoDAOImpl;
import com.jwtly10.aicontentgenerator.service.UserService;
import com.jwtly10.aicontentgenerator.service.VideoService;
import com.jwtly10.aicontentgenerator.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class VideoServiceTest extends IntegrationTestBase {
    // "Unit Test" For integration tests

    @Mock
    private UserVideoDAOImpl userVideoDAOImpl;

    @Mock
    private VideoDAOImpl videoDAOImpl;

    @Mock
    private UserService userService;

    @InjectMocks
    private VideoService videoService;


    @Test
    void logNewProcess() {
        setupAuthentication();
        String processId = FileUtils.generateUUID();
        videoService.logNewVideoProcess(processId);

        Mockito.verify(userVideoDAOImpl, Mockito.times(1)).create(Mockito.any());
        Mockito.verify(videoDAOImpl, Mockito.times(1)).create(Mockito.any());
    }
}
