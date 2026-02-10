package com.portfolio.backend.dto;

import lombok.Data;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String username;
    private String role;

    public JwtResponse(String accessToken, Long id, String email, String username, String role) {
        this.token = accessToken;
        this.id = id;
        this.email = email;
        this.username = username;
        this.role = role;
    }
}
