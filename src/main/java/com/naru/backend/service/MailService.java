package com.naru.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendVerificationEmail(String to, String token) {
        String verificationLink = baseUrl + "/auth/verify?token=" + token + "&email=" + to;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Naru 블로그 회원가입 인증 메일");
        message.setText("인증 링크: " + verificationLink);
        mailSender.send(message);
    }
}