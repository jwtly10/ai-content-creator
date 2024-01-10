package com.jwtly10.aicontentgenerator.config;

import org.testcontainers.containers.GenericContainer;

public class GentleAlignerContainer extends GenericContainer<GentleAlignerContainer> {
    public static final int GENTLE_PORT = 8765;

    public GentleAlignerContainer() {
        super("lowerquality/gentle:latest");
        withExposedPorts(GENTLE_PORT);
    }

    public String getGentleUrl() {
        return String.format("http://%s:%s", getHost(), getMappedPort(GENTLE_PORT));
    }
}
