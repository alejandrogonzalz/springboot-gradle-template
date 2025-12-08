package com.example.backend.product.service;

import com.example.backend.product.dto.ProductDto;
import com.example.backend.product.entity.Product;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.DuplicateResourceException;
import com.example.backend.product.mapper.ProductMapper;
import com.example.backend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.backend.config.CacheConfig.PRODUCTS_CACHE;
import static com.example.backend.config.CacheConfig.PRODUCT_BY_ID_CACHE;

/**
 * Service layer for Product business logic.
 *
 * <p>Implements business rules and coordinates between repository and controller layers.
 * Uses constructor injection via Lombok's @RequiredArgsConstructor for better testability.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    /**
     * Retrieves all products with pagination.
     *
     * @param pageable pagination information
     * @return page of product DTOs
     */
    @Cacheable(value = PRODUCTS_CACHE)
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        log.debug("Fetching all products with pagination: {}", pageable);
        return productRepository.findAll(pageable)
                .map(productMapper::toDto);
    }

    /**
     * Retrieves a product by ID.
     *
     * @param id the product ID
     * @return product DTO
     * @throws ResourceNotFoundException if product not found
     */
    @Cacheable(value = PRODUCT_BY_ID_CACHE, key = "#id")
    public ProductDto getProductById(Long id) {
        log.debug("Fetching product with id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productMapper.toDto(product);
    }

    /**
     * Retrieves a product by SKU.
     *
     * @param sku the product SKU
     * @return product DTO
     * @throws ResourceNotFoundException if product not found
     */
    public ProductDto getProductBySku(String sku) {
        log.debug("Fetching product with sku: {}", sku);
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with sku: " + sku));
        return productMapper.toDto(product);
    }

    /**
     * Creates a new product.
     *
     * @param productDto the product data
     * @return created product DTO
     * @throws DuplicateResourceException if SKU already exists
     */
    @Transactional
    @CacheEvict(value = PRODUCTS_CACHE, allEntries = true)
    public ProductDto createProduct(ProductDto productDto) {
        log.info("Creating new product with sku: {}", productDto.getSku());

        if (productRepository.existsBySku(productDto.getSku())) {
            throw new DuplicateResourceException("Product already exists with sku: " + productDto.getSku());
        }

        Product product = productMapper.toEntity(productDto);
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());

        return productMapper.toDto(savedProduct);
    }

    /**
     * Updates an existing product.
     *
     * @param id the product ID
     * @param productDto the updated product data
     * @return updated product DTO
     * @throws ResourceNotFoundException if product not found
     */
    @Transactional
    @CacheEvict(value = {PRODUCTS_CACHE, PRODUCT_BY_ID_CACHE}, allEntries = true)
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        log.info("Updating product with id: {}", id);

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        productMapper.updateEntityFromDto(productDto, existingProduct);
        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product updated successfully with id: {}", id);

        return productMapper.toDto(updatedProduct);
    }

    /**
     * Deletes a product by ID.
     *
     * @param id the product ID
     * @throws ResourceNotFoundException if product not found
     */
    @Transactional
    @CacheEvict(value = {PRODUCTS_CACHE, PRODUCT_BY_ID_CACHE}, allEntries = true)
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);

        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }

        productRepository.deleteById(id);
        log.info("Product deleted successfully with id: {}", id);
    }

    /**
     * Searches products by keyword.
     *
     * @param keyword the search keyword
     * @param pageable pagination information
     * @return page of matching product DTOs
     */
    public Page<ProductDto> searchProducts(String keyword, Pageable pageable) {
        log.debug("Searching products with keyword: {}", keyword);
        return productRepository.searchProducts(keyword, pageable)
                .map(productMapper::toDto);
    }

    /**
     * Retrieves products by category.
     *
     * @param category the product category
     * @param pageable pagination information
     * @return page of product DTOs
     */
    public Page<ProductDto> getProductsByCategory(String category, Pageable pageable) {
        log.debug("Fetching products by category: {}", category);
        return productRepository.findByCategory(category, pageable)
                .map(productMapper::toDto);
    }

    /**
     * Retrieves low stock products.
     *
     * @param threshold the quantity threshold
     * @return list of low stock product DTOs
     */
    public List<ProductDto> getLowStockProducts(Integer threshold) {
        log.debug("Fetching low stock products with threshold: {}", threshold);
        List<Product> products = productRepository.findLowStockProducts(threshold);
        return productMapper.toDtoList(products);
    }
}
