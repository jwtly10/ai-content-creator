package com.jwtly10.aicontentgenerator.baseTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.jwtly10.aicontentgenerator.model.api.response.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class ControllerTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Value("${test.login.username}")
    private String testLoginUsername;
    @Value("${test.login.password}")
    private String testLoginPassword;

    public String getLoginToken() throws Exception {
        JsonObject req = new JsonObject();
        req.addProperty("email", testLoginUsername);
        req.addProperty("password", testLoginPassword);

        String res = mockMvc.perform(post("/api/v1/auth/authenticate")
                        .header("Content-Type", "application/json")
                        .content(String.valueOf(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        LoginResponse loginResponse = new ObjectMapper().readValue(res, LoginResponse.class);

        return loginResponse.getToken();
    }
}
