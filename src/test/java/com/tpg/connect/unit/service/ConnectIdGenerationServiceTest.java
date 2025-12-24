package com.tpg.connect.unit.service;

import com.tpg.connect.session_authentication.common.services.ConnectIdGenerationService;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConnectIdGenerationServiceTest {

    @Test
    void defaultConstructor_setsNodeIdToOne() {
        ConnectIdGenerationService underTest = new ConnectIdGenerationService();

        long id = underTest.generateConnectId();

        long nodeId = (id >> 12) & 0x3FF;
        assertEquals(1L, nodeId);
    }

    @Test
    void constructor_acceptsValidNodeId() {
        assertDoesNotThrow(() -> new ConnectIdGenerationService(0));
        assertDoesNotThrow(() -> new ConnectIdGenerationService(512));
        assertDoesNotThrow(() -> new ConnectIdGenerationService(1023));
    }

    @Test
    void constructor_throwsExceptionForNegativeNodeId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ConnectIdGenerationService(-1)
        );

        assertTrue(exception.getMessage().contains("Node ID must be between 0 and"));
    }

    @Test
    void constructor_throwsExceptionForNodeIdExceedingMax() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ConnectIdGenerationService(1024)
        );

        assertTrue(exception.getMessage().contains("Node ID must be between 0 and"));
    }

    @Test
    void generateConnectId_returnsPositiveValue() {
        ConnectIdGenerationService underTest = new ConnectIdGenerationService();

        long id = underTest.generateConnectId();

        assertTrue(id > 0);
    }

    @Test
    void generateConnectId_returnsUniqueIds() {
        ConnectIdGenerationService underTest = new ConnectIdGenerationService();
        Set<Long> ids = new HashSet<>();

        for (int i = 0; i < 10000; i++) {
            ids.add(underTest.generateConnectId());
        }

        assertEquals(10000, ids.size());
    }

    @Test
    void generateConnectId_returnsIncreasingIds() {
        ConnectIdGenerationService underTest = new ConnectIdGenerationService();

        long id1 = underTest.generateConnectId();
        long id2 = underTest.generateConnectId();
        long id3 = underTest.generateConnectId();

        assertTrue(id2 > id1);
        assertTrue(id3 > id2);
    }

    @Test
    void generateConnectId_differentNodesProduceDifferentIds() {
        ConnectIdGenerationService service1 = new ConnectIdGenerationService(1);
        ConnectIdGenerationService service2 = new ConnectIdGenerationService(2);

        long id1 = service1.generateConnectId();
        long id2 = service2.generateConnectId();

        assertNotEquals(id1, id2);
    }

    @Test
    void generateConnectId_embedsCorrectNodeId() {
        ConnectIdGenerationService underTest = new ConnectIdGenerationService(500);

        long id = underTest.generateConnectId();

        long extractedNodeId = (id >> 12) & 0x3FF;
        assertEquals(500L, extractedNodeId);
    }
}