package com.jwtly10.aicontentgenerator.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.jwtly10.aicontentgenerator.model.VideoDimensions;
import com.jwtly10.aicontentgenerator.model.VideoGen;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/** FFmpegUtilTest */
@Slf4j
public class FFmpegUtilTest {

    @BeforeAll
    public static void setUp() {
        File outputSrt = new File("/test_out/output.srt");
        if (outputSrt.exists()) {
            outputSrt.delete();
        }

        File outputResizedVideo = new File("/test_out/out_resized_example_video.mq4");
        if (outputResizedVideo.exists()) {
            outputResizedVideo.delete();
        }

        File outputVideo = new File("/test_out/out_resized_example_video.mq4");
        if (outputVideo.exists()) {
            outputVideo.delete();
        }
    }

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

        assertEquals(40, length);
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

            System.out.println(
                    FFmpegUtil.generateVideo(
                            video.getBackgroundVideoPath(),
                            video.getContentAudioPath(),
                            test_srt_loc));
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }

    @Test
    void resizeAndGenerateVideo() {
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

            String test_srt_loc = "test_out/output.srt";

            VideoGen video =
                    VideoGen.builder()
                            .backgroundVideoPath(test_video_loc)
                            .titleImgPath("")
                            .titleAudioPath("")
                            .titleTextPath("")
                            .contentAudioPath(test_audio_loc)
                            .contentTextPath(test_text_loc)
                            .build();

            String resizedVideoPath = FFmpegUtil.resizeVideo(video.getBackgroundVideoPath());
            System.out.println(
                    FFmpegUtil.generateVideo(
                            resizedVideoPath, video.getContentAudioPath(), test_srt_loc));
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }

    @Test
    void getVideoDimensions() {
        try {
            String test_video_loc =
                    new ClassPathResource("test_files/example_video.mp4")
                            .getFile()
                            .getAbsolutePath();

            Optional<VideoDimensions> dimensions = FFmpegUtil.getVideoDimensions(test_video_loc);

            if (dimensions.isEmpty()) {
                throw new Exception("Dimensions not present");
            }

            assertEquals(1280, dimensions.get().getWidth());
            assertEquals(720, dimensions.get().getHeight());
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }
}
