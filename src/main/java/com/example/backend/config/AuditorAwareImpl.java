package com.example.backend.config;

import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Provides the current auditor (username) for Spring Data JPA auditing.
 *
 * <p>Automatically populates @CreatedBy and @LastModifiedBy fields with the current authenticated
 * user's username.
 */
@Component
public class AuditorAwareImpl implements AuditorAware<String> {

  @Override
  public Optional<String> getCurrentAuditor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return Optional.of("system");
    }

    String username = authentication.getName();
    return Optional.ofNullable(username != null ? username : "system");
  }
}
