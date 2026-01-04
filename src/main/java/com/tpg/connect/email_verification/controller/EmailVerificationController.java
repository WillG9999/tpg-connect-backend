package com.tpg.connect.email_verification.controller;

import com.tpg.connect.email_verification.controller.api.SendVerificationCodeApi;
import com.tpg.connect.email_verification.controller.api.VerifyEmailCodeApi;
import com.tpg.connect.email_verification.exceptions.EmailVerificationException;
import com.tpg.connect.email_verification.model.request.SendVerificationCodeRequest;
import com.tpg.connect.email_verification.model.request.VerifyEmailCodeRequest;
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

    public ResponseEntity<Void> sendVerificationCode(SendVerificationCodeRequest request) {
      log.info("Sending verification code to User :: " + request.email());
      if(sendVerificationService.sendVerificationCode(request.email(),request.userName()))
        return ResponseEntity.ok().build();
      throw new EmailVerificationException("Failed to send verification code");
    }

    public ResponseEntity<Void> verifyEmailCode(VerifyEmailCodeRequest request) {
        log.info("verifying code for User :: " + request.email());
        if(sendVerificationService.verifyCodeIsCorrect(request.email(),request.verificationCode()))
            return ResponseEntity.ok().build();
        throw new EmailVerificationException("Failed to verify code");
    }
}
