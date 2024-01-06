package com.jwtly10.aicontentgenerator.controller.Auth;

import com.jwtly10.aicontentgenerator.auth.AuthService;
import com.jwtly10.aicontentgenerator.model.api.request.LoginRequest;
import com.jwtly10.aicontentgenerator.model.api.request.RegisterRequest;
import com.jwtly10.aicontentgenerator.model.api.request.TokenRequest;
import com.jwtly10.aicontentgenerator.model.api.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return service.register(request);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<LoginResponse> authenticate(
            @RequestBody LoginRequest request
    ) {
        return service.login(request);
    }

    @PostMapping("/validate")
    public ResponseEntity<Void> validate(
            @RequestBody TokenRequest request
    ) {
        return service.validateSession(request);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(
            @RequestBody TokenRequest request
    ) {
        return service.refreshToken(request);
    }
}