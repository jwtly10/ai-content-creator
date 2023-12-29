package com.jwtly10.aicontentgenerator.utils;

import com.jwtly10.aicontentgenerator.model.FileMeta;
import com.jwtly10.aicontentgenerator.model.VideoDimensions;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** FFmpegUtil */
@Slf4j
public class FFmpegUtil {
    /**
     * Get audio duration in seconds
     *
     * @param filePath Path to audio file
     * @return Optional Duration in seconds, empty if failed
     */
    public static Optional<Long> getAudioDuration(String filePath) {
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
                        return Optional.of(parseDurationString(durationString));
                    }
                }

                log.error("Failed to read audio duration.");
            } else {
                log.error("Error running FFmpeg command");
            }
            return Optional.empty();

        } catch (IOException | InterruptedException e) {
            log.error("Error: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Get video duration in seconds
     *
     * @param filePath Path to video file
     * @return Optional Duration in seconds, empty if failed
     */
    public static Optional<Long> getVideoDuration(String filePath) {
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
                    return Optional.of((long) Double.parseDouble(durationString));
                } else {
                    log.error("Failed to read video duration.");
                    return Optional.empty();
                }
            } else {
                BufferedReader errorReader =
                        new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    log.error("FFprobe Error: " + errorLine);
                }
                log.error("Error running FFprobe command. Exit code: " + exitCode);
                return Optional.empty();
            }

        } catch (IOException | InterruptedException e) {
            log.error("Error: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Generate video with audio and subtitles
     *
     * @param videoPath Path to video file
     * @param audioPath Path to audio file
     * @param subtitlePath Path to subtitle file
     * @return Optional Path to generated video file, empty if failed
     */
    public static Optional<String> generateVideo(String videoPath, String audioPath, String subtitlePath) {
        FileMeta videoFileMeta = FileUtils.create(videoPath);
        // TODO: Output path via env var
        String outputPath =
                "test_out/" + "out_" + videoFileMeta.getFileName() + "." + videoFileMeta.getExtension();

        try {
            log.info("Generating video...");
            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            "ffmpeg",
                            "-i",
                            videoPath,
                            "-i",
                            audioPath,
                            "-vf",
                            "subtitles="
                                    + subtitlePath
                                    + ":force_style='\"FontName=Londrina"
                                    + " Solid,FontSize=20,PrimaryColour=&H00ffffff,OutlineColour=&H00000000,"
                                    + "BackColour=&H80000000,Bold=1,Italic=0,Alignment=10\"",
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
            log.debug(getProcessOutput(process));

            if (exitCode == 0) {
                log.info("FFmpeg process completed successfully.");
                return Optional.of(outputPath);
            } else {
                log.error("FFmpeg process failed with exit code: " + exitCode);
                return Optional.empty();
            }

        } catch (IOException | InterruptedException e) {
            log.error("Error: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Resize video to 16:9 aspect ratio
     *
     * @param videoPath Path to video file
     * @return Optional Path to resized video file, empty if failed
     */
    public static Optional<String> resizeVideo(String videoPath) {
        // TODO: Accept multiple video formats
        FileMeta videoFileMeta = FileUtils.create(videoPath);
        // TODO: Configurable output path
        String outputPath =
                "test_out/tmp/"
                        + "resized_"
                        + videoFileMeta.getFileName()
                        + "."
                        + videoFileMeta.getExtension();
        try {
            log.info("Resizing video...");
            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            "ffmpeg",
                            "-i",
                            videoPath,
                            "-vf",
                            "crop=in_w*9/16:in_h, scale=ih*16/9:ih",
                            "-c:a",
                            "copy",
                            outputPath);

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            int exitCode = process.waitFor();

            log.info("FFmpeg command output:");
            log.debug(getProcessOutput(process));

            if (exitCode == 0) {
                log.info("FFmpeg resize process completed successfully.");
                return Optional.of(outputPath);
            } else {
                log.error("FFmpeg process failed with exit code: " + exitCode);
                return Optional.empty();
            }

        } catch (IOException | InterruptedException e) {
            log.error("Error: " + e.getMessage());
            return Optional.empty();
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
        return (hours * 3600L) + (minutes * 60L) + (long) seconds;
    }

    /**
     * Get video dimensions given path
     *
     * @param videoPath Path to video file
     * @return Optional Video dimensions, empty if failed
     */
    public static Optional<VideoDimensions> getVideoDimensions(String videoPath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-i", videoPath);

            Process process = processBuilder.start();

            // FFmpeg gives us the information we need in the stderr stream
            // So we need to handle this separately
            int exitCode = process.waitFor();
            log.info("FFmpeg process completed with exit code: " + exitCode);

            // Print the stdout and stderr
            BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String outputLine;
            while ((outputLine = outputReader.readLine()) != null) {
                // We expect this to be empty
                log.debug("FFmpeg output: " + outputLine);
            }

            List<String> messages = errorReader.lines().toList();

            for (String line : messages) {
                if (line.contains("Stream #0:0")) {
                    String[] parts = line.split(", ");
                    for (String part : parts) {
                        if (part.contains("x") && !part.contains("Video:")) { // Escaping additional video codec parts
                            return Optional.of(extractDimensions(part));
                        }
                    }
                }
            }
            // If we get here, we didn't find the dimensions
            log.error("Failed to get video dimensions.");
        } catch (IOException | InterruptedException e) {
            log.error("Error: " + e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Extract dimensions from string
     *
     * @param dims String containing dimensions
     * @return Video dimensions
     */
    private static VideoDimensions extractDimensions(String dims) {
        // Define a regex pattern for extracting dimensions
        String pattern = "(\\d+)x(\\d+)";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(dims);

        if (matcher.find()) {
            int width = Integer.parseInt(matcher.group(1));
            int height = Integer.parseInt(matcher.group(2));
            return new VideoDimensions(width, height);
        } else {
            throw new IllegalArgumentException("Could not extract dimensions from input: " + dims);
        }
    }
}
