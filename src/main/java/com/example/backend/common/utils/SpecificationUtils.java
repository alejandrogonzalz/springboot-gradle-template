package com.example.backend.common.utils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class SpecificationUtils {

  public static <T> Specification<T> contains(String field, String value) {
    return (root, query, cb) -> {
      if (value == null || value.isBlank()) {
        return cb.conjunction();
      }
      return cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
    };
  }

  public static <T> Specification<T> equals(String field, Object value) {
    return (root, query, cb) -> {
      if (value == null) {
        return cb.conjunction();
      }
      return cb.equal(root.get(field), value);
    };
  }

  public static <T> Specification<T> between(String field, Comparable from, Comparable to) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (from != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get(field), from));
      }

      if (to != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get(field), to));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  public static <T> Specification<T> in(String field, Collection<?> values) {
    return (root, query, cb) -> {
      if (values == null || values.isEmpty()) {
        return cb.conjunction();
      }
      return root.get(field).in(values);
    };
  }
}
