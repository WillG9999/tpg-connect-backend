package com.tpg.connect.unit.service;

import com.tpg.connect.user_registration.components.ConnectIdGenerator;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConnectIdGenerationServiceTest {

    @Test
    void defaultConstructor_setsNodeIdToOne() {
        ConnectIdGenerator underTest = new ConnectIdGenerator();

        long id = underTest.generateConnectId();

        long nodeId = (id >> 12) & 0x3FF;
        assertEquals(1L, nodeId);
    }

    @Test
    void constructor_acceptsValidNodeId() {
        assertDoesNotThrow(() -> new ConnectIdGenerator(0));
        assertDoesNotThrow(() -> new ConnectIdGenerator(512));
        assertDoesNotThrow(() -> new ConnectIdGenerator(1023));
    }

    @Test
    void constructor_throwsExceptionForNegativeNodeId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ConnectIdGenerator(-1)
        );

        assertTrue(exception.getMessage().contains("Node ID must be between 0 and"));
    }

    @Test
    void constructor_throwsExceptionForNodeIdExceedingMax() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ConnectIdGenerator(1024)
        );

        assertTrue(exception.getMessage().contains("Node ID must be between 0 and"));
    }

    @Test
    void generateConnectId_returnsPositiveValue() {
        ConnectIdGenerator underTest = new ConnectIdGenerator();

        long id = underTest.generateConnectId();

        assertTrue(id > 0);
    }

    @Test
    void generateConnectId_returnsUniqueIds() {
        ConnectIdGenerator underTest = new ConnectIdGenerator();
        Set<Long> ids = new HashSet<>();

        for (int i = 0; i < 10000; i++) {
            ids.add(underTest.generateConnectId());
        }

        assertEquals(10000, ids.size());
    }

    @Test
    void generateConnectId_returnsIncreasingIds() {
        ConnectIdGenerator underTest = new ConnectIdGenerator();

        long id1 = underTest.generateConnectId();
        long id2 = underTest.generateConnectId();
        long id3 = underTest.generateConnectId();

        assertTrue(id2 > id1);
        assertTrue(id3 > id2);
    }

    @Test
    void generateConnectId_differentNodesProduceDifferentIds() {
        ConnectIdGenerator service1 = new ConnectIdGenerator(1);
        ConnectIdGenerator service2 = new ConnectIdGenerator(2);

        long id1 = service1.generateConnectId();
        long id2 = service2.generateConnectId();

        assertNotEquals(id1, id2);
    }

    @Test
    void generateConnectId_embedsCorrectNodeId() {
        ConnectIdGenerator underTest = new ConnectIdGenerator(500);

        long id = underTest.generateConnectId();

        long extractedNodeId = (id >> 12) & 0x3FF;
        assertEquals(500L, extractedNodeId);
    }
}