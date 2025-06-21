package com.example.minioservice;

import com.example.minioservice.config.AppConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableConfigurationProperties(AppConfig.class)
@EnableDiscoveryClient
public class MinioServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MinioServiceApplication.class, args);
    }

}
