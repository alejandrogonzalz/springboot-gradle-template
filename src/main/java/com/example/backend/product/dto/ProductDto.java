package com.example.backend.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Data Transfer Object for Product.
 *
 * <p>Uses Lombok's @Data for automatic getter/setter generation
 * and @Builder for fluent object creation.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product information")
public class ProductDto {

    @Schema(description = "Product unique identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Schema(description = "Product name", example = "Laptop", required = true)
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    private String name;

    @Schema(description = "Product description", example = "High-performance laptop with 16GB RAM")
    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    @Schema(description = "Stock Keeping Unit", example = "LAP-001", required = true)
    @NotBlank(message = "SKU is required")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU must contain only uppercase letters, numbers, and hyphens")
    private String sku;

    @Schema(description = "Product price", example = "999.99", required = true)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "99999999.99", message = "Price cannot exceed 99,999,999.99")
    private BigDecimal price;

    @Schema(description = "Available quantity", example = "100", required = true)
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @Schema(description = "Product active status", example = "true")
    private Boolean active;

    @Schema(description = "Product category", example = "Electronics")
    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    @Schema(description = "Creation timestamp", example = "2024-01-01T10:00:00Z", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant createdAt;

    @Schema(description = "Last update timestamp", example = "2024-01-01T10:00:00Z", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant updatedAt;
}
