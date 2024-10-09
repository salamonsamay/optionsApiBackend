package com.example.marketData.repo;

import com.example.marketData.modal.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    // Custom method to find a VerificationToken by the token string
    VerificationToken findByToken(String token);
}
