package com.jwtly10.aicontentgenerator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Profile("!test")
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncSchedulingConfig {
}
