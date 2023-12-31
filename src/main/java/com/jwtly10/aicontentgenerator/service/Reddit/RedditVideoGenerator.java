package com.jwtly10.aicontentgenerator.service.Reddit;

import com.jwtly10.aicontentgenerator.model.ElevenLabs.ElevenLabsVoice;
import com.jwtly10.aicontentgenerator.model.Gender;
import com.jwtly10.aicontentgenerator.model.Reddit.RedditTitle;
import com.jwtly10.aicontentgenerator.service.VoiceGenerator;
import com.jwtly10.aicontentgenerator.utils.FFmpegUtil;
import com.jwtly10.aicontentgenerator.utils.FileUtils;
import com.jwtly10.aicontentgenerator.utils.GentleAlignerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class RedditVideoGenerator {

    private VoiceGenerator<ElevenLabsVoice> voiceGenerator;

    private final GentleAlignerUtil gentleAlignerUtil;

    private FFmpegUtil ffmpegUtil;

    public RedditVideoGenerator(VoiceGenerator<ElevenLabsVoice> voiceGenerator, GentleAlignerUtil gentleAlignerUtil, FFmpegUtil ffmpegUtil) {
        this.voiceGenerator = voiceGenerator;
        this.gentleAlignerUtil = gentleAlignerUtil;
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

        // TODO: Use OpenAPI to rewrite the content, and provide gender of speaker
        String newContent = content;
        Gender gender = Gender.FEMALE;

        // Generate voice for title
        Optional<String> titleAudio = voiceGenerator.generateVoice(title.getTitle(), gender, processUUID + "_title");
        if (titleAudio.isEmpty()) {
            log.error("Failed to generate voice for title");
            return Optional.empty();
        }

        // Generate voice for content
        Optional<String> contentAudio = voiceGenerator.generateVoice(newContent, gender, processUUID + "_content");
        if (contentAudio.isEmpty()) {
            log.error("Failed to generate voice for content");
            return Optional.empty();
        }

        // TODO: Generate overlay img
        String overlayImg = "";

        // Generate SRT for voice
        Optional<String> contentSRT = gentleAlignerUtil.alignAndGenerateSRT(contentAudio.get(), newContent, processUUID);
        if (contentSRT.isEmpty()) {
            log.error("Failed to generate SRT for content");
            return Optional.empty();
        }

        // Merge audios
        // For now we ignore the merge as we have more work to do on this
//        Optional<String> mergedAudio = ffmpegUtil.mergeAudio(titleAudio.get(), contentAudio.get(), processUUID);
//        if (mergedAudio.isEmpty()) {
//            log.error("Failed to merge audio");
//            return Optional.empty();
//        }

        // Generate video
        // TODO: Refactor the way we generate the title overlay -
        // Need to delay the SRT generation or come up with another way to do this, based on the lengths of the different audios.
        Optional<String> video = ffmpegUtil.generateVideo(videoPath, contentAudio.get(), contentSRT.get(), processUUID);
        if (video.isEmpty()) {
            log.error("Failed to generate video");
            return Optional.empty();
        }

        return video;

    }
}
