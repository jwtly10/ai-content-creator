package com.jwtly10.aicontentgenerator.utils;

import com.jwtly10.aicontentgenerator.models.VideoGen;

import org.junit.jupiter.api.Test;

/** GentleAlignerTest */
public class GentleAlignerTest {

    @Test
    public void testGentleAligner() {
        VideoGen video =
                VideoGen.builder()
                        .backgroundVideoPath("test_media/example_video.mp4")
                        .titleImgPath("")
                        .titleAudioPath("")
                        .titleTextPath("")
                        .contentAudioPath("test_media/example_audio.mp3")
                        .contentTextPath("test_media/example_text.txt")
                        .build();

        GentleAlignerUtil.alignAndGenerateSRT(
                video.getContentAudioPath(), video.getContentTextPath());
    }
}
