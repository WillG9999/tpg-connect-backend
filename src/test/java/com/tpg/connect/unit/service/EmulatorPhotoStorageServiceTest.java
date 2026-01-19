package com.tpg.connect.unit.service;

import com.tpg.connect.common.storage.service.EmulatorPhotoStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmulatorPhotoStorageServiceTest {

    private EmulatorPhotoStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new EmulatorPhotoStorageService();
        ReflectionTestUtils.setField(storageService, "projectId", "test-project");
        ReflectionTestUtils.setField(storageService, "emulatorHost", "localhost");
        ReflectionTestUtils.setField(storageService, "emulatorPort", "9199");
    }

    @Test
    void getPhotoUrl_returnsCorrectEmulatorUrl() {
        String photoPath = "users/12345/photos/test.jpg";

        String url = storageService.getPhotoUrl(photoPath);

        assertEquals("http://localhost:9199/v0/b/test-project.appspot.com/o/users%2F12345%2Fphotos%2Ftest.jpg?alt=media", url);
    }

    @Test
    void getPhotoUrl_encodesSlashesCorrectly() {
        String photoPath = "users/123/photos/image.png";

        String url = storageService.getPhotoUrl(photoPath);

        assertTrue(url.contains("%2F"));
        assertFalse(url.substring(url.indexOf("/o/") + 3, url.indexOf("?")).contains("/"));
    }

    @Test
    void deletePhoto_handlesNullUrlGracefully() {
        assertDoesNotThrow(() -> storageService.deletePhoto(null));
    }

    @Test
    void deletePhoto_handlesInvalidUrlGracefully() {
        assertDoesNotThrow(() -> storageService.deletePhoto("invalid-url-without-path"));
    }
}

