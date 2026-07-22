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
            throw new RuntimeException("package com.property_management.property_prep.service;\n" +
                    "\n" +
                    "import lombok.RequiredArgsConstructor;\n" +
                    "import lombok.extern.slf4j.Slf4j;\n" +
                    "import org.springframework.beans.factory.annotation.Value;\n" +
                    "import org.springframework.mail.SimpleMailMessage;\n" +
                    "import org.springframework.mail.javamail.JavaMailSender;\n" +
                    "import org.springframework.stereotype.Service;\n" +
                    "\n" +
                    "@Service\n" +
                    "@RequiredArgsConstructor\n" +
                    "@Slf4j\n" +
                    "public class EmailService {\n" +
                    "    private final JavaMailSender mailSender;\n" +
                    "\n" +
                    "    @Value(\"${landlord.email}\")\n" +
                    "    private String landlordEmail;\n" +
                    "\n" +
                    "    // Existing method for payment updates\n" +
                    "    public void sendPaymentUpdateToLandlord(String houseNumber, String tenantName, String paymentStatus) {\n" +
                    "        try {\n" +
                    "            SimpleMailMessage message = new SimpleMailMessage();\n" +
                    "            message.setTo(landlordEmail);\n" +
                    "            message.setSubject(\"\uD83D\uDD14 New Payment Update - \" + houseNumber);\n" +
                    "            message.setText(String.format(\n" +
                    "                    \"Hello Landlord,\\n\\nA new payment status update has been received.\\n\\n\" +\n" +
                    "                            \"\uD83C\uDFE0 House Number: %s\\n\" +\n" +
                    "                            \"\uD83D\uDC64 Tenant: %s\\n\" +\n" +
                    "                            \"\uD83D\uDCB5 Payment Status: %s\\n\\n\" +\n" +
                    "                            \"Please check your dashboard for more details.\",\n" +
                    "                    houseNumber, tenantName, paymentStatus\n" +
                    "            ));\n" +
                    "            mailSender.send(message);\n" +
                    "            log.info(\"Email sent to landlord for house: {}\", houseNumber);\n" +
                    "        } catch (Exception e) {\n" +
                    "            log.error(\"Failed to send email: {}\", e.getMessage());\n" +
                    "            throw new RuntimeException(\"Could not send email notification. Please try again later.\");\n" +
                    "        }\n" +
                    "    }\n" +
                    "\n" +
                    "    // ✅ NEW: Generic method to send simple emails\n" +
                    "    public void sendSimpleEmail(String to, String subject, String body) {\n" +
                    "        try {\n" +
                    "            SimpleMailMessage message = new SimpleMailMessage();\n" +
                    "            message.setTo(to);\n" +
                    "            message.setSubject(subject);\n" +
                    "            message.setText(body);\n" +
                    "            mailSender.send(message);\n" +
                    "            log.info(\"Email sent to {}\", to);\n" +
                    "        } catch (Exception e) {\n" +
                    "            log.error(\"Failed to send email to {}: {}\", to, e.getMessage());\n" +
                    "        }\n" +
                    "    }\n" +
                    "}Could not send email notification. Please try again later.");
        }
    }
}