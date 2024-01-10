package com.jwtly10.aicontentgenerator.service.Supabase;

import com.jwtly10.aicontentgenerator.exceptions.StorageException;
import com.jwtly10.aicontentgenerator.model.ffmpeg.FileMeta;
import com.jwtly10.aicontentgenerator.service.StorageService;
import com.jwtly10.aicontentgenerator.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@Service
@Slf4j
public class SBStorageService implements StorageService {

    private final RestTemplate restTemplate;

    @Value("${supabase.bucketname}")
    private String bucketName;

    @Value("${supabase.apiKey}")
    private String supabaseKey;

    @Value("${supabase.storage.url}")
    private String supabaseUrl;

    @Value("${file.download.path}")
    private String downloadDirectory;

    @Value("${s3.output.folder}")
    private String storageFolder;

    private final String storageUrlSuffix = "/storage/v1/object/";

    public SBStorageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void uploadVideo(String fileUuid, String filePath) throws StorageException {
        log.info("Saving file to {} for process {} to S3", storageFolder, fileUuid);
        try {
            uploadVideoInternal(fileUuid, filePath, storageFolder);
        } catch (Exception e) {
            log.error("Failed to upload video: {}", e.getMessage());
            throw new StorageException("Failed to upload video: " + e.getMessage());
        }
    }

    @Override
    public void uploadVideo(String fileUuid, String filePath, String customFolder) throws StorageException {
        log.info("Saving file to custom loc {} for process {} to S3", customFolder, fileUuid);
        try {
            uploadVideoInternal(fileUuid, filePath, customFolder);
        } catch (Exception e) {
            log.error("Failed to upload video: {}", e.getMessage());
            throw new StorageException("Failed to upload video: " + e.getMessage());
        }
    }


    private void uploadVideoInternal(String fileUuid, String filePath, String folder) throws StorageException {
        FileMeta fileMeta = FileUtils.create(filePath);
        String apiUrl = supabaseUrl + storageUrlSuffix + bucketName + "/" + folder;

        String url =
                apiUrl
                        + fileMeta.getFileName() + "." + fileMeta.getExtension();
        try {
            String mimeType = getMimeType(filePath);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("Content-Type", mimeType);

            HttpEntity<byte[]> req = new HttpEntity<>(getBinaryData(filePath), headers);

            ResponseEntity<String> res = restTemplate.postForEntity(url, req, String.class);
            if (res.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully saved file for process {} to S3", fileUuid);
                // DELETE OUTPUT FILE ONCE SUCCESSFULLY SAVED
                FileUtils.cleanUpFile(filePath);
            } else {
                log.error("Error saving file" + res.getStatusCode() + res.getBody());
                throw new StorageException("Supabase Responded with error: " + res.getStatusCode() + " " + res.getBody());
            }
        } catch (Exception e) {
            log.error("Failed to save file: {}", e.getMessage());
            e.printStackTrace();
            throw new StorageException("Failed to save file: " + e.getMessage());
        }

    }

    @Override
    public String getVideoUrl(String fileUuid) {
        return supabaseUrl + storageUrlSuffix + "public/" + bucketName + "/" + storageFolder + fileUuid + "_final.mp4";
    }

    @Override
    public String downloadVideo(String fileName, String customFolder) throws StorageException {
        return download(fileName, customFolder);
    }

    @Override
    public String downloadVideo(String fileName) throws StorageException {
        return download(fileName, storageFolder);
    }

    private String download(String fileName, String customFolder) throws StorageException {
        String outputPath = downloadDirectory + fileName;
        String apiUrl = supabaseUrl + storageUrlSuffix + bucketName + "/" + customFolder;
        String url = apiUrl + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                byte[] fileContent = responseEntity.getBody();
                if (fileContent != null) {
                    Files.write(Paths.get(outputPath), fileContent);
                } else {
                    log.error("Failed to download file: {}", responseEntity.getStatusCode());
                    throw new StorageException("Failed to download file, content is empty");
                }

                log.info("Successfully downloaded file to {}", outputPath);
                return outputPath;
            } else {
                log.error("Failed to download file: {}", responseEntity.getStatusCode() + " " + Arrays.toString(responseEntity.getBody()));
                throw new StorageException("Failed to download file: " + responseEntity.getStatusCode() + " " + Arrays.toString(responseEntity.getBody()));
            }
        } catch (Exception e) {
            log.error("Failed making request to supabase: {}", e.getMessage());
            throw new StorageException("Failed making request to supabase: " + e.getMessage());
        }
    }

    @Override
    public byte[] proxyDownload(String fileName) {
        try {
            String mediaLocalUrl = downloadVideo(fileName);
            byte[] media = getBinaryData(mediaLocalUrl);
            deleteDownload(fileName);
            log.info("Successfully proxied download");
            return media;
        } catch (Exception e) {
            log.error("Failed to proxy download: {}", e.getMessage());
            throw new StorageException("Failed to proxy download: " + e.getMessage());
        }
    }

    @Override
    public void deleteVideo(String fileName) {
        log.info("Deleting file from S3");
        String apiUrl = supabaseUrl + storageUrlSuffix + bucketName + "/" + storageFolder;
        String url = apiUrl + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully deleted file from S3");
            } else {
                log.error("Failed to delete file: {}", responseEntity.getStatusCode() + " " + Arrays.toString(new String[]{responseEntity.getBody()}));
                throw new StorageException("Failed to delete file: " + responseEntity.getStatusCode() + " " + Arrays.toString(new String[]{responseEntity.getBody()}));
            }
        } catch (Exception e) {
            log.error("Failed making request to supabase: {}", e.getMessage());
            throw new StorageException("Failed making request to supabase: " + e.getMessage());
        }
    }


    @Override
    public void deleteDownload(String fileName) {
        log.info("Deleting local download file");
        File file = new File(downloadDirectory + fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    private byte[] getBinaryData(String fileLocation) throws IOException {
        Path path = Paths.get(fileLocation);
        return Files.readAllBytes(path);
    }

    private String getMimeType(String filePath) throws IOException {
        String mimeType;
            mimeType = Files.probeContentType(Path.of(filePath));
        if (mimeType == null) {
            mimeType = "text/plain";
        }

        return mimeType;
    }
}
