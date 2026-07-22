package com.microservices.pro.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private static final String CACHE_NAME = "products";

    private final Map<Long, Product> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Cacheable(value = CACHE_NAME, key = "'all'")
    public List<Product> findAll() {
        log.info("[CACHE MISS] Loading all products from store");
        return new ArrayList<>(store.values());
    }

    @Cacheable(value = CACHE_NAME, key = "#id")
    public Optional<Product> findById(Long id) {
        log.info("[CACHE MISS] Loading product {} from store", id);
        return Optional.ofNullable(store.get(id));
    }

    @CacheEvict(value = CACHE_NAME, key = "'all'")
    public Product save(Product product) {
        long id = idGenerator.getAndIncrement();
        Product saved = new Product(id, product.name(), product.description(), product.price(), product.category());
        store.put(id, saved);
        return saved;
    }

    // Evicts both the single-product entry and the all-products list in one pass
    // calling a second @CacheEvict method from here would bypass the proxy (self-invocation)
    // and silently never evict, so both keys are declared on the write method itself.
    @Caching(evict = {
            @CacheEvict(value = CACHE_NAME, key = "#id"),
            @CacheEvict(value = CACHE_NAME, key = "'all'")
    })
    public Product update(Long id, Product product) {
        log.info("[CACHE EVICT] Invalidating cache for product {}", id);
        Product updated = new Product(id, product.name(), product.description(), product.price(), product.category());
        store.put(id, updated);
        return updated;
    }

    @Caching(evict = {
            @CacheEvict(value = CACHE_NAME, key = "#id"),
            @CacheEvict(value = CACHE_NAME, key = "'all'")
    })
    public void deleteById(Long id) {
        log.info("[CACHE EVICT] Invalidating cache for product {}", id);
        store.remove(id);
    }
}