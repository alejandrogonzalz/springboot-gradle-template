package com.example.backend.user.repository;

import com.example.backend.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/** Repository interface for User entity operations. */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

  /**
   * Finds a user by username.
   *
   * @param username the username to search for
   * @return Optional containing the user if found
   */
  Optional<User> findByUsername(String username);

  /**
   * Finds a user by email.
   *
   * @param email the email to search for
   * @return Optional containing the user if found
   */
  Optional<User> findByEmail(String email);

  /**
   * Checks if a user exists with the given username.
   *
   * @param username the username to check
   * @return true if exists, false otherwise
   */
  boolean existsByUsername(String username);

  /**
   * Checks if a user exists with the given email.
   *
   * @param email the email to check
   * @return true if exists, false otherwise
   */
  boolean existsByEmail(String email);

  /**
   * Counts all users by active status.
   *
   * @param isActive the active status to filter by
   * @return count of users with the given active status
   */
  long countByIsActive(boolean isActive);

  /**
   * Gets count of users grouped by role.
   *
   * @return list of [role, count] pairs
   */
  @Query("SELECT u.role as role, COUNT(u) as count FROM User u GROUP BY u.role")
  @SuppressWarnings("java:S1214")
  java.util.List<Object[]> countUsersByRole();
}
