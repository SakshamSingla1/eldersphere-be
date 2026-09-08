package com.eldersphere.services;

public interface EmailService {
    void sendEmail(String to, String subject, String htmlContent);
}
