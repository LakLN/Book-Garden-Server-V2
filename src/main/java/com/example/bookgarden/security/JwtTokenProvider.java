package com.example.bookgarden.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.bookgarden.repository.UserRepository;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
@Log4j2
public class JwtTokenProvider {
@Autowired
UserRepository userRepository;

private final Long JWT_ACCESS_EXPIRATION = 3600000L;
private final Long JWT_REFRESH_EXPIRATION = 604800000L;

private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
private final String issuer = "Lak";

private Key getSigningKey() {
    return secretKey;
}

public String generateAccessToken(UserDetail userDetail) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + JWT_ACCESS_EXPIRATION);

    return Jwts.builder()
            .setSubject((userDetail.getUser().getId()))
            .claim("userId", userDetail.getUser().getId())
            .setIssuer(issuer)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, getSigningKey())
            .compact();

}

public String generateRefreshToken(UserDetail userDetail) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + JWT_REFRESH_EXPIRATION);

    return Jwts.builder()
            .setSubject((userDetail.getUser().getId()))
            .setIssuer(issuer)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, getSigningKey())
            .compact();

}


public String getUserIdFromJwt(String token) {
    Claims claims = Jwts.parser()
            .setSigningKey(getSigningKey())
            .parseClaimsJws(token)
            .getBody();
    return ((String) claims.get("userId"));
}

public String getUserIdFromRefreshToken(String token) {
    Claims claims = Jwts.parser()
            .setSigningKey(getSigningKey())
            .parseClaimsJws(token)
            .getBody();
    return (claims.getSubject());
}

public boolean validateToken(String authToken) {
    try {
        Jwts.parser().setSigningKey(getSigningKey().getEncoded()).parseClaimsJws(authToken);
        return true;
    } catch (UnsupportedJwtException ex) {
        log.error("Unsupported JWT token");
    } catch (IllegalArgumentException ex) {
        log.error("JWT claims string is empty.");
    }
    return false;
}

}
