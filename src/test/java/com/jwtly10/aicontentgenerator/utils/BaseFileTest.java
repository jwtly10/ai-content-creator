package com.jwtly10.aicontentgenerator.utils;

import java.io.File;

public class BaseFileTest {
    public void cleanUpFiles(String... paths) {
        for (String path : paths) {
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
        }
    }
}
