package com.bedsheet.orders.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BedsheetType bedsheetType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private BedSize bedSize;

    @Column(nullable = false, length = 200)
    private String beddingDesign;

    @Column(nullable = false)
    private int quantity = 1;

    @Column(nullable = false)
    private int pillowcaseQuantity = 0;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitSellingPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitProfit;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineSellingPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineProfit;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CustomerOrder getOrder() {
        return order;
    }

    public void setOrder(CustomerOrder order) {
        this.order = order;
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

    public String getBeddingDesign() {
        return beddingDesign;
    }

    public void setBeddingDesign(String beddingDesign) {
        this.beddingDesign = beddingDesign;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPillowcaseQuantity() {
        return pillowcaseQuantity;
    }

    public void setPillowcaseQuantity(int pillowcaseQuantity) {
        this.pillowcaseQuantity = pillowcaseQuantity;
    }

    public BigDecimal getUnitSellingPrice() {
        return unitSellingPrice;
    }

    public void setUnitSellingPrice(BigDecimal unitSellingPrice) {
        this.unitSellingPrice = unitSellingPrice;
    }

    public BigDecimal getUnitProfit() {
        return unitProfit;
    }

    public void setUnitProfit(BigDecimal unitProfit) {
        this.unitProfit = unitProfit;
    }

    public BigDecimal getLineSellingPrice() {
        return lineSellingPrice;
    }

    public void setLineSellingPrice(BigDecimal lineSellingPrice) {
        this.lineSellingPrice = lineSellingPrice;
    }

    public BigDecimal getLineProfit() {
        return lineProfit;
    }

    public void setLineProfit(BigDecimal lineProfit) {
        this.lineProfit = lineProfit;
    }
}
