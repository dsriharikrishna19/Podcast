package com.chethan.PasswordMailOTP.service;


import com.chethan.PasswordMailOTP.dto.ApiResponse;
import com.chethan.PasswordMailOTP.entity.User;
import com.chethan.PasswordMailOTP.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ApiResponse registerUser(String email, String password, String name, String gender, Long phoneNumber) {
        // Check if email already exists
        if (userRepo.findByEmail(email).isPresent()) {
            return ApiResponse.error("Email already registered!");
        }

        // Encode password before saving
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(email, encodedPassword, name, gender, phoneNumber); 
        userRepo.save(user);
        System.out.println(user);
        return ApiResponse.success("User registered successfully!", user);
    }

    public ApiResponse loginUser(String email, String password) {
        Optional<User> optionalUser = userRepo.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            // Use passwordEncoder to match encoded password
            if (passwordEncoder.matches(password, user.getPassword())) {
                return ApiResponse.success("Login successful", user);
            } else {
                return ApiResponse.error("Invalid password");
            }
        } else {
            return ApiResponse.error("User not found. Please register first.");
        }
    }
    public Optional<User> getUserByEmail(String email){
        return userRepo.findByEmail(email);
    }
    public Optional<User> updateUser(String email, User updatedUser){
        Optional<User> optionalUser = userRepo.findByEmail(email);

        if(optionalUser.isPresent()){
            User user=optionalUser.get();

            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                // Encode password before updating
                user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }
            if (updatedUser.getName() != null && !updatedUser.getName().isEmpty()) {
                user.setName(updatedUser.getName());
            }
            if (updatedUser.getGender() != null && !updatedUser.getGender().isEmpty()) {
                user.setGender(updatedUser.getGender());
            }
            if (updatedUser.getPhoneNumber() != null) {
                user.setPhoneNumber(updatedUser.getPhoneNumber());
            }

            userRepo.save(user);
            return Optional.of(user);
        }
        else
            return Optional.empty();

    }

    public User upgradeToPremium(Long id) {
        User user = userRepo.findById(id).orElseThrow(()-> new RuntimeException("User Not Found"));
        user.setPremium(true);
        return userRepo.save(user);
    }
}
