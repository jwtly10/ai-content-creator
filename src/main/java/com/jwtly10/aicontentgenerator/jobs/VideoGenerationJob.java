package com.jwtly10.aicontentgenerator.jobs;

import com.jwtly10.aicontentgenerator.exceptions.JobException;
import com.jwtly10.aicontentgenerator.model.*;
import com.jwtly10.aicontentgenerator.model.Reddit.RedditTitle;
import com.jwtly10.aicontentgenerator.model.ffmpeg.FileMeta;
import com.jwtly10.aicontentgenerator.repository.VideoContentDAO;
import com.jwtly10.aicontentgenerator.service.BackgroundVideoService;
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
import org.springframework.core.io.ClassPathResource;
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

        log.info("Processing {} videos", pendingVideos.size());

        // Update video process to processing
        for (UserVideo userVideo : pendingVideos) {
            videoService.updateVideoProcess(userVideo.getVideoId(), VideoProcessingState.PROCESSING, null);
        }

        pendingVideos.parallelStream().forEach(this::process);
    }

    private void process(UserVideo userVideo) {
        String PROCESSID = null;
        try {
            // Get video content from process
            VideoContent videoContent = videoContentDAO.get(userVideo.getVideoId()).orElseThrow(
                    () -> new JobException("Video Content not found")
            );

            PROCESSID = userVideo.getVideoId();
            final String CONTENT = videoContent.getContent();
            // In frontend the value passed for background video will just be the name of the video - mp4
            // TODO: Impl better bg videos
            //        final String BACKGROUND_VIDEO = videoContent.getBackgroundVideo() + ".mp4";
            final String BACKGROUND_VIDEO = BackgroundVideoService.getBackgroundVideo(videoContent.getBackgroundVideo());

            final String TITLE = videoContent.getTitle();
            final String SUBREDDIT = videoContent.getSubreddit();
            // GENERATE VIDEO

            String backgroundVideoPath = "";
            try {
                backgroundVideoPath = new ClassPathResource("media/" + BACKGROUND_VIDEO).getFile().getAbsolutePath();
            } catch (Exception e) {
                log.error("Failed to get background video for process {}.", PROCESSID, e);
                // TODO: Large media videos need upgraded plan. Will wait till then, and instead have a script that populates the media folder
                videoService.updateVideoProcess(userVideo.getVideoId(), VideoProcessingState.FAILED, e.getMessage());
                return;
            }

            videoService.updateVideoProcess(userVideo.getVideoId(), VideoProcessingState.PROCESSING, null);

            Video videoObj = new Video();

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

            if (mergedAudioLength > 90) {
                throw new JobException("Video length of " + mergedAudioLength + " seconds is too long.");
            }

            // Trim background video to length of audio
            backgroundVideoPath = ffmpegUtil.trimVideoToSize(backgroundVideoPath, mergedAudioLength, PROCESSID);

            // Generate overlay image
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
