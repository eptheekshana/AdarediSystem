package com.bedsheet.orders.dto;

import com.bedsheet.orders.model.BedSize;
import com.bedsheet.orders.model.BedsheetType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class OrderItemRequest {

    @NotNull
    private BedsheetType bedsheetType;

    @NotNull
    private BedSize bedSize;

    @NotBlank
    private String beddingDesign;

    @Min(1)
    private int quantity = 1;

    @Min(0)
    private int pillowcaseQuantity = 0;

    /**
     * Optional historical unit selling price. Preserved on edit of previously created orders.
     */
    private BigDecimal unitSellingPrice;

    /**
     * Optional historical unit profit. Preserved on edit of previously created orders.
     */
    private BigDecimal unitProfit;

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
}
