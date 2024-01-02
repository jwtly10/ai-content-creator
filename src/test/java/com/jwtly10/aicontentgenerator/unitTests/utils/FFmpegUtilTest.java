package com.jwtly10.aicontentgenerator.unitTests.utils;

import com.jwtly10.aicontentgenerator.BaseFileTest;
import com.jwtly10.aicontentgenerator.model.ffmpeg.BufferPos;
import com.jwtly10.aicontentgenerator.model.ffmpeg.VideoDimensions;
import com.jwtly10.aicontentgenerator.utils.FFmpegUtil;
import com.jwtly10.aicontentgenerator.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
    public void getLengthOfAudio() {
        String test_audio_loc = getFileLocally("example_audio.mp3").orElseThrow();

        Optional<Long> length = ffmpegUtil.getAudioDuration(test_audio_loc);

        assertFalse(length.isEmpty(), "Audio length is empty");
        assertEquals(38, length.orElseThrow());

        cleanUpFiles(test_audio_loc);
    }

    @Test
    public void getLengthOfVideo() {
        String test_video_loc = getFileLocally("example_video.mp4").orElseThrow();

        Optional<Long> length = ffmpegUtil.getVideoDuration(test_video_loc);

        assertFalse(length.isEmpty(), "Video length is empty");
        assertEquals(40, length.orElseThrow());

        cleanUpFiles(test_video_loc);
    }

    @Test
    public void generateVideo() {
        String fileUUID = "test_video_with_delay";

        String test_video_loc =
                getFileLocally("resized_example_video.mp4").orElseThrow();
        String test_audio_loc =
                getFileLocally("example_audio.mp3").orElseThrow();
        String test_srt_loc =
                getFileLocally("output.srt").orElseThrow();

        Optional<String> outputPath = Optional.empty();

        try {
            outputPath = ffmpegUtil.generateVideo(
                    test_video_loc,
                    test_audio_loc,
                    2,
                    test_srt_loc, fileUUID);

            assertFalse(outputPath.isEmpty(), "Output video path is empty");
            assertEquals(ffmpegOutPath + fileUUID + "_final.mp4", outputPath.orElseThrow());
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }

        cleanTempFiles(fileUUID);
        cleanUpFiles(test_video_loc, test_audio_loc, test_srt_loc, outputPath.orElseThrow());
    }

    @Test
    void resizeVideo() {
        String test_video_loc =
                getFileLocally("example_video.mp4").orElseThrow();

        Optional<String> resizedVideoPath = Optional.empty();
        try {
            resizedVideoPath = ffmpegUtil.resizeVideo(test_video_loc);

            assertFalse(resizedVideoPath.isEmpty(), "Resized video path is empty");
            assertEquals(ffmpegTmpPath + "resized_example_video.mp4", resizedVideoPath.orElseThrow());

            // TODO: Finalise and assert resized video dimensions (when this is configurable)
            Optional<VideoDimensions> resizedDims = ffmpegUtil.getVideoDimensions(resizedVideoPath.orElseThrow());

            assertFalse(resizedDims.isEmpty(), "Resized video dimensions are empty");
            System.out.println("Resized video dimensions: " + resizedDims.orElseThrow().getWidth() + "x" + resizedDims.orElseThrow().getHeight());
            // TODO Assert something
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }

        cleanUpFiles(test_video_loc, resizedVideoPath.orElseThrow());
    }

    @Test
    void getVideoDimensions() {
        String test_video_loc =
                getFileLocally("example_video.mp4").orElseThrow();

        try {

            Optional<VideoDimensions> dimensions = ffmpegUtil.getVideoDimensions(test_video_loc);

            assertFalse(dimensions.isEmpty(), "Video dimensions are empty");
            assertEquals(1280, dimensions.orElseThrow().getWidth());
            assertEquals(720, dimensions.orElseThrow().getHeight());
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }

        cleanUpFiles(test_video_loc);
    }

    @Test
    public void bufferAudioStart() {
        String test_audio_loc =
                getFileLocally("example_audio.mp3").orElseThrow();

        Optional<Long> length = ffmpegUtil.getAudioDuration(test_audio_loc);

        assertFalse(length.isEmpty(), "Audio length is empty");
        assertEquals(38, length.orElseThrow());

        String fileUUID = FileUtils.getUUID();

        Optional<String> bufferedAudioPath_START = ffmpegUtil.bufferAudio(test_audio_loc, BufferPos.START, 2, fileUUID);
        assertFalse(bufferedAudioPath_START.isEmpty(), "Buffered audio path is empty");
        assertEquals(ffmpegTmpPath + fileUUID + "_buffered.mp3", bufferedAudioPath_START.orElseThrow());
        Optional<Long> lengthBuffered_START = ffmpegUtil.getAudioDuration(bufferedAudioPath_START.orElseThrow());
        assertFalse(lengthBuffered_START.isEmpty(), "Buffered audio length is empty");
        assertEquals(40, lengthBuffered_START.orElseThrow());

        cleanTempFiles(fileUUID);
        cleanUpFiles(test_audio_loc);
    }

    @Test
    public void bufferAudioEnd() {
        String test_audio_loc =
                getFileLocally("example_audio.mp3").orElseThrow();

        Optional<Long> length = ffmpegUtil.getAudioDuration(test_audio_loc);

        assertFalse(length.isEmpty(), "Audio length is empty");
        assertEquals(38, length.orElseThrow());

        String fileUUID = FileUtils.getUUID();

        Optional<String> bufferedAudioPath_END = ffmpegUtil.bufferAudio(test_audio_loc, BufferPos.END, 3, fileUUID);
        assertFalse(bufferedAudioPath_END.isEmpty(), "Buffered audio path is empty");
        assertEquals(ffmpegTmpPath + fileUUID + "_buffered.mp3", bufferedAudioPath_END.orElseThrow());
        Optional<Long> lengthBuffered_END = ffmpegUtil.getAudioDuration(bufferedAudioPath_END.orElseThrow());
        assertFalse(lengthBuffered_END.isEmpty(), "Buffered audio length is empty");
        assertEquals(41, lengthBuffered_END.orElseThrow());

        cleanTempFiles(fileUUID);
        cleanUpFiles(test_audio_loc);
    }

    @Test
    public void mergeAudio() {
        String test_title_audio_loc = getFileLocally("example_title_audio.mp3").orElseThrow();
        String test_audio_loc = getFileLocally("example_audio.mp3").orElseThrow();

        String fileUUID = FileUtils.getUUID();

        Optional<String> mergedAudioPath = ffmpegUtil.mergeAudio(test_title_audio_loc, test_audio_loc, fileUUID);

        assertFalse(mergedAudioPath.isEmpty(), "Merged audio path is empty");
        assertEquals(ffmpegTmpPath + fileUUID + "_merged_audio.mp3", mergedAudioPath.orElseThrow());

        Optional<Long> lengthMerged = ffmpegUtil.getAudioDuration(mergedAudioPath.orElseThrow());
        assertFalse(lengthMerged.isEmpty(), "Merged audio length is empty");

        assertEquals(41, lengthMerged.orElseThrow());

        cleanTempFiles(fileUUID);
        cleanUpFiles(test_title_audio_loc, test_audio_loc);
    }

    @Test
    public void overlayVideo() {
        String test_title_img_loc = getFileLocally("reddit_title.png").orElseThrow();
        String test_video_loc = getFileLocally("resized_example_video.mp4").orElseThrow();

        String fileUUID = FileUtils.getUUID();

        Optional<String> overlayedVideoPath = ffmpegUtil.overlayImage(test_title_img_loc, test_video_loc, 3, fileUUID);

        assertFalse(overlayedVideoPath.isEmpty(), "Overlayed video path is empty");
        assertEquals(ffmpegTmpPath + fileUUID + "_overlayed.mp4", overlayedVideoPath.orElseThrow());
        assertFileExists(overlayedVideoPath.orElseThrow());

        cleanTempFiles(fileUUID);
        cleanUpFiles(test_title_img_loc, test_video_loc);
    }

    @Test
    public void loopVideo() {
        String test_short_video_loc = getFileLocally("test_short_video.mp4").orElseThrow();

        String fileUUID = FileUtils.getUUID();

        Optional<String> loopedVideo = ffmpegUtil.loopVideo(39, test_short_video_loc, fileUUID);

        assertFalse(loopedVideo.isEmpty(), "Looped video path is empty");
        assertEquals(ffmpegTmpPath + fileUUID + "_looped.mp4", loopedVideo.orElseThrow());

        Optional<Long> lengthLooped = ffmpegUtil.getVideoDuration(loopedVideo.orElseThrow());
        assertFalse(lengthLooped.isEmpty(), "Looped video length is empty");
        assertEquals(39, lengthLooped.orElseThrow());

        cleanTempFiles(fileUUID);
        cleanUpFiles(test_short_video_loc);
    }

}