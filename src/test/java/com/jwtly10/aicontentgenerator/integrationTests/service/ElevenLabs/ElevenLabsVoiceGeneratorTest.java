package com.jwtly10.aicontentgenerator.integrationTests.service.ElevenLabs;

import com.jwtly10.aicontentgenerator.BaseFileTest;
import com.jwtly10.aicontentgenerator.model.ElevenLabs.ElevenLabsVoice;
import com.jwtly10.aicontentgenerator.model.Gender;
import com.jwtly10.aicontentgenerator.service.ElevenLabs.ElevenLabsVoiceGenerator;
import com.jwtly10.aicontentgenerator.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest
@Slf4j
class ElevenLabsVoiceGeneratorTest extends BaseFileTest {
    @Autowired
    private ElevenLabsVoiceGenerator voiceGenerator;

    @Test
    void generateVoice() {
        String text = """
                Once upon a time, in the heart of the Enchanted Forest, there existed a quaint village named Eldoria. 
                This mystical realm was known for its vibrant colors and magical creatures that roamed freely among the 
                ancient trees. At the edge of the village stood an old, wise oak tree named Eldor, which was said to hold 
                the secrets of the forest.
                One day, a curious young girl named Luna discovered a hidden path leading to the base of Eldor. 
                Intrigued by the whispers of the wind and the soft glow surrounding the tree, Luna felt a connection 
                with the ancient oak. To her amazement, Eldor spoke to her in a gentle, melodic voice that resonated 
                through the air
                """;
        String fileUUID = FileUtils.getUUID();

        voiceGenerator.generateVoice(text, Gender.MALE, fileUUID);
    }

    @Test
    void getVoices() {
        List<ElevenLabsVoice> voices = voiceGenerator.getVoices();

        for (ElevenLabsVoice voice : voices) {
            log.info("Voice: {}", voice.getName());
        }

        assertNotEquals(0, voices.size());
    }
}