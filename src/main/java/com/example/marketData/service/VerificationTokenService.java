package com.example.marketData.service;

import com.example.marketData.modal.MyUser;
import com.example.marketData.modal.VerificationToken;
import com.example.marketData.repo.UserRepo;
import com.example.marketData.repo.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class VerificationTokenService {

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private UserRepo userRepo;

    public VerificationTokenService(VerificationTokenRepository verificationTokenRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
    }
    public VerificationToken findByToken(String token){
       return verificationTokenRepository.findByToken(token);
    }

    public VerificationToken generateToken(String email) throws UsernameNotFoundException{
        Optional<MyUser> user=userRepo.findById(email);
        if(user.isEmpty()){
            throw new UsernameNotFoundException("user name "+email +" not found");
        }
        String token = UUID.randomUUID().toString();
        VerificationToken resetToken = new VerificationToken();
        resetToken.setToken(token);
        resetToken.setUser(user.get());
        resetToken.setExpiryDate(new Date(System.currentTimeMillis() + 86400000)); // 24 hours
        verificationTokenRepository.save(resetToken);
        return resetToken;
    }
}
