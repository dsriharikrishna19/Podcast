package com.chethan.PasswordMailOTP.controller;

import com.chethan.PasswordMailOTP.config.JwtTokenProvider;
import com.chethan.PasswordMailOTP.dto.ApiResponse;
import com.chethan.PasswordMailOTP.dto.JwtAuthResponse;
import com.chethan.PasswordMailOTP.entity.User;
import com.chethan.PasswordMailOTP.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class UserController {

@Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody User userRequest) {
        // Create a new user without ID to ensure it's auto-generated
        User newUser = new User(
            userRequest.getEmail(),
            userRequest.getPassword(),
            userRequest.getName(),
            userRequest.getGender(),
            userRequest.getPhoneNumber()
        );
        
        ApiResponse response = userService.registerUser(
            newUser.getEmail(),
            newUser.getPassword(),
            newUser.getName(),
            newUser.getGender(),
            newUser.getPhoneNumber()
        );
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get user details
        Optional<User> userOptional = userService.getUserByEmail(loginRequest.getEmail());
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        // Generate JWT token
        String token = tokenProvider.generateToken(loginRequest.getEmail());
        
        // Create response
        JwtAuthResponse response = JwtAuthResponse.builder()
                .token(token)
                .user(userOptional.get())
                .message("Login successful")
                .build();

        return ResponseEntity.ok(response);
    }
    @PostMapping("/profile")
    public ResponseEntity<?> viewProfile(@RequestBody User requestUser){
        Optional<User> user=userService.getUserByEmail(requestUser.getEmail());
        if(user.isPresent()){
            return ResponseEntity.ok(user.get());
        }
        else{
            return ResponseEntity.status(404).body("User Not Found");
        }
    }

    @PostMapping("/profileUpdate")
    public ResponseEntity<User> updateProfile(@RequestBody User updatedUser){
        Optional<User> user = userService.updateUser(updatedUser.getEmail(), updatedUser);
//        if(user.isPresent()){
        return user.map(ResponseEntity::ok)
                    .orElseGet(()->ResponseEntity.status(404).build());
//            return ResponseEntity.ok(user.get());
//        }else{
//            return ResponseEntity.status(404).body("User Not Found");
//        }
    }

    @PostMapping("/getPremium")
    public ResponseEntity<User> getPremium(@RequestBody User requestUser){
        Optional<User> optionalUser = userService.getUserByEmail(requestUser.getEmail());

        if(optionalUser.isEmpty()){
            return ResponseEntity.status(404).build();
        }
        User upgradeUser = userService.upgradeToPremium(optionalUser.get().getId());
        return ResponseEntity.ok(requestUser);
    }
}
