package com.portfolio.backend.service;

import com.portfolio.backend.entity.PasswordResetToken;
import com.portfolio.backend.entity.User;
import com.portfolio.backend.repository.PasswordResetTokenRepository;
import com.portfolio.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository userRepository;
        private final PasswordResetTokenRepository tokenRepository;
        private final PasswordEncoder passwordEncoder;
        private final EmailService emailService;

        @Transactional
        public String createPasswordResetToken(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Generate a 6-digit numeric OTP
                String otp = String.format("%06d", new Random().nextInt(1000000));

                // specific "upsert" logic to avoid unique constraint violations
                PasswordResetToken resetToken = tokenRepository.findByUser_Id(user.getId())
                                .orElse(new PasswordResetToken());

                resetToken.setToken(otp);
                resetToken.setUser(user);
                resetToken.setExpiryDate(LocalDateTime.now().plusHours(24));
                resetToken.setUsed(false); // Reset used status if reusing token

                tokenRepository.save(resetToken);

                // Send OTP via email (will log to console if SMTP is not configured)
                emailService.sendPasswordResetOtp(email, otp);

                return otp;
        }

        public boolean validatePasswordResetToken(String token) {
                return tokenRepository.findByToken(token)
                                .filter(t -> !t.isUsed())
                                .filter(t -> t.getExpiryDate().isAfter(LocalDateTime.now()))
                                .isPresent();
        }

        @Transactional
        public void resetPassword(String email, String otp, String newPassword) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                PasswordResetToken resetToken = tokenRepository.findByToken(otp)
                                .filter(t -> t.getUser().getId().equals(user.getId()))
                                .filter(t -> !t.isUsed())
                                .filter(t -> t.getExpiryDate().isAfter(LocalDateTime.now()))
                                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP"));

                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);

                resetToken.setUsed(true);
                tokenRepository.save(resetToken);
        }
}
