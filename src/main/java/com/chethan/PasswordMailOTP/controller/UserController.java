package com.chethan.PasswordMailOTP.controller;

import com.chethan.PasswordMailOTP.entity.User;
import com.chethan.PasswordMailOTP.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        String response = userService.registerUser(user.getEmail(), user.getPassword(), user.getName(), user.getGender(), user.getPhoneNumber());
        System.out.println(response);
        if (response.equals("User registered successfully!")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        String response = userService.loginUser(user.getEmail(), user.getPassword());
        if (response.startsWith("Login Successful")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
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
