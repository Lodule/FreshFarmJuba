package com.example.freshfarmjuba.controller;

import com.example.freshfarmjuba.model.Product;
import com.example.freshfarmjuba.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public String listProducts(Model model) {
        List<Product> products = productService.findAll();
        model.addAttribute("products", products);
        model.addAttribute("pageTitle", "Our Products - Fresh Farm Juba");
        return "products";
    }

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);

        // Get related products (same category, excluding current product)
        List<Product> relatedProducts = productService.getProductsByCategory(product.getCategory())
                .stream()
                .filter(p -> !p.getId().equals(id))
                .limit(4)
                .collect(Collectors.toList());

        model.addAttribute("relatedProducts", relatedProducts);

        return "product-detail";
    }

    @GetMapping("/category/{category}")
    public String filterByCategory(@PathVariable Product.Category category, Model model) {
        List<Product> products = productService.getProductsByCategory(category);
        model.addAttribute("products", products);
        model.addAttribute("currentCategory", category);
        model.addAttribute("pageTitle", category + " - Fresh Farm Juba");
        return "products";
    }

    @GetMapping("/organic")
    public String organicProducts(Model model) {
        List<Product> products = productService.getOrganicProducts();
        model.addAttribute("products", products);
        model.addAttribute("organicFilter", true);
        model.addAttribute("pageTitle", "Organic Products - Fresh Farm Juba");
        return "products";
    }

    @GetMapping("/featured")
    public String featuredProducts(Model model) {
        List<Product> products = productService.getFeaturedProducts();
        model.addAttribute("products", products);
        model.addAttribute("featuredFilter", true);
        model.addAttribute("pageTitle", "Featured Products - Fresh Farm Juba");
        return "products";
    }

    @GetMapping("/new")
    public String newArrivals(Model model) {
        List<Product> products = productService.getNewArrivals();
        model.addAttribute("products", products);
        model.addAttribute("newArrivals", true);
        model.addAttribute("pageTitle", "New Arrivals - Fresh Farm Juba");
        return "products";
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam String q, Model model) {
        List<Product> products = productService.searchProducts(q);
        model.addAttribute("products", products);
        model.addAttribute("searchQuery", q);
        model.addAttribute("resultCount", products.size());
        model.addAttribute("pageTitle", "Search Results: " + q);
        return "products";
    }
}