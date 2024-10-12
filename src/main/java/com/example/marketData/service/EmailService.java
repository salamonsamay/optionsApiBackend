package com.example.marketData.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    public void sendPaymentConfirmationEmail(String to, Map<String, Object> paymentDetails) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Payment Confirmation");
        message.setText(buildEmailContent(paymentDetails));
        emailSender.send(message);
    }

    private String buildEmailContent(Map<String, Object> paymentDetails) {
        StringBuilder content = new StringBuilder();
        content.append("Thank you for your payment!\n\n");
        content.append("Payment ID: ").append(paymentDetails.get("id")).append("\n");
        content.append("Intent: ").append(paymentDetails.get("intent")).append("\n");
        content.append("State: ").append(paymentDetails.get("state")).append("\n");
        content.append("Created Time: ").append(paymentDetails.get("create_time")).append("\n");
        content.append("Transaction Details:\n");

        List<Map<String, Object>> transactions = (List<Map<String, Object>>) paymentDetails.get("transactions");
        for (Map<String, Object> transaction : transactions) {
            content.append("  Amount: ").append(transaction.get("amount"))
                    .append(" ").append(transaction.get("currency")).append("\n");
            content.append("  Description: ").append(transaction.get("description")).append("\n");
        }

        return content.toString();
    }


}

