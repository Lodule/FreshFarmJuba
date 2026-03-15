package com.example.freshfarmjuba.repository;

import com.example.freshfarmjuba.model.Order;
import com.example.freshfarmjuba.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Order findByOrderNumber(String orderNumber);

    List<Order> findByUserOrderByOrderDateDesc(User user);

    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

    List<Order> findByStatus(Order.OrderStatus status);

    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.orderDate BETWEEN :start AND :end")
    List<Order> findByStatusAndDateRange(
            @Param("status") Order.OrderStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC LIMIT :limit")
    List<Order> findTopByOrderByOrderDateDesc(@Param("limit") int limit);

    long countByStatus(Order.OrderStatus status);

    long countByOrderDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(o.total) FROM Order o WHERE o.status = 'DELIVERED'")
    Double getTotalRevenue();

    // FIXED: Use native query for today's order count
    @Query(value = "SELECT COUNT(*) FROM orders WHERE DATE(order_date) = CURRENT_DATE", nativeQuery = true)
    long getTodaysOrderCount();

    // Alternative: Parameterized version
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :start AND :end")
    long countOrdersForDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Monthly sales
    @Query(value = "SELECT EXTRACT(MONTH FROM order_date) as month, " +
            "EXTRACT(YEAR FROM order_date) as year, " +
            "SUM(total) as total " +
            "FROM orders " +
            "WHERE status = 'DELIVERED' " +
            "GROUP BY year, month " +
            "ORDER BY year DESC, month DESC",
            nativeQuery = true)
    List<Object[]> getMonthlySales();

    // Order statistics by status
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> getOrderStatistics();
}