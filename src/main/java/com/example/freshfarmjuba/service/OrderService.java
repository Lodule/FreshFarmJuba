package com.example.freshfarmjuba.service;

import com.example.freshfarmjuba.model.Order;
import com.example.freshfarmjuba.model.OrderItem;
import com.example.freshfarmjuba.model.User;
import com.example.freshfarmjuba.repository.OrderRepository;
import com.example.freshfarmjuba.repository.OrderItemRepository;
import com.example.freshfarmjuba.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    /**
     * Save an order
     */
    @Transactional
    public Order save(Order order) {
        // Generate order number if not set
        if (order.getOrderNumber() == null || order.getOrderNumber().isEmpty()) {
            order.setOrderNumber(generateOrderNumber());
        }

        // Set order date if not set
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }

        // Calculate totals if needed
        if (order.getTotal() == null || order.getTotal().compareTo(BigDecimal.ZERO) == 0) {
            order.calculateTotal();
        }

        // Save the order
        Order savedOrder = orderRepository.save(order);

        // Send confirmation email if user exists
        if (savedOrder.getUser() != null && savedOrder.getUser().getEmail() != null) {
            emailService.sendOrderConfirmationEmail(
                    savedOrder.getUser(),
                    savedOrder.getOrderNumber(),
                    savedOrder.getTotal().doubleValue()
            );
        }

        return savedOrder;
    }

    /**
     * Find order by ID
     */
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    /**
     * Find order by order number
     */
    public Order findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    /**
     * Get all orders
     */
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    /**
     * Get orders by user
     */
    public List<Order> findByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    /**
     * Get orders by user ID
     */


    /**
     * Get orders by status
     */
    public List<Order> findByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * Update order status
     */
    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        order.setStatus(newStatus);

        // Send status update email if user exists
        if (order.getUser() != null && order.getUser().getEmail() != null) {
            emailService.sendOrderStatusUpdateEmail(
                    order.getUser(),
                    order.getOrderNumber(),
                    newStatus.toString()
            );
        }

        return orderRepository.save(order);
    }

    /**
     * Cancel order
     */
    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Check if order can be cancelled (only PENDING or PROCESSING)
        if (order.getStatus() == Order.OrderStatus.PENDING ||
                order.getStatus() == Order.OrderStatus.PROCESSING) {
            order.setStatus(Order.OrderStatus.CANCELLED);
        } else {
            throw new RuntimeException("Order cannot be cancelled in its current state: " + order.getStatus());
        }

        return orderRepository.save(order);
    }

    /**
     * Get orders within date range
     */
    public List<Order> getOrdersBetweenDates(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByOrderDateBetween(start, end);
    }

    /**
     * Get recent orders
     */
    public List<Order> getRecentOrders(int limit) {
        return orderRepository.findTopByOrderByOrderDateDesc(limit);
    }

    /**
     * Get total revenue
     */
    public BigDecimal getTotalRevenue() {
        List<Order> completedOrders = orderRepository.findByStatus(Order.OrderStatus.DELIVERED);
        return completedOrders.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get today's orders count
     */


    /**
     * Get pending orders count
     */
    public long getPendingOrdersCount() {
        return orderRepository.countByStatus(Order.OrderStatus.PENDING);
    }

    /**
     * Delete order (admin only)
     */
    @Transactional
    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }

    /**
     * Generate unique order number
     */
    private String generateOrderNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(7);
        String random = String.valueOf((int)(Math.random() * 1000));
        return "FFJ-" + timestamp + "-" + random;
    }
    // In OrderService.java, update the getTodaysOrderCount method:

    public long getTodaysOrderCount() {
        return orderRepository.getTodaysOrderCount();
    }

    // Or use the alternative method with date range:
    public long getTodaysOrderCountAlternative() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        return orderRepository.countByOrderDateBetween(startOfDay, endOfDay);
    }
    // File: src/main/java/com/example/freshfarmjuba/service/OrderService.java
// Add these missing methods:

// For orderService.findAll() - already exists
// For orderService.getPendingOrdersCount() - already exists
// For orderService.getTotalRevenue() - already exists
// For orderService.getRecentOrders(5) - already exists

    // Add this method for findByUserId
    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId);
    }

    // Add this method for getMonthlySales()
    public Map<String, Object> getMonthlySales() {
        List<Object[]> results = orderRepository.getMonthlySales();
        Map<String, Object> monthlySales = new HashMap<>();

        for (Object[] result : results) {
            Integer month = (Integer) result[0];
            Integer year = (Integer) result[1];
            BigDecimal total = (BigDecimal) result[2];
            monthlySales.put(year + "-" + month, total);
        }

        return monthlySales;
    }

    // Add this method for getOrderStatistics()
    public Map<Order.OrderStatus, Long> getOrderStatistics() {
        List<Object[]> results = orderRepository.getOrderStatistics();
        Map<Order.OrderStatus, Long> stats = new EnumMap<>(Order.OrderStatus.class);

        for (Object[] result : results) {
            Order.OrderStatus status = (Order.OrderStatus) result[0];
            Long count = (Long) result[1];
            stats.put(status, count);
        }

        return stats;
    }
}