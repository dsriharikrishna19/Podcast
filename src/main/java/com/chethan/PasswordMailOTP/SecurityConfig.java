package com.chethan.PasswordMailOTP;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Properties;

@Configuration
public class SecurityConfig {



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())   // disable CSRF
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/register","/login","/profile","/profileUpdate").permitAll()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form.disable());  // disable default login page

        return http.build();
    }
}
