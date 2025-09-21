package com.khundadze.PlaylistConverter.securityConfig;

import com.khundadze.PlaylistConverter.dtos.GuestSession;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    // Extracted constants for readability and single source of truth
    private static final SignatureAlgorithm SIGNING_ALGORITHM = SignatureAlgorithm.HS256;
    private static final long DEFAULT_TOKEN_TTL_MS = 1000L * 60 * 60 * 24;          // 1 day
    private static final long GUEST_TOKEN_TTL_MS = 1000L * 60 * 60 * 24 * 30;       // 30 days
    private static final String CLAIM_AUTHORITIES = "auth";
    private static final String ROLE_ANONYMOUS = "ROLE_ANONYMOUS";

    @Value("${security.secret-key}")
    private String secretKey;

    // Renamed to fix spelling and clarify intent
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(nowDate());
    }

    // Extracted small helpers to avoid repeated time constructions
    private Date nowDate() {
        return new Date(System.currentTimeMillis());
    }

    private Date expireAfterMillis(long ttlMillis) {
        long now = System.currentTimeMillis();
        return new Date(now + ttlMillis);
    }

    // Extracted common token building logic to remove duplication
    private String buildToken(Map<String, Object> claims, String subject, long ttlMillis) {
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(nowDate())
                .setExpiration(expireAfterMillis(ttlMillis))
                .signWith(getSigningKey(), SIGNING_ALGORITHM)
                .compact();
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails.getUsername(), DEFAULT_TOKEN_TTL_MS);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        List<String> authorities = userDetails.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .toList();
        claims.put(CLAIM_AUTHORITIES, authorities);

        return generateToken(claims, userDetails);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String subjectFromToken = extractUsername(token);

        return subjectFromToken.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }


    public GuestSession createGuestSessionToken() {
        String guestId = UUID.randomUUID().toString(); // 1. Generate ID
        List<String> authorities = Collections.singletonList(ROLE_ANONYMOUS);

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_AUTHORITIES, authorities);

        // 2. Build token using the ID
        String token = buildToken(claims, guestId, GUEST_TOKEN_TTL_MS);

        // 3. Return both the ID and the token
        return new GuestSession(guestId, token);
    }
}
