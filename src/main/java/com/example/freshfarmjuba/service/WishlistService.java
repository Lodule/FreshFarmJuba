package com.example.freshfarmjuba.service;

import com.example.freshfarmjuba.model.Product;
import com.example.freshfarmjuba.model.User;
import com.example.freshfarmjuba.model.Wishlist;

import java.util.List;

public interface WishlistService {
    void addToWishlist(User user, Long productId);
    void removeFromWishlist(User user, Long productId);
    List<Wishlist> getUserWishlist(User user);
    boolean isInWishlist(User user, Product product);
    long getWishlistCount(User user);
    void clearWishlist(User user);
}
