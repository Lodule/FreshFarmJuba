package com.example.freshfarmjuba.controller;

import com.example.freshfarmjuba.model.Order;
import com.example.freshfarmjuba.model.User;
import com.example.freshfarmjuba.service.OrderService;
import com.example.freshfarmjuba.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/dashboard")
    public String userDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userService.findByEmail(userDetails.getUsername());
        List<Order> orders = orderService.findByUser(user);
        
        // Calculate stats
        long orderCount = orders.size();
        BigDecimal totalSpent = orders.stream()
                .filter(o -> o.getStatus() != Order.OrderStatus.CANCELLED)
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get 5 most recent orders
        List<Order> recentOrders = orders.size() > 5 ? orders.subList(0, 5) : orders;

        model.addAttribute("user", user);
        model.addAttribute("orders", orders);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("totalSpent", totalSpent);

        return "user/dashboard";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        model.addAttribute("user", user);
        return "user/profile";
    }
}
