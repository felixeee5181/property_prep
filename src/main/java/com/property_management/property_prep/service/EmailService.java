package com.property_management.property_prep.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${landlord.email}")
    private String landlordEmail;

    public void sendPaymentUpdateToLandlord(String houseNumber, String tenantName, String paymentStatus) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(landlordEmail);
            message.setSubject("🔔 New  Payment Update - " + houseNumber);
            message.setText(String.format(
                    "Hello Landlord,\n\nA new payment status update has been received.\n\n" +
                            "🏠 House Number: %s\n" +
                            "👤 Tenant: %s\n" +
                            "💵 Payment Status: %s\n\n" +
                            "Please check your dashboard for more details.",
                    houseNumber, tenantName, paymentStatus
            ));
            mailSender.send(message);
            log.info("Email sent to landlord for house: {}", houseNumber);
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
            throw new RuntimeException("Could not send email notification. Please try again later.");
        }
    }
}