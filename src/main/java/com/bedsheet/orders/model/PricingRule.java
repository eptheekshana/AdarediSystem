package com.bedsheet.orders.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "pricing_rules")
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_key", nullable = false, unique = true, length = 50)
    private String itemKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "bedsheet_type", length = 20)
    private BedsheetType bedsheetType;

    @Enumerated(EnumType.STRING)
    @Column(name = "bed_size", length = 20)
    private BedSize bedSize;

    @Column(name = "item_label", nullable = false, length = 100)
    private String itemLabel;

    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "unit_profit", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitProfit;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PricingRule() {
    }

    public PricingRule(String itemKey, BedsheetType bedsheetType, BedSize bedSize, String itemLabel, BigDecimal sellingPrice, BigDecimal unitProfit) {
        this.itemKey = itemKey;
        this.bedsheetType = bedsheetType;
        this.bedSize = bedSize;
        this.itemLabel = itemLabel;
        this.sellingPrice = sellingPrice;
        this.unitProfit = unitProfit;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemKey() {
        return itemKey;
    }

    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }

    public BedsheetType getBedsheetType() {
        return bedsheetType;
    }

    public void setBedsheetType(BedsheetType bedsheetType) {
        this.bedsheetType = bedsheetType;
    }

    public BedSize getBedSize() {
        return bedSize;
    }

    public void setBedSize(BedSize bedSize) {
        this.bedSize = bedSize;
    }

    public String getItemLabel() {
        return itemLabel;
    }

    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public BigDecimal getMarginPercentage() {
        if (sellingPrice == null || sellingPrice.compareTo(BigDecimal.ZERO) <= 0 || unitProfit == null) {
            return BigDecimal.ZERO;
        }
        return unitProfit.multiply(BigDecimal.valueOf(100))
                .divide(sellingPrice, 1, RoundingMode.HALF_UP);
    }
}
