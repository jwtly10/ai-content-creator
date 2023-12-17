package com.jwtly10.aicontentgenerator.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwtly10.aicontentgenerator.models.GentleResponse;
import com.jwtly10.aicontentgenerator.models.Word;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** GentleAlignerUtil */
public class GentleAlignerUtil {

    private static final Logger logger = LoggerFactory.getLogger(GentleAlignerUtil.class);
    private static final OkHttpClient client = new OkHttpClient();

    /**
     * Align audio and text, and generate SRT file
     *
     * @param audioFilePath Path to audio file
     * @param transcriptFilePath Path to transcript file
     */
    public static void alignAndGenerateSRT(String audioFilePath, String transcriptFilePath) {
        // TODO: Change this, we will be logging to DB at some point, and may need to
        // track multiple requests async
        String outputFilePath = "test_media/output.srt";
        alignTextWithAudio(audioFilePath, transcriptFilePath, outputFilePath);
    }

    /**
     * Align audio and text, and generate SRT file
     *
     * @param audioFilePath Path to audio file
     * @param transcriptFilePath Path to transcript file
     * @param outputFilePath Path to output SRT file
     */
    private static void alignTextWithAudio(
            String audioFilePath, String transcriptFilePath, String outputFilePath) {
        MediaType mediaType = MediaType.parse("audio/wav");

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
            logger.info("Aligning text with audio...");
            Response response = client.newCall(request).execute();
            String jsonResponse = response.body().string();
            System.out.println(jsonResponse);

            // Generate SRT file
            generateSRT(jsonResponse, 13);

        } catch (IOException e) {
            logger.error("Error while aligning text with audio: {}", e.getMessage());
        }
    }

    /**
     * Generate SRT file from Gentle output
     *
     * @param gentleOutput Gentle output
     * @param phraseLength Phrase length
     */
    private static void generateSRT(String gentleOutput, int phraseLength) {
        String srtFilePath = "test_media/output.srt";
        try {
            // Parse Gentle output using Jackson
            ObjectMapper objectMapper = new ObjectMapper();
            GentleResponse gentleResponse =
                    objectMapper.readValue(gentleOutput, GentleResponse.class);

            // Write SRT file with configurable phrase length
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(srtFilePath))) {
                List<Word> words = gentleResponse.getWords();
                int sequenceNumber = 1; // Initialize sequence number
                List<Word> phrase = new ArrayList<>();

                for (int i = 0; i < words.size(); i++) {
                    // Add the word to the current phrase
                    phrase.add(words.get(i));

                    // Check if the phrase length is reached or if it's the last word or other rules
                    if (phrase.size() > phraseLength
                            || i == words.size() - 1
                            || additionalRules(phrase, words, i)) {
                        // Write SRT entry for the phrase
                        writeSRTEntry(writer, phrase, sequenceNumber);

                        // Increment sequence number for the next entry
                        sequenceNumber++;

                        // Clear the phrase for the next one
                        phrase.clear();
                    }
                }

                logger.info("SRT file generated successfully");

            } catch (IOException e) {
                logger.error("Error while writing SRT file: {}", e.getMessage());
            }

        } catch (IOException e) {
            logger.error("Error while parsing Gentle output: {}", e.getMessage());
        }
    }

    /**
     * Write SRT entry
     *
     * @param writer BufferedWriter
     * @param phrase Phrase
     * @param sequenceNumber Sequence number
     */
    private static void writeSRTEntry(BufferedWriter writer, List<Word> phrase, int sequenceNumber)
            throws IOException {
        // SRT format: sequence number, timing, and text
        writer.write(Integer.toString(sequenceNumber)); // Sequence number
        writer.newLine();

        // Determine the timing based on the start and end times of the first and last
        // words in the phrase
        // Here, we assume that the timing of the phrase is based on the start and end
        // times of the first and last words
        String timing =
                formatTime((int) (phrase.get(0).getStart() * 1000))
                        + " --> "
                        + formatTime((int) (phrase.get(phrase.size() - 1).getEnd() * 1000));
        writer.write(timing); // Timing
        writer.newLine();

        // Write the aligned words of the phrase, replacing <unk> with the original text
        for (Word word : phrase) {
            // We can just use the original word, as this will preserve the capitalization.
            String subWord = word.getOriginalWord();
            writer.write(subWord);
            writer.write(" ");
        }
        writer.newLine();
        writer.newLine(); // Blank line between entries
    }

    /**
     * Parse duration string
     *
     * @param durationString Duration string
     * @return Duration in seconds
     */
    private static String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = ((milliseconds / (1000 * 60)) % 60);
        int hours = (milliseconds / (1000 * 60 * 60));

        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, milliseconds % 1000);
    }

    /**
     * Additional rules to check if the phrase is complete
     *
     * @param phrase Phrase
     * @param words List of words
     * @param index Index of the current word
     * @return True if the phrase is complete, false otherwise
     */
    private static boolean additionalRules(List<Word> phrase, List<Word> words, int index) {
        // This should handle the cases of names, we shouldnt end the phrase for these.
        if (Character.isUpperCase(words.get(index).getOriginalWord().charAt(0))) {
            if (words.get(index).getAlignedWord() != "<unk>" && phrase.size() >= 5) {
                logger.info(
                        "Additional rule: Name detected: " + words.get(index).getOriginalWord());
                return true;
            } else {
                return false;
            }
        }

        return false;
    }
}
