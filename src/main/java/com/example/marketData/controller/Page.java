package com.example.marketData.controller;

import com.example.marketData.modal.MyUser;
import com.example.marketData.service.JwtService;
import com.example.marketData.service.MyUserDetailsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping()
public class Page {

    @Autowired
    private MyUserDetailsService userService;
    @Autowired
    private JwtService jwtService;



    public Page(MyUserDetailsService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    public static final String API_KEY = "4xvJJIOpabCyR0wwwFLpPoQ4aDmMplbx";

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        System.out.println("invoke 'login' in class 'Page'");
        String email = body.get("email");
        String password = body.get("password");
        System.out.println("Login request received: email=" + email + ", password=" + password);

        boolean isAuthenticated = userService.check(email, password);
        System.out.println("User authentication result: " + isAuthenticated);

        if (isAuthenticated) {
            final String jwtToken = jwtService.generateToken(userService.loadUserByUsername(email));
            System.out.println("Generated JWT token: " + jwtToken);

            MyUser user = this.userService.getUser(email);
            String apiKey = user.getApiKey();
            System.out.println("User API Key: " + apiKey);

            return ResponseEntity.ok(Map.of("token", jwtToken, "apiKey", apiKey));
        } else {
            System.out.println("Invalid login attempt for email: " + email);
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @PostMapping("/register")
    public boolean register(@RequestBody Map<String, String> body) {
        System.out.println("invoke 'register' in class 'Page'");
        String email = body.get("email");
        String password = body.get("password");
        System.out.println("Register request received: email=" + email + ", password=" + password);

//        boolean isUserAdded = this.userService.addUser(email, password);
        this.userService.registerUser(email,password);

        return true;
    }

    @GetMapping("/optionsChain")
    public JsonNode getOptions(
            @RequestParam(value = "symbol", required = true) String symbol,
            @RequestParam(value = "strike_price", required = false) String strikePrice,
            @RequestParam(value = "start_expiration_date", required = false) String startExpirationDate,
            @RequestParam(value = "end_expiration_date", required = false) String endExpirationDate,
            @RequestParam(value = "contract_type", required = false) String contractType,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "apiKey", required = true) String apiKey
    ) throws JsonProcessingException {
        System.out.println("invoke 'getOptions' in class 'Page'");
        System.out.println("Request parameters: symbol=" + symbol + ", strike_price=" + strikePrice +
                ", start_expiration_date=" + startExpirationDate + ", end_expiration_date=" + endExpirationDate +
                ", contract_type=" + contractType + ", order=" + order + ", limit=" + limit + ", sort=" + sort + ", apiKey=" + apiKey);

        if (this.userService.containApiKey(apiKey)) {
            System.out.println("API Key is valid, replacing with server-side API key.");
            apiKey = Page.API_KEY;
        } else {
            System.out.println("Invalid API Key: " + apiKey);
        }

        // Construct the URL
        String url = "https://api.polygon.io/v3/snapshot/options/" + symbol + "?";
        if (strikePrice != null && !strikePrice.isEmpty()) {
            url += "strike_price=" + strikePrice + "&";
        }
        if (startExpirationDate != null && !startExpirationDate.isEmpty() && endExpirationDate != null && !endExpirationDate.isEmpty()) {
            url += "expiration_date.gt=" + startExpirationDate + "&expiration_date.lt=" + endExpirationDate + "&";
        } else if (startExpirationDate != null && !startExpirationDate.isEmpty()) {
            url += "expiration_date.gt=" + startExpirationDate + "&";
        } else if (endExpirationDate != null && !endExpirationDate.isEmpty()) {
            url += "expiration_date.lt=" + endExpirationDate + "&";
        }
        if (contractType != null && !contractType.isEmpty()) {
            url += "contract_type=" + contractType + "&";
        }
        if (order != null && !order.isEmpty()) {
            url += "order=" + order + "&";
        }
        url += "limit=" + limit + "&";
        if (sort != null && !sort.isEmpty()) {
            url += "sort=" + sort + "&";
        }
        url += "apikey=" + apiKey;

        System.out.println("Constructed URL: " + url);

        ResponseEntity<String> response = null;
        JsonNode node = null;

        try {
            RestTemplate restTemplate = new RestTemplate();
            response = restTemplate.getForEntity(url, String.class);
            node = new ObjectMapper().readTree(response.getBody());
            System.out.println("Response from Polygon API: " + node.toString());
        } catch (HttpClientErrorException errorException) {
            System.out.println("Error occurred while calling Polygon API: " + errorException.getMessage());
            node = new ObjectMapper().readTree(errorException.getMessage());
            return node;
        }

        return node;
    }

    @PostMapping("/user")
    public ResponseEntity<?> getUser(@RequestHeader("Authorization") String authorizationHeader) {
        System.out.println("invoke 'getUser' in class 'Page'");
        System.out.println("Authorization Header: " + authorizationHeader);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            System.out.println("Extracted Token: " + token);

            MyUser user = this.userService.getUser(jwtService.extractUsername(token));
            System.out.println("User info: Email=" + user.getEmail() + ", API Key=" + user.getApiKey());

            return ResponseEntity.ok(Map.of("email", user.getEmail(), "apiKey", user.getApiKey()));
        } else {
            System.out.println("Invalid or missing Authorization header");
            return ResponseEntity.badRequest().body("Invalid or missing Authorization header");
        }
    }
}
