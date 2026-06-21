package com.bedsheet.orders.service;

import com.bedsheet.orders.dto.dashboard.DailyMetric;
import com.bedsheet.orders.dto.dashboard.DashboardView;
import com.bedsheet.orders.dto.dashboard.DesignMetric;
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
        populateCurrentMonthSummary(view);
        view.setDailyTrend(mapDaily(analyticsRepository.dailyTrend(from)));
        view.setByDistrict(mapLabels(analyticsRepository.byDistrict(from)));
        view.setByCity(mapLabels(analyticsRepository.byCity(from)).stream().limit(8).toList());
        view.setByBedsheetType(mapTypes(analyticsRepository.byBedsheetType(from)));
        view.setByBedSize(mapLabelsFromBedSize(analyticsRepository.byBedSize(from)));
        view.setByBeddingDesign(mapDesigns(analyticsRepository.byBeddingDesign(from)).stream().limit(20).toList());
        view.setRecentOrders(mapRecent(analyticsRepository.recentOrders(from, PageRequest.of(0, 10))));
        return view;
    }

    private void populateCurrentMonthSummary(DashboardView view) {
        LocalDateTime monthFrom = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<Object[]> rows = analyticsRepository.summarizeOrders(monthFrom);
        if (rows.isEmpty()) {
            view.setCurrentMonthOrders(0L);
            view.setCurrentMonthItems(0L);
            view.setCurrentMonthRevenue(BigDecimal.ZERO);
            view.setCurrentMonthGrossProfit(BigDecimal.ZERO);
            view.setCurrentMonthNetProfit(BigDecimal.ZERO);
            view.setCurrentMonthCodTotal(BigDecimal.ZERO);
            view.setCurrentMonthAvgOrderValue(BigDecimal.ZERO);
            view.setCurrentMonthAvgProfitPerOrder(BigDecimal.ZERO);
            view.setCurrentMonthTopDistrict("—");
            view.setCurrentMonthTopCity("—");
            view.setCurrentMonthBestProductType("—");
            view.setCurrentMonthTopBedSize("—");
            return;
        }

        Object[] row = rows.get(0);
        long orders = ((Number) row[0]).longValue();
        BigDecimal revenue = toBigDecimal(row[1]);
        BigDecimal gross = toBigDecimal(row[2]);
        BigDecimal net = toBigDecimal(row[3]);
        BigDecimal cod = toBigDecimal(row[6]);
        Long items = analyticsRepository.totalItems(monthFrom);

        view.setCurrentMonthOrders(orders);
        view.setCurrentMonthItems(items == null ? 0L : items.longValue());
        view.setCurrentMonthRevenue(revenue);
        view.setCurrentMonthGrossProfit(gross);
        view.setCurrentMonthNetProfit(net);
        view.setCurrentMonthCodTotal(cod);
        if (orders > 0) {
            view.setCurrentMonthAvgOrderValue(revenue.divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP));
            view.setCurrentMonthAvgProfitPerOrder(net.divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP));
        } else {
            view.setCurrentMonthAvgOrderValue(BigDecimal.ZERO);
            view.setCurrentMonthAvgProfitPerOrder(BigDecimal.ZERO);
        }
        applyCurrentMonthTopMetrics(view, monthFrom);
    }

    private void applyCurrentMonthTopMetrics(DashboardView view, LocalDateTime from) {
        List<Object[]> districtRows = analyticsRepository.byDistrict(from);
        List<Object[]> cityRows = analyticsRepository.byCity(from);
        List<Object[]> typeRows = analyticsRepository.byBedsheetType(from);
        List<Object[]> bedRows = analyticsRepository.byBedSize(from);

        view.setCurrentMonthTopDistrict(districtRows.isEmpty() ? "—" : String.valueOf(districtRows.get(0)[0]));
        view.setCurrentMonthTopCity(cityRows.isEmpty() ? "—" : String.valueOf(cityRows.get(0)[0]));
        view.setCurrentMonthBestProductType(typeRows.isEmpty() ? "—" : String.valueOf(typeRows.get(0)[0]));
        view.setCurrentMonthTopBedSize(bedRows.isEmpty() ? "—" : String.valueOf(bedRows.get(0)[0]).replace('_', ' '));
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
        Long totalItems = analyticsRepository.totalItems(from);
        view.setTotalItems(totalItems == null ? 0L : totalItems.longValue());

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

    private List<DesignMetric> mapDesigns(List<Object[]> rows) {
        List<DesignMetric> metrics = new ArrayList<>();
        for (Object[] row : rows) {
            metrics.add(new DesignMetric(
                    String.valueOf(row[0]),
                    ((Number) row[1]).longValue(),
                    toBigDecimal(row[2]),
                    toBigDecimal(row[3])));
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
            orderRow.setCreatedAt(convertToLocalDateTime(row[4]));
            orderRow.setTotalSellingPrice(toBigDecimal(row[5]));
            orderRow.setNetProfit(toBigDecimal(row[6]));
            orderRow.setItemCount(((Number) row[7]).intValue());
            recent.add(orderRow);
        }
        return recent;
    }

    private static LocalDateTime convertToLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        if (value instanceof java.util.Date d) return LocalDateTime.ofInstant(d.toInstant(), java.time.ZoneId.systemDefault());
        // fallback: try parsing
        try {
            return LocalDateTime.parse(value.toString());
        } catch (Exception ex) {
            return null;
        }
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
