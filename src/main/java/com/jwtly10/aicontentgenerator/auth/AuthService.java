package com.jwtly10.aicontentgenerator.auth;

import com.jwtly10.aicontentgenerator.model.Role;
import com.jwtly10.aicontentgenerator.model.User;
import com.jwtly10.aicontentgenerator.model.api.request.LoginRequest;
import com.jwtly10.aicontentgenerator.model.api.request.RegisterRequest;
import com.jwtly10.aicontentgenerator.model.api.response.LoginResponse;
import com.jwtly10.aicontentgenerator.repository.UserDAOImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
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
}
