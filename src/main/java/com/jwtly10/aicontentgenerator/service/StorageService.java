package com.jwtly10.aicontentgenerator.service;

import com.jwtly10.aicontentgenerator.exceptions.StorageException;

public interface StorageService {
    /**
     * Upload video to storage
     *
     * @param fileUuid UUID of process
     * @param filePath Path to file
     * @throws StorageException If upload fails
     */
    void uploadVideo(String fileUuid, String filePath) throws StorageException;

    /**
     * Upload video to storage with custom path
     *
     * @param fileUuid   UUID of process
     * @param filePath   Path to file
     * @param customPath Custom path to upload to
     * @throws StorageException If upload fails
     */
    void uploadVideo(String fileUuid, String filePath, String customPath) throws StorageException;

    /**
     * Get video URL from storage
     *
     * @param fileUuid UUID of process
     * @return Video storage URL
     */
    String getVideoUrl(String fileUuid);

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
     * Proxy Download video from specific folder, and get bytes
     *
     * @param fileName File name to download
     * @return The file in bytes
     * @throws StorageException If proxy download fails
     */
    byte[] proxyDownload(String fileName) throws StorageException;

    /**
     * Delete video from storage
     *
     * @param fileName File to delete
     * @throws StorageException If delete fails
     */
    void deleteVideo(String fileName) throws StorageException;

    /**
     * Delete download from local storage
     *
     * @param fileName File to delete
     */
    void deleteDownload(String fileName);

}
