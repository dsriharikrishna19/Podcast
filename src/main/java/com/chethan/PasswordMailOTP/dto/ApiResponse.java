package com.chethan.PasswordMailOTP.dto;

import com.chethan.PasswordMailOTP.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;
    private User user;

    public static ApiResponse success(String message, User user) {
        return new ApiResponse(true, message, user);
    }

    public static ApiResponse error(String message) {
        return new ApiResponse(false, message, null);
    }
}
