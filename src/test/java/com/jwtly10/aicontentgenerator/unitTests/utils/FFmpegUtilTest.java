package com.jwtly10.aicontentgenerator.unitTests.utils;

import com.jwtly10.aicontentgenerator.baseTests.TestBase;
import com.jwtly10.aicontentgenerator.model.ffmpeg.BufferPos;
import com.jwtly10.aicontentgenerator.model.ffmpeg.VideoDimensions;
import com.jwtly10.aicontentgenerator.utils.FFmpegUtil;
import com.jwtly10.aicontentgenerator.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** FFmpegUtilTest */
@Slf4j
@SpringBootTest
public class FFmpegUtilTest extends TestBase {

    @Autowired
    private FFmpegUtil ffmpegUtil;

    @Test
    public void getLengthOfAudio() {
        String test_audio_loc = getTestFileLocally("example_audio.mp3").orElseThrow();

        Long length = ffmpegUtil.getAudioDuration(test_audio_loc);

        assertEquals(38, length);

        cleanUpFiles(test_audio_loc);
    }

    @Test
    public void getLengthOfVideo() {
        String test_video_loc = getTestFileLocally("example_video.mp4").orElseThrow();

        Long length = ffmpegUtil.getVideoDuration(test_video_loc);

        assertEquals(40, length);

        cleanUpFiles(test_video_loc);
    }

    @Test
    public void generateVideo() {
        String fileUUID = "test_video_with_delay";

        String test_video_loc =
                getTestFileLocally("resized_example_video.mp4").orElseThrow();
        String test_audio_loc =
                getTestFileLocally("example_audio.mp3").orElseThrow();
        String test_srt_loc =
                getTestFileLocally("output.srt").orElseThrow();

        String outputPath = null;

        try {
            outputPath = ffmpegUtil.generateVideo(
                    test_video_loc,
                    test_audio_loc,
                    2,
                    test_srt_loc, fileUUID);

            assertFalse(outputPath.isEmpty(), "Output video path is empty");
            assertEquals(ffmpegOutPath + fileUUID + "_final.mp4", outputPath);
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }

        cleanTempFiles(fileUUID);
        cleanUpFiles(test_video_loc, test_audio_loc, test_srt_loc, outputPath);
    }

    @Test
    void resizeVideo() {
        String test_video_loc =
                getTestFileLocally("example_video.mp4").orElseThrow();

        String resizedVideoPath = null;
        try {
            resizedVideoPath = ffmpegUtil.resizeVideo(test_video_loc);

            assertFalse(resizedVideoPath.isEmpty(), "Resized video path is empty");
            assertEquals(ffmpegTmpPath + "resized_example_video.mp4", resizedVideoPath);

            // TODO: Finalise and assert resized video dimensions (when this is configurable)
            VideoDimensions resizedDims = ffmpegUtil.getVideoDimensions(resizedVideoPath);

            System.out.println("Resized video dimensions: " + resizedDims.getWidth() + "x" + resizedDims.getHeight());
            // TODO Assert something
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }

        cleanUpFiles(test_video_loc, resizedVideoPath);
    }

    @Test
    void getVideoDimensions() {
        String test_video_loc =
                getTestFileLocally("example_video.mp4").orElseThrow();

        try {

            VideoDimensions dimensions = ffmpegUtil.getVideoDimensions(test_video_loc);

            assertEquals(1280, dimensions.getWidth());
            assertEquals(720, dimensions.getHeight());
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }

        cleanUpFiles(test_video_loc);
    }

    @Test
    public void bufferAudioStart() {
        String test_audio_loc =
                getTestFileLocally("example_audio.mp3").orElseThrow();

        Long length = ffmpegUtil.getAudioDuration(test_audio_loc);

        assertEquals(38, length);

        String fileUUID = FileUtils.generateUUID();

        String bufferedAudioPath_START = ffmpegUtil.bufferAudio(test_audio_loc, BufferPos.START, 2, fileUUID);
        assertFalse(bufferedAudioPath_START.isEmpty(), "Buffered audio path is empty");
        assertEquals(ffmpegTmpPath + fileUUID + "_buffered.mp3", bufferedAudioPath_START);
        Long lengthBuffered_START = ffmpegUtil.getAudioDuration(bufferedAudioPath_START);
        assertEquals(40, lengthBuffered_START);

        cleanTempFiles(fileUUID);
        cleanUpFiles(test_audio_loc);
    }

    @Test
    public void bufferAudioEnd() {
        String test_audio_loc =
                getTestFileLocally("example_audio.mp3").orElseThrow();

        Long length = ffmpegUtil.getAudioDuration(test_audio_loc);

        assertEquals(38, length);

        String fileUUID = FileUtils.generateUUID();

        String bufferedAudioPath_END = ffmpegUtil.bufferAudio(test_audio_loc, BufferPos.END, 3, fileUUID);
        assertFalse(bufferedAudioPath_END.isEmpty(), "Buffered audio path is empty");
        assertEquals(ffmpegTmpPath + fileUUID + "_buffered.mp3", bufferedAudioPath_END);
        Long lengthBuffered_END = ffmpegUtil.getAudioDuration(bufferedAudioPath_END);
        assertEquals(41, lengthBuffered_END);

        cleanTempFiles(fileUUID);
        cleanUpFiles(test_audio_loc);
    }

    @Test
    public void mergeAudio() {
        String test_title_audio_loc = getTestFileLocally("example_title_audio.mp3").orElseThrow();
        String test_audio_loc = getTestFileLocally("example_audio.mp3").orElseThrow();

        String fileUUID = FileUtils.generateUUID();

        String mergedAudioPath = ffmpegUtil.mergeAudio(test_title_audio_loc, test_audio_loc, fileUUID);

        assertFalse(mergedAudioPath.isEmpty(), "Merged audio path is empty");
        assertEquals(ffmpegTmpPath + fileUUID + "_merged_audio.mp3", mergedAudioPath);

        Long lengthMerged = ffmpegUtil.getAudioDuration(mergedAudioPath);

        assertEquals(41, lengthMerged);

        cleanTempFiles(fileUUID);
        cleanUpFiles(test_title_audio_loc, test_audio_loc);
    }

    @Test
    public void overlayVideo() {
        String test_title_img_loc = getTestFileLocally("reddit_title.png").orElseThrow();
        String test_video_loc = getTestFileLocally("resized_example_video.mp4").orElseThrow();

        String fileUUID = FileUtils.generateUUID();

        String overlayedVideoPath = ffmpegUtil.overlayImage(test_title_img_loc, test_video_loc, 3, fileUUID);

        assertFalse(overlayedVideoPath.isEmpty(), "Overlayed video path is empty");
        assertEquals(ffmpegTmpPath + fileUUID + "_overlayed.mp4", overlayedVideoPath);
        assertFileExists(overlayedVideoPath);

        cleanTempFiles(fileUUID);
        cleanUpFiles(test_title_img_loc, test_video_loc);
    }

    @Test
    public void loopVideo() {
        String test_short_video_loc = getTestFileLocally("test_short_video.mp4").orElseThrow();

        String fileUUID = FileUtils.generateUUID();

        String loopedVideo = ffmpegUtil.loopVideo(39, test_short_video_loc, fileUUID);

        assertFalse(loopedVideo.isEmpty(), "Looped video path is empty");
        assertEquals(ffmpegTmpPath + fileUUID + "_looped.mp4", loopedVideo);

        Long lengthLooped = ffmpegUtil.getVideoDuration(loopedVideo);
        assertEquals(39, lengthLooped);

        cleanTempFiles(fileUUID);
        cleanUpFiles(test_short_video_loc);
    }

    @Test
    public void changeAudioTemp() throws IOException {
        String test_audio_loc = new ClassPathResource("local_media/test_audio.mp3").getFile().getAbsolutePath();

        String fileUUID = FileUtils.generateUUID();
        String changedAudioPath = ffmpegUtil.changeAudioTempo(test_audio_loc, 1.5, fileUUID);
        assertFalse(changedAudioPath.isEmpty(), "Changed audio path is empty");
        assertEquals(ffmpegTmpPath + fileUUID + "_sped_up.mp3", changedAudioPath);

        cleanTempFiles(fileUUID);
    }

    @Test
    public void trimVideoToSize() throws IOException {
        String test_video_loc = new ClassPathResource("local_media/minecraft_parkour_1.mp4").getFile().getAbsolutePath();

        String fileUUID = FileUtils.generateUUID();
        String cutVideoPath = ffmpegUtil.trimVideoToSize(test_video_loc, 100L, fileUUID);
        assertFalse(cutVideoPath.isEmpty(), "Cut video path is empty");
        assertEquals(ffmpegTmpPath + fileUUID + "_cut.mp4", cutVideoPath);

        String resized = ffmpegUtil.resizeVideo(cutVideoPath);

//        cleanTempFiles(fileUUID);
//        cleanUpFiles(test_video_loc);
    }

}