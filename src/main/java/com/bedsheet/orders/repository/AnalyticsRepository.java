package com.bedsheet.orders.repository;

import com.bedsheet.orders.model.CustomerOrder;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnalyticsRepository extends JpaRepository<CustomerOrder, Long> {

    @Query("""
            SELECT COUNT(o), COALESCE(SUM(o.codValue - o.deliveryCharge), 0), COALESCE(SUM(o.grossProfit), 0),
                   COALESCE(SUM(o.netProfit), 0), COALESCE(SUM(o.discount), 0),
                   COALESCE(SUM(o.deliveryCharge), 0), COALESCE(SUM(o.codValue), 0)
            FROM CustomerOrder o
            WHERE (:from IS NULL OR o.createdAt >= :from)
            """)
    List<Object[]> summarizeOrders(@Param("from") LocalDateTime from);

    @Query(value = """
            /* Use H2-friendly date expression and avoid alias name 'day' which
               can be interpreted as a function/reserved word. Use order_day
               so the same native query works on MySQL and H2 fallback. */
            SELECT daily.order_day,
                   COUNT(daily.order_id) AS order_count,
                   COALESCE(SUM(daily.revenue), 0) AS revenue,
                   COALESCE(SUM(daily.net_profit), 0) AS net_profit,
                   COALESCE(SUM(daily.items_sold), 0) AS items_sold
            FROM (
                SELECT CAST(o.created_at AS DATE) AS order_day,
                       o.id AS order_id,
                       (o.cod_value - o.delivery_charge) AS revenue,
                       o.net_profit AS net_profit,
                       COALESCE((SELECT SUM(i.quantity)
                                 FROM order_items i
                                 WHERE i.order_id = o.id), 0) AS items_sold
                FROM orders o
                WHERE (:from IS NULL OR o.created_at >= :from)
            ) daily
            GROUP BY daily.order_day
            ORDER BY daily.order_day
            """, nativeQuery = true)
    List<Object[]> dailyTrend(@Param("from") LocalDateTime from);

    @Query(value = """
            SELECT monthly.order_year, monthly.order_month,
                   COUNT(monthly.order_id) AS order_count,
                   COALESCE(SUM(monthly.revenue), 0) AS revenue,
                   COALESCE(SUM(monthly.net_profit), 0) AS net_profit,
                   COALESCE(SUM(monthly.items_sold), 0) AS items_sold
            FROM (
                SELECT YEAR(o.created_at) AS order_year,
                       MONTH(o.created_at) AS order_month,
                       o.id AS order_id,
                       (o.cod_value - o.delivery_charge) AS revenue,
                       o.net_profit AS net_profit,
                       COALESCE((SELECT SUM(i.quantity)
                                 FROM order_items i
                                 WHERE i.order_id = o.id), 0) AS items_sold
                FROM orders o
                WHERE (:from IS NULL OR o.created_at >= :from)
            ) monthly
            GROUP BY monthly.order_year, monthly.order_month
            ORDER BY monthly.order_year, monthly.order_month
            """, nativeQuery = true)
    List<Object[]> monthlyTrend(@Param("from") LocalDateTime from);

    @Query(value = """
            SELECT monthly.order_year, monthly.order_month,
                   COUNT(monthly.order_id) AS order_count,
                   COALESCE(SUM(monthly.items_sold), 0) AS items_sold,
                   COALESCE(SUM(monthly.revenue), 0) AS revenue,
                   COALESCE(SUM(monthly.gross_profit), 0) AS gross_profit,
                   COALESCE(SUM(monthly.net_profit), 0) AS net_profit,
                   COALESCE(SUM(monthly.cod_total), 0) AS cod_total
            FROM (
                SELECT YEAR(o.created_at) AS order_year,
                       MONTH(o.created_at) AS order_month,
                       o.id AS order_id,
                       (o.cod_value - o.delivery_charge) AS revenue,
                       o.gross_profit AS gross_profit,
                       o.net_profit AS net_profit,
                       o.cod_value AS cod_total,
                       COALESCE((SELECT SUM(i.quantity)
                                 FROM order_items i
                                 WHERE i.order_id = o.id), 0) AS items_sold
                FROM orders o
                WHERE (:from IS NULL OR o.created_at >= :from)
            ) monthly
            GROUP BY monthly.order_year, monthly.order_month
            ORDER BY monthly.order_year, monthly.order_month
            """, nativeQuery = true)
    List<Object[]> monthlySummary(@Param("from") LocalDateTime from);

    @Query(value = """
            SELECT monthly.order_year, monthly.order_month, monthly.district,
                   COUNT(monthly.order_id) AS order_count,
                   COALESCE(SUM(monthly.revenue), 0) AS revenue,
                   COALESCE(SUM(monthly.net_profit), 0) AS net_profit
            FROM (
                SELECT YEAR(o.created_at) AS order_year,
                       MONTH(o.created_at) AS order_month,
                       o.id AS order_id,
                       o.district AS district,
                       (o.cod_value - o.delivery_charge) AS revenue,
                       o.net_profit AS net_profit
                FROM orders o
                WHERE (:from IS NULL OR o.created_at >= :from)
            ) monthly
            GROUP BY monthly.order_year, monthly.order_month, monthly.district
            ORDER BY monthly.order_year, monthly.order_month, SUM(monthly.net_profit) DESC
            """, nativeQuery = true)
    List<Object[]> monthlyByDistrict(@Param("from") LocalDateTime from);

    @Query(value = """
            SELECT monthly.order_year, monthly.order_month, monthly.nearest_city,
                   COUNT(monthly.order_id) AS order_count,
                   COALESCE(SUM(monthly.revenue), 0) AS revenue,
                   COALESCE(SUM(monthly.net_profit), 0) AS net_profit
            FROM (
                SELECT YEAR(o.created_at) AS order_year,
                       MONTH(o.created_at) AS order_month,
                       o.id AS order_id,
                       o.nearest_city AS nearest_city,
                       (o.cod_value - o.delivery_charge) AS revenue,
                       o.net_profit AS net_profit
                FROM orders o
                WHERE (:from IS NULL OR o.created_at >= :from)
            ) monthly
            GROUP BY monthly.order_year, monthly.order_month, monthly.nearest_city
            ORDER BY monthly.order_year, monthly.order_month, SUM(monthly.net_profit) DESC
            """, nativeQuery = true)
    List<Object[]> monthlyByCity(@Param("from") LocalDateTime from);

    @Query(value = """
            SELECT monthly.order_year, monthly.order_month, monthly.bedsheet_type,
                   COALESCE(SUM(monthly.quantity), 0) AS quantity,
                   COALESCE(SUM(monthly.line_profit), 0) AS profit
            FROM (
                SELECT YEAR(o.created_at) AS order_year,
                       MONTH(o.created_at) AS order_month,
                       i.bedsheet_type AS bedsheet_type,
                       i.quantity AS quantity,
                       i.line_profit AS line_profit
                FROM order_items i
                JOIN orders o ON o.id = i.order_id
                WHERE (:from IS NULL OR o.created_at >= :from)
            ) monthly
            GROUP BY monthly.order_year, monthly.order_month, monthly.bedsheet_type
            ORDER BY monthly.order_year, monthly.order_month, SUM(monthly.line_profit) DESC
            """, nativeQuery = true)
    List<Object[]> monthlyByBedsheetType(@Param("from") LocalDateTime from);

    @Query(value = """
            SELECT monthly.order_year, monthly.order_month, monthly.bed_size,
                   COALESCE(SUM(monthly.quantity), 0) AS quantity,
                   COALESCE(SUM(monthly.line_selling_price), 0) AS revenue,
                   COALESCE(SUM(monthly.line_profit), 0) AS profit
            FROM (
                SELECT YEAR(o.created_at) AS order_year,
                       MONTH(o.created_at) AS order_month,
                       i.bed_size AS bed_size,
                       i.quantity AS quantity,
                       i.line_selling_price AS line_selling_price,
                       i.line_profit AS line_profit
                FROM order_items i
                JOIN orders o ON o.id = i.order_id
                WHERE (:from IS NULL OR o.created_at >= :from)
            ) monthly
            GROUP BY monthly.order_year, monthly.order_month, monthly.bed_size
            ORDER BY monthly.order_year, monthly.order_month, SUM(monthly.line_profit) DESC
            """, nativeQuery = true)
    List<Object[]> monthlyByBedSize(@Param("from") LocalDateTime from);

    @Query("""
            SELECT o.district, COUNT(o), COALESCE(SUM(o.codValue - o.deliveryCharge), 0), COALESCE(SUM(o.netProfit), 0)
            FROM CustomerOrder o
            WHERE (:from IS NULL OR o.createdAt >= :from)
            GROUP BY o.district
            ORDER BY SUM(o.netProfit) DESC
            """)
    List<Object[]> byDistrict(@Param("from") LocalDateTime from);

    @Query("""
            SELECT o.nearestCity, COUNT(o), COALESCE(SUM(o.codValue - o.deliveryCharge), 0), COALESCE(SUM(o.netProfit), 0)
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

    @Query(value = """
            SELECT i.bedding_design,
                   COALESCE(SUM(i.quantity), 0) AS total_qty,
                   COALESCE(SUM(i.line_selling_price), 0) AS total_revenue,
                   COALESCE(SUM(i.line_profit), 0) AS total_profit
            FROM order_items i
            JOIN orders o ON o.id = i.order_id
            WHERE (:from IS NULL OR o.created_at >= :from)
            GROUP BY i.bedding_design
            ORDER BY SUM(i.quantity) DESC
            """, nativeQuery = true)
    List<Object[]> byBeddingDesign(@Param("from") LocalDateTime from);

    @Query("""
            SELECT o.id, o.waybillNo, o.customerName, o.district, o.createdAt,
                   (o.codValue - o.deliveryCharge), o.netProfit, SIZE(o.items)
            FROM CustomerOrder o
            WHERE (:from IS NULL OR o.createdAt >= :from)
            ORDER BY o.createdAt DESC
            """)
    List<Object[]> recentOrders(@Param("from") LocalDateTime from, org.springframework.data.domain.Pageable pageable);
}
