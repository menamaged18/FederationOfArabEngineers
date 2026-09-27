package com.fae.adminportal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.file-storage")
@Getter
@Setter
public class FileStorageConfig {

    private String uploadDir = "uploads";
    private String publicBaseUrl = "/uploads";
    private long maxFileSize = 26_214_400L;
}