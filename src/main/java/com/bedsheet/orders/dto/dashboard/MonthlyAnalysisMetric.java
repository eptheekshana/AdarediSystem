package com.bedsheet.orders.dto.dashboard;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class MonthlyAnalysisMetric {

    private LocalDate month;
    private long orderCount;
    private long itemsSold;
    private BigDecimal revenue = BigDecimal.ZERO;
    private BigDecimal grossProfit = BigDecimal.ZERO;
    private BigDecimal netProfit = BigDecimal.ZERO;
    private BigDecimal codTotal = BigDecimal.ZERO;
    private String topDistrict;
    private String topCity;
    private String bestProductType;
    private String topBedSize;

    public LocalDate getMonth() {
        return month;
    }

    public void setMonth(LocalDate month) {
        this.month = month;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(long orderCount) {
        this.orderCount = orderCount;
    }

    public long getItemsSold() {
        return itemsSold;
    }

    public void setItemsSold(long itemsSold) {
        this.itemsSold = itemsSold;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
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

    public BigDecimal getCodTotal() {
        return codTotal;
    }

    public void setCodTotal(BigDecimal codTotal) {
        this.codTotal = codTotal;
    }

    public String getTopDistrict() {
        return topDistrict;
    }

    public void setTopDistrict(String topDistrict) {
        this.topDistrict = topDistrict;
    }

    public String getTopCity() {
        return topCity;
    }

    public void setTopCity(String topCity) {
        this.topCity = topCity;
    }

    public String getBestProductType() {
        return bestProductType;
    }

    public void setBestProductType(String bestProductType) {
        this.bestProductType = bestProductType;
    }

    public String getTopBedSize() {
        return topBedSize;
    }

    public void setTopBedSize(String topBedSize) {
        this.topBedSize = topBedSize;
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

    public BigDecimal getProfitMarginPercent() {
        if (revenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return netProfit.divide(revenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}

