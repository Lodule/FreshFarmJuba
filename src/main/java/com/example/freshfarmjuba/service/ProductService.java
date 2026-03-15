package com.example.freshfarmjuba.service;

import com.example.freshfarmjuba.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductService {

    // ==================== CREATE / UPDATE ====================
    Product save(Product product);
    Product saveProduct(Product product);
    Product update(Long id, Product productDetails);

    // ==================== READ ====================
    Product findById(Long id);
    Optional<Product> findByIdOptional(Long id);
    List<Product> findAll();

    // Category methods
    List<Product> getProductsByCategory(Product.Category category);
    List<Product> findByCategory(Product.Category category);

    // Special filters
    List<Product> getOrganicProducts();
    List<Product> getFeaturedProducts();
    List<Product> getNewArrivals();
    List<Product> getLowStockProducts(int threshold);
    List<Product> getProductsByPriceRange(Double min, Double max);

    // Search
    List<Product> searchProducts(String keyword);

    // Statistics and Reports
    long getTotalProductCount();
    long countByCategory(Product.Category category);
    List<Object[]> getCategoryStatistics();
    Double getAveragePrice();
    Double getMinPrice();
    Double getMaxPrice();
    boolean existsById(Long id);

    // NEW METHOD - Top selling products
    List<Product> getTopSellingProducts(int limit);

    // ==================== DELETE ====================
    void deleteById(Long id);
    void deleteProduct(Long id);
    void delete(Product product);

    // ==================== STOCK MANAGEMENT ====================
    Product updateStock(Long productId, int newQuantity);
    void decreaseStock(Long productId, int quantity);
    void increaseStock(Long productId, int quantity);
    boolean hasEnoughStock(Long productId, int requestedQuantity);

    // ==================== PRICE MANAGEMENT ====================
    Product updatePrice(Long productId, Double newPrice);
    Product applyDiscount(Long productId, Double discountPercentage);
    Product removeDiscount(Long productId);

    // ==================== FEATURE MANAGEMENT ====================
    Product toggleFeatured(Long productId);
    Product toggleOrganic(Long productId);
    Product toggleFreshDaily(Long productId);

    // ==================== BULK OPERATIONS ====================
    List<Product> saveAll(List<Product> products);
    void deleteAll(List<Long> ids);
}