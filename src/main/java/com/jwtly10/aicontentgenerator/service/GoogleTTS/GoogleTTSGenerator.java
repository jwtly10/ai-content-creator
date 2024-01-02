package com.jwtly10.aicontentgenerator.service.GoogleTTS;

import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import com.jwtly10.aicontentgenerator.exceptions.AudioGenerationException;
import com.jwtly10.aicontentgenerator.model.Gender;
import com.jwtly10.aicontentgenerator.service.VoiceGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Service
@Slf4j
public class GoogleTTSGenerator implements VoiceGenerator {

    @Value("${file.tmp.path}")
    private String tmpPath;

    @Override
    public String generateVoice(String text, Gender gender, String fileId) {
        // Log the number of words in text
        log.info("Generating audio for {} words", text.split("\\s+").length);
        return switch (gender) {
            case MALE -> generateMp3Voice(text, "en-US-Standard-B", fileId);
            case FEMALE -> generateMp3Voice(text, "en-US-Standard-E", fileId);
        };
    }

    private String generateMp3Voice(String text, String voice, String fileId) {
        String outputPath = tmpPath + fileId + "_audio" + ".mp3";

        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {

            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            VoiceSelectionParams gVoice =
                    VoiceSelectionParams.newBuilder()
                            .setLanguageCode("en-US")
                            .setName(voice)
                            .build();

            AudioConfig audioConfig =
                    AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();

            SynthesizeSpeechResponse response =
                    textToSpeechClient.synthesizeSpeech(input, gVoice, audioConfig);

            ByteString audioContents = response.getAudioContent();

            try (OutputStream out = new FileOutputStream(outputPath)) {
                out.write(audioContents.toByteArray());
                log.info("Audio content written to file: {}  ", outputPath);
                return outputPath;
            } catch (IOException e) {
                log.error("Failed to write audio to file: {}", outputPath, e);
                throw new AudioGenerationException("Failed to write audio to file: " + outputPath + ", " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Failed to create text to speech client: {}", e.getMessage());
            throw new AudioGenerationException("Failed to create text to speech client: " + e.getMessage());
        }
    }
}