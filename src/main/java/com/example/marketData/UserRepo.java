package com.example.marketData;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<MyUser,String> {

    MyUser findByApiKey(String apiKey);
}
