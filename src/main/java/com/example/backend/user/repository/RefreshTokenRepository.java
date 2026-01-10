package com.example.backend.user.repository;

import com.example.backend.user.entity.RefreshToken;
import com.example.backend.user.entity.User;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for managing RefreshToken entities. */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  /**
   * Finds a refresh token by its token string.
   *
   * @param token the token string
   * @return Optional containing the RefreshToken if found
   */
  Optional<RefreshToken> findByToken(String token);

  /**
   * Deletes a refresh token by its token string.
   *
   * @param token the token string
   */
  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.token = :token")
  void deleteByToken(@Param("token") String token);

  /**
   * Deletes all refresh tokens for a specific user.
   *
   * @param user the user
   */
  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
  void deleteByUser(@Param("user") User user);

  /**
   * Deletes all expired refresh tokens.
   *
   * @param now the current time
   * @return number of deleted tokens
   */
  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
  int deleteExpiredTokens(@Param("now") Instant now);
}
