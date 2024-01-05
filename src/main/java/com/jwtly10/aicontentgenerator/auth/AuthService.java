package com.jwtly10.aicontentgenerator.auth;

import com.jwtly10.aicontentgenerator.model.Role;
import com.jwtly10.aicontentgenerator.model.User;
import com.jwtly10.aicontentgenerator.model.api.request.LoginRequest;
import com.jwtly10.aicontentgenerator.model.api.request.RegisterRequest;
import com.jwtly10.aicontentgenerator.model.api.request.TokenRequest;
import com.jwtly10.aicontentgenerator.model.api.response.LoginResponse;
import com.jwtly10.aicontentgenerator.repository.UserDAOImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserDAOImpl userDAOImpl;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Register user
     *
     * @param request RegisterRequest
     * @return LoginResponse
     */
    public ResponseEntity<LoginResponse> register(RegisterRequest request) {
        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        if (userDAOImpl.get(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(LoginResponse.builder().error("User already exists, please login").build());
        }

        userDAOImpl.create(user);
        var jwtToken = jwtService.generateToken(user);

        return ResponseEntity.ok(LoginResponse.builder()
                .email(user.getEmail())
                .token(jwtToken)
                .build());
    }

    /**
     * Login user
     *
     * @param request LoginRequest
     * @return LoginResponse
     */
    public ResponseEntity<LoginResponse> login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            var user = userDAOImpl.get(request.getEmail()).orElseThrow();

            var jwtToken = jwtService.generateToken(user);

            log.info("User {} logged in", user.getId());
            return ResponseEntity.ok(LoginResponse.builder()
                    .email(user.getEmail())
                    .token(jwtToken)
                    .build());
        } catch
        (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.builder().error("Invalid email or password").build());
        }
    }

    /**
     * Validate session
     *
     * @param request TokenRequest
     * @return ResponseEntity<Void> with status 200 if valid, 401 if invalid
     */
    public ResponseEntity<Void> validateSession(TokenRequest request) {
        try {
            boolean valid = jwtService.validateToken(request.getToken());
            if (!valid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            } else {
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Refresh token
     *
     * @param request TokenRequest
     * @return LoginResponse
     */
    public ResponseEntity<LoginResponse> refreshToken(TokenRequest request) {
        try {
            boolean valid = jwtService.validateToken(request.getToken());
            if (!valid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            } else {
                String username = jwtService.extractUsername(request.getToken());
                User user = userDAOImpl.get(username).orElseThrow(
                        () -> new AuthenticationServiceException("User not found while refreshing token")
                );
                var jwtToken = jwtService.generateToken(user);
                return ResponseEntity.ok(LoginResponse.builder()
                        .email(user.getEmail())
                        .token(jwtToken)
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
