package com.example.freshfarmjuba.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Map URLs to view names - only for static views without controller logic
        registry.addViewController("/checkout").setViewName("checkout");
        registry.addViewController("/admin/dashboard").setViewName("admin/dashboard");
        registry.addViewController("/admin/products").setViewName("admin/products");
        registry.addViewController("/admin/products/add").setViewName("admin/addproduct");
        registry.addViewController("/admin/orders").setViewName("admin/orders");
        registry.addViewController("/admin/messages").setViewName("admin/messages");
    }
}
