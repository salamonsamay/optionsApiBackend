package com.example.marketData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MyUserDetailsService implements UserDetailsService {


    @Autowired
    private final UserRepo userRepo;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(10);

    public MyUserDetailsService(UserRepo userRepo) {
        this.userRepo = userRepo;

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("load by user name " + username);
        Optional<MyUser> user = userRepo.findById(username);
        if (user.isPresent()) {
            System.out.println("user found");
            System.out.println(user.get());
            return user.get();
        }

        return user.orElseThrow();
    }

    public boolean check(String email, String password) {
        System.out.println("check method");
        System.out.println(email);
        System.out.println(password);
        Optional<MyUser> optional = this.userRepo.findById(email);

        if (optional.isPresent()) {
            MyUser user = optional.get();


            // Use matches method to compare raw password with hashed password
            if (bCryptPasswordEncoder.matches(password, user.getPassword())) {
                return true;
            }
        }
        return false;
    }


    public boolean addUser(String email, String password) {
        System.out.println("invoke 'addUser' method in MyUserDetailsService ");
//        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//        String hashedPassword = passwordEncoder.encode(password); // BCrypt hashed password

        MyUser user = new MyUser(email, this.bCryptPasswordEncoder.encode(password), ApiKeyGenerator.generateApiKey());
        try {
            this.userRepo.save(user);
            return true;
        } catch (DataIntegrityViolationException e) {
            // Handle cases like unique constraint violations (e.g., email already exists)
            System.err.println("Data integrity violation: " + e.getMessage());
            return false;
        } catch (Exception e) {
            // Catch any other general exception
            System.err.println("An error occurred: " + e.getMessage());
            return false;
        }
    }

    public MyUser getUser(String email) {
        Optional<MyUser> optional = this.userRepo.findById(email);
        if (optional.isPresent()) {
            return optional.get();

        }
        return null;
    }

    public boolean containApiKey(String apiKey){
      return this.userRepo.findByApiKey(apiKey)!=null;
    }

}