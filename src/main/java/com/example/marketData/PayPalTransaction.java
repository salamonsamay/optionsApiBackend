package com.example.marketData;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "paypal_transactions")
public class PayPalTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orderId; // PayPal order ID

    @Column(nullable = false)
    private String payerId; // PayPal payer ID

    @Column(nullable = false)
    private String paymentStatus; // Status of the payment (e.g., COMPLETED, PENDING, etc.)

    @Column(nullable = false)
    private String currency; // Currency used for the transaction

    @Column(nullable = false)
    private Double amount; // Amount paid

    @Column(nullable = false)
    private LocalDateTime createdAt; // Date and time when the transaction was created

    // Constructors, getters, and setters

    public PayPalTransaction() {}

    public PayPalTransaction(String orderId, String payerId, String paymentStatus, String currency, Double amount,  LocalDateTime createdAt) {
        this.orderId = orderId;
        this.payerId = payerId;
        this.paymentStatus = paymentStatus;
        this.currency = currency;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }



    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

