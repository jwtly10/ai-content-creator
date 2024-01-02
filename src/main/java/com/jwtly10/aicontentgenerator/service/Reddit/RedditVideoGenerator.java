package com.jwtly10.aicontentgenerator.service.Reddit;

import com.jwtly10.aicontentgenerator.exceptions.VideoGenerationException;
import com.jwtly10.aicontentgenerator.model.Gender;
import com.jwtly10.aicontentgenerator.model.Reddit.RedditTitle;
import com.jwtly10.aicontentgenerator.model.VideoProcessingState;
import com.jwtly10.aicontentgenerator.model.ffmpeg.FileMeta;
import com.jwtly10.aicontentgenerator.service.GoogleTTS.GoogleTTSGenerator;
import com.jwtly10.aicontentgenerator.service.OpenAI.OpenAPIService;
import com.jwtly10.aicontentgenerator.service.StorageService;
import com.jwtly10.aicontentgenerator.service.UserService;
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

    private final VoiceGenerator voiceGenerator;

    private final StorageService storageService;

    private final GentleAlignerUtil gentleAlignerUtil;

    private final OpenAPIService openAPIService;

    private final RedditTitleImageGenerator redditTitleImageGenerator;

    private final UserService userService;

    private final FFmpegUtil ffmpegUtil;

    public RedditVideoGenerator(GoogleTTSGenerator voiceGenerator, StorageService storageService, GentleAlignerUtil gentleAlignerUtil, OpenAPIService openAPIService, RedditTitleImageGenerator redditTitleImageGenerator, UserService userService, FFmpegUtil ffmpegUtil) {
        this.voiceGenerator = voiceGenerator;
        this.storageService = storageService;
        this.gentleAlignerUtil = gentleAlignerUtil;
        this.openAPIService = openAPIService;
        this.redditTitleImageGenerator = redditTitleImageGenerator;
        this.userService = userService;
        this.ffmpegUtil = ffmpegUtil;
    }

    /**
     * Generate reddit video
     *
     * @param title     Title of reddit post
     * @param content   Content of reddit post
     * @param backgroundVideoPath Path to background video
     * @return Optional path to generated video
     */
    public String generateContent(String processUUID, RedditTitle title, String content, String backgroundVideoPath) {

        log.info("Generating video for title: {}, processUUID: {}", title.getTitle(), processUUID);
        userService.updateVideoProcessLog(userService.getLoggedInUserId(), processUUID, null, VideoProcessingState.PROCESSING);

        String newContent = content; // Default content
        try {
            newContent = openAPIService.improveContent(content);
        } catch (Exception e) {
            log.error("Failed to improve content, using original content ", e);
            throw new VideoGenerationException(e.getMessage());
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
            throw new VideoGenerationException("Failed to generate voice for title");
        }

        Optional<Long> titleLength = ffmpegUtil.getAudioDuration(titleAudio.get());
        if (titleLength.isEmpty()) {
            log.error("Failed to get length of title audio");
            throw new VideoGenerationException("Failed to get length of title audio");
        }

        // Generate voice for content
        Optional<String> contentAudio = voiceGenerator.generateVoice(newContent, gender, processUUID + "_content");
        if (contentAudio.isEmpty()) {
            log.error("Failed to generate voice for content");
            throw new VideoGenerationException("Failed to generate voice for content");
        }

        // Generate SRT for voice
        Optional<String> contentSRT = gentleAlignerUtil.alignAndGenerateSRT(contentAudio.get(), newContent, processUUID);
        if (contentSRT.isEmpty()) {
            log.error("Failed to generate SRT for content");
            throw new VideoGenerationException("Failed to generate SRT for content");
        }

        // Merge audios
        Optional<String> mergedAudio = ffmpegUtil.mergeAudio(titleAudio.get(), contentAudio.get(), processUUID);
        if (mergedAudio.isEmpty()) {
            log.error("Failed to merge audio");
            throw new VideoGenerationException("Failed to merge audio");
        }

        Optional<Long> mergedAudioLength = ffmpegUtil.getAudioDuration(mergedAudio.get());
        if (mergedAudioLength.isEmpty()) {
            log.error("Failed to get length of merged audio");
            throw new VideoGenerationException("Failed to get length of merged audio");
        }

        // Check if background video needs to be looped
        Optional<Long> videoLength = ffmpegUtil.getVideoDuration(backgroundVideoPath);
        if (videoLength.isEmpty()) {
            log.error("Failed to get length of video");
            throw new VideoGenerationException("Failed to get length of video");
        }

        if (mergedAudioLength.get() > videoLength.get()) {
            log.info("Merged audio is longer than video, looping video");
            Optional<String> loopedVideo = ffmpegUtil.loopVideo(mergedAudioLength.get(), backgroundVideoPath, processUUID);
            if (loopedVideo.isEmpty()) {
                log.error("Failed to loop video");
                throw new VideoGenerationException("Failed to loop video");
            }
            backgroundVideoPath = loopedVideo.get();
        }

        Optional<String> overlayImg = redditTitleImageGenerator.generateImage(title, processUUID);
        if (overlayImg.isEmpty()) {
            log.error("Failed to generate image");
            throw new VideoGenerationException("Failed to generate image");
        }
        Optional<String> videoWithOverlay = ffmpegUtil.overlayImage(overlayImg.get(), backgroundVideoPath, titleLength.get(), processUUID);
        if (videoWithOverlay.isEmpty()) {
            log.error("Failed to overlay image");
            throw new VideoGenerationException("Failed to overlay image");
        }

        // Generate video
        Optional<String> video = ffmpegUtil.generateVideo(videoWithOverlay.get(),
                mergedAudio.get(), titleLength.get(), contentSRT.get(), processUUID);
        if (video.isEmpty()) {
            log.error("Failed to generate video");
            throw new VideoGenerationException("Failed to generate video");
        }

        FileUtils.cleanUpTempFiles(processUUID, tmpPath);

        FileMeta videoMeta = FileUtils.create(video.get());

        // Save Video
        storageService.uploadVideo(processUUID, video.get());

        // Update process
        userService.updateVideoProcessLog(userService.getLoggedInUserId(), processUUID,
                videoMeta.getFileName() + "." + videoMeta.getExtension(), VideoProcessingState.COMPLETED);

        // Clean up output folder
        FileUtils.cleanUpFile(video.get());
        return processUUID;
    }
}
