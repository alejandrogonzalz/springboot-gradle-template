package com.example.backend.user.repository;

import com.example.backend.user.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
  Optional<User> findByUsername(@Param("username") String username);

  /**
   * Finds a user by email.
   *
   * @param email the email to search for
   * @return Optional containing the user if found
   */
  Optional<User> findByEmail(@Param("email") String email);

  /**
   * Checks if a user exists with the given username.
   *
   * @param username the username to check
   * @return true if exists, false otherwise
   */
  boolean existsByUsername(@Param("username") String username);

  /**
   * Checks if a user exists with the given email.
   *
   * @param email the email to check
   * @return true if exists, false otherwise
   */
  boolean existsByEmail(@Param("email") String email);

  /**
   * Counts all users by active status.
   *
   * @param isActive the active status to filter by
   * @return count of users with the given active status
   */
  long countByIsActive(@Param("isActive") boolean isActive);

  /**
   * Gets count of users grouped by role.
   *
   * @return list of [role, count] pairs
   */
  @Query("SELECT u.role as role, COUNT(u) as count FROM User u GROUP BY u.role")
  @SuppressWarnings("java:S1214")
  java.util.List<Object[]> countUsersByRole();

  /**
   * Finds users where username or email contains the search term.
   *
   * @param searchTerm the term to search for
   * @param pageable the pagination information
   * @return a Page of matching users
   */
  @Query(
      "SELECT u FROM User u WHERE "
          + "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) "
          + "ORDER BY u.username ASC")
  Page<User> findUserSuggestions(@Param("searchTerm") String searchTerm, Pageable pageable);
}
