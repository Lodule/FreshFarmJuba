package com.example.freshfarmjuba.controller;

import com.example.freshfarmjuba.model.User;
import com.example.freshfarmjuba.model.Wishlist;
import com.example.freshfarmjuba.service.UserService;
import com.example.freshfarmjuba.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String viewWishlist(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        List<Wishlist> wishlistItems = wishlistService.getUserWishlist(user);
        model.addAttribute("wishlistItems", wishlistItems);
        return "wishlist";
    }

    @PostMapping("/add/{productId}")
    public String addToWishlist(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable Long productId,
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        try {
            wishlistService.addToWishlist(user, productId);
            redirectAttributes.addFlashAttribute("successMessage", "Product added to wishlist!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add product: " + e.getMessage());
        }
        return "redirect:/products";
    }

    @PostMapping("/remove/{productId}")
    public String removeFromWishlist(@AuthenticationPrincipal UserDetails userDetails,
                                     @PathVariable Long productId,
                                     RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        wishlistService.removeFromWishlist(user, productId);
        redirectAttributes.addFlashAttribute("successMessage", "Product removed from wishlist.");
        return "redirect:/wishlist";
    }
}
