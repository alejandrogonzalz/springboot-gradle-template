package com.example.backend.external.repository;

import com.example.backend.external.entity.CurrentExchangeRate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrentExchangeRateRepository extends JpaRepository<CurrentExchangeRate, Long> {
  Optional<CurrentExchangeRate> findBySeriesId(String seriesId);
}
