package com.bedsheet.orders.repository;

import com.bedsheet.orders.model.CustomerOrder;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnalyticsRepository extends JpaRepository<CustomerOrder, Long> {

    @Query("""
            SELECT COUNT(o), COALESCE(SUM(o.totalSellingPrice), 0), COALESCE(SUM(o.grossProfit), 0),
                   COALESCE(SUM(o.netProfit), 0), COALESCE(SUM(o.discount), 0),
                   COALESCE(SUM(o.deliveryCharge), 0), COALESCE(SUM(o.codValue), 0)
            FROM CustomerOrder o
            WHERE (:from IS NULL OR o.createdAt >= :from)
            """)
    List<Object[]> summarizeOrders(@Param("from") LocalDateTime from);

    @Query(value = """
            SELECT DATE(o.created_at) AS day,
                   COUNT(DISTINCT o.id) AS order_count,
                   COALESCE(SUM(o.total_selling_price), 0),
                   COALESCE(SUM(o.net_profit), 0),
                   COALESCE(SUM(i.quantity), 0) AS items_sold
            FROM orders o
            LEFT JOIN order_items i ON o.id = i.order_id
            WHERE (:from IS NULL OR o.created_at >= :from)
            GROUP BY DATE(o.created_at)
            ORDER BY day
            """, nativeQuery = true)
    List<Object[]> dailyTrend(@Param("from") LocalDateTime from);

    @Query("""
            SELECT o.district, COUNT(o), COALESCE(SUM(o.totalSellingPrice), 0), COALESCE(SUM(o.netProfit), 0)
            FROM CustomerOrder o
            WHERE (:from IS NULL OR o.createdAt >= :from)
            GROUP BY o.district
            ORDER BY SUM(o.netProfit) DESC
            """)
    List<Object[]> byDistrict(@Param("from") LocalDateTime from);

    @Query("""
            SELECT o.nearestCity, COUNT(o), COALESCE(SUM(o.totalSellingPrice), 0), COALESCE(SUM(o.netProfit), 0)
            FROM CustomerOrder o
            WHERE (:from IS NULL OR o.createdAt >= :from)
            GROUP BY o.nearestCity
            ORDER BY SUM(o.netProfit) DESC
            """)
    List<Object[]> byCity(@Param("from") LocalDateTime from);

    @Query(value = """
            SELECT COALESCE(SUM(i.quantity), 0)
            FROM order_items i
            JOIN orders o ON o.id = i.order_id
            WHERE (:from IS NULL OR o.created_at >= :from)
            """, nativeQuery = true)
    Long totalItems(@Param("from") LocalDateTime from);

    @Query(value = """
            SELECT i.bedsheet_type,
                   COALESCE(SUM(i.quantity), 0),
                   COALESCE(SUM(i.line_profit), 0)
            FROM order_items i
            JOIN orders o ON o.id = i.order_id
            WHERE (:from IS NULL OR o.created_at >= :from)
            GROUP BY i.bedsheet_type
            ORDER BY SUM(i.line_profit) DESC
            """, nativeQuery = true)
    List<Object[]> byBedsheetType(@Param("from") LocalDateTime from);

    @Query(value = """
            SELECT i.bed_size,
                   COALESCE(SUM(i.quantity), 0),
                   COALESCE(SUM(i.line_selling_price), 0),
                   COALESCE(SUM(i.line_profit), 0)
            FROM order_items i
            JOIN orders o ON o.id = i.order_id
            WHERE (:from IS NULL OR o.created_at >= :from)
            GROUP BY i.bed_size
            ORDER BY SUM(i.quantity) DESC
            """, nativeQuery = true)
    List<Object[]> byBedSize(@Param("from") LocalDateTime from);

    @Query("""
            SELECT o.id, o.waybillNo, o.customerName, o.district, o.createdAt,
                   o.totalSellingPrice, o.netProfit, SIZE(o.items)
            FROM CustomerOrder o
            WHERE (:from IS NULL OR o.createdAt >= :from)
            ORDER BY o.createdAt DESC
            """)
    List<Object[]> recentOrders(@Param("from") LocalDateTime from, org.springframework.data.domain.Pageable pageable);
}
