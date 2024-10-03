package com.example.marketData;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Map;

@RestController
@RequestMapping()
public class Page {


    @Autowired
    private  MyUserDetailsService userService;

    @Autowired
    private JwtService jwtService;

    public Page(MyUserDetailsService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }






    public static final String API_KEY="4xvJJIOpabCyR0wwwFLpPoQ4aDmMplbx";






    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        boolean isAuthenticated = userService.check(email, password);

        if (isAuthenticated) {
            // Generate JWT token
            final String jwtToken = jwtService.generateToken(userService.loadUserByUsername(email));

            // Retrieve the user and the API key
            MyUser user = this.userService.getUser(email);
            String apiKey = user.getApiKey();

            // Return both the JWT token and the API key in the response body
            return ResponseEntity.ok(Map.of("token", jwtToken, "apiKey", apiKey));
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }



    @PostMapping("/register")
    public boolean register(@RequestBody Map<String, String> body){
        String email=body.get("email");
        String password=body.get("password");
        return this.userService.addUser(email,password);
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



        if(this.userService.containApiKey(apiKey)){
            apiKey=Page.API_KEY;
        }

        // Base URL
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

        // Create a RestTemplate instance to send the GET request

        ResponseEntity<String> response = null;
        JsonNode node=null;

        try {
            RestTemplate restTemplate = new RestTemplate();
            response  = restTemplate.getForEntity(url, String.class);
            node =new ObjectMapper().readTree(response.getBody());

        }catch (HttpClientErrorException errorException){
            System.out.println(errorException.getMessage());
            node =new ObjectMapper().readTree(errorException.getMessage());

            return node;
        }

        // Return the JSON response
        return node;
    }



    @PostMapping("/user")
    public ResponseEntity<?> getUser( @RequestHeader("Authorization") String authorizationHeader) {
        System.out.println("Authorization Header: " + authorizationHeader); // Debugging output

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            // Debugging: print token to ensure correct token extraction
            System.out.println("Extracted Token: " + token);
            MyUser user = this.userService.getUser(jwtService.extractUsername(token));

            // Assuming jwtService.extractUsername(token) validates and extracts the username correctly
            return ResponseEntity.ok(Map.of("email", user.getEmail(), "apiKey", user.getApiKey()));
        }

        // Return null or better, a proper error response
        return (ResponseEntity<?>) ResponseEntity.badRequest();

    }




    // OptionResponse class to structure the response data


}
