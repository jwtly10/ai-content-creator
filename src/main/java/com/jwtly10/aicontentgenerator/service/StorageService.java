package com.jwtly10.aicontentgenerator.service;

import com.jwtly10.aicontentgenerator.exceptions.StorageException;

public interface StorageService {
    /**
     * Upload video to storage
     *
     * @param fileUuid UUID of process
     * @param filePath Path to file
     */
    void uploadVideo(String fileUuid, String filePath) throws StorageException;

    /**
     * Download video from specific folder
     * Used for test cases, where we want to download from a specific test folder
     *
     * @param fileName File name to download
     * @param folder Folder to download from
     * @return Video local path
     * @throws StorageException If download fails
     */
    String downloadVideo(String fileName, String folder) throws StorageException;

    /**
     * Default storage mechanic for application
     *
     * @param fileName File name to download
     * @return video local path
     * @throws StorageException If download fails
     */
    String downloadVideo(String fileName) throws StorageException;

    /**
     * Delete video from storage
     *
     * @param fileName File to delete
     * @throws StorageException If delete fails
     */
    void deleteVideo(String fileName) throws StorageException;
}
