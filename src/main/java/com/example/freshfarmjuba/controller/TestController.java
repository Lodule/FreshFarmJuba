package com.example.freshfarmjuba.controller;

import com.example.freshfarmjuba.model.User;
import com.example.freshfarmjuba.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/test-pages")
    @ResponseBody
    public Map<String, String> testPages() {
        Map<String, String> pages = new HashMap<>();
        pages.put("/", "index.html");
        pages.put("/index", "index.html");
        pages.put("/home", "index.html");
        pages.put("/products", "products.html");
        pages.put("/contact", "contact.html");
        pages.put("/login", "login.html");
        pages.put("/register", "register.html");
        pages.put("/cart", "cart.html");
        pages.put("/order", "order.html");
        pages.put("/order/success", "success.html");
        pages.put("/admin/dashboard", "admin/dashboard.html");
        pages.put("/admin/products", "admin/products.html");
        pages.put("/admin/products/add", "admin/addproduct.html");
        pages.put("/admin/orders", "admin/orders.html");
        return pages;
    }

    @GetMapping("/debug")
    @ResponseBody
    public String debug() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Debug Information</h1>");
        sb.append("<ul>");
        sb.append("<li>Java Version: ").append(System.getProperty("java.version")).append("</li>");
        sb.append("<li>Working Directory: ").append(System.getProperty("user.dir")).append("</li>");
        sb.append("<li>Context Path: ").append("/").append("</li>");
        sb.append("</ul>");
        return sb.toString();
    }

    @GetMapping("/test-login")
    @ResponseBody
    public String testLogin(@RequestParam(required = false, defaultValue = "admin@freshfarmjuba.com") String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return "User not found in database: " + email;
        }
        
        boolean passMatch = passwordEncoder.matches("admin123", user.getPassword());
        
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>User Found!</h1>");
        sb.append("<p>Email: ").append(user.getEmail()).append("</p>");
        sb.append("<p>Roles: ").append(user.getRoles()).append("</p>");
        sb.append("<p>Active: ").append(user.getActive()).append("</p>");
        sb.append("<p>Account Non Locked: ").append(user.isAccountNonLocked()).append("</p>");
        sb.append("<p>Password 'admin123' match: ").append(passMatch).append("</p>");
        sb.append("<hr>");
        sb.append("<p>Raw password from DB: ").append(user.getPassword()).append("</p>");

        return sb.toString();
    }

    @GetMapping("/reset-admin")
    @ResponseBody
    public String resetAdmin() {
        String email = "admin@freshfarmjuba.com";
        User user = userRepository.findByEmail(email);
        
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setFirstName("Admin");
            user.setLastName("User");
            user.setPhone("+211981502973");
            user.setCreatedAt(LocalDateTime.now());
            user.setAccountType("admin");
        }
        
        user.setPassword(passwordEncoder.encode("admin123"));
        user.setRoles("ROLE_ADMIN");
        user.setActive(true);
        user.setEmailVerified(true);
        user.setAccountNonLocked(true);
        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        user.setFailedAttempts(0);
        
        userRepository.save(user);
        
        return "Admin user reset successfully! Try logging in now with admin@freshfarmjuba.com and admin123.";
    }
}