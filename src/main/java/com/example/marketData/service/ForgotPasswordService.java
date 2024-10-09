package com.example.marketData.service;

import com.example.marketData.modal.MyUser;
import com.example.marketData.modal.PasswordResetToken;
import com.example.marketData.repo.ResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ForgotPasswordService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MyUserDetailsService userService; // Assuming you have a service to interact with the user

    @Autowired
    private ResetTokenRepository resetTokenRepository;

    public void sendResetPasswordEmail(String email) {
        // Validate if the email exists
        MyUser user = userService.getUser(email);
        if (user == null) {
            throw new IllegalArgumentException("No user found with this email.");
        }

        // Generate a reset token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        resetTokenRepository.save(resetToken);

        // Send the reset email
        String resetUrl = "http://localhost:3000/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Password Reset Request");
        message.setText("Click the following link to reset your password: " + resetUrl);

        mailSender.send(message);
    }

    public boolean validateResetToken(String token) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(token);
        if (resetToken == null || resetToken.isExpired()) {
            return false;
        }
        return true;
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(token);
        if (resetToken == null || resetToken.isExpired()) {
            throw new IllegalArgumentException("Invalid or expired token.");
        }

        MyUser user = resetToken.getUser();

        userService.updateUserPassword(user, newPassword);

        // Optionally, delete the token after successful reset
        resetTokenRepository.delete(resetToken);
    }
}

