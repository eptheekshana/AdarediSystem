package com.bedsheet.orders.service;

import com.bedsheet.orders.model.BedSize;
import com.bedsheet.orders.model.BedsheetType;
import com.bedsheet.orders.web.PriceRowView;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

    private final Map<BedsheetType, Map<BedSize, BigDecimal>> sellingPrices = new EnumMap<>(BedsheetType.class);
    private final Map<BedsheetType, Map<BedSize, BigDecimal>> unitProfits = new EnumMap<>(BedsheetType.class);

    public PricingService() {
        sellingPrices.put(BedsheetType.FLAT, map(
                BedSize.SINGLE, 1400,
                BedSize.DOUBLE, 1600,
                BedSize.QUEEN, 1750,
                BedSize.KING, 1900,
                BedSize.SUPER_KING, 2050));
        sellingPrices.put(BedsheetType.FITTED, map(
                BedSize.SINGLE, 1800,
                BedSize.DOUBLE, 2000,
                BedSize.QUEEN, 2150,
                BedSize.KING, 2300,
                BedSize.SUPER_KING, 2450));

        unitProfits.put(BedsheetType.FLAT, map(
                BedSize.SINGLE, 750,
                BedSize.DOUBLE, 900,
                BedSize.QUEEN, 920,
                BedSize.KING, 1020,
                BedSize.SUPER_KING, 1100));
        unitProfits.put(BedsheetType.FITTED, map(
                BedSize.SINGLE, 1150,
                BedSize.DOUBLE, 1300,
                BedSize.QUEEN, 1320,
                BedSize.KING, 1420,
                BedSize.SUPER_KING, 1500));
    }

    public BigDecimal getSellingPrice(BedsheetType type, BedSize size) {
        return requirePrice(sellingPrices, type, size, "selling price");
    }

    public BigDecimal getUnitProfit(BedsheetType type, BedSize size) {
        return requirePrice(unitProfits, type, size, "profit");
    }

    public Map<BedsheetType, Map<BedSize, BigDecimal>> getAllSellingPrices() {
        return copy(sellingPrices);
    }

    public Map<BedsheetType, Map<BedSize, BigDecimal>> getAllUnitProfits() {
        return copy(unitProfits);
    }

    public List<PriceRowView> getPriceRows() {
        List<PriceRowView> rows = new ArrayList<>();
        for (BedsheetType type : BedsheetType.values()) {
            for (BedSize size : BedSize.values()) {
                rows.add(new PriceRowView(
                        type.name(),
                        size.name().replace('_', ' '),
                        getSellingPrice(type, size).toPlainString(),
                        getUnitProfit(type, size).toPlainString()));
            }
        }
        return rows;
    }

    public Map<String, Map<String, BigDecimal>> getSellingPricesForJson() {
        return toStringKeyMap(sellingPrices);
    }

    public Map<String, Map<String, BigDecimal>> getUnitProfitsForJson() {
        return toStringKeyMap(unitProfits);
    }

    private static Map<String, Map<String, BigDecimal>> toStringKeyMap(
            Map<BedsheetType, Map<BedSize, BigDecimal>> source) {
        Map<String, Map<String, BigDecimal>> result = new LinkedHashMap<>();
        for (BedsheetType type : BedsheetType.values()) {
            Map<String, BigDecimal> bySize = new LinkedHashMap<>();
            for (BedSize size : BedSize.values()) {
                bySize.put(size.name(), source.get(type).get(size));
            }
            result.put(type.name(), bySize);
        }
        return result;
    }

    private static Map<BedSize, BigDecimal> map(Object... typeAndAmount) {
        Map<BedSize, BigDecimal> prices = new EnumMap<>(BedSize.class);
        for (int i = 0; i < typeAndAmount.length; i += 2) {
            prices.put((BedSize) typeAndAmount[i], BigDecimal.valueOf(((Number) typeAndAmount[i + 1]).longValue()));
        }
        return prices;
    }

    private static BigDecimal requirePrice(
            Map<BedsheetType, Map<BedSize, BigDecimal>> table,
            BedsheetType type,
            BedSize size,
            String label) {
        Map<BedSize, BigDecimal> bySize = table.get(type);
        if (bySize == null || !bySize.containsKey(size)) {
            throw new IllegalArgumentException("No " + label + " for " + type + " " + size);
        }
        return bySize.get(size);
    }

    private static Map<BedsheetType, Map<BedSize, BigDecimal>> copy(Map<BedsheetType, Map<BedSize, BigDecimal>> source) {
        Map<BedsheetType, Map<BedSize, BigDecimal>> copy = new EnumMap<>(BedsheetType.class);
        source.forEach((type, bySize) -> copy.put(type, Map.copyOf(bySize)));
        return copy;
    }
}
