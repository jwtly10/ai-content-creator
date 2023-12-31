package com.jwtly10.aicontentgenerator.utils;

import com.jwtly10.aicontentgenerator.exceptions.SRTGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


/** GentleAlignerTest */
@Slf4j
@SpringBootTest
public class GentleAlignerTest extends BaseFileTest {
    @Autowired
    private GentleAlignerUtil gentleAlignerUtil;

    @Test
    public void testGenerateSRT() {
        File outputSRT = new File(ffmpegTmpPath + "output.srt");
        if (outputSRT.exists()) {
            outputSRT.delete();
        }

        String fileUUID = FileUtils.getUUID();

        try {
            String test_audio_loc =
                    new ClassPathResource("test_files/example_audio.mp3")
                            .getFile()
                            .getAbsolutePath();

            String content = """
                    Once upon a time, in the heart of the Enchanted Forest, there existed a quaint village named Eldoria. 
                    This mystical realm was known for its vibrant colors and magical creatures that roamed freely among the 
                    ancient trees. At the edge of the village stood an old, wise oak tree named Eldor, which was said to hold 
                    the secrets of the forest.
                    One day, a curious young girl named Luna discovered a hidden path leading to the base of Eldor. 
                    Intrigued by the whispers of the wind and the soft glow surrounding the tree, Luna felt a connection 
                    with the ancient oak. To her amazement, Eldor spoke to her in a gentle, melodic voice that resonated 
                    through the air
                    """;

            Optional<String> outSRT = gentleAlignerUtil.alignAndGenerateSRT(
                    test_audio_loc, content, fileUUID);

            assertFalse(outSRT.isEmpty(), "Output SRT file path is empty");
            assertEquals(outSRT.get(), ffmpegTmpPath + fileUUID + ".srt");
        } catch (IOException e) {
            log.error("Failed to generate SRT file");
        } catch (SRTGenerationException e) {
            fail();
            log.error("Failed to generate SRT file");
        }

        cleanUp(fileUUID);
    }

}
