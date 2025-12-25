package com.tpg.connect.unit.component;

import com.tpg.connect.email_verification.components.EmailVerificationCodeGenerator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailVerificationCodeGeneratorTest {

    @Test
    void generateCode_shouldReturnSixCharacterAlphanumericCode() {
        EmailVerificationCodeGenerator generator = new EmailVerificationCodeGenerator();
        String code = generator.generateCode();
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("[0-9A-Z]{6}"));
    }
}
