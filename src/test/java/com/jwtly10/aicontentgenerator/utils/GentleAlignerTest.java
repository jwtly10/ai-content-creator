package com.jwtly10.aicontentgenerator.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


/** GentleAlignerTest */
@Slf4j
public class GentleAlignerTest {

    @Test
    public void testGenerateSRT() {
        File outputSRT = new File("test_out/output.srt");
        if (outputSRT.exists()) {
            outputSRT.delete();
        }

        try {
            String test_audio_loc =
                    new ClassPathResource("test_files/example_audio.mp3")
                            .getFile()
                            .getAbsolutePath();
            String test_text_loc =
                    new ClassPathResource("test_files/example_text.txt")
                            .getFile()
                            .getAbsolutePath();

            Optional<String> outSRT = GentleAlignerUtil.alignAndGenerateSRT(
                    test_audio_loc, test_text_loc);

            if (outSRT.isEmpty()) {
                fail();
                throw new Exception("Output SRT not present");
            }

            assertEquals(outSRT.get(), "test_out/output.srt");
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }

}
