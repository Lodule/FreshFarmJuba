package com.example.freshfarmjuba.serviceimpl;

import com.example.freshfarmjuba.model.Product;
import com.example.freshfarmjuba.model.User;
import com.example.freshfarmjuba.model.Wishlist;
import com.example.freshfarmjuba.repository.ProductRepository;
import com.example.freshfarmjuba.repository.WishlistRepository;
import com.example.freshfarmjuba.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistServiceImpl implements WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public void addToWishlist(User user, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (!wishlistRepository.existsByUserAndProduct(user, product)) {
            Wishlist wishlist = new Wishlist();
            wishlist.setUser(user);
            wishlist.setProduct(product);
            wishlistRepository.save(wishlist);
        }
    }

    @Override
    @Transactional
    public void removeFromWishlist(User user, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        wishlistRepository.deleteByUserAndProduct(user, product);
    }

    @Override
    public List<Wishlist> getUserWishlist(User user) {
        return wishlistRepository.findByUser(user);
    }

    @Override
    public boolean isInWishlist(User user, Product product) {
        return wishlistRepository.existsByUserAndProduct(user, product);
    }

    @Override
    public long getWishlistCount(User user) {
        return wishlistRepository.countByUser(user);
    }

    @Override
    @Transactional
    public void clearWishlist(User user) {
        List<Wishlist> wishlistItems = wishlistRepository.findByUser(user);
        wishlistRepository.deleteAll(wishlistItems);
    }
}
