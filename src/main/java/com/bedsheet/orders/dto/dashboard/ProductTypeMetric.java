package com.bedsheet.orders.dto.dashboard;

import java.math.BigDecimal;

public class ProductTypeMetric {

    private String type;
    private long quantity;
    private BigDecimal profit = BigDecimal.ZERO;

    public ProductTypeMetric() {
    }

    public ProductTypeMetric(String type, long quantity, BigDecimal profit) {
        this.type = type;
        this.quantity = quantity;
        this.profit = profit;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }
}
