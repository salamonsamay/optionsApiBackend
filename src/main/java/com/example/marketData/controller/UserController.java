package com.example.marketData.controller;


import com.example.marketData.model.MyUser;
import com.example.marketData.model.VerificationToken;
import com.example.marketData.repo.UserRepo;
import com.example.marketData.service.JwtService;
import com.example.marketData.service.MyUserDetailsService;
import com.example.marketData.service.VerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
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


    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, String> body) {

        System.out.println("invoke changePassword method");

        String oldPassword = body.get("password");
        String newPassword = body.get("newPassword");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Unauthorized.");
        }

        String token = authorizationHeader.substring(7);
        String email;

        try {
            email = jwtService.extractUsername(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid or expired token.");
        }

        if (oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("Password fields cannot be empty.");
        }

        if (!myUserDetailsService.verifyPassword(email, oldPassword)) {
            return ResponseEntity.status(400).body("Old password is incorrect.");
        }

        if (!myUserDetailsService.isValidPassword(newPassword)) {
            return ResponseEntity.badRequest().body("New password does not meet security requirements.");
        }

        if (myUserDetailsService.isSamePassword(email, newPassword)) {
            return ResponseEntity.status(400).body("New password cannot be the same as the old password.");
        }

        myUserDetailsService.changePassword(email, oldPassword, newPassword);
        return ResponseEntity.ok("Password changed successfully.");
    }




    @GetMapping("/data")
    public ResponseEntity<?> getUser(@RequestHeader("Authorization") String authorizationHeader ) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            String email = jwtService.extractUsername(token);

            MyUser user = myUserDetailsService.getUser(email);

            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                // Return 404 if user does not exist
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found");
            }
        }
        // Return 400 Bad Request if the Authorization header is missing or incorrect
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid Authorization header");
    }


}
