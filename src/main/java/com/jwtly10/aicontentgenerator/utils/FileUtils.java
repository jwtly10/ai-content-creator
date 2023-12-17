package com.jwtly10.aicontentgenerator.utils;

import com.jwtly10.aicontentgenerator.models.FileMeta;

/** FileUtil */
public class FileUtils {
    /**
     * Get file name from file path, stripping extension
     *
     * @param filePath Path to file
     * @return File name
     */
    public static FileMeta create(String filePath) {
        String[] split = filePath.substring(filePath.lastIndexOf("/") + 1).split("\\.");
        return new FileMeta(
                split[0],
                split[1]);
    }
}
