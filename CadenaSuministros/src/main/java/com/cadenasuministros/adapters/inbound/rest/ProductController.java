package com.cadenasuministros.adapters.inbound.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;
import com.cadenasuministros.domain.model.Product;
import com.cadenasuministros.domain.port.out.ProductRepository;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<Product> listAll() {
        return productRepository.listAllProducts();
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable UUID id) {
        return productRepository.findProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    @PostMapping
    public Product create(@RequestBody ProductRequest request) {
        Product product = new Product(
                UUID.randomUUID(),
                request.sku(),
                request.name()
        );
        return productRepository.save(product);
    }

    public record ProductRequest(
            String sku,
            String name
    ) {}
}