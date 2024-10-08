package com.example.marketData.security;

import com.example.marketData.JwtRequestFilter;
import com.example.marketData.service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class Security {

    @Autowired
    private final MyUserDetailsService userDetailsService;


    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    public Security(MyUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // Authentication manager for custom UserDetailsService
    @Bean
    public UserDetailsService userDetailsService(){
        return this.userDetailsService;
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder(10));
        provider.setUserDetailsService(this.userDetailsService);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("invoke 'securityFilterChain(HttpSecurity http)' method in 'Security' class");


        http.csrf(customizer -> customizer.disable());
        http.authorizeHttpRequests(authorizeRequests ->
                                authorizeRequests
//                                .requestMatchers("/employee/all").permitAll()
                                        .requestMatchers("/login").permitAll()
                                        .requestMatchers("/register").permitAll()
                                        .requestMatchers("/api/**","/optionsChain").permitAll() // Allow all requests to /api/**
                                        .requestMatchers("/admin").hasAuthority("ADMIN") // Restrict /admin to users with ADMIN role
                                        .anyRequest().authenticated()

                )
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(jwtRequestFilter,UsernamePasswordAuthenticationFilter.class)
//                .formLogin(Customizer.withDefaults())
        ;
        return http.build();
    }
}
