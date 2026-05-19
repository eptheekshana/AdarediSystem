# Daily Analysis Enhancement - Dashboard Update

## Overview
Enhanced the dashboard with comprehensive daily analysis features to provide deeper insights into daily business performance.

## Changes Made

### 1. Backend - Enhanced Daily Metrics (DailyMetric.java)
Added new calculated properties to provide daily analysis metrics:

- **getAvgOrderValue()** - Average order value per day
- **getAvgProfitPerOrder()** - Average profit per order for each day
- **getAvgItemsPerOrder()** - Average items per order for each day  
- **getProfitMarginPercent()** - Daily profit margin as a percentage

These methods automatically calculate based on the day's revenue, profit, and order counts.

### 2. Frontend - Daily Analysis Table (dashboard/index.html)
Replaced the basic "Daily revenue, quantity & profit" table with an enhanced "Daily analysis" table showing:

| Column | Description |
|--------|-------------|
| Date | Order date |
| Orders | Number of orders that day |
| Qty sold | Total items sold |
| Avg order (LKR) | Average order value |
| Revenue (LKR) | Total daily revenue |
| Net profit (LKR) | Total daily net profit |
| Avg profit/order (LKR) | Average profit per order |
| Profit % | Profit margin percentage |

### 3. Frontend - New Daily Analysis Charts (dashboard.js)
Added two new chart visualizations:

#### a. Daily Average Order Value Chart
- **Type:** Line chart
- **Data:** Average order value trend over the selected period
- **Color:** Purple (#8b5cf6)
- **Purpose:** Identify patterns in order values across days

#### b. Daily Profit Margin % Chart
- **Type:** Line chart  
- **Data:** Profit margin percentage trend over the selected period
- **Color:** Teal (#14b8a6)
- **Purpose:** Identify profitability trends and variations

### 4. Chart Grid Enhancement
Added two new canvas elements to the chart grid section for the new daily analysis charts.

## Data Flow

```
DashboardService.buildDashboard()
    ↓
AnalyticsRepository.dailyTrend() → List<DailyMetric>
    ↓
DailyMetric.getAvgOrderValue()
DailyMetric.getAvgProfitPerOrder()
DailyMetric.getProfitMarginPercent()
    ↓
Dashboard Template renders:
  - Daily Analysis Table
  - Daily Avg Order Value Chart
  - Daily Profit Margin % Chart
```

## Features Enabled

✅ **Daily Performance Analysis** - Comprehensive per-day breakdown of key metrics
✅ **Trend Visualization** - Charts showing how daily metrics trend over time
✅ **Profitability Insights** - Daily profit margin tracking
✅ **Order Value Analysis** - Average order value trends
✅ **Responsive Design** - Charts adapt to theme changes (light/dark mode)

## Usage

1. Navigate to the Dashboard (`/dashboard`)
2. Use period filters (7 days, 30 days, 90 days, All time)
3. View the "Daily analysis" table for detailed daily metrics
4. Check the new charts:
   - "Daily avg order value" - visualizes order value trends
   - "Daily profit margin %" - visualizes profitability trends

## Technical Details

- **Calculation Method:** All averages use `BigDecimal` with `RoundingMode.HALF_UP` for precision
- **Chart Library:** Chart.js 4.4.1
- **Theme Support:** Charts automatically adapt to light/dark theme
- **Formatting:** 
  - Currency values: LKR with comma separators
  - Percentages: 2 decimal places
  - Averages: 2 decimal places

## Testing Recommendations

1. **Table Verification:** Verify that daily metrics are accurately calculated
2. **Chart Rendering:** Confirm both new charts render correctly with sample data
3. **Theme Switching:** Test that charts update when theme is changed
4. **Period Filtering:** Test all period options (7d, 30d, 90d, all-time)
5. **Edge Cases:** Test with:
   - Zero order days
   - Low revenue periods
   - High profit days to verify margin calculations

