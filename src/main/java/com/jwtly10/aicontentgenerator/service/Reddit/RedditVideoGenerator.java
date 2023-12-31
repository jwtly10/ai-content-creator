package com.jwtly10.aicontentgenerator.service.Reddit;

import com.jwtly10.aicontentgenerator.model.ElevenLabs.ElevenLabsVoice;
import com.jwtly10.aicontentgenerator.model.Gender;
import com.jwtly10.aicontentgenerator.model.Reddit.RedditTitle;
import com.jwtly10.aicontentgenerator.service.OpenAI.OpenAPIService;
import com.jwtly10.aicontentgenerator.service.VoiceGenerator;
import com.jwtly10.aicontentgenerator.utils.FFmpegUtil;
import com.jwtly10.aicontentgenerator.utils.FileUtils;
import com.jwtly10.aicontentgenerator.utils.GentleAlignerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class RedditVideoGenerator {

    @Value("${file.tmp.path}")
    private String tmpPath;

    private VoiceGenerator<ElevenLabsVoice> voiceGenerator;

    private final GentleAlignerUtil gentleAlignerUtil;

    private final OpenAPIService openAPIService;

    private final RedditTitleImageGenerator redditTitleImageGenerator;

    private final FFmpegUtil ffmpegUtil;

    public RedditVideoGenerator(VoiceGenerator<ElevenLabsVoice> voiceGenerator, GentleAlignerUtil gentleAlignerUtil, OpenAPIService openAPIService, RedditTitleImageGenerator redditTitleImageGenerator, FFmpegUtil ffmpegUtil) {
        this.voiceGenerator = voiceGenerator;
        this.gentleAlignerUtil = gentleAlignerUtil;
        this.openAPIService = openAPIService;
        this.redditTitleImageGenerator = redditTitleImageGenerator;
        this.ffmpegUtil = ffmpegUtil;
    }

    /**
     * Generate reddit video
     *
     * @param title     Title of reddit post
     * @param content   Content of reddit post
     * @param videoPath Path to background video
     * @return Optional path to generated video
     */
    public Optional<String> generateContent(RedditTitle title, String content, String videoPath) {

        String processUUID = FileUtils.getUUID();

        log.info("Generating video for title: {}, processUUID: {}", title.getTitle(), processUUID);

        String newContent = content; // Default content
        try {
            newContent = openAPIService.improveContent(content);
        } catch (Exception e) {
            log.error("Failed to improve content, using original content ", e);
        }

        Gender gender = Gender.MALE; // The default voice
        try {
            gender = openAPIService.determineGender(newContent);
        } catch (Exception e) {
            log.error("Failed to determine Gender, defaulting to male");
        }

        // Generate voice for title
        Optional<String> titleAudio = voiceGenerator.generateVoice(title.getTitle(), gender, processUUID + "_title");
        if (titleAudio.isEmpty()) {
            log.error("Failed to generate voice for title");
            return Optional.empty();
        }

        Optional<Long> titleLength = ffmpegUtil.getAudioDuration(titleAudio.get());
        if (titleLength.isEmpty()) {
            log.error("Failed to get length of title audio");
            return Optional.empty();
        }

        // Generate voice for content
        Optional<String> contentAudio = voiceGenerator.generateVoice(newContent, gender, processUUID + "_content");
        if (contentAudio.isEmpty()) {
            log.error("Failed to generate voice for content");
            return Optional.empty();
        }

        // Generate SRT for voice
        Optional<String> contentSRT = gentleAlignerUtil.alignAndGenerateSRT(contentAudio.get(), newContent, processUUID);
        if (contentSRT.isEmpty()) {
            log.error("Failed to generate SRT for content");
            return Optional.empty();
        }

        // Merge audios
        Optional<String> mergedAudio = ffmpegUtil.mergeAudio(titleAudio.get(), contentAudio.get(), processUUID);
        if (mergedAudio.isEmpty()) {
            log.error("Failed to merge audio");
            return Optional.empty();
        }

        Optional<Long> mergedAudioLength = ffmpegUtil.getAudioDuration(mergedAudio.get());
        if (mergedAudioLength.isEmpty()) {
            log.error("Failed to get length of merged audio");
            return Optional.empty();
        }

        // Check if background video needs to be looped
        Optional<Long> videoLength = ffmpegUtil.getVideoDuration(videoPath);
        if (videoLength.isEmpty()) {
            log.error("Failed to get length of video");
            return Optional.empty();
        }

        if (mergedAudioLength.get() > videoLength.get()) {
            log.info("Merged audio is longer than video, looping video");
            Optional<String> loopedVideo = ffmpegUtil.loopVideo(mergedAudioLength.get(), videoPath, processUUID);
            if (loopedVideo.isEmpty()) {
                log.error("Failed to loop video");
                return Optional.empty();
            }
            videoPath = loopedVideo.get();
        }

        Optional<String> overlayImg = redditTitleImageGenerator.generateImage(title, processUUID);
        if (overlayImg.isEmpty()) {
            log.error("Failed to generate image");
            return Optional.empty();
        }
        Optional<String> videoWithOverlay = ffmpegUtil.overlayImage(overlayImg.get(), videoPath, titleLength.get(), processUUID);
        if (videoWithOverlay.isEmpty()) {
            log.error("Failed to overlay image");
            return Optional.empty();
        }

        // Generate video
        Optional<String> video = ffmpegUtil.generateVideo(videoWithOverlay.get(),
                mergedAudio.get(), titleLength.get(), contentSRT.get(), processUUID);
        if (video.isEmpty()) {
            log.error("Failed to generate video");
            return Optional.empty();
        }

        FileUtils.cleanUpTempFiles(processUUID, tmpPath);

        // TODO:
        // Log process to DB
        // Upload video to S3
        // Return video URL
        // Bubble up errors
        // Clean up local files
        return video;
    }
}
