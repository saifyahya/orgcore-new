package com.engineering.orgcore.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtUtil {
    private final String jwtSecret = "default";

    private long expirationMs = 24 * 60 * 60 * 1000L; // 24 hours

    private final ObjectMapper objectMapper = new ObjectMapper();


    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] base64UrlDecode(String str) {
        return Base64.getUrlDecoder().decode(str);
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] sig = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(sig);
        } catch (Exception e) {
            throw new RuntimeException("Unable to sign token", e);
        }
    }

    public  String generateToken(Map<String, Object> claims, String subject) {
        try {
            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            long now = Instant.now().toEpochMilli();
            long exp = now + expirationMs;

            Map<String, Object> payload = new HashMap<>(claims);
            payload.put("sub", subject);
            payload.put("iat", now);
            payload.put("exp", exp);

            String headerJson = objectMapper.writeValueAsString(header);
            String payloadJson = objectMapper.writeValueAsString(payload);

            String headerB64 = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
            String payloadB64 = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));

            String toSign = headerB64 + "." + payloadB64;
            String signature = sign(toSign);
            return toSign + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> parseClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new RuntimeException("Invalid token");
            String headerB64 = parts[0];
            String payloadB64 = parts[1];
            String signature = parts[2];

            String toSign = headerB64 + "." + payloadB64;
            String expectedSig = sign(toSign);
            if (!expectedSig.equals(signature)) throw new RuntimeException("Invalid signature");

            byte[] payloadBytes = base64UrlDecode(payloadB64);
            Map<String, Object> payload = objectMapper.readValue(payloadBytes, new TypeReference<Map<String, Object>>(){});

            // verify exp
            Object expObj = payload.get("exp");
            if (expObj != null) {
                long exp = 0L;
                if (expObj instanceof Number) exp = ((Number) expObj).longValue();
                else exp = Long.parseLong(expObj.toString());
                long now = Instant.now().toEpochMilli();
                if (now > exp) throw new RuntimeException("Token expired");
            }

            return payload;
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse token", e);
        }
    }

    public Long extractTenantId(String token) {
        Map<String, Object> claims = parseClaims(token);
        Object t = claims.get("tenantId");
        if (t == null) return null;
        if (t instanceof Number) return ((Number) t).longValue();
        try { return Long.parseLong(t.toString()); } catch (Exception e) { return null; }
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token); // verifies signature + expiration
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String extractUsername(String token) {
         Map<String, Object> claims = parseClaims(token);
         Object sub = claims.get("username");
         if (sub == null) return null;
         return sub.toString();

    }



}
