package com.example.marketData;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders") // Use a different name for the table
public class Order {
    @Id
    private String orderId; // PayPal order ID
    private String paymentStatus; // 'pending', 'completed', 'failed', etc.
    private Double amount;
    private LocalDateTime createdAt;
    @ManyToOne
    private Customer customer; // Links the order to the user (customer)

    public Order() {
    }

    public Order(String orderId, String paymentStatus, Double amount, Customer customer) {
        this.orderId = orderId;
        this.paymentStatus = paymentStatus;
        this.amount = amount;
        this.customer = customer;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters


    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
