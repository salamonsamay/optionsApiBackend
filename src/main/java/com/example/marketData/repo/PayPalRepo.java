package com.example.marketData.repo;

import com.example.marketData.PayPalTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayPalRepo extends JpaRepository<PayPalTransaction,Long> {
}
