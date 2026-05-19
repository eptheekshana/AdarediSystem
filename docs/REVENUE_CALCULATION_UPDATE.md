# Revenue Calculation Update

## Overview
Updated the revenue calculation across the dashboard and daily analysis to use **COD Value - Delivery Charge** instead of `total_selling_price`.

## Changes Made

### Database Queries Updated
All revenue calculations in the `AnalyticsRepository` have been updated:

#### 1. `summarizeOrders()` - Dashboard Summary
**Before:**
```java
SELECT COUNT(o), COALESCE(SUM(o.totalSellingPrice), 0), ...
```

**After:**
```java
SELECT COUNT(o), COALESCE(SUM(o.codValue - o.deliveryCharge), 0), ...
```

#### 2. `dailyTrend()` - Daily Analysis Table & Charts
**Before (Native SQL):**
```sql
SELECT DATE(o.created_at) AS day,
       COUNT(DISTINCT o.id) AS order_count,
       COALESCE(SUM(o.total_selling_price), 0),
       ...
```

**After:**
```sql
SELECT DATE(o.created_at) AS day,
       COUNT(DISTINCT o.id) AS order_count,
       COALESCE(SUM(o.cod_value - o.delivery_charge), 0),
       ...
```

#### 3. `byDistrict()` - District Analysis
**Before:**
```java
SELECT o.district, COUNT(o), COALESCE(SUM(o.totalSellingPrice), 0), ...
```

**After:**
```java
SELECT o.district, COUNT(o), COALESCE(SUM(o.codValue - o.deliveryCharge), 0), ...
```

#### 4. `byCity()` - City Analysis
**Before:**
```java
SELECT o.nearestCity, COUNT(o), COALESCE(SUM(o.totalSellingPrice), 0), ...
```

**After:**
```java
SELECT o.nearestCity, COUNT(o), COALESCE(SUM(o.codValue - o.deliveryCharge), 0), ...
```

#### 5. `recentOrders()` - Recent Orders List
**Before:**
```java
SELECT o.id, o.waybillNo, ..., o.totalSellingPrice, o.netProfit, ...
```

**After:**
```java
SELECT o.id, o.waybillNo, ..., (o.codValue - o.deliveryCharge), o.netProfit, ...
```

## Impact

### Dashboard KPI Section
- **Revenue (LKR)** - Now shows sum of (COD Value - Delivery Charge) for the selected period

### Daily Analysis Table
- **Revenue (LKR)** column - Now shows (COD - Delivery Charge) per day
- **Avg order (LKR)** - Recalculated based on new revenue formula

### Charts
- **Revenue & Profit Trend** - Revenue line now reflects new calculation
- **Daily Avg Order Value** - Updated to use new revenue formula

### Recent Orders Table
- Revenue column now displays (COD - Delivery Charge) for each order

## Finance Impact
This change reflects how actual revenue is calculated in your business model:
- **Revenue** = Cash On Delivery amount collected minus delivery charges paid
- **Net Profit** = Gross Profit - Discounts - (Delivery Charge - 400 LKR free limit)

## Verification Steps

1. **Local Testing:**
   ```bash
   mvn clean test
   ```

2. **Navigate to Dashboard:**
   - Go to `/dashboard` in your application
   - View the "Daily analysis" section
   - Verify revenue values match: COD - Delivery Charge

3. **Compare with Period Filters:**
   - Try different period filters (7 days, 30 days, 90 days, All time)
   - Confirm revenue calculations are consistent

## Files Modified
- `/src/main/java/com/bedsheet/orders/repository/AnalyticsRepository.java`
  - `summarizeOrders()` method
  - `dailyTrend()` method
  - `byDistrict()` method
  - `byCity()` method
  - `recentOrders()` method

## Rollback Instructions

To revert to the previous `total_selling_price` calculation, replace the revenue calculations in AnalyticsRepository with `SUM(o.totalSellingPrice)` or `SUM(o.total_selling_price)` respectively.

