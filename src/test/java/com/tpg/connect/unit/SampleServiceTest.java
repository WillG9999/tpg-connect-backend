package com.tpg.connect.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SampleServiceTest {

    @Mock
    private Object underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Sample test with assertions")
    void testSampleMethod() {
        String expected = "Hello World";
        String actual = "Hello World";

        assertEquals(expected, actual);
        assertNotNull(actual);
    }

    @Test
    @DisplayName("Sample test with Mockito")
    void testWithMocking() {
        when(underTest.toString()).thenReturn("Mocked Value");
        String result = underTest.toString();

        assertEquals("Mocked Value", result);
    }
}