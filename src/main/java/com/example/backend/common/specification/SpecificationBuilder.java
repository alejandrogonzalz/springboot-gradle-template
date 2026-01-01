package com.example.backend.common.specification;

import com.example.backend.common.utils.SpecificationUtils;
import jakarta.persistence.criteria.Join;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.jpa.domain.Specification;

/**
 * Generic builder for creating JPA Specifications with fluent API.
 *
 * <p>Eliminates boilerplate code when building complex queries with multiple optional filters.
 * Internally delegates to {@link SpecificationUtils} for core specification logic.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * Specification<User> spec = SpecificationBuilder.<User>builder()
 *     .equals("isActive", filter.getIsActive())
 *     .contains("username", filter.getUsername())
 *     .contains("email", filter.getEmail())
 *     .in("role", filter.getRoles())
 *     .between("createdAt", filter.getCreatedAtFrom(), filter.getCreatedAtTo())
 *     .build();
 * }</pre>
 *
 * @param <T> the entity type
 */
public class SpecificationBuilder<T> {

  private final List<Specification<T>> specifications = new ArrayList<>();

  private SpecificationBuilder() {}

  /**
   * Creates a new SpecificationBuilder instance.
   *
   * @param <T> the entity type
   * @return new builder instance
   */
  public static <T> SpecificationBuilder<T> builder() {
    return new SpecificationBuilder<>();
  }

  /**
   * Adds a custom specification.
   *
   * @param spec the specification to add
   * @return this builder for chaining
   */
  public SpecificationBuilder<T> where(Specification<T> spec) {
    if (spec != null) {
      specifications.add(spec);
    }
    return this;
  }

  /**
   * Adds an equality condition if value is not null. Uses {@link SpecificationUtils#equals}.
   *
   * @param field the field name
   * @param value the value to match
   * @return this builder for chaining
   */
  public SpecificationBuilder<T> equals(String field, Object value) {
    if (value != null) {
      specifications.add(SpecificationUtils.equals(field, value));
    }
    return this;
  }

  /**
   * Adds a case-insensitive contains condition if value is not null/blank. Uses {@link
   * SpecificationUtils#contains}.
   *
   * @param field the field name
   * @param value the value to search for (case-insensitive partial match)
   * @return this builder for chaining
   */
  public SpecificationBuilder<T> contains(String field, String value) {
    if (value != null && !value.isBlank()) {
      specifications.add(SpecificationUtils.contains(field, value));
    }
    return this;
  }

  /**
   * Adds a range condition (between) for comparable fields. Uses {@link
   * SpecificationUtils#between}.
   *
   * @param field the field name
   * @param from the minimum value (inclusive), null means no lower bound
   * @param to the maximum value (inclusive), null means no upper bound
   * @return this builder for chaining
   */
  public SpecificationBuilder<T> between(String field, Comparable from, Comparable to) {
    if (from != null || to != null) {
      specifications.add(SpecificationUtils.between(field, from, to));
    }
    return this;
  }

  /**
   * Adds a date range condition for Instant fields. Alias for {@link #between}.
   *
   * @param field the field name
   * @param from the start date (inclusive), null means no lower bound
   * @param to the end date (inclusive), null means no upper bound
   * @return this builder for chaining
   */
  public SpecificationBuilder<T> betweenInstant(String field, Instant from, Instant to) {
    return between(field, from, to);
  }

  /**
   * Adds an IN condition if values collection is not null/empty. Uses {@link
   * SpecificationUtils#in}.
   *
   * @param field the field name
   * @param values the collection of values
   * @return this builder for chaining
   */
  public SpecificationBuilder<T> in(String field, Collection<?> values) {
    if (values != null && !values.isEmpty()) {
      specifications.add(SpecificationUtils.in(field, values));
    }
    return this;
  }

  /**
   * Adds a NOT IN condition if values collection is not null/empty.
   *
   * @param field the field name
   * @param values the collection of values
   * @return this builder for chaining
   */
  public SpecificationBuilder<T> notIn(String field, Collection<?> values) {
    if (values != null && !values.isEmpty()) {
      specifications.add((root, query, cb) -> cb.not(root.get(field).in(values)));
    }
    return this;
  }

  /**
   * Adds a boolean TRUE condition.
   *
   * @param field the field name
   * @return this builder for chaining
   */
  public SpecificationBuilder<T> isTrue(String field) {
    specifications.add((root, query, cb) -> cb.isTrue(root.get(field)));
    return this;
  }

  /**
   * Adds a boolean FALSE condition.
   *
   * @param field the field name
   * @return this builder for chaining
   */
  public SpecificationBuilder<T> isFalse(String field) {
    specifications.add((root, query, cb) -> cb.isFalse(root.get(field)));
    return this;
  }

  /**
   * Adds a NULL check condition.
   *
   * @param field the field name
   * @return this builder for chaining
   */
  public SpecificationBuilder<T> isNull(String field) {
    specifications.add((root, query, cb) -> cb.isNull(root.get(field)));
    return this;
  }

  /**
   * Adds a NOT NULL check condition.
   *
   * @param field the field name
   * @return this builder for chaining
   */
  public SpecificationBuilder<T> isNotNull(String field) {
    specifications.add((root, query, cb) -> cb.isNotNull(root.get(field)));
    return this;
  }

  /**
   * Adds a condition based on a joined entity's field.
   *
   * @param joinField the field to join on (e.g., "additionalPermissions")
   * @param values the collection of values to match in the joined entity
   * @return this builder for chaining
   */
  public SpecificationBuilder<T> joinIn(String joinField, Collection<?> values) {
    if (values != null && !values.isEmpty()) {
      specifications.add(
          (root, query, cb) -> {
            Join<T, ?> join = root.join(joinField);
            return join.in(values);
          });
    }
    return this;
  }

  /**
   * Adds a conditional specification based on a predicate.
   *
   * @param condition the condition to check
   * @param specSupplier the specification supplier (only called if condition is true)
   * @return this builder for chaining
   */
  public SpecificationBuilder<T> when(
      boolean condition, Function<SpecificationBuilder<T>, SpecificationBuilder<T>> specSupplier) {
    if (condition) {
      specSupplier.apply(this);
    }
    return this;
  }

  /**
   * Builds the final Specification by combining all added specifications with AND.
   *
   * @return the combined Specification
   */
  public Specification<T> build() {
    if (specifications.isEmpty()) {
      return Specification.where(null);
    }

    Specification<T> result = specifications.get(0);
    for (int i = 1; i < specifications.size(); i++) {
      result = result.and(specifications.get(i));
    }
    return result;
  }
}
