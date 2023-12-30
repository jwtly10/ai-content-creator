package com.jwtly10.aicontentgenerator.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


/** GentleAlignerTest */
@Slf4j
@SpringBootTest
public class GentleAlignerTest {
    @Value("${ffmpeg.tmp.path}")
    private String ffmpegTmpPath;

    @Autowired
    private GentleAlignerUtil gentleAlignerUtil;

    @Test
    public void testGenerateSRT() {
        File outputSRT = new File(ffmpegTmpPath + "output.srt");
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

            Optional<String> outSRT = gentleAlignerUtil.alignAndGenerateSRT(
                    test_audio_loc, test_text_loc);

            assertFalse(outSRT.isEmpty(), "Output SRT file path is empty");
            assertEquals(outSRT.get(), ffmpegTmpPath + "output.srt");
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }

}
