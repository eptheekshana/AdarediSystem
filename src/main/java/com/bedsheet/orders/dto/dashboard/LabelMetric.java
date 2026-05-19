package com.bedsheet.orders.dto.dashboard;

import java.math.BigDecimal;

public class LabelMetric {

    private String label;
    private long count;
    private BigDecimal revenue = BigDecimal.ZERO;
    private BigDecimal profit = BigDecimal.ZERO;

    public LabelMetric() {
    }

    public LabelMetric(String label, long count, BigDecimal revenue, BigDecimal profit) {
        this.label = label;
        this.count = count;
        this.revenue = revenue;
        this.profit = profit;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }
}
