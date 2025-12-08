package com.example.backend.product.repository;

import com.example.backend.product.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Product entity.
 *
 * <p>Provides CRUD operations and custom queries for products.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  /**
   * Finds a product by its SKU.
   *
   * @param sku the product SKU
   * @return Optional containing the product if found
   */
  Optional<Product> findBySku(String sku);

  /**
   * Finds all active products.
   *
   * @return list of active products
   */
  List<Product> findByActiveTrue();

  /**
   * Finds products by category with pagination.
   *
   * @param category the product category
   * @param pageable pagination information
   * @return page of products
   */
  Page<Product> findByCategory(String category, Pageable pageable);

  /**
   * Finds products with quantity below the specified threshold.
   *
   * @param threshold the quantity threshold
   * @return list of products below threshold
   */
  @Query("SELECT p FROM Product p WHERE p.quantity < :threshold AND p.active = true")
  List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

  /**
   * Searches products by name or description containing the keyword.
   *
   * @param keyword the search keyword
   * @param pageable pagination information
   * @return page of matching products
   */
  @Query(
      "SELECT p FROM Product p WHERE "
          + "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
          + "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

  /**
   * Checks if a product with the given SKU exists.
   *
   * @param sku the product SKU
   * @return true if exists, false otherwise
   */
  boolean existsBySku(String sku);
}
