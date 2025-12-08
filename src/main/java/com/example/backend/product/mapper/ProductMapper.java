package com.example.backend.product.mapper;

import com.example.backend.product.dto.ProductDto;
import com.example.backend.product.entity.Product;
import java.util.List;
import org.mapstruct.*;

/**
 * MapStruct mapper for Product entity and ProductDto.
 *
 * <p>Automatically generates mapping code at compile time, reducing boilerplate and improving
 * performance.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

  /**
   * Converts Product entity to ProductDto.
   *
   * @param product the product entity
   * @return product DTO
   */
  ProductDto toDto(Product product);

  /**
   * Converts ProductDto to Product entity.
   *
   * @param productDto the product DTO
   * @return product entity
   */
  Product toEntity(ProductDto productDto);

  /**
   * Converts a list of Product entities to ProductDto list.
   *
   * @param products list of product entities
   * @return list of product DTOs
   */
  List<ProductDto> toDtoList(List<Product> products);

  /**
   * Updates an existing Product entity with values from ProductDto.
   *
   * @param productDto the source DTO
   * @param product the target entity to update
   */
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(ProductDto productDto, @MappingTarget Product product);
}
