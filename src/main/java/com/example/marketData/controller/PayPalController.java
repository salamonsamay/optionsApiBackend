package com.example.marketData.controller;

import com.example.marketData.service.JwtService;
import com.example.marketData.service.MyUserDetailsService;
import com.example.marketData.service.PayPalService;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.Refund;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/paypal")
public class PayPalController {
    @Autowired
    private PayPalService payPalService;
    @Autowired
    private JwtService jwtService;

    @Autowired
    private MyUserDetailsService userDetailsService;

    public PayPalController(PayPalService payPalService, JwtService jwtService, MyUserDetailsService user) {
        this.payPalService = payPalService;
        this.jwtService = jwtService;
        this.userDetailsService = user;
    }

    @PostMapping("/payment")
    public ResponseEntity<?> confirmPayment(@RequestHeader("Authorization") String authorizationHeader,@RequestBody Map<String, String> body) {
        String orderId = body.get("orderId");
        String payerId = body.get("payerId");
        String token = authorizationHeader.substring(7);
        String email=jwtService.extractUsername(token);

        try {
            System.out.println("Invoking 'confirmPayment'");
            Map<String, Object> paymentMap = payPalService.createPayment(orderId, payerId);
            payPalService.save(paymentMap);
            userDetailsService.updateSubscribe(email,true);
            return new ResponseEntity<>(paymentMap, HttpStatus.OK);
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Payment creation failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/execute")
    public Payment executePayment(@RequestBody Map<String, String> body) {
        String paymentId = body.get("paymentId");
        String payerId = body.get("payerId");

        if (paymentId == null || payerId == null) {
            throw new IllegalArgumentException("Payment ID and Payer ID must be provided");
        }

        try {
            return payPalService.executePayment(paymentId, payerId);
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            // Consider returning a meaningful response or throwing an exception
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment execution failed", e);
        }
    }


    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<?> getPaymentDetails(@PathVariable String paymentId) {

        try {
            Map<String,Object>  paymentDetails= payPalService.getPaymentDetails(paymentId);
            return new ResponseEntity<>(paymentDetails, HttpStatus.OK);
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to retrieve payment details: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/refund")
    public ResponseEntity<?> refundPayment(@RequestBody Map<String, String> body) {
        String paymentId = body.get("paymentId");
        try {
            Refund refund = payPalService.refundPayment(paymentId);
            return new ResponseEntity<>(refund, HttpStatus.OK);
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Refund failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/cancel")
    public ResponseEntity<String> cancelPayment() {
        System.out.println("Payment was canceled by the user.");
        return new ResponseEntity<>("Payment was canceled", HttpStatus.OK);
    }


}
