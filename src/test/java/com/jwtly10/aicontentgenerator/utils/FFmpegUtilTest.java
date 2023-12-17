package com.jwtly10.aicontentgenerator.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.jwtly10.aicontentgenerator.models.VideoGen;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/** FFmpegUtilTest */
@Slf4j
public class FFmpegUtilTest {

    @Test
    public void getLengthOfAudio() throws IOException {

        String test_audio_loc =
                new ClassPathResource("test_files/example_audio.mp3").getFile().getAbsolutePath();
        String test_text_loc =
                new ClassPathResource("test_files/example_text.txt").getFile().getAbsolutePath();
        String test_video_loc =
                new ClassPathResource("test_files/example_video.mp4").getFile().getAbsolutePath();

        VideoGen video =
                VideoGen.builder()
                        .backgroundVideoPath(test_video_loc)
                        .titleImgPath("")
                        .titleAudioPath("")
                        .titleTextPath("")
                        .contentAudioPath(test_audio_loc)
                        .contentTextPath(test_text_loc)
                        .build();

        Long length = FFmpegUtil.getAudioDuration(video.getContentAudioPath());

        assertEquals(38, length);
    }

    @Test
    public void getLengthOfVideo() throws IOException {
        String test_audio_loc =
                new ClassPathResource("test_files/example_audio.mp3").getFile().getAbsolutePath();
        String test_text_loc =
                new ClassPathResource("test_files/example_text.txt").getFile().getAbsolutePath();
        String test_video_loc =
                new ClassPathResource("test_files/example_video.mp4").getFile().getAbsolutePath();

        VideoGen video =
                VideoGen.builder()
                        .backgroundVideoPath(test_video_loc)
                        .titleImgPath("")
                        .titleAudioPath("")
                        .titleTextPath("")
                        .contentAudioPath(test_audio_loc)
                        .contentTextPath(test_text_loc)
                        .build();

        Long length = FFmpegUtil.getVideoDuration(video.getBackgroundVideoPath());

        assertEquals(299, length);
    }

    @Test
    public void generateVideo() {
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

            String test_srt_loc =
                    new ClassPathResource("test_files/output.srt").getFile().getAbsolutePath();

            VideoGen video =
                    VideoGen.builder()
                            .backgroundVideoPath(test_video_loc)
                            .titleImgPath("")
                            .titleAudioPath("")
                            .titleTextPath("")
                            .contentAudioPath(test_audio_loc)
                            .contentTextPath(test_text_loc)
                            .build();

            FFmpegUtil.generateVideo(
                    video.getBackgroundVideoPath(), video.getContentAudioPath(), test_srt_loc);
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }
}
