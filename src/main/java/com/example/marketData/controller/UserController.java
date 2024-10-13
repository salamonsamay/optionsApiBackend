package com.example.marketData.controller;


import com.example.marketData.modal.MyUser;
import com.example.marketData.modal.VerificationToken;
import com.example.marketData.repo.UserRepo;
import com.example.marketData.service.JwtService;
import com.example.marketData.service.MyUserDetailsService;
import com.example.marketData.service.VerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
@RequestMapping("/user")
public class UserController {


    @Autowired
    private VerificationTokenService verificationTokenService;

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private UserRepo userRepository;
    @Autowired
    private MyUserDetailsService myUserDetailsService;
    @Autowired
    private JwtService jwtService;

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token){
        VerificationToken verificationToken = verificationTokenService.findByToken(token);

        if (verificationToken == null) {
            return ResponseEntity.badRequest().body("Invalid token.");
        }

        MyUser user = verificationToken.getUser();
        if (verificationToken.getExpiryDate().before(new Date())) {
            return ResponseEntity.badRequest().body("Token has expired.");
        }

        user.setEnabled(true);
        userRepository.save(user);
        return ResponseEntity.ok("Email verified successfully!");
    }


    @GetMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestParam("email") String email) {
        MyUser user = userRepository.findById(email).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found.");
        }
        VerificationToken verificationToken=verificationTokenService.generateToken( email);

        // Send email with reset link
        String resetUrl = "http://optionsapi.dev:8080/user/resetPasswordForm?token=" + verificationToken.getToken();
        sendResetEmail(user.getEmail(), resetUrl);

        return ResponseEntity.ok("Password reset email sent.");
    }

    private void sendResetEmail(String email, String resetUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Reset Password");
        message.setText("Click the following link to reset your password: " + resetUrl);
        mailSender.send(message);
    }

    @GetMapping("/resetPasswordForm")
    public ResponseEntity<?> resetPasswordForm(@RequestParam("token") String token, @RequestParam("newPassword") String newPassword) {
        VerificationToken verificationToken = verificationTokenService.findByToken(token);
        if (verificationToken == null) {
            return ResponseEntity.badRequest().body("Invalid token.");
        }

        MyUser user = verificationToken.getUser();
        if (verificationToken.getExpiryDate().before(new Date())) {
            return ResponseEntity.badRequest().body("Token has expired.");
        }

        // Reset the password and save the user
        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));

        userRepository.save(user);

        return ResponseEntity.ok("Password reset successfully.");
    }


    @PutMapping("/changePassword")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword){

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            String email=jwtService.extractUsername(token);
            myUserDetailsService.changePassword(email,oldPassword,newPassword);
            return ResponseEntity.ok("Password changed successfully.");
        }
        return  ResponseEntity.status(401).body("Unauthorized.");
    }



}
