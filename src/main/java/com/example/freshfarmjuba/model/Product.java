package com.example.freshfarmjuba.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @Column(name = "is_organic")
    private boolean organic = false;

    @Column(name = "fresh_daily")
    private boolean freshDaily = false;

    @Column(name = "is_featured")
    private boolean featured = false;

    @Column(name = "unit")
    private String unit = "kg";

    @Column(name = "discount")
    private Double discount = 0.0;

    @Column(name = "rating")
    private Double rating = 0.0;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;



    public enum Category {
        POULTRY, VEGETABLES, FRUITS, ROOTS, HERBS, EGGS, OTHER
    }

    // Helper method to get discounted price
    public Double getDiscountedPrice() {
        if (discount != null && discount > 0) {
            return price - (price * discount / 100);
        }
        return price;
    }

    // Helper method for template compatibility (maps stock to stockQuantity)
    public Integer getStock() {
        return stockQuantity;
    }

    // Helper method for template compatibility (returns original price if discounted)
    public Double getOldPrice() {
        if (discount != null && discount > 0) {
            return price;
        }
        return null;
    }

    // Helper method to check if in stock
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }
}
