package com.bvnd115.tracuuthuoc.service;

import com.bvnd115.tracuuthuoc.exception.ApiException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AdminSessionService {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final String username;
    private final String password;
    private final byte[] secret;

    public AdminSessionService(
            @Value("${app.admin-username}") String username,
            @Value("${app.admin-password}") String password,
            @Value("${app.admin-token-secret}") String secret
    ) {
        this.username = username;
        this.password = password;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String login(String username, String password) {
        if (!same(this.username, username) || !same(this.password, password)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Sai tên đăng nhập hoặc mật khẩu");
        }
        long expiry = Instant.now().plus(Duration.ofHours(8)).getEpochSecond();
        String nonce = UUID.randomUUID().toString();
        String payload = expiry + "." + nonce;
        return payload + "." + sign(payload);
    }

    public boolean valid(String authorization) {
        if (authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return false;
        }
        String[] parts = authorization.substring(7).trim().split("\\.", -1);
        if (parts.length != 3) {
            return false;
        }
        long expiry;
        try {
            expiry = Long.parseLong(parts[0]);
        } catch (NumberFormatException ex) {
            return false;
        }
        if (expiry < Instant.now().getEpochSecond()) {
            return false;
        }
        return signaturesMatch(parts[0] + "." + parts[1], parts[2]);
    }

    private boolean signaturesMatch(String payload, String providedSignature) {
        byte[] expected;
        byte[] provided;
        try {
            expected = URL_DECODER.decode(sign(payload));
            provided = URL_DECODER.decode(providedSignature);
        } catch (IllegalArgumentException ex) {
            return false;
        }
        if (expected.length != provided.length) {
            return false;
        }
        return MessageDigest.isEqual(expected, provided);
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret, HMAC_SHA256));
            return URL_ENCODER.encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException(ex);
        }
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
