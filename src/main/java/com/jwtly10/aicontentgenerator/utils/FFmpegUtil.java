package com.jwtly10.aicontentgenerator.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/** FFmpegUtil */
@Slf4j
public class FFmpegUtil {
    /**
     * Get audio duration in seconds
     *
     * @param filePath Path to audio file
     * @return Duration in seconds
     */
    public static long getAudioDuration(String filePath) {
        try {
            ProcessBuilder processBuilder =
                    new ProcessBuilder("ffmpeg", "-i", filePath, "-f", "null", "-");

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("Duration:")) {
                        String durationString = line.split("Duration: ")[1].split(",")[0].trim();
                        return parseDurationString(durationString);
                    }
                }
            } else {
                log.error("Error running FFmpeg command");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Get video duration in seconds
     *
     * @param filePath Path to video file
     * @return Duration in seconds
     */
    public static long getVideoDuration(String filePath) {
        try {
            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            "ffprobe",
                            "-i",
                            filePath,
                            "-v",
                            "error",
                            "-show_entries",
                            "format=duration",
                            "-of",
                            "default=noprint_wrappers=1:nokey=1");

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()));
                String durationString = reader.readLine();
                if (durationString != null) {
                    return (long) Double.parseDouble(durationString);
                } else {
                    log.error("Failed to read video duration.");
                }
            } else {
                BufferedReader errorReader =
                        new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    log.error("FFprobe Error: " + errorLine);
                }
                log.error("Error running FFprobe command. Exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Generate video with audio and subtitle
     *
     * @param videoPath Path to video file
     * @param audioPath Path to audio file
     * @param subtitlePath Path to subtitle file
     */
    public static void generateVideo(String videoPath, String audioPath, String subtitlePath) {
        String outputPath = "test_media/output.mp4";
        try {
            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            "ffmpeg",
                            "-i",
                            videoPath,
                            "-i",
                            audioPath,
                            "-vf",
                            "subtitles=" + subtitlePath,
                            "-c:v",
                            "libx264",
                            "-c:a",
                            "libmp3lame",
                            // "-strict",
                            // "experimental",
                            // "-b:a",
                            // "libmp3lame",
                            outputPath);

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            int exitCode = process.waitFor();

            log.info("FFmpeg command output:");
            log.info(getProcessOutput(process));

            if (exitCode == 0) {
                log.info("FFmpeg process completed successfully.");
            } else {
                log.error("FFmpeg process failed with exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String getProcessOutput(Process process) throws IOException {
        try (java.util.Scanner s =
                new java.util.Scanner(process.getInputStream()).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }

    /**
     * Parse duration string from FFmpeg output
     *
     * @param durationString Duration string from FFmpeg output
     * @return Duration in seconds
     */
    private static long parseDurationString(String durationString) {
        String[] parts = durationString.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        float seconds = Float.parseFloat(parts[2]);
        return (hours * 3600) + (minutes * 60) + (long) seconds;
    }
}
