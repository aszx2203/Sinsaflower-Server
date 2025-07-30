package com.sinsaflower.server.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "file.upload")
@Getter @Setter
public class FileUploadProperties {
    
    private String basePath = "./uploads";
    private List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "gif", "pdf");
    private long maxFileSize = 10485760; // 10MB
} 