package com.cadenasuministros.adapters.outbound.persistence.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;

@Entity(name = "products")
public class ProductJpaEntity {
    @Id
    public UUID id;
    public String sku;
    public String name;
}