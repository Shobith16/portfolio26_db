package com.portfolio.backend.controller;

import com.portfolio.backend.dto.JwtResponse;
import com.portfolio.backend.dto.LoginRequest;
import com.portfolio.backend.dto.SignupRequest;
import com.portfolio.backend.entity.User;
import com.portfolio.backend.repository.UserRepository;
import com.portfolio.backend.security.JwtUtils;
import com.portfolio.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    com.portfolio.backend.service.AuthService authService;

    @PostMapping("/request-reset")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            authService.createPasswordResetToken(email);
            return ResponseEntity.ok(Map.of("message", "OTP sent to your email."));
        } catch (Exception e) {
            log.error("Error creating reset token: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            String otp = payload.get("otp");
            String password = payload.get("newPassword");
            authService.resetPassword(email, otp, password);
            return ResponseEntity.ok(Map.of("message", "Password has been reset successfully."));
        } catch (Exception e) {
            log.error("Error resetting password: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        // Support email or username - depends on how AuthenticationManager is
        // configured with UserDetailsServiceImpl
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getEmail(),
                userDetails.getUsername(),
                userDetails.getAuthorities().iterator().next().getAuthority()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use!");
        }

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is already taken!");
        }

        // Create new user's account
        User user = new User();
        user.setEmail(signUpRequest.getEmail());
        user.setUsername(signUpRequest.getUsername());
        user.setFullName(signUpRequest.getFullName());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setRole(signUpRequest.getRole() != null ? signUpRequest.getRole() : "ROLE_USER");
        user.setStatus(com.portfolio.backend.entity.UserStatus.ACTIVE);

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully!");
    }
}
