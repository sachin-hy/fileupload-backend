package com.fileupload.fileproject.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final String SECRET_KEY =
            "TaK+HaV^uvCHEFsEVfypW#7g9^k*Z8$Vdasdae#DadDhadahjb78a*276";


    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }


    public Claims getClaimsFromToken(String token) {


            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

    }

    public String generateToken(String username) {
        return createToken(new HashMap<>(), username);
    }

    public String generateToken(
            Long userid,
            String email,
            String tenantKey,
            String subdomain,
            String role,
            Long tenantId) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("userid", userid);
        claims.put("tenantid", tenantId);
        claims.put("tenantkey", tenantKey);
        claims.put("subdomain", subdomain);
        claims.put("role", role);

        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String subject) {

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .header()
                .empty()
                .add("typ", "JWT")
                .and()
                .issuedAt(new Date(now))
                .expiration(new Date(now + 24L * 60 * 60 * 1000)) // 24 hours
                .signWith(getSigningKey())
                .compact();
    }
}