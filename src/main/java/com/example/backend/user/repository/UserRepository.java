package com.example.backend.user.repository;

import com.example.backend.user.entity.User;
import com.example.backend.user.entity.UserRole;
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
   * Counts all non-deleted users.
   *
   * @return count of users where deletedAt is null
   */
  long countByDeletedAtIsNull();

  /**
   * Counts all non-deleted active users.
   *
   * @return count of active users where deletedAt is null
   */
  long countByIsActiveAndDeletedAtIsNull(boolean isActive);

  /**
   * Counts all soft-deleted users.
   *
   * @return count of users where deletedAt is not null
   */
  long countByDeletedAtIsNotNull();

  /**
   * Counts users by role (excluding deleted).
   *
   * @param role the role to count
   * @return count of users with the given role
   */
  long countByRoleAndDeletedAtIsNull(UserRole role);

  /**
   * Gets count of users grouped by role (excluding deleted).
   *
   * @return list of [role, count] pairs
   */
  @Query(
      "SELECT u.role as role, COUNT(u) as count FROM User u WHERE u.deletedAt IS NULL GROUP BY"
          + " u.role")
  @SuppressWarnings("java:S1214")
  java.util.List<Object[]> countUsersByRole();
}
