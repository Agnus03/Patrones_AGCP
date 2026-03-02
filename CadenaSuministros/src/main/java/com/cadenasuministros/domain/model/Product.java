package com.cadenasuministros.domain.model;

import java.util.UUID;

public record Product(UUID id, String sku, String name) {}
