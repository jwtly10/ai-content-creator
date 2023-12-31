package com.jwtly10.aicontentgenerator.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwtly10.aicontentgenerator.exceptions.SRTGenerationException;
import com.jwtly10.aicontentgenerator.model.GentleResponse;
import com.jwtly10.aicontentgenerator.model.Word;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** GentleAlignerUtil */
@Slf4j
@Service
public class GentleAlignerUtil {
    @Value("${file.tmp.path}")
    private String tmpPath;

    private final OkHttpClient client = new OkHttpClient();

    /**
     * Align text with audio and generate SRT file
     *
     * @param audioFilePath Path to audio file
     * @param content Content to transcribe
     * @param fileId fileId of process
     * @return Optional Path to generated SRT file, empty if error
     */
    public Optional<String> alignAndGenerateSRT(
            String audioFilePath, String content, String fileId) {
        String outputPath = tmpPath + fileId + ".srt";

        MediaType mediaType = MediaType.parse("audio/wav");

        String transcriptFilePath = generateTextFileFromContent(content, fileId);

        RequestBody requestBody =
                new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(
                                "audio",
                                "audio.wav",
                                RequestBody.create(new java.io.File(audioFilePath), mediaType))
                        .addFormDataPart(
                                "transcript",
                                "transcript.txt",
                                RequestBody.create(
                                        new java.io.File(transcriptFilePath),
                                        MediaType.parse("text/plain")))
                        .build();

        Request request =
                new Request.Builder()
                        .url("http://172.17.0.2:8765/transcriptions?async=false")
                        .method("POST", requestBody)
                        .build();

        try {
            log.info("Aligning text with audio...");

            Response response = client.newCall(request).execute();
            String jsonResponse = response.body().string();

            log.debug("Gentle response: {}", jsonResponse);

            List<String> localWords = new ArrayList<>();
            File file = new File(transcriptFilePath);
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            for (String line : lines) {
                String[] words = line.split(" ");
                for (String word : words) {
                    if (!word.isEmpty()) localWords.add(word);
                }
            }

            return generateSRT(localWords, jsonResponse, outputPath);
        } catch (IOException | SRTGenerationException e) {
            log.error("Error while aligning text with audio: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Generate transcript text file from content
     *
     * @param content Content to transcribe
     * @param fileId  fileId of process
     * @return Path to generated text file
     */
    private String generateTextFileFromContent(String content, String fileId) {
        String outputPath = tmpPath + fileId + ".txt";
        try {
            Path path = Paths.get(outputPath);
            Files.write(path, content.getBytes());

            log.info("Transcript file created successfully at: {}", outputPath);
        } catch (IOException e) {
            log.error("Error while creating text file: {}", e.getMessage());
            throw new RuntimeException("Error while creating text file: " + e.getMessage());
        }

        return outputPath;
    }

    /**
     * Generate SRT file
     *
     * @param inputText List of words
     * @param gentleOutput Gentle response
     * @throws SRTGenerationException If error while generating SRT file
     */
    private Optional<String> generateSRT(
            List<String> inputText, String gentleOutput, String outputPath) throws SRTGenerationException {
        // TODO: Make this configurable
        int phraseLength = 10;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            GentleResponse gentleResponse =
                    objectMapper.readValue(gentleOutput, GentleResponse.class);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
                List<Word> words = gentleResponse.getWords();

                // Better logging here, as will be hard to debug later
                if (words.size() != inputText.size()) {
                    log.error("LOCAL AND GENTLE WORD COUNTS DO NOT MATCH. THIS SHOULD NOT HAPPEN. LOGGING WORDS");
                    log.info("Count Gentle words: " + words.size());
                    log.info("Count input words: " + inputText.size());
                    log.error("Gentle words: " + words);
                    log.error("Local words: " + inputText);
                }

                int sequenceNumber = 1;
                List<Word> phrase = new ArrayList<>();
                Word currentWord;

                for (int i = 0; i < words.size(); i++) {
                    // If word in the input file has punctuation, add it to the word object
                    if (inputText.get(i).contains(words.get(i).getOriginalWord())) {
                        words.get(i).setWordWithPunc(inputText.get(i));
                    }

                    currentWord = words.get(i);
                    phrase.add(currentWord);

                    // Current algorithm to check how to write SRT file.
                    // In order of priority for making 'good' subtitles
                    // TODO: Revise this once we have more data

                    // Condition to check if the phrase is complete now
                    if (i == words.size() - 1) {
                        writeSRTEntry(writer, phrase, sequenceNumber);
                        break;
                    }

                    // If word ends with punctuation, write new line
                    if (i > 0 && StringUtils.endsWithPunctuation(currentWord.getWordWithPunc())) {
                        writeSRTEntry(writer, phrase, sequenceNumber);
                        sequenceNumber++;
                        phrase.clear();
                        continue;
                    }

                    // If the next word is capitalized, write new line
                    if (i > 0 && StringUtils.isCapitalized(words.get(i + 1).getWordWithPunc())) {
                        writeSRTEntry(writer, phrase, sequenceNumber);
                        sequenceNumber++;
                        phrase.clear();
                        continue;
                    }

                    // ELSE, if we hit limit, write new line
                    if (phrase.size() > phraseLength) {
                        writeSRTEntry(writer, phrase, sequenceNumber);
                        sequenceNumber++;
                        phrase.clear();
                    }
                }
                log.info("SRT file generated successfully at {}", outputPath);
                return Optional.of(outputPath);
            } catch (IOException e) {
                throw new SRTGenerationException("Error while writing SRT file: " + e.getMessage());
            }

        } catch (IOException e) {
            throw new SRTGenerationException("Error while parsing Gentle response: " + e.getMessage());
        }
    }


    /**
     * Write SRT entry
     *
     * @param writer BufferedWriter
     * @param phrase Phrase
     * @param sequenceNumber Sequence number
     */
    private void writeSRTEntry(BufferedWriter writer, List<Word> phrase, int sequenceNumber)
            throws IOException {
        writer.write(Integer.toString(sequenceNumber));
        writer.newLine();

        String timing =
                formatTime((int) (phrase.get(0).getStart() * 1000))
                        + " --> "
                        + formatTime((int) (phrase.get(phrase.size() - 1).getEnd() * 1000));
        writer.write(timing);
        writer.newLine();

        for (Word word : phrase) {
            if (word.getWordWithPunc() != null) {
                writer.write(word.getWordWithPunc());
                writer.write(" ");
            } else {
                String subWord = word.getOriginalWord();
                writer.write(subWord);
                writer.write(" ");
            }
        }
        writer.newLine();
        writer.newLine();
    }

    /**
     * Parse duration string
     *
     * @param milliseconds Duration
     * @return Duration in seconds
     */
    private String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = ((milliseconds / (1000 * 60)) % 60);
        int hours = (milliseconds / (1000 * 60 * 60));

        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, milliseconds % 1000);
    }
}
