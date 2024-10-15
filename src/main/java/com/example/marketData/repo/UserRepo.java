package com.example.marketData.repo;

import com.example.marketData.model.MyUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<MyUser,String> {

    MyUser findByApiKey(String apiKey);
}
