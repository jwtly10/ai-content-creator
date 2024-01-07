package com.jwtly10.aicontentgenerator;

import com.jwtly10.aicontentgenerator.model.Role;
import com.jwtly10.aicontentgenerator.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
public abstract class IntegrationTestBase extends BaseFileTest {

    @Value("${test.login.username}")
    private String testLoginUsername;
    @Value("${test.login.password}")
    private String testLoginPassword;

    /**
     * Sets up the authentication for the test user
     */
    public void setupAuthentication() {
        User userDetails = new User();
        userDetails.setId(6);
        userDetails.setEmail(testLoginUsername);
        userDetails.setPassword(testLoginPassword);
        userDetails.setRole(Role.USER);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


}
