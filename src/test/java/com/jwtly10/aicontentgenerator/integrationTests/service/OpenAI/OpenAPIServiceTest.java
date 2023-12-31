package com.jwtly10.aicontentgenerator.integrationTests.service.OpenAI;

import com.jwtly10.aicontentgenerator.model.Gender;
import com.jwtly10.aicontentgenerator.service.OpenAI.OpenAPIService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class OpenAPIServiceTest {
    @Autowired
    private OpenAPIService service;

    @Test
    void improveContent() {
        String content = """
                Once upon a time, in the heart of the Enchanted Forest, there existed an quaint village named Eldoria. 
                This mystical realm was known for its vibrant colors and magical creatures that roamed freely among the trees.
                """;

        String expected = "a quaint village named Eldoria";
        assertTrue(service.improveContent(content).contains(expected));
    }

    @Test
    void determineGender() {
        String content = "Hey my name is James, I have a girlfriend who did x y z, and it was really cool.";
        assertEquals(Gender.MALE, service.determineGender(content));
    }
}