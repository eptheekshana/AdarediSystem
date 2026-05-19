package com.bedsheet.orders.service;

import com.bedsheet.orders.dto.dashboard.DailyMetric;
import com.bedsheet.orders.dto.dashboard.DashboardView;
import com.bedsheet.orders.dto.dashboard.LabelMetric;
import com.bedsheet.orders.dto.dashboard.ProductTypeMetric;
import com.bedsheet.orders.dto.dashboard.RecentOrderRow;
import com.bedsheet.orders.repository.AnalyticsRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final AnalyticsRepository analyticsRepository;

    public DashboardService(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    @Transactional(readOnly = true)
    public DashboardView buildDashboard(int periodDays) {
        LocalDateTime from = periodDays > 0
                ? LocalDateTime.now().minusDays(periodDays).with(LocalTime.MIN)
                : null;

        DashboardView view = new DashboardView();
        view.setPeriodDays(periodDays);
        applySummary(view, from);
        view.setDailyTrend(mapDaily(analyticsRepository.dailyTrend(from)));
        view.setByDistrict(mapLabels(analyticsRepository.byDistrict(from)));
        view.setByCity(mapLabels(analyticsRepository.byCity(from)).stream().limit(8).toList());
        view.setByBedsheetType(mapTypes(analyticsRepository.byBedsheetType(from)));
        view.setByBedSize(mapLabelsFromBedSize(analyticsRepository.byBedSize(from)));
        view.setRecentOrders(mapRecent(analyticsRepository.recentOrders(from, PageRequest.of(0, 10))));
        return view;
    }

    private void applySummary(DashboardView view, LocalDateTime from) {
        List<Object[]> rows = analyticsRepository.summarizeOrders(from);
        if (rows.isEmpty()) {
            return;
        }
        Object[] row = rows.get(0);
        long orders = ((Number) row[0]).longValue();
        BigDecimal revenue = toBigDecimal(row[1]);
        BigDecimal gross = toBigDecimal(row[2]);
        BigDecimal net = toBigDecimal(row[3]);
        BigDecimal discount = toBigDecimal(row[4]);
        BigDecimal delivery = toBigDecimal(row[5]);
        BigDecimal cod = toBigDecimal(row[6]);

        view.setTotalOrders(orders);
        view.setTotalRevenue(revenue);
        view.setGrossProfit(gross);
        view.setNetProfit(net);
        view.setTotalDiscount(discount);
        view.setTotalDeliveryCharge(delivery);
        view.setTotalCod(cod);
        view.setTotalItems(analyticsRepository.totalItems(from));

        if (orders > 0) {
            view.setAvgOrderValue(revenue.divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP));
            view.setAvgNetProfit(net.divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP));
        }
    }

    private List<DailyMetric> mapDaily(List<Object[]> rows) {
        List<DailyMetric> metrics = new ArrayList<>();
        for (Object[] row : rows) {
            LocalDate date = toLocalDate(row[0]);
            long orders = ((Number) row[1]).longValue();
            java.math.BigDecimal revenue = toBigDecimal(row[2]);
            java.math.BigDecimal net = toBigDecimal(row[3]);
            long items = row.length > 4 && row[4] != null ? ((Number) row[4]).longValue() : 0L;
            metrics.add(new DailyMetric(date, orders, revenue, net, items));
        }
        return metrics;
    }

    private List<LabelMetric> mapLabels(List<Object[]> rows) {
        List<LabelMetric> metrics = new ArrayList<>();
        for (Object[] row : rows) {
            metrics.add(new LabelMetric(
                    String.valueOf(row[0]),
                    ((Number) row[1]).longValue(),
                    toBigDecimal(row[2]),
                    toBigDecimal(row[3])));
        }
        return metrics;
    }

    private List<LabelMetric> mapLabelsFromBedSize(List<Object[]> rows) {
        List<LabelMetric> metrics = new ArrayList<>();
        for (Object[] row : rows) {
            String size = String.valueOf(row[0]).replace('_', ' ');
            metrics.add(new LabelMetric(
                    size,
                    ((Number) row[1]).longValue(),
                    toBigDecimal(row[2]),
                    toBigDecimal(row[3])));
        }
        return metrics;
    }

    private List<ProductTypeMetric> mapTypes(List<Object[]> rows) {
        List<ProductTypeMetric> metrics = new ArrayList<>();
        for (Object[] row : rows) {
            metrics.add(new ProductTypeMetric(
                    String.valueOf(row[0]),
                    ((Number) row[1]).longValue(),
                    toBigDecimal(row[2])));
        }
        return metrics;
    }

    private List<RecentOrderRow> mapRecent(List<Object[]> rows) {
        List<RecentOrderRow> recent = new ArrayList<>();
        for (Object[] row : rows) {
            RecentOrderRow orderRow = new RecentOrderRow();
            orderRow.setId(((Number) row[0]).longValue());
            orderRow.setWaybillNo(String.valueOf(row[1]));
            orderRow.setCustomerName(String.valueOf(row[2]));
            orderRow.setDistrict(String.valueOf(row[3]));
            orderRow.setCreatedAt((LocalDateTime) row[4]);
            orderRow.setTotalSellingPrice(toBigDecimal(row[5]));
            orderRow.setNetProfit(toBigDecimal(row[6]));
            orderRow.setItemCount(((Number) row[7]).intValue());
            recent.add(orderRow);
        }
        return recent;
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return new BigDecimal(value.toString());
    }
}
