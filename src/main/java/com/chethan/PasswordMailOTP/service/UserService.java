package com.chethan.PasswordMailOTP.service;


import com.chethan.PasswordMailOTP.entity.User;
import com.chethan.PasswordMailOTP.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    public String registerUser(String email, String password, String name, String gender, Long phoneNumber) {
        // Check if email already exists
        if (userRepo.findByEmail(email).isPresent()) {
            return "Email already registered!";
        }

        User user = new User(email, password, name, gender, phoneNumber); // You can hash the password here if needed
        userRepo.save(user);
        System.out.println(user);
        return "User registered successfully!";

    }

    public String loginUser(String email, String password) {
        Optional<User> optionalUser = userRepo.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getPassword().equals(password)) {
                return "Login Successful for: " + user.getEmail();
            } else {
                return "Invalid Password";
            }
        } else {
            return "Need to Register";
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
                user.setPassword(updatedUser.getPassword());
            }

            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                user.setPassword(updatedUser.getPassword());
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
