package com.jwtly10.aicontentgenerator.service.ElevenLabs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwtly10.aicontentgenerator.exceptions.AudioGenerationException;
import com.jwtly10.aicontentgenerator.model.ElevenLabs.ElevenLabsBody;
import com.jwtly10.aicontentgenerator.model.ElevenLabs.ElevenLabsVoice;
import com.jwtly10.aicontentgenerator.model.Gender;
import com.jwtly10.aicontentgenerator.model.ffmpeg.BufferPos;
import com.jwtly10.aicontentgenerator.service.VoiceGenerator;
import com.jwtly10.aicontentgenerator.utils.FFmpegUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;

@Service
@Slf4j
public class ElevenLabsVoiceGenerator implements VoiceGenerator {

    @Value("${elevenlabs.api.url}")
    private String elevenLabsApiUrl;

    @Value("${elevenlabs.api.key}")
    private String elevenLabsApiKey;

    @Value("${file.tmp.path}")
    private String tmpPath;

    private final FFmpegUtil ffmpegUtil;

    private final RestTemplate restTemplate;

    public ElevenLabsVoiceGenerator(FFmpegUtil ffmpegUtil, RestTemplate restTemplate) {
        this.ffmpegUtil = ffmpegUtil;
        this.restTemplate = restTemplate;
    }

    /**
     * Generate voice from text
     *
     * @param text       Text to generate voice from
     * @param gender     Voice to use
     * @param fileId Path to output file
     * @return Optional path to generated voice, empty if failed
     */
    @Override
    public String generateVoice(String text, Gender gender, String fileId) {
        return switch (gender) {
            case MALE -> generateMp3Voice(createBody(text), "ErXwobaYiN019PkySvjV", fileId);
            case FEMALE -> generateMp3Voice(createBody(text), "21m00Tcm4TlvDq8ikWAM", fileId);
        };
    }

    /**
     * Generate mp3 voice from text
     *
     * @param body       Body of request
     * @param voiceId    Voice id to use
     * @param fileId fileId of the current process
     */
    public String generateMp3Voice(ElevenLabsBody body, String voiceId, String fileId) {

        String outputPath = tmpPath + fileId + "_audio" + ".mp3";

        try {
            String apiUrl = UriComponentsBuilder.fromUriString(elevenLabsApiUrl)
                    .path("/text-to-speech/" + voiceId)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("xi-api-key", elevenLabsApiKey);

            HttpEntity<ElevenLabsBody> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, requestEntity, byte[].class);

            try (OutputStream out = new FileOutputStream(outputPath)) {
                out.write(Objects.requireNonNull(responseEntity.getBody()));
            }

            log.info("Generated voice: {}", outputPath);

            // We buffer the audio here because the audio generated by ElevenLabs is too short
            return ffmpegUtil.bufferAudio(outputPath, BufferPos.END, 1, fileId);

        } catch (Exception e) {
            log.error("Error generating voice", e);
            throw new AudioGenerationException("Error generating voice: " + e.getMessage());
        }
    }

    /**
     * Get list of voices
     *
     * @return List of voices
     */
    public List<ElevenLabsVoice> getVoices() {
        List<ElevenLabsVoice> voices = new ArrayList<>();

        try {
            String apiUrl = UriComponentsBuilder.fromUriString(elevenLabsApiUrl)
                    .path("/voices")
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("xi-api-key", elevenLabsApiKey);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    apiUrl, HttpMethod.GET, requestEntity, String.class);


            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseEntity.getBody());

            JsonNode voicesNode = jsonNode.get("voices");


            if (voicesNode.isArray()) {
                for (JsonNode voiceNode : voicesNode) {
                    ElevenLabsVoice voice = new ElevenLabsVoice();
                    voice.setVoice_id(voiceNode.get("voice_id") != null ? voiceNode.get("voice_id").asText() : "");
                    voice.setName(voiceNode.get("name") != null ? voiceNode.get("name").asText() : "");
                    voice.setCategory(voiceNode.get("category") != null ? voiceNode.get("category").asText() : "");

                    JsonNode labelsNode = voiceNode.get("labels");
                    voice.setAccent(labelsNode.get("accent") != null ? labelsNode.get("accent").asText() : "");
                    voice.setDescription(labelsNode.get("description") != null ? labelsNode.get("description").asText() : "");
                    voice.setAge(labelsNode.get("age") != null ? labelsNode.get("age").asText() : "");
                    voice.setGender(labelsNode.get("gender") != null ? labelsNode.get("gender").asText() : "");
                    voice.setUse_case(labelsNode.get("use case") != null ? labelsNode.get("use case").asText() : "");
                    voices.add(voice);
                }
            }

            log.info("Found {} voices", voices.size());
        } catch (Exception e) {
            log.error("Error getting voice list", e);
        }

        return voices;
    }


    /**
     * Pick random voice from list of voices
     *
     * @param voices List of voices
     * @param gender Gender of voice
     * @return Optional voice id
     */
    private Optional<String> pickVoice(List<ElevenLabsVoice> voices, String gender) {
        // TODO: Revise these rules
        List<ElevenLabsVoice> filteredVoices = voices.stream()
                .filter(voice -> gender.equalsIgnoreCase(voice.getGender())
                        && "american".equalsIgnoreCase(voice.getAccent())
                        && "narration".equalsIgnoreCase(voice.getUse_case()))
                .toList();

        if (filteredVoices.isEmpty()) {
            log.error("No voice found for gender: " + gender);
            return Optional.empty();
        }

        Random random = new Random();
        ElevenLabsVoice selectedVoice = filteredVoices.get(random.nextInt(filteredVoices.size()));

        return Optional.of(selectedVoice.getVoice_id());
    }

    /**
     * Create body for request
     *
     * @param text Text to generate voice from
     * @return Body of request
     */
    private ElevenLabsBody createBody(String text) {
        ElevenLabsBody body = new ElevenLabsBody();
        body.setText(text);
        return body;
    }
}
