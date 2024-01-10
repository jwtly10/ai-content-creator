package com.jwtly10.aicontentgenerator.utils;

import com.jwtly10.aicontentgenerator.exceptions.FFmpegException;
import com.jwtly10.aicontentgenerator.model.ffmpeg.BufferPos;
import com.jwtly10.aicontentgenerator.model.ffmpeg.FileMeta;
import com.jwtly10.aicontentgenerator.model.ffmpeg.VideoDimensions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** FFmpegUtil */
@Slf4j
@Service
public class FFmpegUtil {

    @Value("${file.tmp.path}")
    private String ffmpegTmpPath;

    @Value("${file.out.path}")
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
     * @param fileId fileId of process
     * @return Path to generated video file
     * @throws FFmpegException if command fails
     */
    public String generateVideo(String videoPath, String audioPath, long titleDuration, String subtitlePath, String fileId) throws FFmpegException {
        try {
            FileMeta videoFileMeta = FileUtils.create(videoPath);
            String outputPath =
                    ffmpegOutPath + fileId + "_final" + "." + videoFileMeta.getExtension();

            String bufferedSRTPath = delaySRT(subtitlePath, titleDuration, fileId);

            log.info("Generating video...");
            List<String> commands = List.of(
                    "ffmpeg",
                    "-i", videoPath,
                    "-i", audioPath,
                    "-vf", "subtitles=" + bufferedSRTPath + ":force_style='FontName=Londrina Solid,FontSize=15,PrimaryColour=&H00ffffff,OutlineColour=&H00000000,BackColour=&H80000000,Bold=1,Italic=0,Alignment=10\"'",
                    "-c:v", "libx264",
                    "-c:a", "libmp3lame",
                    outputPath);

            ProcessBuilder processBuilder = setupProcessBuilder(commands);
            int exitCode = executeProcess(processBuilder);

            if (exitCode == 0) {
                log.info("FFmpeg video gen process completed successfully." + " Output path: " + outputPath);
                return outputPath;
            } else {
                log.error("FFmpeg video gen process failed with exit code: " + exitCode);
                throw new FFmpegException("FFmpeg video gen process failed with exit code: " + exitCode);
            }

        } catch (Exception e) {
            log.error("Error running ffmpeg process", e);
            throw new FFmpegException("Error running FFmpeg command: " + e.getMessage());
        }
    }

    /**
     * Merge two audio files
     *
     * @param audioPath1 Path to audio file 1
     * @param audioPath2 Path to audio file 2
     * @param fileId     fileId of process
     * @return Path to merged audio file
     * @throws FFmpegException if command fails
     */
    public String mergeAudio(String audioPath1, String audioPath2, String fileId) throws FFmpegException {
        String outputPath =
                ffmpegTmpPath + fileId + "_merged" + "_audio" + "." + "mp3";

        try {
            log.info("Merging audio...");
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
                return outputPath;
            } else {
                log.error("FFmpeg merge process failed with exit code: " + exitCode);
                throw new FFmpegException("FFmpeg merge process failed with exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error running ffmpeg process", e);
            throw new FFmpegException("Error running FFmpeg command: " + e.getMessage());
        }
    }

    /**
     * Overlay image on video
     *
     * @param imagePath Path to image file
     * @param videoPath Path to video file
     * @param duration  Duration in seconds
     * @return Path to overlayed video file
     * @throws FFmpegException if command fails
     */
    public String overlayImage(String imagePath, String videoPath, long duration, String fileId) throws FFmpegException {
        log.info("Overlaying image on video...");
        FileMeta videoFileMeta = FileUtils.create(videoPath);
        String outputPath =
                ffmpegTmpPath
                        + fileId
                        + "_overlayed"
                        + "." + videoFileMeta.getExtension();

        try {
            VideoDimensions dims = getVideoDimensions(videoPath);
            imagePath = resizeImage(imagePath, dims.getWidth());

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
                return outputPath;
            } else {
                log.error("FFmpeg overlay process failed with exit code: " + exitCode);
                throw new FFmpegException("FFmpeg overlay process failed with exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error running ffmpeg process", e);
            throw new FFmpegException("Error running FFmpeg command: " + e.getMessage());
        }
    }

    /**
     * Resize image to target size
     *
     * @param imagePath   Path to image file
     * @param targetWidth Target width
     * @return Path to resized image file
     * @throws FFmpegException if command fails
     */
    public String resizeImage(String imagePath, long targetWidth) throws FFmpegException {
        log.info("Resizing image...");
        FileMeta imageFileMeta = FileUtils.create(imagePath);
        String outputPath =
                ffmpegTmpPath + FileUtils.generateUUID() + "." + imageFileMeta.getExtension();

        // Our template image is quite large, so this just ensures its zoomed in enough
        targetWidth += 200;

        try {
            List<String> commands = List.of(
                    "ffmpeg",
                    "-i", imagePath,
                    "-vf", "scale=" + targetWidth + ":-1",
                    outputPath
            );

            ProcessBuilder processBuilder = setupProcessBuilder(commands);
            int exitCode = executeProcess(processBuilder);

            if (exitCode == 0) {
                log.info("FFmpeg image resize process completed successfully. Output path: " + outputPath);
                return outputPath;
            } else {
                log.error("FFmpeg image resize process failed with exit code: " + exitCode);
                throw new FFmpegException("FFmpeg image resize process failed with exit code: " + exitCode);
            }

        } catch (Exception e) {
            log.error("Error running ffmpeg process", e);
            throw new FFmpegException("Error running FFmpeg command: " + e.getMessage());
        }
    }

    /**
     * Resize video to 9:16 aspect ratio
     *
     * @param videoPath Path to video file
     * @return Path to resized video file
     * @throws FFmpegException if command fails
     */
    public String resizeVideo(String videoPath) {
        try {
            log.info("Resizing video...");
            // TODO: Accept multiple video formats
            FileMeta videoFileMeta = FileUtils.create(videoPath);
            String outputPath =
                    ffmpegTmpPath
                            + "resized_"
                            + videoFileMeta.getFileName()
                            + "."
                            + videoFileMeta.getExtension();

            VideoDimensions inputDimensions = getVideoDimensions(videoPath);
            int targetWidth = Math.min(inputDimensions.getWidth(), inputDimensions.getHeight() * 9 / 16);
            int targetHeight = inputDimensions.getHeight();

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
                return outputPath;
            } else {
                log.error("FFmpeg resize process failed with exit code: " + exitCode);
                throw new FFmpegException("FFmpeg resize process failed with exit code: " + exitCode);
            }

        } catch (Exception e) {
            log.error("Error running ffmpeg process", e);
            throw new FFmpegException("Error running FFmpeg command: " + e.getMessage());
        }
    }

    /**
     * Buffer audio at start or end
     *
     * @param audioPath Path to audio file
     * @param pos       Buffer position
     * @param duration  Duration in seconds
     * @param fileId    fileId of current process
     * @return Path to buffered audio file
     * @throws FFmpegException if command fails
     */
    public String bufferAudio(String audioPath, BufferPos pos, long duration, String fileId) throws FFmpegException {
        String outputPath =
                ffmpegTmpPath
                        + fileId
                        + "_buffered"
                        + ".mp3";

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
                return outputPath;
            } else {
                log.error("FFmpeg buffer process failed with exit code: " + exitCode);
                throw new FFmpegException("FFmpeg buffer process failed with exit code: " + exitCode);
            }
        } catch (Exception e) {
            log.error("Error running ffmpeg process", e);
            throw new FFmpegException("Error running FFmpegprocess: " + e.getMessage());
        }
    }

    /**
     * Delay SRT file
     *
     * @param srtPath Path to SRT file
     * @param delay   Delay in seconds
     * @param fileId  fileId of current process
     * @return Path to delayed SRT file
     * @throws FFmpegException if command fails
     */
    public String delaySRT(String srtPath, long delay, String fileId) throws FFmpegException {
        String outputPath =
                ffmpegTmpPath
                        + fileId
                        + "_delayed"
                        + ".srt";

        double latency = 0.1;
        delay = (long) (delay + latency);
        log.info("Delaying by " + delay + " seconds.");

        try {
            List<String> commands = List.of(
                    "ffmpeg",
                    "-itsoffset",
                    String.valueOf(delay),
                    "-i",
                    srtPath,
                    "-c",
                    "copy",
                    outputPath);

            ProcessBuilder processBuilder = setupProcessBuilder(commands);
            int exitCode = executeProcess(processBuilder);

            if (exitCode == 0) {
                log.info("FFmpeg SRT delay process completed successfully.");
                return outputPath;
            } else {
                log.error("FFmpeg SRT delay process failed with exit code: " + exitCode);
                throw new FFmpegException("FFmpeg SRT delay process failed with exit code: " + exitCode);
            }
        } catch (Exception e) {
            log.error("Error running ffmpeg process", e);
            throw new FFmpegException("Error running FFmpeg command: " + e.getMessage());
        }
    }

    /**
     * Loop video
     *
     * @param audioDuration Duration of audio in seconds
     * @param videoPath     Path to video file
     * @param fileId        fileId of current process
     * @return Path to looped video file
     * @throws FFmpegException if command fails
     */
    public String loopVideo(long audioDuration, String videoPath, String fileId) throws FFmpegException {
        try {
            Long videoDuration = getVideoDuration(videoPath);

            String outputPath =
                    ffmpegTmpPath
                            + fileId
                            + "_looped"
                            + ".mp4";

            int numberOfRepeats = (int) Math.ceil((double) audioDuration / videoDuration);

            log.info("Looping video " + numberOfRepeats + " times.");

            List<String> commands = List.of("ffmpeg",
                    "-stream_loop", String.valueOf(numberOfRepeats),
                    "-i", videoPath,
                    "-vf", "trim=duration=" + audioDuration,
                    "-c:a", "copy",
                    outputPath);

            ProcessBuilder processBuilder = setupProcessBuilder(commands);
            int exitCode = executeProcess(processBuilder);

            if (exitCode == 0) {
                log.info("FFmpeg loop process completed successfully.");
                return outputPath;
            } else {
                log.error("FFmpeg loop process failed with exit code: " + exitCode);
                throw new FFmpegException("FFmpeg loop process failed with exit code: " + exitCode);
            }
        } catch (Exception e) {
            log.error("Error running ffmpeg process", e);
            throw new FFmpegException("Error running FFmpeg command: " + e.getMessage());
        }
    }

    /**
     * Get audio duration in seconds
     *
     * @param filePath Path to audio file
     * @return Duration in seconds
     * @throws FFmpegException if command fails
     */
    public Long getAudioDuration(String filePath) throws FFmpegException {
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
                log.error("Failed to read audio duration.");
                throw new FFmpegException("Failed to read audio duration.");
            } else {
                log.error("Error running FFmpeg command");
                throw new FFmpegException("Error running FFmpeg command");
            }

        } catch (IOException | InterruptedException e) {
            log.error("Error running ffmpeg process", e);
            throw new FFmpegException("Error running FFmpeg command: " + e.getMessage());
        }
    }

    /**
     * Get video duration in seconds
     *
     * @param filePath Path to video file
     * @return Duration in seconds
     * @throws FFmpegException if command fails
     */
    public Long getVideoDuration(String filePath) throws FFmpegException {
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
                    throw new FFmpegException("Failed to read video duration.");
                }
            } else {
                BufferedReader errorReader =
                        new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    log.error("FFprobe Error: " + errorLine);
                }
                log.error("Error running FFprobe command. Exit code: " + exitCode);
                throw new FFmpegException("Error running FFprobe command. Exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            log.error("Error running ffmpeg process", e);
            throw new FFmpegException("Error running FFprobe command: " + e.getMessage());
        }
    }

    /**
     * Get video dimensions
     *
     * @param videoPath Path to video file
     * @return Video dimensions
     * @throws FFmpegException if command fails
     */
    public VideoDimensions getVideoDimensions(String videoPath) throws FFmpegException {
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
                            return extractDimensions(part);
                        }
                    }
                }
            }
            // If we get here, we didn't find the dimensions
            log.error("Failed to get video dimensions.");
            throw new FFmpegException("Failed to read video dimensions.");
        } catch (IOException | InterruptedException e) {
            log.error("Error running ffmpeg process", e);
            throw new FFmpegException("Error running FFmpeg command: " + e.getMessage());
        }
    }

    /**
     * Change audio tempo
     *
     * @param audioPath  Path to audio file
     * @param multiplier Tempo multiplier
     * @param fileId     fileId of current process
     * @return Path to sped up audio file
     * @throws FFmpegException if command fails
     */
    public String changeAudioTempo(String audioPath, double multiplier, String fileId) throws FFmpegException {
        String outputPath =
                ffmpegTmpPath + fileId + "_sped_up" + "." + "mp3";

        try {
            log.info("Speeding up audio...");
            List<String> command = List.of(
                    "ffmpeg",
                    "-i", audioPath,
                    "-filter:a", "atempo=" + multiplier,
                    outputPath
            );

            ProcessBuilder processBuilder = setupProcessBuilder(command);
            int exitCode = executeProcess(processBuilder);

            if (exitCode == 0) {
                log.info("FFmpeg speed up process completed successfully. Output path: " + outputPath);
                return outputPath;
            } else {
                log.error("FFmpeg speed up process failed with exit code: " + exitCode);
                throw new FFmpegException("FFmpeg merge process failed with exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error running FFmpeg command", e);
            throw new FFmpegException("Error running FFmpeg command: " + e.getMessage());
        }
    }

    /**
     * Extract dimensions from string
     *
     * @param dims Dimensions string
     * @return Video dimensions
     * @throws IllegalArgumentException if string is not in the format "WxH"
     */
    private VideoDimensions extractDimensions(String dims) throws IllegalArgumentException {
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
    private String getProcessOutput(Process process) {
        try (java.util.Scanner s =
                     new java.util.Scanner(process.getInputStream()).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }

}