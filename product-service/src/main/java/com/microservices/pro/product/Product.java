package com.microservices.pro.product;

import java.io.Serializable;
import java.math.BigDecimal;

public record Product(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String category
) implements Serializable {}