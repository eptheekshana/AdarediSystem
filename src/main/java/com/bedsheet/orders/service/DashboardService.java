package com.bedsheet.orders.service;

import com.bedsheet.orders.dto.dashboard.DailyMetric;
import com.bedsheet.orders.dto.dashboard.DashboardView;
import com.bedsheet.orders.dto.dashboard.LabelMetric;
import com.bedsheet.orders.dto.dashboard.MonthlyAnalysisMetric;
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
        // Current month summary
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDateTime monthFrom = monthStart.atStartOfDay();
        List<Object[]> monthRows = analyticsRepository.summarizeOrders(monthFrom);
        if (!monthRows.isEmpty()) {
            Object[] mrow = monthRows.get(0);
            long mOrders = ((Number) mrow[0]).longValue();
            java.math.BigDecimal mRevenue = toBigDecimal(mrow[1]);
            java.math.BigDecimal mNet = toBigDecimal(mrow[3]);
            view.setCurrentMonthOrders(mOrders);
            view.setCurrentMonthRevenue(mRevenue);
            view.setCurrentMonthNetProfit(mNet);
        } else {
            view.setCurrentMonthOrders(0L);
            view.setCurrentMonthRevenue(java.math.BigDecimal.ZERO);
            view.setCurrentMonthNetProfit(java.math.BigDecimal.ZERO);
        }
        Long mItems = analyticsRepository.totalItems(monthFrom);
        view.setCurrentMonthItems(mItems == null ? 0L : mItems.longValue());
        view.setDailyTrend(mapDaily(analyticsRepository.dailyTrend(from)));
        // Always show monthly analysis for all time (ignore the period filter)
        view.setMonthlyTrend(mapMonthly(analyticsRepository.monthlyTrend(null)));
        view.setMonthlyAnalysis(buildMonthlyAnalysis());
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

    private List<DailyMetric> mapMonthly(List<Object[]> rows) {
        List<DailyMetric> metrics = new ArrayList<>();
        for (Object[] row : rows) {
            // row: [year, month, order_count, revenue, net_profit, items_sold]
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            long orders = ((Number) row[2]).longValue();
            java.math.BigDecimal revenue = toBigDecimal(row[3]);
            java.math.BigDecimal net = toBigDecimal(row[4]);
            long items = row.length > 5 && row[5] != null ? ((Number) row[5]).longValue() : 0L;
            LocalDate date = LocalDate.of(year, month, 1);
            metrics.add(new DailyMetric(date, orders, revenue, net, items));
        }
        return metrics;
    }

    private List<MonthlyAnalysisMetric> buildMonthlyAnalysis() {
        List<Object[]> summaryRows = analyticsRepository.monthlySummary(null);
        java.util.LinkedHashMap<String, MonthlyAnalysisMetric> byMonth = new java.util.LinkedHashMap<>();

        for (Object[] row : summaryRows) {
            String key = monthKey(row[0], row[1]);
            MonthlyAnalysisMetric metric = new MonthlyAnalysisMetric();
            metric.setMonth(toMonthDate(row[0], row[1]));
            metric.setOrderCount(((Number) row[2]).longValue());
            metric.setItemsSold(row[3] == null ? 0L : ((Number) row[3]).longValue());
            metric.setRevenue(toBigDecimal(row[4]));
            metric.setGrossProfit(toBigDecimal(row[5]));
            metric.setNetProfit(toBigDecimal(row[6]));
            metric.setCodTotal(toBigDecimal(row[7]));
            byMonth.put(key, metric);
        }

        applyTopDistricts(byMonth, analyticsRepository.monthlyByDistrict(null));
        applyTopCities(byMonth, analyticsRepository.monthlyByCity(null));
        applyBestProductTypes(byMonth, analyticsRepository.monthlyByBedsheetType(null));
        applyTopBedSizes(byMonth, analyticsRepository.monthlyByBedSize(null));

        return new ArrayList<>(byMonth.values());
    }

    private static void applyTopDistricts(java.util.Map<String, MonthlyAnalysisMetric> byMonth, List<Object[]> rows) {
        for (Object[] row : rows) {
            MonthlyAnalysisMetric metric = byMonth.get(monthKey(row[0], row[1]));
            if (metric != null && metric.getTopDistrict() == null) {
                metric.setTopDistrict(String.valueOf(row[2]));
            }
        }
    }

    private static void applyTopCities(java.util.Map<String, MonthlyAnalysisMetric> byMonth, List<Object[]> rows) {
        for (Object[] row : rows) {
            MonthlyAnalysisMetric metric = byMonth.get(monthKey(row[0], row[1]));
            if (metric != null && metric.getTopCity() == null) {
                metric.setTopCity(String.valueOf(row[2]));
            }
        }
    }

    private static void applyBestProductTypes(java.util.Map<String, MonthlyAnalysisMetric> byMonth, List<Object[]> rows) {
        for (Object[] row : rows) {
            MonthlyAnalysisMetric metric = byMonth.get(monthKey(row[0], row[1]));
            if (metric != null && metric.getBestProductType() == null) {
                metric.setBestProductType(String.valueOf(row[2]));
            }
        }
    }

    private static void applyTopBedSizes(java.util.Map<String, MonthlyAnalysisMetric> byMonth, List<Object[]> rows) {
        for (Object[] row : rows) {
            MonthlyAnalysisMetric metric = byMonth.get(monthKey(row[0], row[1]));
            if (metric != null && metric.getTopBedSize() == null) {
                metric.setTopBedSize(String.valueOf(row[2]).replace('_', ' '));
            }
        }
    }

    private static String monthKey(Object yearValue, Object monthValue) {
        return ((Number) yearValue).intValue() + "-" + ((Number) monthValue).intValue();
    }

    private static LocalDate toMonthDate(Object yearValue, Object monthValue) {
        return LocalDate.of(((Number) yearValue).intValue(), ((Number) monthValue).intValue(), 1);
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
