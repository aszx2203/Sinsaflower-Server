package com.sinsaflower.server.global.service;

import com.sinsaflower.server.global.config.FileUploadProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final FileUploadProperties fileUploadProperties;

    /**
     * 파일을 저장하고 저장된 파일 경로를 반환
     */
    public String saveFile(MultipartFile file, String subDirectory) throws IOException {
        // 파일 유효성 검사
        validateFile(file);
        
        // 저장 디렉토리 생성
        Path uploadDir = createUploadDirectory(subDirectory);
        
        // 파일명 생성 (중복 방지)
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);
        String newFileName = generateUniqueFileName(fileExtension);
        
        // 파일 저장
        Path targetPath = uploadDir.resolve(newFileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // 상대 경로 반환
        String relativePath = subDirectory + "/" + newFileName;
        log.info("파일 업로드 완료: {}", relativePath);
        
        return relativePath;
    }

    /**
     * 파일 유효성 검사
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 선택되지 않았습니다.");
        }

        // 파일 크기 검사
        if (file.getSize() > fileUploadProperties.getMaxFileSize()) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다. 최대 " + 
                (fileUploadProperties.getMaxFileSize() / (1024 * 1024)) + "MB까지 허용됩니다.");
        }

        // 파일 확장자 검사
        String originalFileName = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFileName)) {
            throw new IllegalArgumentException("파일명이 유효하지 않습니다.");
        }

        String fileExtension = getFileExtension(originalFileName);
        if (!fileUploadProperties.getAllowedExtensions().contains(fileExtension.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. 허용 형식: " + 
                String.join(", ", fileUploadProperties.getAllowedExtensions()));
        }

        // MIME 타입 검사 (이미지 파일인 경우)
        String contentType = file.getContentType();
        if (contentType != null && !isValidImageMimeType(contentType, fileExtension)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. 이미지 파일만 허용됩니다.");
        }
    }

    /**
     * 이미지 MIME 타입 검증
     */
    private boolean isValidImageMimeType(String contentType, String fileExtension) {
        if (!List.of("jpg", "jpeg", "png", "gif").contains(fileExtension.toLowerCase())) {
            return true;
        }
        
        // 이미지 파일인 경우 MIME 타입 검증
        return contentType.startsWith("image/");
    }

    /**
     * 업로드 디렉토리 생성
     */
    private Path createUploadDirectory(String subDirectory) throws IOException {
        Path uploadDir = Paths.get(fileUploadProperties.getBasePath(), subDirectory);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        return uploadDir;
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }

    /**
     * 고유한 파일명 생성
     */
    private String generateUniqueFileName(String fileExtension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + "." + fileExtension;
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return;
        }
        
        try {
            Path path = Paths.get(fileUploadProperties.getBasePath(), filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("파일 삭제 완료: {}", filePath);
            }
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", filePath, e);
        }
    }
} 