package com.example.marketData.service;

import com.example.marketData.repo.PayPalRepo;
import com.example.marketData.PayPalTransaction;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PayPalService {

    private APIContext apiContext;

    private String clientId = "ATREEzXHUfhyGhouBiK5fy_98oErWwkyAGhhbkWLGVySiPOoVTpCLDVcC-4_37Y5h6xfT4AI2M7a2CRc";
    private String clientSecret = "EGzCBd01TjLzZRH1tARoPTCzubN_OXIpTElO7X2jnKNOh81b2evPxOAiKz74JapPpXj_gCEH30bS1ehn";
    private String mode = "sandbox";


    @Autowired
    private PayPalRepo payPalRepo;

    @PostConstruct
    public void init() {
        apiContext = new APIContext(clientId, clientSecret, mode);
    }

    public Map<String, Object> createPayment(String orderId, String payerId) throws PayPalRESTException {
        Amount amount = new Amount();
        amount.setCurrency("USD");
        amount.setTotal("10.00");

        Transaction transaction = new Transaction();
        transaction.setDescription("Payment for order ID: " + orderId);
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");
        PayerInfo payerInfo = new PayerInfo();
        payerInfo.setPayerId(payerId);
        payer.setPayerInfo(payerInfo);

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setReturnUrl("http://localhost:8080/return");
        redirectUrls.setCancelUrl("http://localhost:8080/cancel");
        payment.setRedirectUrls(redirectUrls);

        Payment createdPayment = payment.create(apiContext);  // Create the payment with PayPal

        // Build the paymentMap
        Map<String, Object> paymentMap = new HashMap<>();
        paymentMap.put("id", createdPayment.getId());
        paymentMap.put("intent", createdPayment.getIntent());
        paymentMap.put("state", createdPayment.getState());
        paymentMap.put("create_time", createdPayment.getCreateTime());

        // Payer details
        Map<String, Object> payerMap = new HashMap<>();
        payerMap.put("payment_method", createdPayment.getPayer().getPaymentMethod());

        Map<String, String> payerInfoMap = new HashMap<>();
        payerInfoMap.put("payer_id", createdPayment.getPayer().getPayerInfo().getPayerId());
        payerMap.put("payer_info", payerInfoMap);

        paymentMap.put("payer", payerMap);

        // Transactions
        List<Transaction> createdTransactions = createdPayment.getTransactions();
        List<Map<String, Object>> transactionList = new ArrayList<>();
        for (Transaction createdTransaction : createdTransactions) {
            Map<String, Object> transactionMap = new HashMap<>();
            transactionMap.put("amount", createdTransaction.getAmount().getTotal());
            transactionMap.put("currency", createdTransaction.getAmount().getCurrency());
            transactionMap.put("description", createdTransaction.getDescription());
            transactionList.add(transactionMap);
        }
        paymentMap.put("transactions", transactionList);

        // Links
        List<Map<String, String>> linksList = new ArrayList<>();
        for (Links link : createdPayment.getLinks()) {
            Map<String, String> linkMap = new HashMap<>();
            linkMap.put("href", link.getHref());
            linkMap.put("rel", link.getRel());
            linkMap.put("method", link.getMethod());
            linksList.add(linkMap);
        }
        paymentMap.put("links", linksList);

        return paymentMap;
    }


    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        System.out.println("execute method");
        // Check for null or empty paymentId and payerId
        if (paymentId == null || paymentId.isEmpty()) {
            throw new IllegalArgumentException("Payment ID cannot be null or empty");
        }
        if (payerId == null || payerId.isEmpty()) {
            throw new IllegalArgumentException("Payer ID cannot be null or empty");
        }

        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);


        // Execute the payment
        try {
            return payment.execute(apiContext, paymentExecution);
        } catch (PayPalRESTException e) {
            // Log the error or handle it as needed
            throw new PayPalRESTException("Failed to execute payment: " + e.getMessage(), e);
        }
    }


//    public Payment getPaymentDetails(String paymentId) throws PayPalRESTException {
//        return Payment.get(apiContext, paymentId);
//    }

    public Map<String, Object> getPaymentDetails(String paymentId) throws PayPalRESTException {
        // Fetch the payment details using the paymentId
        Payment payment = Payment.get(apiContext, paymentId);

        // Build the paymentMap
        Map<String, Object> paymentMap = new HashMap<>();
        paymentMap.put("id", payment.getId());
        paymentMap.put("intent", payment.getIntent());
        paymentMap.put("state", payment.getState());
        paymentMap.put("cart", payment.getCart());
        paymentMap.put("create_time", payment.getCreateTime());
        paymentMap.put("update_time", payment.getUpdateTime());

        // Transactions
        List<Transaction> transactions = payment.getTransactions();
        List<Map<String, Object>> transactionList = new ArrayList<>();
        for (Transaction transaction : transactions) {
            Map<String, Object> transactionMap = new HashMap<>();

            // Amount
            Map<String, Object> amountMap = new HashMap<>();
            amountMap.put("total", transaction.getAmount().getTotal());
            amountMap.put("currency", transaction.getAmount().getCurrency());
            transactionMap.put("amount", amountMap);

            // Payee details
            Map<String, Object> payeeMap = new HashMap<>();
            payeeMap.put("merchant_id", transaction.getPayee().getMerchantId());
            payeeMap.put("email", transaction.getPayee().getEmail());
            transactionMap.put("payee", payeeMap);

            // Description
            transactionMap.put("description", transaction.getDescription());

            // Related resources (if any)
            transactionMap.put("related_resources", transaction.getRelatedResources());

            transactionList.add(transactionMap);
        }
        paymentMap.put("transactions", transactionList);

        // Redirect URLs
        Map<String, Object> redirectUrlsMap = new HashMap<>();
        redirectUrlsMap.put("return_url", payment.getRedirectUrls().getReturnUrl());
        redirectUrlsMap.put("cancel_url", payment.getRedirectUrls().getCancelUrl());
        paymentMap.put("redirect_urls", redirectUrlsMap);

        // Links
        List<Map<String, String>> linksList = new ArrayList<>();
        for (Links link : payment.getLinks()) {
            Map<String, String> linkMap = new HashMap<>();
            linkMap.put("href", link.getHref());
            linkMap.put("rel", link.getRel());
            linkMap.put("method", link.getMethod());
            linksList.add(linkMap);
        }
        paymentMap.put("links", linksList);

        return paymentMap;
    }



    public Refund refundPayment(String paymentId) throws PayPalRESTException {
        Sale sale = new Sale();
        sale.setId(paymentId);

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setAmount(new Amount("USD", "10.00"));

        return sale.refund(apiContext, refundRequest);
    }

    public PayPalTransaction save(Map<String, Object> paymentMap) {
        System.out.println(paymentMap);
        try {
            // Extracting required fields from the paymentMap
            String orderId = (String) paymentMap.get("id");

            Map<String, Object> payer = (Map<String, Object>) paymentMap.get("payer");
            if (payer == null) {
                throw new IllegalArgumentException("Payer information is missing");
            }

            Map<String, Object> payerInfo = (Map<String, Object>) payer.get("payer_info");
            if (payerInfo == null) {
                throw new IllegalArgumentException("Payer info is missing");
            }

            String payerId = (String) payerInfo.get("payer_id");
            String paymentStatus = (String) paymentMap.get("state");

            List<Map<String, Object>> transactions = (List<Map<String, Object>>) paymentMap.get("transactions");
            if (transactions == null || transactions.isEmpty()) {
                throw new IllegalArgumentException("Transaction information is missing");
            }

            Map<String, Object> transaction = transactions.get(0);
            String currency = (String) transaction.get("currency");
            String amountString = (String) transaction.get("amount");
            if (amountString == null) {
                throw new IllegalArgumentException("Amount is missing");
            }
            Double amount = Double.valueOf(amountString);

            String email = (String) payerInfo.get("email");
            LocalDateTime createdAt = LocalDateTime.now();

            // Create a new PayPalTransaction instance
            PayPalTransaction payPalTransaction = new PayPalTransaction(orderId, payerId, paymentStatus, currency, amount, createdAt);

            // Assuming you have a repository for PayPalTransaction
            return this.payPalRepo.save(payPalTransaction);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Invalid data type in payment map", e);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Required field is missing in payment map", e);
        }
    }

}
