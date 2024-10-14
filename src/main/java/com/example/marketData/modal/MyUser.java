package com.example.marketData.modal;

import com.example.marketData.ApiKeyGenerator;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;

@Entity
public class MyUser implements UserDetails {

    @Id
    private String email;
    private String password;
    private String apiKey;


    private boolean isSubscribe = false; // Default to false
    private LocalDate paymentExpirationDate; // When the payment will expire
    private boolean enabled = false; // Set false by default until the user verifies email


    public MyUser() {
    }

    public MyUser(String email, String password, String apiKey) {
        this.email = email;
        this.password = password;
        this.apiKey = apiKey;
    }

    public MyUser(String email, String password) {
        this.email = email;
        this.password = password;
        this.apiKey = ApiKeyGenerator.generateApiKey();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    // New methods for payment status
    public boolean isSubscribe() {
        // Check if the payment status is still valid
        if (isSubscribe && paymentExpirationDate != null) {
            return !LocalDate.now().isAfter(paymentExpirationDate);
        }
        return false;
    }

    public void setSubscribe(boolean subscribe) {
        this.isSubscribe = subscribe;
        if (subscribe) {
            // Set the expiration date to 30 days from now
            this.paymentExpirationDate = LocalDate.now().plusDays(30);
        } else {
            this.paymentExpirationDate = null; // Reset expiration if not paid
        }
    }

    public LocalDate getPaymentExpirationDate() {
        return paymentExpirationDate;
    }

    public void setPaymentExpirationDate(LocalDate paymentExpirationDate) {
        this.paymentExpirationDate = paymentExpirationDate;
    }

    @Override
    public String toString() {
        return "MyUser{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", apiKey='" + apiKey + '\'' +
                ", updateSubscribe=" + isSubscribe +
                ", paymentExpirationDate=" + paymentExpirationDate +
                '}';
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


}
