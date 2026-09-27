// main purpose of this class is: 
// Static resource handler for the upload folder, so clients can hit /uploads/2025/01/uuid.jpg directly.
package com.fae.adminportal.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final FileStorageConfig fileStorageConfig;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String root = Paths.get(fileStorageConfig.getUploadDir())
                .toAbsolutePath().normalize().toString();
        registry.addResourceHandler(fileStorageConfig.getPublicBaseUrl() + "/**")
                .addResourceLocations("file:" + root + "/");
    }
}