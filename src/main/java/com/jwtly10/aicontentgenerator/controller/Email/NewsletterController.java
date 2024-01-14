package com.jwtly10.aicontentgenerator.controller.Email;

import com.jwtly10.aicontentgenerator.model.api.request.NewsletterRequest;
import com.jwtly10.aicontentgenerator.service.NewsletterService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/newsletter")
@Slf4j
public class NewsletterController {

    private final NewsletterService newsletterService;

    private final Bucket bucket;

    public NewsletterController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
        Bandwidth limit = Bandwidth.classic(50, Refill.intervally(10, Duration.ofMinutes(10)));
        this.bucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@RequestBody NewsletterRequest req) {
        if (bucket.tryConsume(1)) {
            return newsletterService.subscribe(req.getEmail());
        }

        log.info("Subscribe request limit exceeded");

        return ResponseEntity.status(429).body("Too many requests");
    }
}
