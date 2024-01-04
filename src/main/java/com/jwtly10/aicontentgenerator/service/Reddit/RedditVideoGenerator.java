package com.jwtly10.aicontentgenerator.service.Reddit;

import com.jwtly10.aicontentgenerator.exceptions.VideoGenerationException;
import com.jwtly10.aicontentgenerator.model.Gender;
import com.jwtly10.aicontentgenerator.model.Reddit.RedditTitle;
import com.jwtly10.aicontentgenerator.model.Video;
import com.jwtly10.aicontentgenerator.model.VideoProcessingState;
import com.jwtly10.aicontentgenerator.model.ffmpeg.FileMeta;
import com.jwtly10.aicontentgenerator.service.GoogleTTS.GoogleTTSGenerator;
import com.jwtly10.aicontentgenerator.service.OpenAI.OpenAPIService;
import com.jwtly10.aicontentgenerator.service.StorageService;
import com.jwtly10.aicontentgenerator.service.VideoService;
import com.jwtly10.aicontentgenerator.service.VoiceGenerator;
import com.jwtly10.aicontentgenerator.utils.FFmpegUtil;
import com.jwtly10.aicontentgenerator.utils.FileUtils;
import com.jwtly10.aicontentgenerator.utils.GentleAlignerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
@Slf4j
public class RedditVideoGenerator {

    @Value("${file.tmp.path}")
    private String tmpPath;

    private final VoiceGenerator voiceGenerator;

    private final StorageService storageService;

    private final VideoService videoService;

    private final GentleAlignerUtil gentleAlignerUtil;

    private final OpenAPIService openAPIService;

    private final RedditTitleImageGenerator redditTitleImageGenerator;

    private final FFmpegUtil ffmpegUtil;

    public RedditVideoGenerator(GoogleTTSGenerator voiceGenerator, StorageService storageService, VideoService videoService, GentleAlignerUtil gentleAlignerUtil, OpenAPIService openAPIService, RedditTitleImageGenerator redditTitleImageGenerator, FFmpegUtil ffmpegUtil) {
        this.voiceGenerator = voiceGenerator;
        this.storageService = storageService;
        this.videoService = videoService;
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
     * @param backgroundVideoPath Path to background video
     * @return Process uuid
     */
    public String generateContent(String processUUID, RedditTitle title, String content, String backgroundVideoPath) {

        log.info("Generating video for title: {}, processUUID: {}", title.getTitle(), processUUID);
        videoService.updateVideoProcessLog(processUUID, VideoProcessingState.PROCESSING, null);
        Video videoObj = new Video();

        try {
            String newContent = content; // Default content
            try {
                // TODO: Ensure we can trust OpenAI content changes
                // Noticed its making some weird changes to the content, so disabling for now
//                newContent = openAPIService.improveContent(content);
            } catch (Exception e) {
                log.error("Failed to improve content, using original content: {}", e.getMessage());
            }

            Gender gender = Gender.MALE; // The default voice
            try {
                gender = openAPIService.determineGender(newContent);
            } catch (Exception e) {
                log.error("Failed to determine Gender, defaulting to male");
            }

            String titleAudio = voiceGenerator.generateVoice(title.getTitle(), gender, processUUID + "_title");

            Long titleLength = ffmpegUtil.getAudioDuration(titleAudio);

            String contentAudio = voiceGenerator.generateVoice(newContent, gender, processUUID + "_content");

            // Generate SRT for voice
            String contentSRT = gentleAlignerUtil.alignAndGenerateSRT(contentAudio, newContent, processUUID);

            // Merge audios
            String mergedAudio = ffmpegUtil.mergeAudio(titleAudio, contentAudio, processUUID);

            Long mergedAudioLength = ffmpegUtil.getAudioDuration(mergedAudio);

            // Check if background video needs to be looped
            Long videoLength = ffmpegUtil.getVideoDuration(backgroundVideoPath);

            if (mergedAudioLength > videoLength) {
                log.info("Merged audio is longer than video, looping video");
                backgroundVideoPath = ffmpegUtil.loopVideo(mergedAudioLength, backgroundVideoPath, processUUID);
            }

            String overlayImg = redditTitleImageGenerator.generateImage(title, processUUID);

            String videoWithOverlay = ffmpegUtil.overlayImage(overlayImg, backgroundVideoPath, titleLength, processUUID);

            // Generate video
            String video = ffmpegUtil.generateVideo(videoWithOverlay,
                    mergedAudio, titleLength, contentSRT, processUUID);

            FileUtils.cleanUpTempFiles(processUUID, tmpPath);

            // Log video data
            FileMeta videoMeta = FileUtils.create(video);
            videoObj.setVideoId(processUUID);
            videoObj.setFileName(videoMeta.getFileName() + "." + videoMeta.getExtension());
            videoObj.setLength(ffmpegUtil.getVideoDuration(video));
            videoObj.setTitle(title.getTitle());

            // Save Video
            storageService.uploadVideo(processUUID, video);
            // Set video url here
//            videoObj.setFileUrl();
            videoObj.setUploadDate(new Timestamp(System.currentTimeMillis()));
            // Set video upload date

            // Update process
            videoService.updateVideoProcessLog(processUUID, VideoProcessingState.COMPLETED, null);
            // Update video
            videoService.updateVideo(videoObj);

            // Clean up output folder
            FileUtils.cleanUpFile(video);


        } catch (Exception e) {
            log.error("Failed to generate video for title: {}, processUUID: {}", title.getTitle(), processUUID);
            videoService.updateVideoProcessLog(processUUID, VideoProcessingState.FAILED, e.getMessage());
            FileUtils.cleanUpTempFiles(processUUID, tmpPath);
            throw new VideoGenerationException(e.getMessage());
        }

        return processUUID;
    }
}
