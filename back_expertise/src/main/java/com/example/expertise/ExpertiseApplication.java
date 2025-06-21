package com.example.expertise;

import com.example.expertise.config.AppConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableConfigurationProperties(AppConfig.class)
public class ExpertiseApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExpertiseApplication.class, args);
	}

}
