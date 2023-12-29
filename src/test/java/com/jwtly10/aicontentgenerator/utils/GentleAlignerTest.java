package com.jwtly10.aicontentgenerator.utils;

import com.jwtly10.aicontentgenerator.model.VideoGen;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

/** GentleAlignerTest */
@Slf4j
public class GentleAlignerTest {

    @Test
    public void testGentleAligner() {
        try {

            String test_audio_loc =
                    new ClassPathResource("test_files/example_audio.mp3")
                            .getFile()
                            .getAbsolutePath();
            String test_text_loc =
                    new ClassPathResource("test_files/example_text.txt")
                            .getFile()
                            .getAbsolutePath();
            String test_video_loc =
                    new ClassPathResource("test_files/example_video.mp4")
                            .getFile()
                            .getAbsolutePath();

            VideoGen video =
                    VideoGen.builder()
                            .backgroundVideoPath(test_video_loc)
                            .titleImgPath("")
                            .titleAudioPath("")
                            .titleTextPath("")
                            .contentAudioPath(test_audio_loc)
                            .contentTextPath(test_text_loc)
                            .build();

            GentleAlignerUtil.alignAndGenerateSRT(
                    video.getContentAudioPath(), video.getContentTextPath());

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }
}
