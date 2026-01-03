package com.example.backend.external.entity;

import com.example.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "current_exchange_rate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CurrentExchangeRate extends BaseEntity {

  @Column(name = "series_id", nullable = false, unique = true)
  private String seriesId;

  @Column(name = "rate_date", nullable = false)
  private LocalDate rateDate;

  @Column(name = "rate_value", nullable = false, precision = 10, scale = 4)
  private BigDecimal rateValue;
}
