package com.example.marketData.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // Generate a secure key for HS256
    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Extract username (email) from token
    public String extractUsername(String token) {
        System.out.println("invoke 'extractUsername' in class 'JwtService'");
        String username = extractClaim(token, Claims::getSubject);
        System.out.println("Extracted Username: " + username);
        return username;
    }

    // Extract expiration date from token
    public Date extractExpiration(String token) {
        System.out.println("invoke 'extractExpiration' in class 'JwtService'");
        Date expiration = extractClaim(token, Claims::getExpiration);
        System.out.println("Extracted Expiration: " + expiration);
        return expiration;
    }

    // Extract a specific claim from the token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        System.out.println("invoke 'extractClaim' in class 'JwtService'");
        final Claims claims = extractAllClaims(token);
        T claim = claimsResolver.apply(claims);
        System.out.println("Extracted Claim: " + claim);
        return claim;
    }

    // Extract all claims from the token
    private Claims extractAllClaims(String token) {
        System.out.println("invoke 'extractAllClaims' in class 'JwtService'");
        System.out.println("Token: " + token);
        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        System.out.println("All Claims extracted: " + claims);
        return claims;
    }

    // Check if the token is expired
    private Boolean isTokenExpired(String token) {
        System.out.println("invoke 'isTokenExpired' in class 'JwtService'");
        boolean isExpired = extractExpiration(token).before(new Date());
        System.out.println("Is Token Expired: " + isExpired);
        return isExpired;
    }

    // Generate JWT token for a user
    public String generateToken(UserDetails userDetails) {
        System.out.println("invoke 'generateToken' in class 'JwtService'");
        Map<String, Object> claims = new HashMap<>();
        String token = createToken(claims, userDetails.getUsername());
        System.out.println("Generated Token: " + token);
        return token;
    }

    // Create JWT token with claims and subject (username)
    private String createToken(Map<String, Object> claims, String subject) {
        System.out.println("invoke 'createToken' in class 'JwtService'");
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours validity
                .signWith(secretKey) // Use the secretKey here
                .compact();
        System.out.println("Created Token: " + token);
        return token;
    }

    // Validate JWT token
    public Boolean validateToken(String token, UserDetails userDetails) {
        System.out.println("invoke 'validateToken' in class 'JwtService'");
        final String username = extractUsername(token);
        boolean isValid = (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        System.out.println("Is Token Valid: " + isValid);
        return isValid;
    }

    public static void main(String[] args) {
        System.out.println("invoke 'main' in class 'JwtService'");
        JwtService jwtService = new JwtService();

        // Simulating token extraction
        String sampleToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYWxhMDk4NkBnbWFpbC5jb20iLCJpYXQiOjE3MjgyMTQyNzgsImV4cCI6MTcyODI1MDI3OH0.ygFDynSmpLgJGZNm2C_kmP_oP2eioC52DjuxwNbVDic";
        System.out.println("Extracted Username: " + jwtService.extractUsername(sampleToken));
    }
}
