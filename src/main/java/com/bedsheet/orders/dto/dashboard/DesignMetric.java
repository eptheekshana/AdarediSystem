package com.bedsheet.orders.dto.dashboard;

import java.math.BigDecimal;

public class DesignMetric {

    private String design;
    private long quantity;
    private BigDecimal revenue = BigDecimal.ZERO;
    private BigDecimal profit = BigDecimal.ZERO;

    public DesignMetric() {
    }

    public DesignMetric(String design, long quantity, BigDecimal revenue, BigDecimal profit) {
        this.design = design;
        this.quantity = quantity;
        this.revenue = revenue;
        this.profit = profit;
    }

    public String getDesign() {
        return design;
    }

    public void setDesign(String design) {
        this.design = design;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
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
