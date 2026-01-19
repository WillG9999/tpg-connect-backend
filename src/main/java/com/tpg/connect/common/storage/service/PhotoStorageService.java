package com.tpg.connect.common.storage.service;

import com.google.cloud.storage.Blob;
import com.google.firebase.cloud.StorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@Profile("prod")
public class PhotoStorageService implements PhotoStorageServiceApi {

    @Value("${firebase.project.id}")
    private String projectId;

    @Value("${firebase.emulator.storage.host:#{null}}")
    private String emulatorHost;

    @Value("${firebase.emulator.storage.port:#{null}}")
    private String emulatorPort;

    private String getBucketName() {
        return projectId + ".appspot.com";
    }

    private boolean isEmulatorMode() {
        return emulatorHost != null && emulatorPort != null;
    }

    @Override
    public String uploadPhoto(String userId, MultipartFile file) {
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            String photoPath = "users/" + userId + "/photos/" + fileName;
            String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";

            log.info("Uploading photo to path: {}", photoPath);

            StorageClient.getInstance()
                    .bucket(getBucketName())
                    .create(photoPath, file.getInputStream(), contentType);

            log.info("Photo uploaded successfully: {}", photoPath);
            return getPhotoUrl(photoPath);
        } catch (IOException e) {
            log.error("Failed to upload photo for user: {}", userId, e);
            throw new RuntimeException("Failed to upload photo", e);
        }
    }

    @Override
    public String uploadPhoto(String userId, byte[] photoData, String fileName, String contentType) {
        String photoPath = "users/" + userId + "/photos/" + fileName;

        log.info("Uploading photo bytes to path: {}", photoPath);

        StorageClient.getInstance()
                .bucket(getBucketName())
                .create(photoPath, photoData, contentType != null ? contentType : "image/jpeg");

        log.info("Photo uploaded successfully: {}", photoPath);
        return getPhotoUrl(photoPath);
    }

    @Override
    public void deletePhoto(String photoUrl) {
        String photoPath = extractPathFromUrl(photoUrl);
        if (photoPath == null) {
            log.warn("Could not extract path from URL: {}", photoUrl);
            return;
        }

        log.info("Deleting photo: {}", photoPath);

        Blob blob = StorageClient.getInstance()
                .bucket(getBucketName())
                .get(photoPath);

        if (blob != null && blob.delete()) {
            log.info("Deleted photo: {}", photoPath);
        } else {
            log.warn("Photo not found for deletion: {}", photoPath);
        }
    }

    @Override
    public String getPhotoUrl(String photoPath) {
        if (isEmulatorMode()) {
            return String.format("http://%s:%s/v0/b/%s/o/%s?alt=media",
                    emulatorHost, emulatorPort, getBucketName(), photoPath.replace("/", "%2F"));
        }
        return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                getBucketName(), photoPath.replace("/", "%2F"));
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

