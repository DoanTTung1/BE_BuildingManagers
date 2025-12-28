package com.example.buildingmanager.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@Slf4j // Annotation của Lombok để ghi log
public class JwtTokenProvider {

    // Khóa bí mật (SECRET_KEY) phải đủ dài (ít nhất 512 bits) để dùng thuật toán HS512.
    // Đây là chuỗi Base64 demo, trong thực tế nên để trong application.properties
    private final String JWT_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    // Thời gian hết hạn của Token (tính bằng mili giây)
    // 604800000L = 7 ngày
    private final long JWT_EXPIRATION = 604800000L;

    /**
     * Lấy Key ký tên từ chuỗi Secret
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Tạo ra token từ username
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Dùng HS256 cho gọn, hoặc HS512 nếu muốn bảo mật cao hơn
                .compact();
    }

    /**
     * Lấy username từ token đã mã hóa
     */
    public String getUserNameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * Kiểm tra token có hợp lệ không
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }
}