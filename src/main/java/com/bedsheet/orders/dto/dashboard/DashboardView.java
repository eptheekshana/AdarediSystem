package com.bedsheet.orders.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

public class DashboardView {

    private int periodDays;
    private long totalOrders;
    private long totalItems;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal grossProfit = BigDecimal.ZERO;
    private BigDecimal netProfit = BigDecimal.ZERO;
    private BigDecimal totalDiscount = BigDecimal.ZERO;
    private BigDecimal totalDeliveryCharge = BigDecimal.ZERO;
    private BigDecimal totalCod = BigDecimal.ZERO;
    private BigDecimal avgOrderValue = BigDecimal.ZERO;
    private BigDecimal avgNetProfit = BigDecimal.ZERO;
    private List<DailyMetric> dailyTrend = List.of();
    private List<LabelMetric> byDistrict = List.of();
    private List<LabelMetric> byCity = List.of();
    private List<ProductTypeMetric> byBedsheetType = List.of();
    private List<LabelMetric> byBedSize = List.of();
    private List<RecentOrderRow> recentOrders = List.of();

    public int getPeriodDays() {
        return periodDays;
    }

    public void setPeriodDays(int periodDays) {
        this.periodDays = periodDays;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(BigDecimal grossProfit) {
        this.grossProfit = grossProfit;
    }

    public BigDecimal getNetProfit() {
        return netProfit;
    }

    public void setNetProfit(BigDecimal netProfit) {
        this.netProfit = netProfit;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(BigDecimal totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public BigDecimal getTotalDeliveryCharge() {
        return totalDeliveryCharge;
    }

    public void setTotalDeliveryCharge(BigDecimal totalDeliveryCharge) {
        this.totalDeliveryCharge = totalDeliveryCharge;
    }

    public BigDecimal getTotalCod() {
        return totalCod;
    }

    public void setTotalCod(BigDecimal totalCod) {
        this.totalCod = totalCod;
    }

    public BigDecimal getAvgOrderValue() {
        return avgOrderValue;
    }

    public void setAvgOrderValue(BigDecimal avgOrderValue) {
        this.avgOrderValue = avgOrderValue;
    }

    public BigDecimal getAvgNetProfit() {
        return avgNetProfit;
    }

    public void setAvgNetProfit(BigDecimal avgNetProfit) {
        this.avgNetProfit = avgNetProfit;
    }

    public List<DailyMetric> getDailyTrend() {
        return dailyTrend;
    }

    public void setDailyTrend(List<DailyMetric> dailyTrend) {
        this.dailyTrend = dailyTrend;
    }

    public List<LabelMetric> getByDistrict() {
        return byDistrict;
    }

    public void setByDistrict(List<LabelMetric> byDistrict) {
        this.byDistrict = byDistrict;
    }

    public List<LabelMetric> getByCity() {
        return byCity;
    }

    public void setByCity(List<LabelMetric> byCity) {
        this.byCity = byCity;
    }

    public List<ProductTypeMetric> getByBedsheetType() {
        return byBedsheetType;
    }

    public void setByBedsheetType(List<ProductTypeMetric> byBedsheetType) {
        this.byBedsheetType = byBedsheetType;
    }

    public List<LabelMetric> getByBedSize() {
        return byBedSize;
    }

    public void setByBedSize(List<LabelMetric> byBedSize) {
        this.byBedSize = byBedSize;
    }

    public List<RecentOrderRow> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<RecentOrderRow> recentOrders) {
        this.recentOrders = recentOrders;
    }
}
