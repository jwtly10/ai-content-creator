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

        if (length.isEmpty()) {
            throw new IOException("Length not present");
        }

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

        if (length.isEmpty()) {
            throw new IOException("Length not present");
        }

        assertEquals(40, length.get());
    }

    @Test
    public void generateVideo() {
        File outputVideo = new File("test_out/out_example_video.mp4");
        if (outputVideo.exists()) {
            outputVideo.delete();
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

            Optional<String> outputPath = FFmpegUtil.generateVideo(
                    video.getBackgroundVideoPath(),
                    video.getContentAudioPath(),
                    test_srt_loc);

            if (outputPath.isEmpty()) {
                throw new Exception("Output path not present");
            }

            assertEquals("test_out/out_example_video.mp4", outputPath.get());
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

            Optional<String> resizedVideoPath = FFmpegUtil.resizeVideo(video.getBackgroundVideoPath());

            if (resizedVideoPath.isEmpty()) {
                throw new Exception("Resized video path not present");
            }
            assertEquals("test_out/tmp/resized_example_video.mp4", resizedVideoPath.get());

            // TODO: Finalise and assert resized video dimensions (when this is configurable)
            Optional<VideoDimensions> resizedDims = FFmpegUtil.getVideoDimensions(resizedVideoPath.get());
            if (resizedDims.isEmpty()) {
                throw new Exception("Resized video dimensions not present");
            }

            System.out.println("Resized video dimensions: " + resizedDims.get().getWidth() + "x" + resizedDims.get().getHeight());

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
