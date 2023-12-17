package com.jwtly10.aicontentgenerator.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.jwtly10.aicontentgenerator.models.VideoGen;

import org.junit.jupiter.api.Test;

/** FFmpegUtilTest */
public class FFmpegUtilTest {

    @Test
    public void getLengthOfAudio() {
        VideoGen video =
                VideoGen.builder()
                        .backgroundVideoPath("test_media/example_video.mp4")
                        .titleImgPath("")
                        .titleAudioPath("")
                        .titleTextPath("")
                        .contentAudioPath("test_media/example_audio.mp3")
                        .contentTextPath("test_media/example_text.txt")
                        .build();

        Long length = FFmpegUtil.getAudioDuration(video.getContentAudioPath());

        assertEquals(38, length);
    }

    @Test
    public void getLengthOfVideo() {
        VideoGen video =
                VideoGen.builder()
                        .backgroundVideoPath("test_media/example_video.mp4")
                        .titleImgPath("")
                        .titleAudioPath("")
                        .titleTextPath("")
                        .contentAudioPath("test_media/example_audio.mp3")
                        .contentTextPath("test_media/example_text.txt")
                        .build();

        Long length = FFmpegUtil.getVideoDuration(video.getBackgroundVideoPath());

        assertEquals(299, length);
    }

    @Test
    public void generateVideo() {
        VideoGen video =
                VideoGen.builder()
                        .backgroundVideoPath("test_media/example_video.mp4")
                        .titleImgPath("")
                        .titleAudioPath("")
                        .titleTextPath("")
                        .contentAudioPath("test_media/example_audio.mp3")
                        .contentTextPath("test_media/example_text.txt")
                        .build();

        FFmpegUtil.generateVideo(
                video.getBackgroundVideoPath(),
                video.getContentAudioPath(),
                "test_media/output.srt");
    }
}
