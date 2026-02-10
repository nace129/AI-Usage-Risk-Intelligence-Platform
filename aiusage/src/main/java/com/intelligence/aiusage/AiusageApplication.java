package com.intelligence.aiusage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication(scanBasePackages = "com.intelligence")
@EnableJpaRepositories(basePackages = "com.intelligence.capture.repo")
@EntityScan(basePackages = "com.intelligence.capture.model")
public class AiusageApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiusageApplication.class, args);
	}

}
