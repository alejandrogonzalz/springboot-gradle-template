package com.example.backend.service;

import com.example.backend.product.dto.ProductDto;
import com.example.backend.product.entity.Product;
import com.example.backend.exception.DuplicateResourceException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.product.mapper.ProductMapper;
import com.example.backend.product.service.ProductService;
import com.example.backend.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService.
 *
 * <p>Uses Mockito for mocking dependencies and AssertJ for fluent assertions.
 * Demonstrates best practices for unit testing service layer.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductDto testProductDto;

    @BeforeEach
    void setUp() {
        // Arrange: Create test data
        testProduct = Product.builder()
                .id(1L)
                .name("Test Laptop")
                .description("High-performance laptop")
                .sku("LAP-001")
                .price(BigDecimal.valueOf(999.99))
                .quantity(100)
                .active(true)
                .category("Electronics")
                .build();

        testProductDto = ProductDto.builder()
                .id(1L)
                .name("Test Laptop")
                .description("High-performance laptop")
                .sku("LAP-001")
                .price(BigDecimal.valueOf(999.99))
                .quantity(100)
                .active(true)
                .category("Electronics")
                .build();
    }

    @Test
    @DisplayName("Should successfully retrieve all products with pagination")
    void getAllProducts_WithValidPagination_ShouldReturnPageOfProducts() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(productMapper.toDto(testProduct)).thenReturn(testProductDto);

        // Act
        Page<ProductDto> result = productService.getAllProducts(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSku()).isEqualTo("LAP-001");

        verify(productRepository, times(1)).findAll(pageable);
        verify(productMapper, times(1)).toDto(testProduct);
    }

    @Test
    @DisplayName("Should successfully retrieve product by ID")
    void getProductById_WithValidId_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productMapper.toDto(testProduct)).thenReturn(testProductDto);

        // Act
        ProductDto result = productService.getProductById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSku()).isEqualTo("LAP-001");

        verify(productRepository, times(1)).findById(1L);
        verify(productMapper, times(1)).toDto(testProduct);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product ID not found")
    void getProductById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 999");

        verify(productRepository, times(1)).findById(999L);
        verify(productMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Should successfully retrieve product by SKU")
    void getProductBySku_WithValidSku_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findBySku("LAP-001")).thenReturn(Optional.of(testProduct));
        when(productMapper.toDto(testProduct)).thenReturn(testProductDto);

        // Act
        ProductDto result = productService.getProductBySku("LAP-001");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSku()).isEqualTo("LAP-001");

        verify(productRepository, times(1)).findBySku("LAP-001");
        verify(productMapper, times(1)).toDto(testProduct);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when SKU not found")
    void getProductBySku_WithInvalidSku_ShouldThrowException() {
        // Arrange
        when(productRepository.findBySku(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.getProductBySku("INVALID"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with sku: INVALID");

        verify(productRepository, times(1)).findBySku("INVALID");
        verify(productMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Should successfully create a new product")
    void createProduct_WithValidData_ShouldReturnCreatedProduct() {
        // Arrange
        when(productRepository.existsBySku("LAP-001")).thenReturn(false);
        when(productMapper.toEntity(testProductDto)).thenReturn(testProduct);
        when(productRepository.save(testProduct)).thenReturn(testProduct);
        when(productMapper.toDto(testProduct)).thenReturn(testProductDto);

        // Act
        ProductDto result = productService.createProduct(testProductDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSku()).isEqualTo("LAP-001");

        verify(productRepository, times(1)).existsBySku("LAP-001");
        verify(productRepository, times(1)).save(testProduct);
        verify(productMapper, times(1)).toEntity(testProductDto);
        verify(productMapper, times(1)).toDto(testProduct);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when SKU already exists")
    void createProduct_WithDuplicateSku_ShouldThrowException() {
        // Arrange
        when(productRepository.existsBySku("LAP-001")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(testProductDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Product already exists with sku: LAP-001");

        verify(productRepository, times(1)).existsBySku("LAP-001");
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully update an existing product")
    void updateProduct_WithValidData_ShouldReturnUpdatedProduct() {
        // Arrange
        ProductDto updatedDto = ProductDto.builder()
                .name("Updated Laptop")
                .price(BigDecimal.valueOf(1299.99))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(testProduct)).thenReturn(testProduct);
        when(productMapper.toDto(testProduct)).thenReturn(testProductDto);
        doNothing().when(productMapper).updateEntityFromDto(updatedDto, testProduct);

        // Act
        ProductDto result = productService.updateProduct(1L, updatedDto);

        // Assert
        assertThat(result).isNotNull();

        verify(productRepository, times(1)).findById(1L);
        verify(productMapper, times(1)).updateEntityFromDto(updatedDto, testProduct);
        verify(productRepository, times(1)).save(testProduct);
        verify(productMapper, times(1)).toDto(testProduct);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent product")
    void updateProduct_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct(999L, testProductDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 999");

        verify(productRepository, times(1)).findById(999L);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully delete a product")
    void deleteProduct_WithValidId_ShouldDeleteProduct() {
        // Arrange
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent product")
    void deleteProduct_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(productRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> productService.deleteProduct(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 999");

        verify(productRepository, times(1)).existsById(999L);
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should successfully search products by keyword")
    void searchProducts_WithKeyword_ShouldReturnMatchingProducts() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.searchProducts("laptop", pageable)).thenReturn(productPage);
        when(productMapper.toDto(testProduct)).thenReturn(testProductDto);

        // Act
        Page<ProductDto> result = productService.searchProducts("laptop", pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(productRepository, times(1)).searchProducts("laptop", pageable);
    }

    @Test
    @DisplayName("Should successfully retrieve products by category")
    void getProductsByCategory_WithValidCategory_ShouldReturnProducts() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findByCategory("Electronics", pageable)).thenReturn(productPage);
        when(productMapper.toDto(testProduct)).thenReturn(testProductDto);

        // Act
        Page<ProductDto> result = productService.getProductsByCategory("Electronics", pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategory()).isEqualTo("Electronics");

        verify(productRepository, times(1)).findByCategory("Electronics", pageable);
    }

    @Test
    @DisplayName("Should successfully retrieve low stock products")
    void getLowStockProducts_WithThreshold_ShouldReturnLowStockProducts() {
        // Arrange
        Product lowStockProduct = Product.builder()
                .id(2L)
                .name("Low Stock Item")
                .sku("LOW-001")
                .price(BigDecimal.valueOf(50.00))
                .quantity(5)
                .active(true)
                .build();

        ProductDto lowStockDto = ProductDto.builder()
                .id(2L)
                .name("Low Stock Item")
                .sku("LOW-001")
                .price(BigDecimal.valueOf(50.00))
                .quantity(5)
                .active(true)
                .build();

        List<Product> lowStockProducts = Arrays.asList(lowStockProduct);

        when(productRepository.findLowStockProducts(10)).thenReturn(lowStockProducts);
        when(productMapper.toDtoList(lowStockProducts)).thenReturn(Arrays.asList(lowStockDto));

        // Act
        List<ProductDto> result = productService.getLowStockProducts(10);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuantity()).isLessThan(10);

        verify(productRepository, times(1)).findLowStockProducts(10);
        verify(productMapper, times(1)).toDtoList(lowStockProducts);
    }
}
