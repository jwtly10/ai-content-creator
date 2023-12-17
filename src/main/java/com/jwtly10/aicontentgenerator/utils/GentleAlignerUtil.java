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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/** GentleAlignerUtil */
public class GentleAlignerUtil {

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
            Response response = client.newCall(request).execute();
            String jsonResponse = response.body().string();
            // System.out.println(jsonResponse);

            // Generate SRT file
            generateSRT(jsonResponse);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateSRT(String gentleOutput) {
        String srtFilePath = "test_media/output.srt";
        try {
            // Parse Gentle output using Jackson
            ObjectMapper objectMapper = new ObjectMapper();
            GentleResponse gentleResponse =
                    objectMapper.readValue(gentleOutput, GentleResponse.class);

            // Write SRT file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(srtFilePath))) {
                List<Word> words = gentleResponse.getWords();
                for (int i = 0; i < words.size(); i++) {
                    // SRT format: sequence number, timing, and text
                    writer.write(Integer.toString(i + 1)); // Sequence number
                    writer.newLine();

                    String timing =
                            formatTime((int) (words.get(i).getStart() * 1000))
                                    + " --> "
                                    + formatTime((int) (words.get(i).getEnd() * 1000));
                    writer.write(timing); // Timing
                    writer.newLine();

                    writer.write(words.get(i).getAlignedWord()); // Text
                    writer.newLine();
                    writer.newLine(); // Blank line between entries
                }

                System.out.println("SRT file created successfully.");

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
