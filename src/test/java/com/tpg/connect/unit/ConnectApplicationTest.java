package com.tpg.connect.unit;

import com.tpg.connect.ConnectApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ConnectApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class ConnectApplicationTest {

    @Test
    void contextLoads() {
    }
}