package com.jwtly10.aicontentgenerator.service;

import java.util.Optional;

public interface StorageService {
    /**
     * Upload video to storage
     *
     * @param fileUuid UUID of process
     * @param filePath Path to file
     */
    void uploadVideo(String fileUuid, String filePath);

    /**
     * Download video from specific folder
     * Used for test cases, where we want to download from a specific test folder
     *
     * @param fileName File name to download
     * @param folder Folder to download from
     * @return Optional of video local path, empty if not found
     */
    Optional<String> downloadVideo(String fileName, String folder);

    /**
     * Default storage mechanic for application
     *
     * @param fileName File name to download
     * @return Optional of video local path, empty if not found
     */
    Optional<String> downloadVideo(String fileName);

    /**
     * Delete video from storage
     *
     * @param fileName File to delete
     */
    void deleteVideo(String fileName);
}
