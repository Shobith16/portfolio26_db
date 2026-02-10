package com.portfolio.backend.dto;

import lombok.Data;

@Data
public class SignupRequest {
    private String email;
    private String username;
    private String fullName;
    private String password;
    private String role; // optional
}
