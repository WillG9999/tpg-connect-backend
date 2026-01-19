package com.tpg.connect.common.storage.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@Primary
@Profile({"dev", "test", "local"})
public class EmulatorPhotoStorageService implements PhotoStorageServiceApi {

    @Value("${firebase.project.id}")
    private String projectId;

    @Value("${firebase.emulator.storage.host:localhost}")
    private String emulatorHost;

    @Value("${firebase.emulator.storage.port:9199}")
    private String emulatorPort;

    private final RestTemplate restTemplate = new RestTemplate();

    private String getBucketName() {
        return projectId + ".appspot.com";
    }

    private String getEmulatorBaseUrl() {
        return String.format("http://%s:%s", emulatorHost, emulatorPort);
    }

    @Override
    public String uploadPhoto(String userId, MultipartFile file) {
        try {
            return uploadPhoto(userId, file.getBytes(), generateFileName(file.getOriginalFilename()), file.getContentType());
        } catch (IOException e) {
            log.error("Failed to read file bytes for user: {}", userId, e);
            throw new RuntimeException("Failed to upload photo", e);
        }
    }

    @Override
    public String uploadPhoto(String userId, byte[] photoData, String fileName, String contentType) {
        String photoPath = "users/" + userId + "/photos/" + fileName;
        String encodedPath = photoPath.replace("/", "%2F");

        String uploadUrl = String.format("%s/v0/b/%s/o?name=%s",
                getEmulatorBaseUrl(), getBucketName(), encodedPath);

        log.info("Uploading photo to emulator: {}", uploadUrl);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"));

            HttpEntity<byte[]> request = new HttpEntity<>(photoData, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Photo uploaded successfully to emulator: {}", photoPath);
                return getPhotoUrl(photoPath);
            } else {
                log.error("Failed to upload photo to emulator. Status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to upload photo to emulator");
            }
        } catch (Exception e) {
            log.error("Failed to upload photo to emulator for user: {}", userId, e);
            throw new RuntimeException("Failed to upload photo", e);
        }
    }

    @Override
    public void deletePhoto(String photoUrl) {
        String photoPath = extractPathFromUrl(photoUrl);
        if (photoPath == null) {
            log.warn("Could not extract path from URL: {}", photoUrl);
            return;
        }

        String encodedPath = photoPath.replace("/", "%2F");
        String deleteUrl = String.format("%s/v0/b/%s/o/%s",
                getEmulatorBaseUrl(), getBucketName(), encodedPath);

        log.info("Deleting photo from emulator: {}", deleteUrl);

        try {
            restTemplate.delete(deleteUrl);
            log.info("Deleted photo from emulator: {}", photoPath);
        } catch (Exception e) {
            log.warn("Failed to delete photo from emulator: {}", photoPath, e);
        }
    }

    @Override
    public String getPhotoUrl(String photoPath) {
        String encodedPath = photoPath.replace("/", "%2F");
        return String.format("%s/v0/b/%s/o/%s?alt=media",
                getEmulatorBaseUrl(), getBucketName(), encodedPath);
    }

    private String generateFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        return UUID.randomUUID().toString() + extension;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return ".jpg";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private String extractPathFromUrl(String url) {
        if (url == null) {
            return null;
        }
        int startIndex = url.indexOf("/o/");
        if (startIndex == -1) {
            return null;
        }
        int endIndex = url.indexOf("?");
        String encodedPath = endIndex > startIndex ? url.substring(startIndex + 3, endIndex) : url.substring(startIndex + 3);
        return encodedPath.replace("%2F", "/");
    }
}

