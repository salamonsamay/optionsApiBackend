package com.example.marketData.service;

import com.example.marketData.ApiKeyGenerator;
import com.example.marketData.exeptions.IncorrectPasswordException;
import com.example.marketData.modal.MyUser;
import com.example.marketData.modal.VerificationToken;
import com.example.marketData.repo.UserRepo;
import com.example.marketData.repo.VerificationTokenRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private final UserRepo userRepo;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(10);

    private final Set<String> userSubscribeApiKeys = new HashSet<>();


    public MyUserDetailsService(UserRepo userRepo) {
        this.userRepo = userRepo;
        loadUserApiKeys(); // Load API keys on initialization

    }

    private void loadUserApiKeys() {
        List<MyUser> users = userRepo.findAll(); // Assuming you have a method to fetch all users
        userSubscribeApiKeys.clear(); // Clear previous keys
        for (MyUser user : users) {
            if(user.isSubscribe()){
                userSubscribeApiKeys.add(user.getApiKey());
            }
        }
    }

    // Scheduled method to refresh API keys every 24 hours
    @Scheduled(fixedRate = 86400000) // 86400000 ms = 24 hours
    public void refreshUserApiKeys() {
        loadUserApiKeys(); // Reload API keys from the database
        System.out.println("User API keys refreshed.");
    }

    // Method to get all user API keys
    public Set<String> getUserSubscribeApiKeys() {
        return new HashSet<>(userSubscribeApiKeys); // Return a copy to avoid external modification
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("invoke 'loadUserByUsername' in class 'MyUserDetailsService'");
        System.out.println("Loading user by username: " + username);

        Optional<MyUser> user = userRepo.findById(username);
        if (user.isPresent()) {
            System.out.println("User found: " + user.get());
            return user.get();
        }

        System.out.println("User not found for username: " + username);
        return user.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public void registerUser(String email,String password) {
        MyUser user=new MyUser(email,bCryptPasswordEncoder.encode(password));
        userRepo.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(new Date(System.currentTimeMillis() + 86400000)); // 24-hour expiry
        tokenRepository.save(verificationToken);

        sendVerificationEmail(user.getEmail(), token);
    }

    public void sendVerificationEmail(String email, String token) {
        String verificationUrl = "https://optionsapi.dev:8080/user/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Email Verification");
        message.setText("To verify your email, click the link: " + verificationUrl);
        mailSender.send(message);
    }
    public boolean check(String email, String password) {
        System.out.println("invoke 'check' in class 'MyUserDetailsService'");
        System.out.println("Checking credentials for email: " + email);

        Optional<MyUser> optional = this.userRepo.findById(email);

        if (optional.isPresent()) {
            MyUser user = optional.get();
            System.out.println("User found: " + user);

            if (bCryptPasswordEncoder.matches(password, user.getPassword())) {
                if(!user.isEnabled()){
                    return false;
                }
                System.out.println("Password matches for user: " + email);
                return true;
            } else {
                System.out.println("Password does not match for user: " + email);
            }
        } else {
            System.out.println("User not found for email: " + email);
        }
        return false;
    }

    public boolean addUser(String email, String password) {
        System.out.println("invoke 'addUser' in class 'MyUserDetailsService'");
        System.out.println("Adding user with email: " + email);

        MyUser user = new MyUser(email, this.bCryptPasswordEncoder.encode(password), ApiKeyGenerator.generateApiKey());
        try {
            this.userRepo.save(user);
            System.out.println("User successfully added: " + email);
            return true;
        } catch (DataIntegrityViolationException e) {
            System.err.println("Data integrity violation while adding user: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("An error occurred while adding user: " + e.getMessage());
            return false;
        }
    }

    public void updateUser(MyUser newUser) throws UsernameNotFoundException{
        Optional<MyUser> existUser=userRepo.findById(newUser.getEmail());
        if(existUser.isPresent()){
            existUser.get().setApiKey(newUser.getApiKey());
            existUser.get().setEnabled(newUser.isEnabled());
            existUser.get().setSubscribe(newUser.isSubscribe());
            existUser.get().setPassword(newUser.getPassword());
            existUser.get().setPaymentExpirationDate(newUser.getPaymentExpirationDate());

        }
        throw new UsernameNotFoundException("user not found");

    }

    public void changePassword(String email, String oldPassword, String newPassword) {
        Optional<MyUser> optionalUser = userRepo.findById(email);

        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        MyUser user = optionalUser.get();

        // Check if the old password matches the stored password
        if (!bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IncorrectPasswordException("Old password does not match.");
        }

        // If everything is fine, update the password
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepo.save(user); // Save the updated user back to the repository
    }
    public MyUser getUser(String email) {
        System.out.println("invoke 'getUser' in class 'MyUserDetailsService'");
        System.out.println("Fetching user with email: " + email);

        Optional<MyUser> optional = this.userRepo.findById(email);
        if (optional.isPresent()) {
            System.out.println("User found: " + optional.get());
            return optional.get();
        }

        System.out.println("User not found for email: " + email);
        return null;
    }

    public void processPayment(String email) {
        System.out.println("invoke 'processPayment' in class 'MyUserDetailsService'");
        System.out.println("Processing payment for user: " + email);

        Optional<MyUser> userOptional = userRepo.findById(email);
        if (userOptional.isPresent()) {
            MyUser user = userOptional.get();
            user.setSubscribe(true); // Set user as paid
            userRepo.save(user); // Save the user with updated payment status
            System.out.println("Payment processed for user: " + email);
        } else {
            System.out.println("User not found for email: " + email);
        }
    }

    public void resetPayment(String email) {
        System.out.println("invoke 'resetPayment' in class 'MyUserDetailsService'");
        System.out.println("Resetting payment for user: " + email);

        Optional<MyUser> userOptional = userRepo.findById(email);
        if (userOptional.isPresent()) {
            MyUser user = userOptional.get();
            user.setSubscribe(false); // Set user as not paid
            userRepo.save(user); // Save the user with updated payment status
            System.out.println("Payment reset for user: " + email);
        } else {
            System.out.println("User not found for email: " + email);
        }
    }

    public boolean containApiKey(String apiKey) {
        System.out.println("invoke 'containApiKey' in class 'MyUserDetailsService'");
        System.out.println("Checking if API key exists: " + apiKey);
        return this.userSubscribeApiKeys.contains(apiKey);
    }

    public void updateSubscribe(String email, boolean b) {
        Optional<MyUser> user=userRepo.findById(email);
        user.get().setSubscribe(b);
        user.get().setPaymentExpirationDate(LocalDate.now().plusMonths(1));
        userRepo.save(user.get());

    }

    public void forgotPassword(String email) throws MessagingException {


    }

    public boolean verifyPassword(String email, String oldPassword) {
        Optional<MyUser> user =this.userRepo.findById(email);
        return user.filter(myUser -> this.bCryptPasswordEncoder.matches(oldPassword, myUser.getPassword())).isPresent();
    }

    public boolean isSamePassword(String email, String newPassword) {
        Optional<MyUser> user =this.userRepo.findById(email);
        return bCryptPasswordEncoder.matches(newPassword,user.get().getPassword());
    }

    public boolean isValidPassword(String newPassword){
        return true;
    }


//
//    @PostMapping("/isSubscribe")
//    public  boolean isSubscribe(@RequestHeader("Authorization")  String authorizationHeader){
//
//        return null;
//    }
}
