//package com.example.marketData.controller;
//
//import com.example.marketData.modal.MyUser;
//import com.example.marketData.modal.VerificationToken;
//import com.example.marketData.repo.UserRepo;
//import com.example.marketData.repo.VerificationTokenRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Date;
//
//@RestController
//@RequestMapping("/verification")
//public class VerificationController {
//
//    @Autowired
//    private VerificationTokenRepository tokenRepository;
//
//    @Autowired
//    private UserRepo userRepository;
//
//    @GetMapping("/verify")
//    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token) {
//        VerificationToken verificationToken = tokenRepository.findByToken(token);
//
//        if (verificationToken == null) {
//            return ResponseEntity.badRequest().body("Invalid token.");
//        }
//
//        MyUser user = verificationToken.getUser();
//        if (verificationToken.getExpiryDate().before(new Date())) {
//            return ResponseEntity.badRequest().body("Token has expired.");
//        }
//
//        user.setEnabled(true);
//        userRepository.save(user);
//        return ResponseEntity.ok("Email verified successfully!");
//    }
//
//
//}
