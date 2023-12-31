package com.jwtly10.aicontentgenerator.unitTests.utils;

import com.jwtly10.aicontentgenerator.BaseFileTest;
import com.jwtly10.aicontentgenerator.model.BufferPos;
import com.jwtly10.aicontentgenerator.model.VideoDimensions;
import com.jwtly10.aicontentgenerator.utils.FFmpegUtil;
import com.jwtly10.aicontentgenerator.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** FFmpegUtilTest */
@Slf4j
@SpringBootTest
public class FFmpegUtilTest extends BaseFileTest {

    @Autowired
    private FFmpegUtil ffmpegUtil;

    @Test
    public void getLengthOfAudio() throws IOException {
        String test_audio_loc =
                new ClassPathResource("test_files/example_audio.mp3").getFile().getAbsolutePath();

        Optional<Long> length = ffmpegUtil.getAudioDuration(test_audio_loc);

        assertFalse(length.isEmpty(), "Audio length is empty");
        assertEquals(38, length.get());
    }

    @Test
    public void getLengthOfVideo() throws IOException {
        String test_video_loc =
                new ClassPathResource("test_files/example_video.mp4").getFile().getAbsolutePath();

        Optional<Long> length = ffmpegUtil.getVideoDuration(test_video_loc);

        assertFalse(length.isEmpty(), "Video length is empty");
        assertEquals(40, length.get());
    }

    @Test
    public void generateVideo() {
        String fileUUID = FileUtils.getUUID();

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

            Optional<String> outputPath = ffmpegUtil.generateVideo(
                    test_video_loc,
                    test_audio_loc,
                    2,
                    test_srt_loc, fileUUID);

            assertFalse(outputPath.isEmpty(), "Output video path is empty");
            assertEquals(ffmpegOutPath + fileUUID + "_final.mp4", outputPath.get());
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }

        cleanUp(fileUUID);
    }

    @Test
    void resizeVideo() {
        cleanUpFiles(ffmpegTmpPath + "resized_example_video.mp4");

        try {
            String test_video_loc =
                    new ClassPathResource("test_files/example_video.mp4")
                            .getFile()
                            .getAbsolutePath();


            Optional<String> resizedVideoPath = ffmpegUtil.resizeVideo(test_video_loc);

            assertFalse(resizedVideoPath.isEmpty(), "Resized video path is empty");
            assertEquals(ffmpegTmpPath + "resized_example_video.mp4", resizedVideoPath.get());

            // TODO: Finalise and assert resized video dimensions (when this is configurable)
            Optional<VideoDimensions> resizedDims = ffmpegUtil.getVideoDimensions(resizedVideoPath.get());

            assertFalse(resizedDims.isEmpty(), "Resized video dimensions are empty");
            System.out.println("Resized video dimensions: " + resizedDims.get().getWidth() + "x" + resizedDims.get().getHeight());
            // TODO Assert something
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }

        cleanUpFiles(ffmpegTmpPath + "resized_example_video.mp4");
    }

    @Test
    void getVideoDimensions() {
        try {
            String test_video_loc =
                    new ClassPathResource("test_files/example_video.mp4")
                            .getFile()
                            .getAbsolutePath();

            Optional<VideoDimensions> dimensions = ffmpegUtil.getVideoDimensions(test_video_loc);

            assertFalse(dimensions.isEmpty(), "Video dimensions are empty");
            assertEquals(1280, dimensions.get().getWidth());
            assertEquals(720, dimensions.get().getHeight());
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }

    @Test
    public void bufferAudioStart() throws IOException {
        String test_audio_loc =
                new ClassPathResource("test_files/example_audio.mp3").getFile().getAbsolutePath();

        Optional<Long> length = ffmpegUtil.getAudioDuration(test_audio_loc);

        assertFalse(length.isEmpty(), "Audio length is empty");
        assertEquals(38, length.get());

        String fileUUID = FileUtils.getUUID();

        Optional<String> bufferedAudioPath_START = ffmpegUtil.bufferAudio(test_audio_loc, BufferPos.START, 2, fileUUID);
        assertFalse(bufferedAudioPath_START.isEmpty(), "Buffered audio path is empty");
        assertEquals(ffmpegTmpPath + fileUUID + "_buffered.mp3", bufferedAudioPath_START.get());
        Optional<Long> lengthBuffered_START = ffmpegUtil.getAudioDuration(bufferedAudioPath_START.get());
        assertFalse(lengthBuffered_START.isEmpty(), "Buffered audio length is empty");
        assertEquals(40, lengthBuffered_START.get());

        cleanUp(fileUUID);
    }

    @Test
    public void bufferAudioEnd() throws IOException {
        String test_audio_loc =
                new ClassPathResource("test_files/example_audio.mp3").getFile().getAbsolutePath();

        Optional<Long> length = ffmpegUtil.getAudioDuration(test_audio_loc);

        assertFalse(length.isEmpty(), "Audio length is empty");
        assertEquals(38, length.get());

        String fileUUID = FileUtils.getUUID();

        Optional<String> bufferedAudioPath_END = ffmpegUtil.bufferAudio(test_audio_loc, BufferPos.END, 3, fileUUID);
        assertFalse(bufferedAudioPath_END.isEmpty(), "Buffered audio path is empty");
        assertEquals(ffmpegTmpPath + fileUUID + "_buffered.mp3", bufferedAudioPath_END.get());
        Optional<Long> lengthBuffered_END = ffmpegUtil.getAudioDuration(bufferedAudioPath_END.get());
        assertFalse(lengthBuffered_END.isEmpty(), "Buffered audio length is empty");
        assertEquals(41, lengthBuffered_END.get());

        cleanUp(fileUUID);
    }

    @Test
    public void mergeAudio() throws IOException {
        String test_title_audio_loc = new ClassPathResource("test_files/example_title_audio.mp3").getFile().getAbsolutePath();
        String test_audio_loc = new ClassPathResource("test_files/example_audio.mp3").getFile().getAbsolutePath();

        String fileUUID = FileUtils.getUUID();

        Optional<String> mergedAudioPath = ffmpegUtil.mergeAudio(test_title_audio_loc, test_audio_loc, fileUUID);

        assertFalse(mergedAudioPath.isEmpty(), "Merged audio path is empty");
        assertEquals(ffmpegTmpPath + fileUUID + "_merged_audio.mp3", mergedAudioPath.get());

        Optional<Long> lengthMerged = ffmpegUtil.getAudioDuration(mergedAudioPath.get());
        assertFalse(lengthMerged.isEmpty(), "Merged audio length is empty");

        assertEquals(41, lengthMerged.get());

        cleanUp(fileUUID);
    }

    @Test
    public void overlayVideo() throws IOException {
        String test_title_img_loc = new ClassPathResource("test_files/reddit_title.png").getFile().getAbsolutePath();
        String test_video_loc = new ClassPathResource("test_files/resized_example_video.mp4").getFile().getAbsolutePath();

        String fileUUID = FileUtils.getUUID();

        Optional<String> overlayedVideoPath = ffmpegUtil.overlayImage(test_title_img_loc, test_video_loc, 3, fileUUID);

        assertFalse(overlayedVideoPath.isEmpty(), "Overlayed video path is empty");
        assertEquals(ffmpegTmpPath + fileUUID + "_overlayed.mp4", overlayedVideoPath.get());
        assertFileExists(overlayedVideoPath.get());

        cleanUp(fileUUID);
    }

    @Test
    public void loopVideo() throws IOException {
        String test_short_video_loc = new ClassPathResource("test_files/test_short_video.mp4").getFile().getAbsolutePath();

        String fileUUID = FileUtils.getUUID();

        Optional<String> loopedVideo = ffmpegUtil.loopVideo(39, test_short_video_loc, fileUUID);

        assertFalse(loopedVideo.isEmpty(), "Looped video path is empty");
        assertEquals(ffmpegTmpPath + fileUUID + "_looped.mp4", loopedVideo.get());

        Optional<Long> lengthLooped = ffmpegUtil.getVideoDuration(loopedVideo.get());
        assertFalse(lengthLooped.isEmpty(), "Looped video length is empty");
        assertEquals(39, lengthLooped.get());

        cleanUp(fileUUID);
    }

}