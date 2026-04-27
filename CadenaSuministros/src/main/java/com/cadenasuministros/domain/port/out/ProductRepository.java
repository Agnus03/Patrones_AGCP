package com.cadenasuministros.domain.port.out;

import com.cadenasuministros.domain.model.Product;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Optional<Product> findProductById(UUID id);
    List<Product> listAllProducts();
    Product save(Product product);
}