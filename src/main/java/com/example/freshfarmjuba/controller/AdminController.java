package com.example.freshfarmjuba.controller;

import com.example.freshfarmjuba.model.Message;
import com.example.freshfarmjuba.model.Order;
import com.example.freshfarmjuba.model.Product;
import com.example.freshfarmjuba.model.User;
import com.example.freshfarmjuba.repository.MessageRepository;
import com.example.freshfarmjuba.service.OrderService;
import com.example.freshfarmjuba.service.ProductService;
import com.example.freshfarmjuba.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageRepository messageRepository;

    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Statistics
        long totalOrders = orderService.findAll().size();
        long pendingOrders = orderService.getPendingOrdersCount();
        long totalProducts = productService.findAll().size();
        long totalUsers = userService.findAll().size();
        BigDecimal totalRevenue = orderService.getTotalRevenue();

        // Recent orders
        List<Order> recentOrders = orderService.getRecentOrders(5);
        List<Message> recentMessages = messageRepository.findAllByOrderByCreatedAtDesc();
        if (recentMessages.size() > 5) {
            recentMessages = recentMessages.subList(0, 5);
        }

        model.addAttribute("orderCount", totalOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("productCount", totalProducts);
        model.addAttribute("userCount", totalUsers);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("recentMessages", recentMessages);

        return "admin/dashboard";
    }

    // ==================== PRODUCT MANAGEMENT ====================

    @GetMapping("/products")
    public String listProducts(Model model) {
        List<Product> products = productService.findAll();
        model.addAttribute("products", products);
        return "admin/products";
    }

    @GetMapping("/products/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", Product.Category.values());
        return "admin/addproduct";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product,
                              RedirectAttributes redirectAttributes) {
        try {
            productService.save(product);
            redirectAttributes.addFlashAttribute("successMessage", "Product saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving product: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", Product.Category.values());
        return "admin/addproduct"; // Reuse addproduct for editing
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            productService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting product: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    // ==================== ORDER MANAGEMENT ====================

    @GetMapping("/orders")
    public String listOrders(Model model) {
        List<Order> orders = orderService.findAll();
        model.addAttribute("orders", orders);
        return "admin/orders";
    }

    @PostMapping("/orders/update-status")
    public String updateOrderStatus(@RequestParam Long orderId,
                                    @RequestParam Order.OrderStatus status,
                                    RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(orderId, status);
            redirectAttributes.addFlashAttribute("successMessage", "Order status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating order: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    // ==================== CUSTOMER MANAGEMENT ====================

    @GetMapping("/customers")
    public String listCustomers(Model model) {
        List<User> customers = userService.findAll();
        model.addAttribute("customers", customers);
        model.addAttribute("totalCustomers", customers.size());
        model.addAttribute("newCustomersToday", 5); // Mock data for now
        return "admin/customers";
    }

    @PostMapping("/customers/toggle-status/{id}")
    public String toggleCustomerStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "User status updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/customers";
    }

    @GetMapping("/customers/view/{id}")
    public String viewCustomer(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("customer", user);
        // Could add orders for this specific customer
        return "admin/customer-details";
    }

    // ==================== MESSAGES ====================

    @GetMapping("/messages")
    public String listMessages(Model model) {
        model.addAttribute("messages", messageRepository.findAllByOrderByCreatedAtDesc());
        return "admin/messages";
    }

    @PostMapping("/messages/read/{id}")
    @ResponseBody
    public ResponseEntity<?> markMessageAsRead(@PathVariable Long id) {
        messageRepository.findById(id).ifPresent(msg -> {
            msg.setRead(true);
            messageRepository.save(msg);
        });
        return ResponseEntity.ok().build();
    }

    @GetMapping("/messages/delete/{id}")
    public String deleteMessage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        messageRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Message deleted.");
        return "redirect:/admin/messages";
    }

    // ==================== REPORTS ====================

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("monthlySales", orderService.getMonthlySales());
        model.addAttribute("topProducts", productService.getTopSellingProducts(5));
        model.addAttribute("orderStats", orderService.getOrderStatistics());
        model.addAttribute("totalRevenue", orderService.getTotalRevenue());
        model.addAttribute("totalOrders", orderService.findAll().size());
        return "admin/reports";
    }

    // ==================== SETTINGS ====================

    @GetMapping("/settings")
    public String settings(Model model) {
        // Mock settings or system info
        model.addAttribute("systemStatus", "Healthy");
        model.addAttribute("appVersion", "1.0.0-PRO");
        return "admin/settings";
    }
}
