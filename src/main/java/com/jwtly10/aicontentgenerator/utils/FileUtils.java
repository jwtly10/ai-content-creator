package com.jwtly10.aicontentgenerator.utils;

import com.jwtly10.aicontentgenerator.model.FileMeta;

/** FileUtil */
public class FileUtils {
    /**
     * Create FileMeta object from file path
     *
     * @param filePath Path to file
     * @return FileMeta object
     */
    public static FileMeta create(String filePath) {
        String[] split = filePath.substring(filePath.lastIndexOf("/") + 1).split("\\.");
        return new FileMeta(
                split[0],
                split[1]);
    }
}
