package com.tpg.connect.email_verification.controller;

import com.tpg.connect.email_verification.controller.api.SendVerificationCodeApi;
import com.tpg.connect.email_verification.controller.api.VerifyEmailCodeApi;
import com.tpg.connect.email_verification.exceptions.EmailVerificationException;
import com.tpg.connect.email_verification.service.SendVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class EmailVerificationController implements SendVerificationCodeApi, VerifyEmailCodeApi {
    private final SendVerificationService sendVerificationService;

    public ResponseEntity<Void> sendVerificationCode(String email, String userName) {
      log.info("Sending verification code to User :: " + email);
      if(sendVerificationService.sendVerificationCode(email,userName))
        return ResponseEntity.ok().body(null);
      throw new EmailVerificationException("Failed to send verification code");
    }

    public ResponseEntity<Void> verifyEmailCode(String email, String verificationCode) {
        log.info("verifying code for User :: " + email);
        if(sendVerificationService.verifyCodeIsCorrect(email,verificationCode))
            return ResponseEntity.ok().body(null);
        throw new EmailVerificationException("Failed to verify code");
    }
}
