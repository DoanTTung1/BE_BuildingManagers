package com.example.buildingmanager.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider {

    // Thay vì để cứng chuỗi String, hãy dùng @Value
    @org.springframework.beans.factory.annotation.Value("${jwt.secret}")
    private String JWT_SECRET;

    @org.springframework.beans.factory.annotation.Value("${jwt.expiration}")
    private long JWT_EXPIRATION;

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * SỬA QUAN TRỌNG: Thay đổi tham số từ (String username) -> (Authentication
     * authentication)
     * Để lấy được cả Username và Roles
     */
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);

        // 1. Lấy danh sách quyền từ Authentication (Ví dụ: ["ROLE_ADMIN", "ROLE_USER"])
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(username)

                // 2. THÊM DÒNG NÀY: Nhét quyền vào Token
                .claim("roles", roles)

                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ==============================================================
    // THÊM HÀM NÀY ĐỂ HỖ TRỢ GOOGLE LOGIN
    // (Vì Google Login trả về User Entity, không phải Authentication)
    // ==============================================================
    public String generateToken(com.example.buildingmanager.entities.User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);

        // Lấy quyền từ User Entity
        // Lưu ý: Đảm bảo user.getRoles() trả về danh sách quyền (Set<String> hoặc
        // Set<Role>)
        List<String> roles = user.getRoles().stream()
                .map(Object::toString) // Chuyển Role thành String
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(user.getUserName()) // Lấy username
                .claim("roles", roles) // Nhét quyền vào
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserNameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

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