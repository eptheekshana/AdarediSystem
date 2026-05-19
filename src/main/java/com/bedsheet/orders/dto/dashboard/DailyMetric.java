package com.bedsheet.orders.dto.dashboard;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class DailyMetric {

    private LocalDate date;
    private long orderCount;
    private BigDecimal revenue = BigDecimal.ZERO;
    private BigDecimal netProfit = BigDecimal.ZERO;
    private long itemsSold;

    public DailyMetric() {
    }

    public DailyMetric(LocalDate date, long orderCount, BigDecimal revenue, BigDecimal netProfit, long itemsSold) {
        this.date = date;
        this.orderCount = orderCount;
        this.revenue = revenue;
        this.netProfit = netProfit;
        this.itemsSold = itemsSold;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(long orderCount) {
        this.orderCount = orderCount;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public BigDecimal getNetProfit() {
        return netProfit;
    }

    public void setNetProfit(BigDecimal netProfit) {
        this.netProfit = netProfit;
    }

    public long getItemsSold() {
        return itemsSold;
    }

    public void setItemsSold(long itemsSold) {
        this.itemsSold = itemsSold;
    }

    public BigDecimal getAvgOrderValue() {
        if (orderCount == 0) {
            return BigDecimal.ZERO;
        }
        return revenue.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getAvgProfitPerOrder() {
        if (orderCount == 0) {
            return BigDecimal.ZERO;
        }
        return netProfit.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getAvgItemsPerOrder() {
        if (orderCount == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(itemsSold).divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getProfitMarginPercent() {
        if (revenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return netProfit.divide(revenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }
}
