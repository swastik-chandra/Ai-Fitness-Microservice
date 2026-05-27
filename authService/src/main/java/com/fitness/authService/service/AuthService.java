package com.fitness.authService.service;

import com.fitness.authService.config.JwtUtil;
import com.fitness.authService.dto.AuthResponse;
import com.fitness.authService.dto.LoginRequest;
import com.fitness.authService.dto.RegisterRequest;
import com.fitness.authService.model.AuthUser;
import com.fitness.authService.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final AuthUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOKEN_BLOCKLIST_PREFIX = "blocklist:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        AuthUser user = AuthUser.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        AuthUser savedUser = userRepository.save(user);
        log.info("User registered: {}", savedUser.getEmail());

        // Publish user.registered event to Kafka
        Map<String, Object> event = new HashMap<>();
        event.put("userId", savedUser.getId());
        event.put("email", savedUser.getEmail());
        event.put("firstName", savedUser.getFirstName());
        event.put("lastName", savedUser.getLastName());
        kafkaTemplate.send("user.registered", savedUser.getId(), event);

        return generateTokens(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        AuthUser user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        log.info("User logged in: {}", user.getEmail());
        return generateTokens(user);
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        String tokenType = jwtUtil.extractTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new RuntimeException("Invalid token type");
        }

        String userId = jwtUtil.extractUserId(refreshToken);

        // Check if refresh token exists in Redis
        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new RuntimeException("Refresh token revoked or expired");
        }

        AuthUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Rotate: invalidate old refresh token, issue new pair
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        log.info("Token refreshed for user: {}", user.getEmail());
        return generateTokens(user);
    }

    public void logout(String accessToken, String userId) {
        // Add access token to blocklist with remaining TTL
        try {
            var claims = jwtUtil.extractClaims(accessToken);
            long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                        TOKEN_BLOCKLIST_PREFIX + accessToken,
                        "blocked",
                        ttl,
                        TimeUnit.MILLISECONDS
                );
            }
        } catch (Exception e) {
            log.warn("Could not blocklist access token: {}", e.getMessage());
        }

        // Remove refresh token
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        log.info("User logged out: {}", userId);
    }

    public boolean isTokenBlocklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLOCKLIST_PREFIX + token));
    }

    private AuthResponse generateTokens(AuthUser user) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // Store refresh token in Redis with TTL
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + user.getId(),
                refreshToken,
                jwtUtil.getRefreshExpirationMs(),
                TimeUnit.MILLISECONDS
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresIn(jwtUtil.getAccessExpirationMs())
                .build();
    }
}
