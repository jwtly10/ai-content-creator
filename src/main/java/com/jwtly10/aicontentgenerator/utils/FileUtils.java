package com.jwtly10.aicontentgenerator.utils;

import com.jwtly10.aicontentgenerator.model.ffmpeg.FileMeta;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/** FileUtil */
@Slf4j
public class FileUtils {
    /**
     * Create FileMeta object from file path
     *
     * @param filePath Path to file
     * @return FileMeta object
     */
    public static FileMeta create(String filePath) {
        String[] split = filePath.substring(filePath.lastIndexOf("/") + 1).split("\\.");
        return new FileMeta(split[0], split[1]);
    }

    /**
     * Get UUID for process
     *
     * @return UUID
     */
    public static String getUUID() {
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * Clean up temp files
     *
     * @param uuid    UUID of process
     * @param tmpPath Path to temp files
     */
    public static void cleanUpTempFiles(String uuid, String tmpPath) {
        String[] files = new File(tmpPath).list();
        if (files == null) {
            return;
        }

        for (String file : files) {
            if (file.contains(uuid)) {
                log.info("Deleting file: {}", tmpPath + file);
                new File(tmpPath + file).delete();
            }
        }
    }

    /**
     * Clean up file
     *
     * @param filePath Path to file
     */
    public static void cleanUpFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }
}
