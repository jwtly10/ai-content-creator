package com.jwtly10.aicontentgenerator.integrationTests.service;

import com.jwtly10.aicontentgenerator.IntegrationTestBase;
import com.jwtly10.aicontentgenerator.service.VideoGenRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class VideoGenRequestServiceTest extends IntegrationTestBase {

    @Autowired
    private VideoGenRequestService videoGenRequestService;

    @Test
    public void proxyDownload() {
        setupAuthentication();
        ResponseEntity<byte[]> res = videoGenRequestService.proxyDownload("2343ebe6-e9b4-4853-9837-7bc1808900e2");

        assertNotNull(res.getBody());
    }
}