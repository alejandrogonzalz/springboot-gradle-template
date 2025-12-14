package com.example.backend.security;

import com.example.backend.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/** Service for handling JWT token generation and validation. */
@Service
@Slf4j
public class JwtService {

  @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
  private String secret;

  @Value("${jwt.expiration:86400000}")
  private long jwtExpiration;

  @Value("${jwt.refresh-expiration:604800000}")
  private long refreshExpiration;

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String generateToken(User user) {
    return buildAccessToken(user);
  }

  public String generateRefreshToken(User user) {
    return buildRefreshToken(user);
  }

  /**
   * Builds an access token with full user claims (role, permissions, email, etc.). Used for
   * authenticating API requests.
   *
   * @param user the user to generate token for
   * @return JWT access token with full claims
   */
  private String buildAccessToken(User user) {
    List<String> permissions =
        user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

    return Jwts.builder()
        .subject(user.getUsername())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
        .claim("role", user.getRole().name())
        .claim("userId", user.getId())
        .claim("email", user.getEmail())
        .claim("permissions", permissions)
        .signWith(getSignInKey())
        .compact();
  }

  /**
   * Builds a refresh token with MINIMAL claims (only subject). No sensitive data like role,
   * permissions, etc. Used only for obtaining new access tokens.
   *
   * @param user the user to generate token for
   * @return JWT refresh token with minimal claims
   */
  private String buildRefreshToken(User user) {

    return Jwts.builder()
        .subject(user.getUsername())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
        .signWith(getSignInKey())
        .compact();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public Claims extractAllClaimsPublic(String token) {
    return extractAllClaims(token);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
  }

  private SecretKey getSignInKey() {
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
