package com.tpg.connect.email_verification.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

import static com.tpg.connect.common.constants.RepositoryNamesConstants.KEY_PREFIX;

@Repository
@RequiredArgsConstructor
@Slf4j
public class VerificationCodeRepository {

    private final RedisTemplate<String, String> redisTemplate;


    public boolean saveCode(String email, String code, long ttlSeconds) {
        try {
            String key = KEY_PREFIX + email;
            redisTemplate.opsForValue().set(
                    key,
                    code,
                    Duration.ofSeconds(ttlSeconds)
            );
            log.info("Verification code saved for email: {}", email);
            return true;
        } catch (Exception e) {
            log.error("Failed to save verification code for email: {}", email, e);
            return false;
        }
    }

    public String retrieveCode(String email) {
        try {
            String key = KEY_PREFIX + email;
            String code = redisTemplate.opsForValue().get(key);
            log.info("Retrieved verification code for email: {}", email);
            return code;
        } catch (Exception e) {
            log.error("Failed to retrieve verification code for email: {}", email, e);
            return null;
        }
    }

    public boolean deleteCode(String email) {
        try {
            String key = KEY_PREFIX + email;
            Boolean deleted = redisTemplate.delete(key);
            log.info("Deleted verification code for email: {}", email);
            return Boolean.TRUE.equals(deleted);
        } catch (Exception e) {
            log.error("Failed to delete verification code for email: {}", email, e);
            return false;
        }
    }
}
