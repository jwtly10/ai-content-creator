package com.jwtly10.aicontentgenerator.service.ElevenLabs;

import com.jwtly10.aicontentgenerator.model.ElevenLabs.ElevenLabsVoice;
import com.jwtly10.aicontentgenerator.model.Gender;
import com.jwtly10.aicontentgenerator.service.VoiceGenerator;
import com.jwtly10.aicontentgenerator.utils.BaseFileTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@SpringBootTest
class ElevenLabsVoiceGeneratorTest extends BaseFileTest {
    // TODO: TURN INTO INTEGRATION TEST
    // FOR NOW, DEV TESTING ONLY

    @Autowired
    private final VoiceGenerator<ElevenLabsVoice> voiceGenerator = new ElevenLabsVoiceGenerator(new RestTemplate());

    @Test
    void generateVoice() {
        cleanUpFiles("test_out/elevenlabs.mp3");
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
        String outputPath = "test_out/elevenlabs.mp3";

        voiceGenerator.generateVoice(text, Gender.MALE, outputPath);
    }

    @Test
    void getVoices() {
        List<ElevenLabsVoice> voices = voiceGenerator.getVoices();

        for (ElevenLabsVoice voice : voices) {
            System.out.println(voice);
        }
    }
}