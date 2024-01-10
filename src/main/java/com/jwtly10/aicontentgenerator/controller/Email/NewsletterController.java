package com.jwtly10.aicontentgenerator.controller.Email;

import com.jwtly10.aicontentgenerator.model.api.request.NewsletterRequest;
import com.jwtly10.aicontentgenerator.service.NewsletterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/newsletter")
public class NewsletterController {

    private final NewsletterService newsletterService;

    public NewsletterController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@RequestBody NewsletterRequest req) {
        return newsletterService.subscribe(req.getEmail());
    }
}
