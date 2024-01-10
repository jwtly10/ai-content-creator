package com.jwtly10.aicontentgenerator.baseTests;

import com.jwtly10.aicontentgenerator.config.GentleAlignerContainer;
import com.jwtly10.aicontentgenerator.service.StorageService;
import com.jwtly10.aicontentgenerator.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.annotation.BeforeTestExecution;
import org.testcontainers.junit.jupiter.Container;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

@SpringBootTest
@Slf4j
@ContextConfiguration(initializers = TestBase.Initializer.class)
public class TestBase {

    @Value("${file.tmp.path}")
    public String ffmpegTmpPath;

    @Value("${file.out.path}")
    public String ffmpegOutPath;

    @Value("${file.download.path}")
    public String downloadPath;

    @Autowired
    private StorageService storageService;

    @Container
    public static final GentleAlignerContainer gentleAlignerContainer =
            new GentleAlignerContainer();

    /**
     * Start the Gentle Aligner container
     */
    @BeforeAll
    public static void init() {
        gentleAlignerContainer.start();
    }

    /**
     * Clean up all test temp files, after all tests have run
     *
     * @throws IOException if issue deleting files
     */

    @AfterAll
    public static void cleanUp() throws IOException {
        org.apache.commons.io.FileUtils.cleanDirectory(new File("test_tmp/"));
        org.apache.commons.io.FileUtils.cleanDirectory(new File("test_download/"));
        org.apache.commons.io.FileUtils.cleanDirectory(new File("test_out/"));
    }

    /**
     * Clean up all test temp files, before each test
     *
     * @throws IOException if issue deleting files
     */
    @BeforeTestExecution
    public void setup() throws IOException {
        cleanUp();
    }


    /**
     * Set the Gentle URL for the test container
     */
    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of("gentle.aligner.url=" + gentleAlignerContainer.getGentleUrl())
                    .applyTo(applicationContext);
        }
    }

    /**
     * Clean up files
     *
     * @param paths Paths to files
     */
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
    public Optional<String> getTestFileLocally(String fileName) {
        return Optional.of(storageService.downloadVideo(fileName, "test-media/"));
    }

    /**
     * Compare two text files
     *
     * @param filePath1 Path to first file
     * @param filePath2 Path to second file
     * @return True if files are equal, false otherwise
     */
    public static boolean compareTextFiles(String filePath1, String filePath2) {
        try (BufferedReader reader1 = new BufferedReader(new FileReader(filePath1));
             BufferedReader reader2 = new BufferedReader(new FileReader(filePath2))) {

            String line1, line2;
            while ((line1 = reader1.readLine()) != null && (line2 = reader2.readLine()) != null) {
                if (!line1.equals(line2)) {
                    return false;
                }
            }

            return reader1.readLine() == null && reader2.readLine() == null;

        } catch (IOException e) {
            log.error("Error comparing text files", e);
        }
        return false;
    }
}
