package com.tpg.connect.unit.component;

import com.tpg.connect.application.components.AppIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AppIdGeneratorTest {

    private AppIdGenerator appIdGenerator;

    @BeforeEach
    void setUp() {
        appIdGenerator = new AppIdGenerator();
    }

    @Test
    void generateAppId_returnsIdWithAppPrefix() {
        String appId = appIdGenerator.generateAppId();

        assertNotNull(appId);
        assertTrue(appId.startsWith("APP-"));
    }

    @Test
    void generateAppId_returnsUniqueIds() {
        Set<String> generatedIds = new HashSet<>();

        for (int i = 0; i < 1000; i++) {
            String appId = appIdGenerator.generateAppId();
            assertFalse(generatedIds.contains(appId), "Duplicate ID generated: " + appId);
            generatedIds.add(appId);
        }

        assertEquals(1000, generatedIds.size());
    }

    @Test
    void generateAppId_containsNumericSuffix() {
        String appId = appIdGenerator.generateAppId();

        String suffix = appId.substring(4);
        assertDoesNotThrow(() -> Long.parseLong(suffix), "Suffix should be numeric");
    }

    @Test
    void generateAppId_returnsPositiveSuffix() {
        String appId = appIdGenerator.generateAppId();

        String suffix = appId.substring(4);
        long numericValue = Long.parseLong(suffix);
        assertTrue(numericValue > 0, "ID suffix should be positive");
    }

    @Test
    void generateAppId_idsAreIncreasing() {
        String firstId = appIdGenerator.generateAppId();
        String secondId = appIdGenerator.generateAppId();

        long firstNumeric = Long.parseLong(firstId.substring(4));
        long secondNumeric = Long.parseLong(secondId.substring(4));

        assertTrue(secondNumeric > firstNumeric, "IDs should be monotonically increasing");
    }
}

