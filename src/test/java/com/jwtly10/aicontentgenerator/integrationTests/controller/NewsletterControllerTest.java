package com.jwtly10.aicontentgenerator.integrationTests.controller;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class NewsletterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Transactional
    @Rollback
    public void validSubscribe() throws Exception {
        JsonObject req = new JsonObject();
        req.addProperty("email", "test_subscribe@gmail.com");
        mockMvc.perform(
                        post("/api/v1/newsletter/subscribe")
                                .header("Content-Type", "application/json")
                                .content(req.toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void invalidSubscribeEmail() throws Exception {
        JsonObject req = new JsonObject();
        req.addProperty("email", ".test_subscribe@gmail.com");
        mockMvc.perform(
                        post("/api/v1/newsletter/subscribe")
                                .header("Content-Type", "application/json")
                                .content(req.toString()))
                .andExpect(status().isBadRequest());

        req.remove("email");
        req.addProperty("email", "");

        mockMvc.perform(
                        post("/api/v1/newsletter/subscribe")
                                .header("Content-Type", "application/json")
                                .content(req.toString()))
                .andExpect(status().isBadRequest());

        req.remove("email");
        req.addProperty("email", "thisshouldfail");

        mockMvc.perform(
                        post("/api/v1/newsletter/subscribe")
                                .header("Content-Type", "application/json")
                                .content(req.toString()))
                .andExpect(status().isBadRequest());
    }


}
