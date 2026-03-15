package com.example.freshfarmjuba.serviceimpl;

import com.example.freshfarmjuba.model.Product;
import com.example.freshfarmjuba.repository.ProductRepository;
import com.example.freshfarmjuba.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    // ==================== CREATE / UPDATE ====================

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product saveProduct(Product product) {
        return save(product);
    }

    @Override
    public Product update(Long id, Product productDetails) {
        Product product = findById(id);

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setCategory(productDetails.getCategory());
        product.setImageUrl(productDetails.getImageUrl());
        product.setStockQuantity(productDetails.getStockQuantity());
        product.setOrganic(productDetails.isOrganic());
        product.setFreshDaily(productDetails.isFreshDaily());
        product.setFeatured(productDetails.isFeatured());
        product.setUnit(productDetails.getUnit());
        product.setDiscount(productDetails.getDiscount());

        return productRepository.save(product);
    }

    // ==================== READ ====================

    @Override
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    public Optional<Product> findByIdOptional(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getProductsByCategory(Product.Category category) {
        if (category == null) {
            return findAll();
        }
        return productRepository.findByCategory(category);
    }

    @Override
    public List<Product> findByCategory(Product.Category category) {
        return getProductsByCategory(category);
    }

    @Override
    public List<Product> getOrganicProducts() {
        return productRepository.findByOrganicTrue();
    }

    @Override
    public List<Product> getFeaturedProducts() {
        return productRepository.findByFeaturedTrue();
    }

    @Override
    public List<Product> getNewArrivals() {
        return productRepository.findTop10ByOrderByCreatedDateDesc();
    }

    @Override
    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findByStockQuantityLessThan(threshold);
    }

    @Override
    public List<Product> getProductsByPriceRange(Double min, Double max) {
        return productRepository.findByPriceBetween(min, max);
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        return productRepository.findByNameContainingIgnoreCase(keyword.trim());
    }

    // ==================== NEW METHOD - TOP SELLING PRODUCTS ====================

    @Override
    // In ProductService.java, update the getTopSellingProducts method:

    public List<Product> getTopSellingProducts(int limit) {
        try {
            // Use the native query with limit parameter
            return productRepository.findTopSellingProducts(limit);
        } catch (Exception e) {
            // Log the error
            System.err.println("Error fetching top selling products: " + e.getMessage());

            // Fallback implementation - return featured products
            List<Product> featured = productRepository.findByFeaturedTrue();
            if (featured.size() >= limit) {
                return featured.subList(0, limit);
            }

            // If not enough featured products, add organic products
            List<Product> organic = productRepository.findByOrganicTrue();
            List<Product> result = new ArrayList<>(featured);

            for (Product p : organic) {
                if (!result.contains(p) && result.size() < limit) {
                    result.add(p);
                }
            }

            // If still not enough, add random products
            if (result.size() < limit) {
                List<Product> all = productRepository.findAll();
                for (Product p : all) {
                    if (!result.contains(p) && result.size() < limit) {
                        result.add(p);
                    }
                }
            }

            return result.stream().limit(limit).collect(Collectors.toList());
        }
    }
    // ==================== STATISTICS ====================

    @Override
    public long getTotalProductCount() {
        return productRepository.count();
    }

    @Override
    public long countByCategory(Product.Category category) {
        return productRepository.countByCategory(category);
    }

    @Override
    public List<Object[]> getCategoryStatistics() {
        return productRepository.getCategoryStatistics();
    }

    @Override
    public Double getAveragePrice() {
        return productRepository.getAveragePrice();
    }

    @Override
    public Double getMinPrice() {
        return productRepository.getMinPrice();
    }

    @Override
    public Double getMaxPrice() {
        return productRepository.getMaxPrice();
    }

    @Override
    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }

    // ==================== DELETE ====================

    @Override
    public void deleteById(Long id) {
        if (!existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    public void deleteProduct(Long id) {
        deleteById(id);
    }

    @Override
    public void delete(Product product) {
        productRepository.delete(product);
    }

    // ==================== STOCK MANAGEMENT ====================

    @Override
    public Product updateStock(Long productId, int newQuantity) {
        Product product = findById(productId);
        product.setStockQuantity(newQuantity);
        return productRepository.save(product);
    }

    @Override
    public void decreaseStock(Long productId, int quantity) {
        Product product = findById(productId);
        int newStock = product.getStockQuantity() - quantity;
        if (newStock < 0) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }
        product.setStockQuantity(newStock);
        productRepository.save(product);
    }

    @Override
    public void increaseStock(Long productId, int quantity) {
        Product product = findById(productId);
        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);
    }

    @Override
    public boolean hasEnoughStock(Long productId, int requestedQuantity) {
        Product product = findById(productId);
        return product.getStockQuantity() >= requestedQuantity;
    }

    // ==================== PRICE MANAGEMENT ====================

    @Override
    public Product updatePrice(Long productId, Double newPrice) {
        Product product = findById(productId);
        product.setPrice(newPrice);
        return productRepository.save(product);
    }

    @Override
    public Product applyDiscount(Long productId, Double discountPercentage) {
        Product product = findById(productId);
        product.setDiscount(discountPercentage);
        return productRepository.save(product);
    }

    @Override
    public Product removeDiscount(Long productId) {
        Product product = findById(productId);
        product.setDiscount(0.0);
        return productRepository.save(product);
    }

    // ==================== FEATURE MANAGEMENT ====================

    @Override
    public Product toggleFeatured(Long productId) {
        Product product = findById(productId);
        product.setFeatured(!product.isFeatured());
        return productRepository.save(product);
    }

    @Override
    public Product toggleOrganic(Long productId) {
        Product product = findById(productId);
        product.setOrganic(!product.isOrganic());
        return productRepository.save(product);
    }

    @Override
    public Product toggleFreshDaily(Long productId) {
        Product product = findById(productId);
        product.setFreshDaily(!product.isFreshDaily());
        return productRepository.save(product);
    }

    // ==================== BULK OPERATIONS ====================

    @Override
    public List<Product> saveAll(List<Product> products) {
        return productRepository.saveAll(products);
    }

    @Override
    public void deleteAll(List<Long> ids) {
        for (Long id : ids) {
            deleteById(id);
        }
    }

}