package com.example.backend.product.mapper;

import com.example.backend.product.dto.ProductDto;
import com.example.backend.product.entity.Product;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-06T18:06:46-0600",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Amazon.com Inc.)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public ProductDto toDto(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductDto.ProductDtoBuilder productDto = ProductDto.builder();

        productDto.id( product.getId() );
        productDto.name( product.getName() );
        productDto.description( product.getDescription() );
        productDto.sku( product.getSku() );
        productDto.price( product.getPrice() );
        productDto.quantity( product.getQuantity() );
        productDto.active( product.getActive() );
        productDto.category( product.getCategory() );
        productDto.createdAt( product.getCreatedAt() );
        productDto.updatedAt( product.getUpdatedAt() );

        return productDto.build();
    }

    @Override
    public Product toEntity(ProductDto productDto) {
        if ( productDto == null ) {
            return null;
        }

        Product.ProductBuilder<?, ?> product = Product.builder();

        product.id( productDto.getId() );
        product.createdAt( productDto.getCreatedAt() );
        product.updatedAt( productDto.getUpdatedAt() );
        product.name( productDto.getName() );
        product.description( productDto.getDescription() );
        product.sku( productDto.getSku() );
        product.price( productDto.getPrice() );
        product.quantity( productDto.getQuantity() );
        product.active( productDto.getActive() );
        product.category( productDto.getCategory() );

        return product.build();
    }

    @Override
    public List<ProductDto> toDtoList(List<Product> products) {
        if ( products == null ) {
            return null;
        }

        List<ProductDto> list = new ArrayList<ProductDto>( products.size() );
        for ( Product product : products ) {
            list.add( toDto( product ) );
        }

        return list;
    }

    @Override
    public void updateEntityFromDto(ProductDto productDto, Product product) {
        if ( productDto == null ) {
            return;
        }

        if ( productDto.getId() != null ) {
            product.setId( productDto.getId() );
        }
        if ( productDto.getCreatedAt() != null ) {
            product.setCreatedAt( productDto.getCreatedAt() );
        }
        if ( productDto.getUpdatedAt() != null ) {
            product.setUpdatedAt( productDto.getUpdatedAt() );
        }
        if ( productDto.getName() != null ) {
            product.setName( productDto.getName() );
        }
        if ( productDto.getDescription() != null ) {
            product.setDescription( productDto.getDescription() );
        }
        if ( productDto.getSku() != null ) {
            product.setSku( productDto.getSku() );
        }
        if ( productDto.getPrice() != null ) {
            product.setPrice( productDto.getPrice() );
        }
        if ( productDto.getQuantity() != null ) {
            product.setQuantity( productDto.getQuantity() );
        }
        if ( productDto.getActive() != null ) {
            product.setActive( productDto.getActive() );
        }
        if ( productDto.getCategory() != null ) {
            product.setCategory( productDto.getCategory() );
        }
    }
}
