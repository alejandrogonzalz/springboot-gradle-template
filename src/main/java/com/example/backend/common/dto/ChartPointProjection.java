package com.example.backend.common.dto;

/**
 * Interface-based projection for native chart queries. Spring maps column aliases 'label' and
 * 'value' to these methods.
 */
public interface ChartPointProjection {
  String getLabel();

  Long getValue();
}
