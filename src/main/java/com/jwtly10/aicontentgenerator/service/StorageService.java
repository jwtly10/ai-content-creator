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
     * Get video from storage
     *
     * @param fileUuid UUID of file/process
     * @return Optional of video, empty if not found
     */
    Optional<String> downloadVideo(String fileUuid);

    /**
     * Delete video from storage
     *
     * @param fileName File to delete
     */
    void deleteVideo(String fileName);
}
