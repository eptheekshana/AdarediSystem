package com.bedsheet.orders.dto;

import java.math.BigDecimal;

public class PricingRuleUpdateDto {

    private String itemKey;
    private BigDecimal sellingPrice;
    private BigDecimal unitProfit;

    public PricingRuleUpdateDto() {
    }

    public PricingRuleUpdateDto(String itemKey, BigDecimal sellingPrice, BigDecimal unitProfit) {
        this.itemKey = itemKey;
        this.sellingPrice = sellingPrice;
        this.unitProfit = unitProfit;
    }

    public String getItemKey() {
        return itemKey;
    }

    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public BigDecimal getUnitProfit() {
        return unitProfit;
    }

    public void setUnitProfit(BigDecimal unitProfit) {
        this.unitProfit = unitProfit;
    }
}
