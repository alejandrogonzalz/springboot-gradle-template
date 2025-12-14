package com.example.backend.product.controller;

import com.example.backend.common.ApiResponse;
import com.example.backend.product.dto.ProductDto;
import com.example.backend.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Product operations.
 *
 * <p>Provides endpoints for CRUD operations and product queries. Uses Swagger annotations for API
 * documentation.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@Validated
@Tag(name = "Products", description = "Product management APIs")
public class ProductController {

  private final ProductService productService;

  /**
   * Get all products with pagination and sorting.
   *
   * @param page page number (0-based)
   * @param size page size
   * @param sortBy field to sort by
   * @param sortDir sort direction (asc/desc)
   * @return paginated list of products
   */
  @Operation(
      summary = "Get all products",
      description = "Retrieves a paginated list of all products with optional sorting")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved products",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
      })
  @GetMapping
  @PreAuthorize("hasAuthority('PERMISSION_READ')")
  public ResponseEntity<ApiResponse<Page<ProductDto>>> getAllProducts(
      @Parameter(description = "Page number (0-based)", example = "0")
          @RequestParam(defaultValue = "0")
          @Min(0)
          int page,
      @Parameter(description = "Page size", example = "10")
          @RequestParam(defaultValue = "10")
          @Min(1)
          int size,
      @Parameter(description = "Sort by field", example = "name") @RequestParam(defaultValue = "id")
          String sortBy,
      @Parameter(description = "Sort direction", example = "asc")
          @RequestParam(defaultValue = "asc")
          String sortDir) {

    log.info(
        "GET /api/v1/products - page: {}, size: {}, sortBy: {}, sortDir: {}",
        page,
        size,
        sortBy,
        sortDir);

    Sort sort =
        sortDir.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

    Pageable pageable = PageRequest.of(page, size, sort);
    Page<ProductDto> products = productService.getAllProducts(pageable);

    return ResponseEntity.ok(ApiResponse.success(products));
  }

  /**
   * Get product by ID.
   *
   * @param id product ID
   * @return product details
   */
  @Operation(
      summary = "Get product by ID",
      description = "Retrieves a single product by its unique identifier")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved product"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Product not found")
      })
  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('PERMISSION_READ')")
  public ResponseEntity<ApiResponse<ProductDto>> getProductById(
      @Parameter(description = "Product ID", example = "1", required = true) @PathVariable
          Long id) {

    log.info("GET /api/v1/products/{}", id);
    ProductDto product = productService.getProductById(id);
    return ResponseEntity.ok(ApiResponse.success(product));
  }

  /**
   * Get product by SKU.
   *
   * @param sku product SKU
   * @return product details
   */
  @Operation(summary = "Get product by SKU", description = "Retrieves a single product by its SKU")
  @GetMapping("/sku/{sku}")
  @PreAuthorize("hasAuthority('PERMISSION_READ')")
  public ResponseEntity<ApiResponse<ProductDto>> getProductBySku(
      @Parameter(description = "Product SKU", example = "LAP-001", required = true) @PathVariable
          String sku) {

    log.info("GET /api/v1/products/sku/{}", sku);
    ProductDto product = productService.getProductBySku(sku);
    return ResponseEntity.ok(ApiResponse.success(product));
  }

  /**
   * Create a new product.
   *
   * @param productDto product data
   * @return created product
   */
  @Operation(
      summary = "Create a new product",
      description = "Creates a new product with the provided information")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Product created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid input data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Product with SKU already exists")
      })
  @PostMapping
  @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
  public ResponseEntity<ApiResponse<ProductDto>> createProduct(
      @Valid @RequestBody ProductDto productDto) {

    log.info("POST /api/v1/products - Creating product with sku: {}", productDto.getSku());
    ProductDto createdProduct = productService.createProduct(productDto);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(createdProduct, "Product created successfully"));
  }

  /**
   * Update an existing product.
   *
   * @param id product ID
   * @param productDto updated product data
   * @return updated product
   */
  @Operation(
      summary = "Update a product",
      description = "Updates an existing product with the provided information")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Product updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Product not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid input data")
      })
  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('PERMISSION_UPDATE')")
  public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
      @Parameter(description = "Product ID", example = "1", required = true) @PathVariable Long id,
      @Valid @RequestBody ProductDto productDto) {

    log.info("PUT /api/v1/products/{}", id);
    ProductDto updatedProduct = productService.updateProduct(id, productDto);
    return ResponseEntity.ok(ApiResponse.success(updatedProduct, "Product updated successfully"));
  }

  /**
   * Delete a product.
   *
   * @param id product ID
   * @return success response
   */
  @Operation(summary = "Delete a product", description = "Deletes a product by its ID")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Product deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Product not found")
      })
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('PERMISSION_DELETE')")
  public ResponseEntity<ApiResponse<Void>> deleteProduct(
      @Parameter(description = "Product ID", example = "1", required = true) @PathVariable
          Long id) {

    log.info("DELETE /api/v1/products/{}", id);
    productService.deleteProduct(id);
    return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
  }

  /**
   * Search products by keyword.
   *
   * @param keyword search keyword
   * @param page page number
   * @param size page size
   * @return matching products
   */
  @Operation(summary = "Search products", description = "Searches products by name or description")
  @GetMapping("/search")
  @PreAuthorize("hasAuthority('PERMISSION_READ')")
  public ResponseEntity<ApiResponse<Page<ProductDto>>> searchProducts(
      @Parameter(description = "Search keyword", example = "laptop", required = true) @RequestParam
          String keyword,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) int size) {

    log.info("GET /api/v1/products/search - keyword: {}", keyword);
    Pageable pageable = PageRequest.of(page, size);
    Page<ProductDto> products = productService.searchProducts(keyword, pageable);
    return ResponseEntity.ok(ApiResponse.success(products));
  }

  /**
   * Get products by category.
   *
   * @param category product category
   * @param page page number
   * @param size page size
   * @return products in category
   */
  @Operation(
      summary = "Get products by category",
      description = "Retrieves all products in a specific category")
  @GetMapping("/category/{category}")
  @PreAuthorize("hasAuthority('PERMISSION_READ')")
  public ResponseEntity<ApiResponse<Page<ProductDto>>> getProductsByCategory(
      @Parameter(description = "Product category", example = "Electronics", required = true)
          @PathVariable
          String category,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) int size) {

    log.info("GET /api/v1/products/category/{}", category);
    Pageable pageable = PageRequest.of(page, size);
    Page<ProductDto> products = productService.getProductsByCategory(category, pageable);
    return ResponseEntity.ok(ApiResponse.success(products));
  }

  /**
   * Get low stock products.
   *
   * @param threshold quantity threshold
   * @return low stock products
   */
  @Operation(
      summary = "Get low stock products",
      description = "Retrieves products with quantity below the specified threshold")
  @GetMapping("/low-stock")
  public ResponseEntity<ApiResponse<List<ProductDto>>> getLowStockProducts(
      @Parameter(description = "Quantity threshold", example = "10")
          @RequestParam(defaultValue = "10")
          @Min(1)
          Integer threshold) {

    log.info("GET /api/v1/products/low-stock - threshold: {}", threshold);
    List<ProductDto> products = productService.getLowStockProducts(threshold);
    return ResponseEntity.ok(ApiResponse.success(products));
  }
}
