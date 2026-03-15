package com.example.freshfarmjuba.controller;

import com.example.freshfarmjuba.model.Order;
import com.example.freshfarmjuba.model.OrderItem;
import com.example.freshfarmjuba.model.Product;
import com.example.freshfarmjuba.model.User;
import com.example.freshfarmjuba.service.OrderService;
import com.example.freshfarmjuba.service.ProductService;
import com.example.freshfarmjuba.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String showOrderForm(@RequestParam(required = false) Long productId, Model model) {
        if (productId != null) {
            Product product = productService.findById(productId);
            model.addAttribute("selectedProduct", product);
        }

        List<Product> products = productService.findAll();
        model.addAttribute("products", products);
        model.addAttribute("order", new Order());

        return "order";
    }

    @GetMapping("/my")
    public String myOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        List<Order> orders = orderService.findByUser(user);
        model.addAttribute("orders", orders);
        return "my-orders";
    }

    @PostMapping
    public String placeOrder(@AuthenticationPrincipal UserDetails userDetails,
                             @ModelAttribute Order order,
                             @RequestParam Long productId,
                             @RequestParam Integer quantity,
                             @RequestParam(required = false) String deliveryOption,
                             @RequestParam(required = false) String paymentMethod,
                             @RequestParam(required = false) String orderNotes,
                             RedirectAttributes redirectAttributes) {

        try {
            // Get the user if logged in
            if (userDetails != null) {
                User user = userService.findByEmail(userDetails.getUsername());
                order.setUser(user);
                order.setCustomerName(user.getFullName());
                order.setCustomerEmail(user.getEmail());
                order.setCustomerPhone(user.getPhone());
                order.setDeliveryAddress(user.getAddress());
            }

            // Get the product
            Product product = productService.findById(productId);

            // Set order details
            order.setOrderNumber(generateOrderNumber());
            order.setOrderDate(LocalDateTime.now());
            order.setStatus(Order.OrderStatus.PENDING);

            // Calculate amounts
            BigDecimal productPrice = BigDecimal.valueOf(product.getPrice());
            BigDecimal subtotal = productPrice.multiply(BigDecimal.valueOf(quantity));

            order.setSubtotal(subtotal);
            order.setDeliveryFee(deliveryOption != null && deliveryOption.equals("express") ?
                    BigDecimal.valueOf(15.00) : BigDecimal.valueOf(5.00));
            order.setTax(subtotal.multiply(BigDecimal.valueOf(0.05))); // 5% tax

            BigDecimal total = order.getSubtotal()
                    .add(order.getDeliveryFee())
                    .add(order.getTax());
            order.setTotal(total);

            // Set payment info
            if (paymentMethod != null) {
                switch (paymentMethod) {
                    case "cod":
                        order.setPaymentMethod(Order.PaymentMethod.CASH_ON_DELIVERY);
                        break;
                    case "mobile":
                        order.setPaymentMethod(Order.PaymentMethod.MOBILE_MONEY);
                        break;
                    case "bank":
                        order.setPaymentMethod(Order.PaymentMethod.BANK_TRANSFER);
                        break;
                    case "card":
                        order.setPaymentMethod(Order.PaymentMethod.CARD);
                        break;
                }
            }

            order.setNotes(orderNotes);

            // Create order item
            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setProductImage(product.getImageUrl());
            item.setQuantity(quantity);
            item.setPrice(productPrice);
            item.setProductCategory(product.getCategory() != null ? product.getCategory().toString() : "OTHER");
            item.setOrganic(product.isOrganic());
            item.setFreshDaily(product.isFreshDaily());
            item.setOrder(order);
            item.calculateSubtotal();

            order.addItem(item);
            order.setItemCount(1);

            // Save order
            orderService.save(order);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Order placed successfully! Your order number is: " + order.getOrderNumber());

            return "redirect:/order/success";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to place order: " + e.getMessage());
            return "redirect:/order?error";
        }
    }

    @GetMapping("/success")
    public String orderSuccess(Model model) {
        return "success";
    }

    @GetMapping("/track")
    public String trackOrder(@RequestParam String orderNumber, Model model) {
        Order order = orderService.findByOrderNumber(orderNumber);
        model.addAttribute("order", order);
        return "order-tracking";
    }

    private String generateOrderNumber() {
        return "FFJ-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
}
