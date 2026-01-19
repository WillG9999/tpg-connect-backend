package com.tpg.connect.common.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    public String hashPassword(String base64EncodedPassword) {
        String decodedPassword = decodeBase64Password(base64EncodedPassword);
        return passwordEncoder.encode(decodedPassword);
    }

    public String decodeBase64Password(String base64EncodedPassword) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedPassword);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            log.error("Failed to decode base64 password", e);
            throw new RuntimeException("Invalid password format");
        }
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}

