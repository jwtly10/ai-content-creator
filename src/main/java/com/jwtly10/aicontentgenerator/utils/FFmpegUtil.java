package com.jwtly10.aicontentgenerator.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/** FFmpegUtil */
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
                System.err.println("Error running FFmpeg command");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return -1; // Return -1 in case of an error
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
                            "error", // Suppress verbose output
                            "-show_entries",
                            "format=duration",
                            "-of",
                            "default=noprint_wrappers=1:nokey=1");

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                // Read the output to get the duration
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()));
                String durationString = reader.readLine();
                if (durationString != null) {
                    return (long) Double.parseDouble(durationString);
                } else {
                    System.err.println("Failed to read video duration.");
                }
            } else {
                // Print the error stream
                BufferedReader errorReader =
                        new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    System.err.println("FFprobe Error: " + errorLine);
                }
                System.err.println("Error running FFprobe command. Exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return -1; // Return -1 in case of an error
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
            // Build the FFmpeg command
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

            // Redirect error stream to standard output
            processBuilder.redirectErrorStream(true);

            // Start the FFmpeg process
            Process process = processBuilder.start();

            // Wait for the process to complete
            int exitCode = process.waitFor();

            // Print the FFmpeg command output
            System.out.println("FFmpeg command output:");
            System.out.println(getProcessOutput(process));

            // Check the exit code to determine if the process was successful
            if (exitCode == 0) {
                System.out.println("FFmpeg process completed successfully.");
            } else {
                System.err.println("FFmpeg process failed with exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String getProcessOutput(Process process) throws IOException {
        // Read the output of the process
        java.util.Scanner s = new java.util.Scanner(process.getInputStream()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
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
