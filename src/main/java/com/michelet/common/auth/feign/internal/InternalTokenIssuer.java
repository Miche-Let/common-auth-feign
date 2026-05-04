package com.michelet.common.auth.feign.internal;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;

public class InternalTokenIssuer {
    private final SecretKey secretKey;
    private final String issuer;

    public InternalTokenIssuer(String secret, String issuer) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
    }
    public String issue(String audience) {
        Instant now = Instant.now();

        return Jwts.builder()
                .issuer(issuer)
                .subject("internal-call")
                .audience().add(audience).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(60)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }
}
