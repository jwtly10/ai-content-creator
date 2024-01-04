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
        ResponseEntity<byte[]> res = videoGenRequestService.proxyDownload("0bf187ce-6e79-4e7d-96a6-e3738647ebe0");

        assertNotNull(res.getBody());
    }
}