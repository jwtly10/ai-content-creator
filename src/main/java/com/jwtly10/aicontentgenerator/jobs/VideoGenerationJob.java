package com.jwtly10.aicontentgenerator.jobs;

import com.jwtly10.aicontentgenerator.exceptions.JobException;
import com.jwtly10.aicontentgenerator.model.*;
import com.jwtly10.aicontentgenerator.model.Reddit.RedditTitle;
import com.jwtly10.aicontentgenerator.model.ffmpeg.FileMeta;
import com.jwtly10.aicontentgenerator.repository.VideoContentDAO;
import com.jwtly10.aicontentgenerator.service.GoogleTTS.GoogleTTSGenerator;
import com.jwtly10.aicontentgenerator.service.OpenAI.OpenAPIService;
import com.jwtly10.aicontentgenerator.service.Reddit.RedditTitleImageGenerator;
import com.jwtly10.aicontentgenerator.service.StorageService;
import com.jwtly10.aicontentgenerator.service.Supabase.SBStorageService;
import com.jwtly10.aicontentgenerator.service.VideoService;
import com.jwtly10.aicontentgenerator.service.VoiceGenerator;
import com.jwtly10.aicontentgenerator.utils.FFmpegUtil;
import com.jwtly10.aicontentgenerator.utils.FileUtils;
import com.jwtly10.aicontentgenerator.utils.GentleAlignerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

/**
 * VideoGenerationJob
 */
@Service
@Slf4j
public class VideoGenerationJob {

    private final VideoContentDAO<VideoContent> videoContentDAO;
    private final VideoService videoService;
    private final VoiceGenerator voiceGenerator;
    private final StorageService storageService;
    private final GentleAlignerUtil gentleAlignerUtil;
    private final OpenAPIService openAPIService;
    private final FFmpegUtil ffmpegUtil;

    private final RedditTitleImageGenerator redditTitleImageGenerator;

    @Value("${video.generation.job.rate}")
    private long rate;

    @Value("${file.tmp.path}")
    private String tmpPath;

    @Value("${job.process.limit}")
    private int processLimit;

    public VideoGenerationJob(VideoContentDAO<VideoContent> videoContentDAO,
                              VideoService videoService,
                              GoogleTTSGenerator voiceGenerator,
                              SBStorageService storageService,
                              GentleAlignerUtil gentleAlignerUtil,
                              OpenAPIService openAPIService,
                              FFmpegUtil ffmpegUtil,
                              RedditTitleImageGenerator redditTitleImageGenerator
    ) {
        this.videoContentDAO = videoContentDAO;
        this.videoService = videoService;
        this.voiceGenerator = voiceGenerator;
        this.storageService = storageService;
        this.gentleAlignerUtil = gentleAlignerUtil;
        this.openAPIService = openAPIService;
        this.ffmpegUtil = ffmpegUtil;
        this.redditTitleImageGenerator = redditTitleImageGenerator;
    }

    @Async
    @Scheduled(fixedRateString = "${video.generation.job.rate}")
    public void run() {
        log.info("VideoGenerationJob started");

        // Configurable limit to process
        List<UserVideo> pendingVideos = videoService.getPendingVideos(processLimit);

        if (pendingVideos.isEmpty()) {
            log.info("No videos to process");
            return;
        }

        // Update video process to processing
        for (UserVideo userVideo : pendingVideos) {
            videoService.updateVideoProcess(userVideo.getVideoId(), VideoProcessingState.PROCESSING, null);
        }

        pendingVideos.parallelStream().forEach(this::process);
    }

    private void process(UserVideo userVideo) {

        // Get video content from process
        VideoContent videoContent = videoContentDAO.get(userVideo.getVideoId()).orElseThrow(
                () -> new JobException("Video Content not found")
        );

        final String PROCESSID = userVideo.getVideoId();
        final String CONTENT = videoContent.getContent();
        // In frontend the value passed for background video will just be the name of the video - mp4
        // TODO: Impl better bg videos
//        final String BACKGROUND_VIDEO = videoContent.getBackgroundVideo() + ".mp4";
        final String BACKGROUND_VIDEO = "test_short_video.mp4";

        final String TITLE = videoContent.getTitle();
        final String SUBREDDIT = videoContent.getSubreddit();

        // GENERATE VIDEO
        // Get background video if it hasn't already been downloaded
        String backgroundVideoPath = storageService.downloadVideo(BACKGROUND_VIDEO, "background-videos/");

        videoService.updateVideoProcess(userVideo.getVideoId(), VideoProcessingState.PROCESSING, null);

        Video videoObj = new Video();


        try {
            // Get voice
            Gender gender = Gender.MALE; // The default voice
            try {
                gender = openAPIService.determineGender(CONTENT);
            } catch (Exception e) {
                log.error("Failed to determine Gender, defaulting to male for process {}", PROCESSID);
            }

            String titleAudio = voiceGenerator.generateVoice(TITLE, gender, PROCESSID + "_title");
            Long titleLength = ffmpegUtil.getAudioDuration(titleAudio);

            String contentAudio = voiceGenerator.generateVoice(CONTENT, gender, PROCESSID + "_content");

            // Generate SRT for voice
            String contentSRT = gentleAlignerUtil.alignAndGenerateSRT(contentAudio, CONTENT, PROCESSID);

            // Merge audios
            String mergedAudio = ffmpegUtil.mergeAudio(titleAudio, contentAudio, PROCESSID);
            Long mergedAudioLength = ffmpegUtil.getAudioDuration(mergedAudio);

            // Implement server side max video length based on user role

            // Check if background video needs to be looped
            Long videoLength = ffmpegUtil.getVideoDuration(backgroundVideoPath);

            if (mergedAudioLength > videoLength) {
                log.info("Merged audio is longer than video, looping video for proccess {}", PROCESSID);
                backgroundVideoPath = ffmpegUtil.loopVideo(mergedAudioLength, backgroundVideoPath, PROCESSID);
            }

            String overlayImg = redditTitleImageGenerator.generateImage(new RedditTitle(TITLE, SUBREDDIT), PROCESSID);
            String videoWithOverlay = ffmpegUtil.overlayImage(overlayImg, backgroundVideoPath, titleLength, PROCESSID);

            // Generate video
            String video = ffmpegUtil.generateVideo(videoWithOverlay,
                    mergedAudio, titleLength, contentSRT, PROCESSID);

            FileUtils.cleanUpTempFiles(PROCESSID, tmpPath);

            // Log video data from generated video
            FileMeta videoMeta = FileUtils.create(video);
            videoObj.setVideoId(PROCESSID);
            videoObj.setFileName(videoMeta.getFileName() + "." + videoMeta.getExtension());
            videoObj.setLength(ffmpegUtil.getVideoDuration(video));

            // Save Video
            storageService.uploadVideo(PROCESSID, video);
            // Set upload related data
            videoObj.setFileUrl(storageService.getVideoUrl(PROCESSID));
            videoObj.setUploadDate(new Timestamp(System.currentTimeMillis()));

            // Update process
            videoService.updateVideoProcess(PROCESSID, VideoProcessingState.COMPLETED, null);

            // Update video
            videoService.updateVideo(videoObj);

            // Clean up output folder
            FileUtils.cleanUpFile(video);

            videoService.updateVideoProcess(userVideo.getVideoId(), VideoProcessingState.COMPLETED, null);
        } catch (Exception e) {
            log.error("Error generating video for process {}", PROCESSID, e);
            videoService.updateVideoProcess(userVideo.getVideoId(), VideoProcessingState.FAILED, e.getMessage());
            FileUtils.cleanUpTempFiles(PROCESSID, tmpPath);
        }
    }
}
