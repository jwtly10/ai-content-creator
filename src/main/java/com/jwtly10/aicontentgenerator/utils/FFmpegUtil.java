package com.jwtly10.aicontentgenerator.utils;

import com.jwtly10.aicontentgenerator.model.BufferPos;
import com.jwtly10.aicontentgenerator.model.FileMeta;
import com.jwtly10.aicontentgenerator.model.VideoDimensions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** FFmpegUtil */
@Slf4j
@Service
public class FFmpegUtil {

    @Value("${ffmpeg.tmp.path}")
    private String ffmpegTmpPath;

    @Value("${ffmpeg.out.path}")
    private String ffmpegOutPath;

    /**
     * Setup ProcessBuilder
     *
     * @param command Command to run
     * @return ProcessBuilder
     */
    private ProcessBuilder setupProcessBuilder(List<String> command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        return processBuilder;
    }

    /**
     * Execute FFmpeg process
     *
     * @param processBuilder ProcessBuilder
     * @return Exit code
     */
    private int executeProcess(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        log.debug("FFmpeg command output:");
        log.debug(getProcessOutput(process));
        return exitCode;
    }

    /**
     * Generate video with audio and subtitles
     *
     * @param videoPath Path to video file
     * @param audioPath Path to audio file
     * @param subtitlePath Path to subtitle file
     * @return Optional Path to generated video file, empty if failed
     */
    public Optional<String> generateVideo(String videoPath, String audioPath, String subtitlePath) {
        FileMeta videoFileMeta = FileUtils.create(videoPath);
        // TODO: Output path via env var
        String outputPath =
                ffmpegOutPath + "out_" + videoFileMeta.getFileName() + "." + videoFileMeta.getExtension();

        try {
            log.info("Generating video...");
            List<String> commands = List.of(
                            "ffmpeg",
                            "-i",
                            videoPath,
                            "-i",
                            audioPath,
                            "-vf",
                            "subtitles="
                                    + subtitlePath
                                    + ":force_style='\"FontName=Londrina"
                                    + " Solid,FontSize=17,PrimaryColour=&H00ffffff,OutlineColour=&H00000000,"
                                    + "BackColour=&H80000000,Bold=1,Italic=0,Alignment=10\"",
                            "-c:v",
                            "libx264",
                            "-c:a",
                            "libmp3lame",
                            outputPath);

            ProcessBuilder processBuilder = setupProcessBuilder(commands);
            int exitCode = executeProcess(processBuilder);

            if (exitCode == 0) {
                log.info("FFmpeg video gen process completed successfully." + " Output path: " + outputPath);
                return Optional.of(outputPath);
            } else {
                log.error("FFmpeg video gen process failed with exit code: " + exitCode);
                return Optional.empty();
            }

        } catch (IOException | InterruptedException e) {
            log.error("Error: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Merge two audio files
     *
     * @param audioPath1 Path to audio file 1
     * @param audioPath2 Path to audio file 2
     * @return Optional Path to merged audio file, empty if failed
     */
    public Optional<String> mergeAudio(String audioPath1, String audioPath2) {
        FileMeta audioFileMeta1 = FileUtils.create(audioPath1);
        FileMeta audioFileMeta2 = FileUtils.create(audioPath2);

        String outputPath =
                ffmpegTmpPath
                        + "merged_"
                        + audioFileMeta1.getFileName()
                        + "_"
                        + audioFileMeta2.getFileName()
                        + "."
                        + "mp3";

        try {
            List<String> command = List.of(
                    "ffmpeg",
                    "-i", "concat:" + audioPath1 + "|" + audioPath2,
                    "-c", "copy",
                    outputPath
            );

            ProcessBuilder processBuilder = setupProcessBuilder(command);
            int exitCode = executeProcess(processBuilder);

            if (exitCode == 0) {
                log.info("FFmpeg merge process completed successfully. Output path: " + outputPath);
                return Optional.of(outputPath);
            } else {
                log.error("FFmpeg merge process failed with exit code: " + exitCode);
                return Optional.empty();
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Overlay image on video
     *
     * @param imagePath Path to image file
     * @param videoPath Path to video file
     * @param duration  Duration in seconds
     * @return Optional Path to overlayed video file, empty if failed
     */
    public Optional<String> overlayImage(String imagePath, String videoPath, long duration) {
        FileMeta imageFileMeta = FileUtils.create(imagePath);
        FileMeta videoFileMeta = FileUtils.create(videoPath);
        String outputPath =
                ffmpegTmpPath
                        + "overlayed_"
                        + imageFileMeta.getFileName()
                        + "_"
                        + videoFileMeta.getFileName()
                        + "."
                        + videoFileMeta.getExtension();

        try {
            List<String> commands = List.of(
                    "ffmpeg",
                    "-i", videoPath,
                    "-i", imagePath,
                    "-filter_complex", "[0:v][1:v] overlay=(W-w)/2:(H-h)/2:enable='between(t,0," + duration + ")' [out]",
                    "-map", "[out]",
                    "-c:a", "copy",
                    outputPath
            );

            ProcessBuilder processBuilder = setupProcessBuilder(commands);
            int exitCode = executeProcess(processBuilder);

            if (exitCode == 0) {
                log.info("FFmpeg overlay process completed successfully. Output path: " + outputPath);
                return Optional.of(outputPath);
            } else {
                log.error("FFmpeg overlay process failed with exit code: " + exitCode);
                return Optional.empty();
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Resize video to 9:16 aspect ratio
     *
     * @param videoPath Path to video file
     * @return Optional Path to resized video file, empty if failed
     */
    public Optional<String> resizeVideo(String videoPath) {
        // TODO: Accept multiple video formats
        FileMeta videoFileMeta = FileUtils.create(videoPath);
        // TODO: Configurable output path
        String outputPath =
                ffmpegTmpPath
                        + "resized_"
                        + videoFileMeta.getFileName()
                        + "."
                        + videoFileMeta.getExtension();

        VideoDimensions inputDimensions = getVideoDimensions(videoPath).orElseThrow();
        int targetWidth = Math.min(inputDimensions.getWidth(), inputDimensions.getHeight() * 9 / 16);
        int targetHeight = inputDimensions.getHeight();

        try {
            log.info("Resizing video... ");
            List<String> commands = List.of(
                            "ffmpeg",
                            "-i", videoPath,
                            "-vf",
                            "crop=" + targetWidth + ":" + targetHeight,
                            "-c:a",
                            "copy",
                            outputPath);

            ProcessBuilder processBuilder = setupProcessBuilder(commands);
            int exitCode = executeProcess(processBuilder);

            if (exitCode == 0) {
                log.info("FFmpeg resize process completed successfully. Output path: " + outputPath);
                return Optional.of(outputPath);
            } else {
                log.error("FFmpeg resize process failed with exit code: " + exitCode);
                return Optional.empty();
            }

        } catch (IOException | InterruptedException e) {
            log.error("Error: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Buffer audio file
     *
     * @param audioPath Path to audio file
     * @param pos       Buffer position
     * @param duration  Duration in seconds
     * @return Optional Path to buffered audio file, empty if failed
     */
    public Optional<String> bufferAudio(String audioPath, BufferPos pos, long duration) {
        FileMeta audioFileMeta = FileUtils.create(audioPath);
        String outputPath =
                ffmpegTmpPath
                        + "buffered_"
                        + audioFileMeta.getFileName()
                        + "_"
                        + pos.toString()
                        + "."
                        + audioFileMeta.getExtension();

        try {
            List<String> commands = switch (pos) {
                case START -> {
                    log.info("Buffering audio at start...");
                    yield List.of(
                            "ffmpeg",
                            "-i",
                            audioPath,
                            "-af",
                            "adelay=" + duration + "s:all=true",
                            outputPath);
                }
                case END -> {
                    log.info("Buffering audio at end...");
                    yield List.of(
                            "ffmpeg",
                            "-i",
                            audioPath,
                            "-af",
                            "apad=pad_dur=" + duration,
                            outputPath);
                }
            };

            ProcessBuilder processBuilder = setupProcessBuilder(commands);
            int exitCode = executeProcess(processBuilder);

            if (exitCode == 0) {
                log.info("FFmpeg buffer process completed successfully.");
                return Optional.of(outputPath);
            } else {
                log.error("FFmpeg buffer process failed with exit code: " + exitCode);
                return Optional.empty();
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Get audio duration in seconds
     *
     * @param filePath Path to audio file
     * @return Optional Duration in seconds, empty if failed
     */
    public Optional<Long> getAudioDuration(String filePath) {
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
    public Optional<Long> getVideoDuration(String filePath) {
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
     * Get video dimensions given path
     *
     * @param videoPath Path to video file
     * @return Optional Video dimensions, empty if failed
     */
    public Optional<VideoDimensions> getVideoDimensions(String videoPath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-i", videoPath);

            Process process = processBuilder.start();

            // FFmpeg gives us the information we need in the stderr stream
            // So we need to handle this separately
            int exitCode = process.waitFor();
            log.info("FFmpeg getVideoDimensions process completed with exit code: " + exitCode);

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
    private VideoDimensions extractDimensions(String dims) {
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

    /**
     * Parse duration string from FFmpeg output
     *
     * @param durationString Duration string from FFmpeg output
     * @return Duration in seconds
     */
    private long parseDurationString(String durationString) {
        String[] parts = durationString.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        float seconds = Float.parseFloat(parts[2]);
        return (hours * 3600L) + (minutes * 60L) + (long) seconds;
    }

    /**
     * Get process output
     *
     * @param process Process
     * @return Process output
     */
    private String getProcessOutput(Process process) throws IOException {
        try (java.util.Scanner s =
                     new java.util.Scanner(process.getInputStream()).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }

}