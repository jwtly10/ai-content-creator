package com.jwtly10.aicontentgenerator;

import com.jwtly10.aicontentgenerator.service.StorageService;
import com.jwtly10.aicontentgenerator.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@SpringBootTest
@Slf4j
public class BaseFileTest {

    @Value("${file.tmp.path}")
    public String ffmpegTmpPath;

    @Value("${file.out.path}")
    public String ffmpegOutPath;

    @Value("${file.download.path}")
    public String downloadPath;

    @Autowired
    private StorageService storageService;


    public void cleanUpFiles(String... paths) {
        for (String path : paths) {
            File f = new File(path);
            if (f.exists()) {
                log.info("Deleting file: {}", path);
                f.delete();
            }
        }
    }

    /**
     * Clean up test temp files for a given UUID
     *
     * @param fileUuid UUID of file
     */
    public void cleanTempFiles(String fileUuid) {
        FileUtils.cleanUpTempFiles(fileUuid, ffmpegTmpPath);
    }

    /**
     * Assert file exists
     *
     * @param path Path to file
     */
    public void assertFileExists(String path) {
        File f = new File(path);
        assert f.exists();
    }

    /**
     * Get file locally
     *
     * @param fileName Name of file
     * @return Optional path to file
     */
    public Optional<String> getFileLocally(String fileName) {
        return Optional.of(storageService.downloadVideo(fileName, "test-media/"));
    }

    public void deleteAllTestTempFiles() {
    }

    /**
     * Clean up all test temp files, after all tests have run
     *
     * @throws IOException if issue deleting files
     */
    @AfterAll
    public static void cleanUp() throws IOException {
        org.apache.commons.io.FileUtils.cleanDirectory(new File("test_tmp/"));
    }
}
