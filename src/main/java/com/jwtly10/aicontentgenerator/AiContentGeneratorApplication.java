package com.jwtly10.aicontentgenerator;

import com.jwtly10.aicontentgenerator.config.AsyncSchedulingConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(AsyncSchedulingConfig.class)
public class AiContentGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiContentGeneratorApplication.class, args);
	}

}
