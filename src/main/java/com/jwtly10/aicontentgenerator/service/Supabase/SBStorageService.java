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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

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
    private String localDirectory;

    private final String storageUrlSuffix = "/storage/v1/object/";
    private final String storageFolder = "generated-videos/";

    String apiUrl = supabaseUrl + storageUrlSuffix + bucketName + "/" + storageFolder;

    public SBStorageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void uploadVideo(String fileUuid, String filePath) {
        FileMeta fileMeta = FileUtils.create(filePath);
        String mimeType = getMimeType(filePath);
        String apiUrl = supabaseUrl + storageUrlSuffix + bucketName + "/" + storageFolder;

        log.info("Saving file for process {} to S3", fileUuid);
        String url =
                apiUrl
                + fileMeta.getFileName() + "." + fileMeta.getExtension();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("Content-Type", mimeType);

        HttpEntity<byte[]> req = null;
        try {
            req = new HttpEntity<>(getBinaryData(filePath), headers);
        } catch (IOException e) {
            throw new StorageException(e.getMessage());
        }

        try {
            ResponseEntity<String> res = restTemplate.postForEntity(url, req, String.class);
            if (res.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully saved file for process {} to S3", fileUuid);
            } else {
                log.error("Error saving file" + res.getStatusCode() + res.getBody());
                throw new StorageException("Error saving file");
            }
        } catch (Exception e) {
            log.error("Failed to save file: {}", e.getMessage());
            throw new StorageException("Failed to save file");
        }

        FileUtils.cleanUpFile(filePath);
    }

    @Override
    public Optional<String> downloadVideo(String fileName) {
        String outputPath = localDirectory + "/" + fileName;
        String apiUrl = supabaseUrl + storageUrlSuffix + bucketName + "/" + storageFolder;
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
                return Optional.of(outputPath);
            } else {
                log.error("Failed to download file: {}", responseEntity.getStatusCode() + " "  + Arrays.toString(responseEntity.getBody()));
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Failed making request to supabase: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void deleteVideo(String fileName) {
        //TODO: Implement
    }

    private byte[] getBinaryData(String fileLocation) throws IOException {

        Path path = Paths.get(fileLocation);
        return Files.readAllBytes(path);
    }

    private String getMimeType(String filePath) {
        String mimeType = null;
        try {
            mimeType = Files.probeContentType(Path.of(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (mimeType == null) {
            mimeType = "text/plain";
        }

        return mimeType;
    }
}
