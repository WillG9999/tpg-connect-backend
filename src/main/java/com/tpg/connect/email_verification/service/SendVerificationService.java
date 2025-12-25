package com.tpg.connect.email_verification.service;

import com.tpg.connect.email_verification.components.EmailVerificationCodeGenerator;
import com.tpg.connect.email_verification.exceptions.EmailVerificationException;
import com.tpg.connect.email_verification.repository.VerificationCodeRepository;
import com.tpg.connect.external.email.client.EmailClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class SendVerificationService {

    private final EmailClient emailClient;
    private final EmailVerificationCodeGenerator emailVerificationCodeGenerator;
    private final VerificationCodeRepository verificationCodeRepository;
    private final long TTS = 1800 ;

    public boolean sendVerificationCode(String email, String userName){
        String generatedVerificationCode = emailVerificationCodeGenerator.generateCode();
        if(emailClient.sendVerificationEmail(email,userName,generatedVerificationCode))
            return verificationCodeRepository.saveCode(email,generatedVerificationCode,TTS);
        throw new EmailVerificationException("Unable to send/save verification code for user:: " + email);
    }

    public boolean verifyCodeIsCorrect(String email, String verificationCode){
        if(Objects.equals(verificationCodeRepository.retrieveCode(email), verificationCode))
            return verificationCodeRepository.deleteCode(email);
        throw new EmailVerificationException("Unable to complete verification for user:: " + email);
    }
}
