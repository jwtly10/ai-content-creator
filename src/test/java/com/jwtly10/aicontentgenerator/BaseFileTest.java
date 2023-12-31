package com.jwtly10.aicontentgenerator;

import com.jwtly10.aicontentgenerator.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest
public class BaseFileTest {

    @Value("${file.tmp.path}")
    public String ffmpegTmpPath;

    @Value("${file.out.path}")
    public String ffmpegOutPath;

    public void cleanUpFiles(String... paths) {
        for (String path : paths) {
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
        }
    }

    public void cleanUp(String fileUuid) {
        FileUtils.cleanUpTempFiles(fileUuid, ffmpegTmpPath);
    }

    public void assertFileExists(String path) {
        File f = new File(path);
        assert f.exists();
    }
}
