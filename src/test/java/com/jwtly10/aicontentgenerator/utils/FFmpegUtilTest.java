package com.jwtly10.aicontentgenerator.utils;

import com.jwtly10.aicontentgenerator.model.BufferPos;
import com.jwtly10.aicontentgenerator.model.VideoDimensions;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** FFmpegUtilTest */
@Slf4j
public class FFmpegUtilTest extends BaseFileTest {
    @Test
    public void getLengthOfAudio() throws IOException {
        String test_audio_loc =
                new ClassPathResource("test_files/example_audio.mp3").getFile().getAbsolutePath();

        Optional<Long> length = FFmpegUtil.getAudioDuration(test_audio_loc);

        assertFalse(length.isEmpty(), "Audio length is empty");
        assertEquals(38, length.get());
    }

    @Test
    public void getLengthOfVideo() throws IOException {
        String test_video_loc =
                new ClassPathResource("test_files/example_video.mp4").getFile().getAbsolutePath();

        Optional<Long> length = FFmpegUtil.getVideoDuration(test_video_loc);

        assertFalse(length.isEmpty(), "Video length is empty");
        assertEquals(40, length.get());
    }

    @Test
    public void generateVideo() {
        cleanUpFiles("test_out/out_resized_example_video.mp4");

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
        cleanUpFiles("test_out/tmp/resized_example_video.mp4");

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

    @Test
    public void bufferAudioStart() throws IOException {
        cleanUpFiles("test_out/tmp/buffered_example_audio_START.mp3");

        String test_audio_loc =
                new ClassPathResource("test_files/example_audio.mp3").getFile().getAbsolutePath();

        Optional<Long> length = FFmpegUtil.getAudioDuration(test_audio_loc);

        assertFalse(length.isEmpty(), "Audio length is empty");
        assertEquals(38, length.get());


        Optional<String> bufferedAudioPath_START = FFmpegUtil.bufferAudio(test_audio_loc, BufferPos.START, 2);
        assertFalse(bufferedAudioPath_START.isEmpty(), "Buffered audio path is empty");
        assertEquals("test_out/tmp/buffered_example_audio_START.mp3", bufferedAudioPath_START.get());
        Optional<Long> lengthBuffered_START = FFmpegUtil.getAudioDuration(bufferedAudioPath_START.get());
        assertFalse(lengthBuffered_START.isEmpty(), "Buffered audio length is empty");
        assertEquals(40, lengthBuffered_START.get());
    }

    @Test
    public void bufferAudioEnd() throws IOException {
        cleanUpFiles("test_out/tmp/buffered_example_audio_END.mp3");

        String test_audio_loc =
                new ClassPathResource("test_files/example_audio.mp3").getFile().getAbsolutePath();

        Optional<Long> length = FFmpegUtil.getAudioDuration(test_audio_loc);

        assertFalse(length.isEmpty(), "Audio length is empty");
        assertEquals(38, length.get());

        Optional<String> bufferedAudioPath_END = FFmpegUtil.bufferAudio(test_audio_loc, BufferPos.END, 3);
        assertFalse(bufferedAudioPath_END.isEmpty(), "Buffered audio path is empty");
        assertEquals("test_out/tmp/buffered_example_audio_END.mp3", bufferedAudioPath_END.get());
        Optional<Long> lengthBuffered_END = FFmpegUtil.getAudioDuration(bufferedAudioPath_END.get());
        assertFalse(lengthBuffered_END.isEmpty(), "Buffered audio length is empty");
        assertEquals(41, lengthBuffered_END.get());
    }

    @Test
    public void mergeAudio() throws IOException {
        cleanUpFiles("test_out/tmp/merged_example_title_audio_example_audio.mp3");
        String test_title_audio_loc = new ClassPathResource("test_files/example_title_audio.mp3").getFile().getAbsolutePath();
        String test_audio_loc = new ClassPathResource("test_files/example_audio.mp3").getFile().getAbsolutePath();

        Optional<String> mergedAudioPath = FFmpegUtil.mergeAudio(test_title_audio_loc, test_audio_loc);

        assertFalse(mergedAudioPath.isEmpty(), "Merged audio path is empty");
        assertEquals("test_out/tmp/merged_example_title_audio_example_audio.mp3", mergedAudioPath.get());

        Optional<Long> lengthMerged = FFmpegUtil.getAudioDuration(mergedAudioPath.get());
        assertFalse(lengthMerged.isEmpty(), "Merged audio length is empty");

        assertEquals(41, lengthMerged.get());
    }

    @Test
    public void overlayVideo() throws IOException {
        String test_title_img_loc = new ClassPathResource("test_files/example_title.png").getFile().getAbsolutePath();
        String test_video_loc = new ClassPathResource("test_files/resized_example_video.mp4").getFile().getAbsolutePath();

        Optional<String> overlayedVideoPath = FFmpegUtil.overlayImage(test_title_img_loc, test_video_loc, 3);

        assertFalse(overlayedVideoPath.isEmpty(), "Overlayed video path is empty");
        assertEquals("test_out/tmp/overlayed_example_title_resized_example_video.mp4", overlayedVideoPath.get());
    }
}