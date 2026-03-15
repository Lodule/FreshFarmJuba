package com.example.freshfarmjuba.repository;

import com.example.freshfarmjuba.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find by category
    List<Product> findByCategory(Product.Category category);

    // Find organic products
    List<Product> findByOrganicTrue();

    // Find featured products
    List<Product> findByFeaturedTrue();

    // Search by name (case insensitive)
    List<Product> findByNameContainingIgnoreCase(String name);

    // Find low stock products
    List<Product> findByStockQuantityLessThan(int threshold);

    // Find by price range
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    // Find top 10 new arrivals
    List<Product> findTop10ByOrderByCreatedDateDesc();

    // Count by category
    long countByCategory(Product.Category category);

    // Get category statistics
    @Query("SELECT p.category, COUNT(p), AVG(p.price) FROM Product p GROUP BY p.category")
    List<Object[]> getCategoryStatistics();

    // Get average price
    @Query("SELECT AVG(p.price) FROM Product p")
    Double getAveragePrice();

    // Get min price
    @Query("SELECT MIN(p.price) FROM Product p")
    Double getMinPrice();

    // Get max price
    @Query("SELECT MAX(p.price) FROM Product p")
    Double getMaxPrice();

    // Find products with discount
    List<Product> findByDiscountGreaterThan(Double discount);

    // Find by multiple categories
    @Query("SELECT p FROM Product p WHERE p.category IN :categories")
    List<Product> findByCategories(@Param("categories") List<Product.Category> categories);

    // ==================== TOP SELLING PRODUCTS ====================

    // FIXED: Use native query that joins with order_items table
    @Query(value = "SELECT p.* FROM products p " +
            "LEFT JOIN order_items oi ON p.id = oi.product_id " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(oi.id) DESC LIMIT :limit",
            nativeQuery = true)
    List<Product> findTopSellingProducts(@Param("limit") int limit);

    // Alternative: Without parameter (fixed limit of 10)
    @Query(value = "SELECT p.* FROM products p " +
            "LEFT JOIN order_items oi ON p.id = oi.product_id " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(oi.id) DESC LIMIT 10",
            nativeQuery = true)
    List<Product> findTopSellingProductsDefault();
}