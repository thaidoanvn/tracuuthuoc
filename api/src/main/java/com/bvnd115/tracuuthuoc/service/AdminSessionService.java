package com.bvnd115.tracuuthuoc.service;

import com.bvnd115.tracuuthuoc.exception.ApiException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AdminSessionService {

    private final String username;
    private final String password;
    private final Map<String, Instant> tokens = new ConcurrentHashMap<>();

    public AdminSessionService(
            @Value("${app.admin-username}") String username,
            @Value("${app.admin-password}") String password
    ) {
        this.username = username;
        this.password = password;
    }

    public String login(String username, String password) {
        if (!same(this.username, username) || !same(this.password, password)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Sai tên đăng nhập hoặc mật khẩu");
        }
        String token = UUID.randomUUID().toString();
        tokens.put(token, Instant.now().plus(Duration.ofHours(8)));
        return token;
    }

    public boolean valid(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return false;
        }
        String token = authorization.substring("Bearer ".length()).trim();
        Instant expires = tokens.get(token);
        if (expires == null || expires.isBefore(Instant.now())) {
            tokens.remove(token);
            return false;
        }
        return true;
    }

    private static boolean same(String expected, String provided) {
        if (expected == null || provided == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8)
        );
    }
}
