package com.tpg.connect.common.storage.service;

import org.springframework.web.multipart.MultipartFile;

public interface PhotoStorageServiceApi {
    String uploadPhoto(String userId, MultipartFile file);
    String uploadPhoto(String userId, byte[] photoData, String fileName, String contentType);
    void deletePhoto(String photoUrl);
    String getPhotoUrl(String photoPath);
}

