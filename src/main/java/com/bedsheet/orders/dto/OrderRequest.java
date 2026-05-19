package com.bedsheet.orders.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderRequest {

    @NotBlank
    private String waybillNo;

    @NotBlank
    private String customerName;

    @NotBlank
    private String primaryPhone;

    private String secondaryPhone;

    @NotBlank
    private String address;

    @NotBlank
    private String nearestCity;

    @NotBlank
    private String district;

    private BigDecimal weight;

    @NotNull
    private BigDecimal deliveryCharge = BigDecimal.ZERO;

    @NotNull
    private BigDecimal codValue = BigDecimal.ZERO;

    @NotNull
    private BigDecimal discount = BigDecimal.ZERO;

    private boolean pushToCurfox = true;

    @NotEmpty
    @Valid
    private List<OrderItemRequest> items = new ArrayList<>();

    public String getWaybillNo() {
        return waybillNo;
    }

    public void setWaybillNo(String waybillNo) {
        this.waybillNo = waybillNo;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPrimaryPhone() {
        return primaryPhone;
    }

    public void setPrimaryPhone(String primaryPhone) {
        this.primaryPhone = primaryPhone;
    }

    public String getSecondaryPhone() {
        return secondaryPhone;
    }

    public void setSecondaryPhone(String secondaryPhone) {
        this.secondaryPhone = secondaryPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNearestCity() {
        return nearestCity;
    }

    public void setNearestCity(String nearestCity) {
        this.nearestCity = nearestCity;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getDeliveryCharge() {
        return deliveryCharge;
    }

    public void setDeliveryCharge(BigDecimal deliveryCharge) {
        this.deliveryCharge = deliveryCharge;
    }

    public BigDecimal getCodValue() {
        return codValue;
    }

    public void setCodValue(BigDecimal codValue) {
        this.codValue = codValue;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public boolean isPushToCurfox() {
        return pushToCurfox;
    }

    public void setPushToCurfox(boolean pushToCurfox) {
        this.pushToCurfox = pushToCurfox;
    }


    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}
