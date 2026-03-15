package com.example.freshfarmjuba.repository;

import com.example.freshfarmjuba.model.Product;
import com.example.freshfarmjuba.model.User;
import com.example.freshfarmjuba.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUser(User user);
    Optional<Wishlist> findByUserAndProduct(User user, Product product);
    void deleteByUserAndProduct(User user, Product product);
    boolean existsByUserAndProduct(User user, Product product);
    long countByUser(User user);
}
