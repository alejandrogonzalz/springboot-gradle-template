package com.example.backend.product.entity;

import com.example.backend.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Product entity representing a product in the system.
 *
 * <p>Uses Lombok's @SuperBuilder for fluent builder pattern
 * and inherits auditing fields from BaseEntity.</p>
 */
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_sku", columnList = "sku"),
        @Index(name = "idx_product_name", columnList = "name"),
        @Index(name = "idx_product_active", columnList = "active")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class Product extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    @Column(name = "sku", nullable = false, unique = true, length = 100)
    @NotBlank(message = "SKU is required")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU must contain only uppercase letters, numbers, and hyphens")
    private String sku;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "99999999.99", message = "Price cannot exceed 99,999,999.99")
    private BigDecimal price;

    @Column(name = "quantity", nullable = false)
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "category", length = 100)
    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    /**
     * Checks if the product is in stock.
     *
     * @return true if quantity is greater than 0, false otherwise
     */
    public boolean isInStock() {
        return quantity != null && quantity > 0;
    }

    /**
     * Reduces the product quantity by the specified amount.
     *
     * @param amount the amount to reduce
     * @throws IllegalArgumentException if amount is negative or exceeds available quantity
     */
    public void reduceQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (this.quantity < amount) {
            throw new IllegalArgumentException("Insufficient quantity available");
        }
        this.quantity -= amount;
    }

    /**
     * Increases the product quantity by the specified amount.
     *
     * @param amount the amount to add
     * @throws IllegalArgumentException if amount is negative
     */
    public void addQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.quantity += amount;
    }
}
