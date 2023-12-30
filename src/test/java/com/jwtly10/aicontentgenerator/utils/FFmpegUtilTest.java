package com.jwtly10.aicontentgenerator.utils;

import com.jwtly10.aicontentgenerator.model.VideoDimensions;
import com.jwtly10.aicontentgenerator.model.VideoGen;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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

        Optional<Long> length = FFmpegUtil.getAudioDuration(video.getContentAudioPath());

        assertFalse(length.isEmpty(), "Audio length is empty");
        assertEquals(38, length.get());
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

        Optional<Long> length = FFmpegUtil.getVideoDuration(video.getBackgroundVideoPath());

        assertFalse(length.isEmpty(), "Video length is empty");
        assertEquals(40, length.get());
    }

    @Test
    public void generateVideo() {
        File outputVideo = new File("test_out/out_resized_example_video.mp4");
        if (outputVideo.exists()) {
            outputVideo.delete();
        }
        try {
            String test_video_loc =
                    new ClassPathResource("test_files/resized_example_video.mp4")
                            .getFile()
                            .getAbsolutePath();
            String test_audio_loc =
                    new ClassPathResource("test_files/example_audio.mp3")
                            .getFile()
                            .getAbsolutePath();
            String test_srt_loc =
                    new ClassPathResource("test_files/output.srt").getFile().getAbsolutePath();

            Optional<String> outputPath = FFmpegUtil.generateVideo(
                    test_video_loc,
                    test_audio_loc,
                    test_srt_loc);

            assertFalse(outputPath.isEmpty(), "Output video path is empty");
            assertEquals("test_out/out_resized_example_video.mp4", outputPath.get());
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }

    @Test
    void resizeVideo() {
        // Delete resized test video if exists
        File outputResizedVideo = new File("test_out/tmp/resized_example_video.mp4");
        if (outputResizedVideo.exists()) {
            outputResizedVideo.delete();
        }

        try {
            String test_video_loc =
                    new ClassPathResource("test_files/example_video.mp4")
                            .getFile()
                            .getAbsolutePath();


            Optional<String> resizedVideoPath = FFmpegUtil.resizeVideo(test_video_loc);

            assertFalse(resizedVideoPath.isEmpty(), "Resized video path is empty");
            assertEquals("test_out/tmp/resized_example_video.mp4", resizedVideoPath.get());

            // TODO: Finalise and assert resized video dimensions (when this is configurable)
            Optional<VideoDimensions> resizedDims = FFmpegUtil.getVideoDimensions(resizedVideoPath.get());

            assertFalse(resizedDims.isEmpty(), "Resized video dimensions are empty");
            System.out.println("Resized video dimensions: " + resizedDims.get().getWidth() + "x" + resizedDims.get().getHeight());
            // TODO Assert something
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

            assertFalse(dimensions.isEmpty(), "Video dimensions are empty");
            assertEquals(1280, dimensions.get().getWidth());
            assertEquals(720, dimensions.get().getHeight());
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }
}
