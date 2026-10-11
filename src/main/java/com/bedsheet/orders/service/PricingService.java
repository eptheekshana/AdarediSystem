package com.bedsheet.orders.service;

import com.bedsheet.orders.dto.PricingRuleUpdateDto;
import com.bedsheet.orders.model.BedSize;
import com.bedsheet.orders.model.BedsheetType;
import com.bedsheet.orders.model.PricingRule;
import com.bedsheet.orders.repository.PricingRuleRepository;
import com.bedsheet.orders.web.PriceRowView;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PricingService {

    public static final String KEY_PILLOWCASE = "PILLOWCASE";

    private final PricingRuleRepository pricingRuleRepository;

    private final Map<BedsheetType, Map<BedSize, BigDecimal>> sellingPrices = new EnumMap<>(BedsheetType.class);
    private final Map<BedsheetType, Map<BedSize, BigDecimal>> unitProfits = new EnumMap<>(BedsheetType.class);
    private BigDecimal pillowcaseSellingPrice = BigDecimal.valueOf(220);
    private BigDecimal pillowcaseUnitProfit = BigDecimal.valueOf(125);

    public PricingService() {
        this(null);
    }

    @Autowired
    public PricingService(@Autowired(required = false) PricingRuleRepository pricingRuleRepository) {
        this.pricingRuleRepository = pricingRuleRepository;
        initDefaultMaps();
        if (pricingRuleRepository != null) {
            syncFromDatabase();
        }
    }

    @PostConstruct
    public void init() {
        if (pricingRuleRepository != null) {
            syncFromDatabase();
        }
    }

    public synchronized void syncFromDatabase() {
        if (pricingRuleRepository == null) {
            return;
        }

        // Seed defaults if table is empty or any key is missing
        for (PricingRule defaultRule : defaultRuleList()) {
            Optional<PricingRule> existing = pricingRuleRepository.findByItemKey(defaultRule.getItemKey());
            if (existing.isEmpty()) {
                pricingRuleRepository.save(new PricingRule(
                        defaultRule.getItemKey(),
                        defaultRule.getBedsheetType(),
                        defaultRule.getBedSize(),
                        defaultRule.getItemLabel(),
                        defaultRule.getSellingPrice(),
                        defaultRule.getUnitProfit()));
            }
        }

        List<PricingRule> rules = pricingRuleRepository.findAllByOrderByIdAsc();
        for (PricingRule rule : rules) {
            applyRuleToCache(rule);
        }
    }

    private void applyRuleToCache(PricingRule rule) {
        if (KEY_PILLOWCASE.equalsIgnoreCase(rule.getItemKey())) {
            this.pillowcaseSellingPrice = rule.getSellingPrice();
            this.pillowcaseUnitProfit = rule.getUnitProfit();
        } else if (rule.getBedsheetType() != null && rule.getBedSize() != null) {
            sellingPrices.computeIfAbsent(rule.getBedsheetType(), k -> new EnumMap<>(BedSize.class))
                    .put(rule.getBedSize(), rule.getSellingPrice());
            unitProfits.computeIfAbsent(rule.getBedsheetType(), k -> new EnumMap<>(BedSize.class))
                    .put(rule.getBedSize(), rule.getUnitProfit());
        }
    }

    private void initDefaultMaps() {
        for (BedsheetType type : BedsheetType.values()) {
            sellingPrices.put(type, new EnumMap<>(BedSize.class));
            unitProfits.put(type, new EnumMap<>(BedSize.class));
        }

        for (PricingRule rule : defaultRuleList()) {
            applyRuleToCache(rule);
        }
    }

    public static List<PricingRule> defaultRuleList() {
        List<PricingRule> list = new ArrayList<>();
        list.add(new PricingRule("FLAT_SINGLE", BedsheetType.FLAT, BedSize.SINGLE, "Flat Sheet — Single (60\" × 90\")", BigDecimal.valueOf(1400), BigDecimal.valueOf(750)));
        list.add(new PricingRule("FLAT_DOUBLE", BedsheetType.FLAT, BedSize.DOUBLE, "Flat Sheet — Double (72\" × 90\")", BigDecimal.valueOf(1600), BigDecimal.valueOf(900)));
        list.add(new PricingRule("FLAT_QUEEN", BedsheetType.FLAT, BedSize.QUEEN, "Flat Sheet — Queen (80\" × 90\")", BigDecimal.valueOf(1750), BigDecimal.valueOf(920)));
        list.add(new PricingRule("FLAT_KING", BedsheetType.FLAT, BedSize.KING, "Flat Sheet — King (90\" × 100\")", BigDecimal.valueOf(1900), BigDecimal.valueOf(1020)));
        list.add(new PricingRule("FLAT_SUPER_KING", BedsheetType.FLAT, BedSize.SUPER_KING, "Flat Sheet — Super King (108\" × 108\")", BigDecimal.valueOf(2050), BigDecimal.valueOf(1100)));

        list.add(new PricingRule("FITTED_SINGLE", BedsheetType.FITTED, BedSize.SINGLE, "Fitted Sheet — Single (36\" × 78\")", BigDecimal.valueOf(1800), BigDecimal.valueOf(1150)));
        list.add(new PricingRule("FITTED_DOUBLE", BedsheetType.FITTED, BedSize.DOUBLE, "Fitted Sheet — Double (48\" × 78\")", BigDecimal.valueOf(2000), BigDecimal.valueOf(1300)));
        list.add(new PricingRule("FITTED_QUEEN", BedsheetType.FITTED, BedSize.QUEEN, "Fitted Sheet — Queen (60\" × 78\")", BigDecimal.valueOf(2150), BigDecimal.valueOf(1320)));
        list.add(new PricingRule("FITTED_KING", BedsheetType.FITTED, BedSize.KING, "Fitted Sheet — King (72\" × 78\")", BigDecimal.valueOf(2300), BigDecimal.valueOf(1420)));
        list.add(new PricingRule("FITTED_SUPER_KING", BedsheetType.FITTED, BedSize.SUPER_KING, "Fitted Sheet — Super King (78\" × 84\")", BigDecimal.valueOf(2450), BigDecimal.valueOf(1500)));

        list.add(new PricingRule(KEY_PILLOWCASE, null, null, "Extra Pillowcase (Standard)", BigDecimal.valueOf(220), BigDecimal.valueOf(125)));
        return Collections.unmodifiableList(list);
    }

    public synchronized BigDecimal getSellingPrice(BedsheetType type, BedSize size) {
        return requirePrice(sellingPrices, type, size, "selling price");
    }

    public synchronized BigDecimal getUnitProfit(BedsheetType type, BedSize size) {
        return requirePrice(unitProfits, type, size, "profit");
    }

    public synchronized BigDecimal getPillowcaseSellingPrice() {
        return pillowcaseSellingPrice;
    }

    public synchronized BigDecimal getPillowcaseUnitProfit() {
        return pillowcaseUnitProfit;
    }

    public synchronized Map<BedsheetType, Map<BedSize, BigDecimal>> getAllSellingPrices() {
        return copy(sellingPrices);
    }

    public synchronized Map<BedsheetType, Map<BedSize, BigDecimal>> getAllUnitProfits() {
        return copy(unitProfits);
    }

    public synchronized List<PricingRule> getAllRules() {
        if (pricingRuleRepository != null) {
            List<PricingRule> rules = pricingRuleRepository.findAllByOrderByIdAsc();
            if (!rules.isEmpty()) {
                return rules;
            }
        }
        return defaultRuleList();
    }

    @Transactional
    public synchronized void updateRule(String itemKey, BigDecimal sellingPrice, BigDecimal unitProfit) {
        if (sellingPrice == null || unitProfit == null) {
            throw new IllegalArgumentException("Selling price and unit profit cannot be null");
        }
        if (pricingRuleRepository != null) {
            PricingRule rule = pricingRuleRepository.findByItemKey(itemKey)
                    .orElseThrow(() -> new IllegalArgumentException("Rule not found for key: " + itemKey));
            rule.setSellingPrice(sellingPrice);
            rule.setUnitProfit(unitProfit);
            rule.setUpdatedAt(LocalDateTime.now());
            pricingRuleRepository.save(rule);
            applyRuleToCache(rule);
        } else {
            // Memory-only fallback
            if (KEY_PILLOWCASE.equalsIgnoreCase(itemKey)) {
                this.pillowcaseSellingPrice = sellingPrice;
                this.pillowcaseUnitProfit = unitProfit;
            } else {
                for (PricingRule def : defaultRuleList()) {
                    if (def.getItemKey().equalsIgnoreCase(itemKey)) {
                        sellingPrices.get(def.getBedsheetType()).put(def.getBedSize(), sellingPrice);
                        unitProfits.get(def.getBedsheetType()).put(def.getBedSize(), unitProfit);
                        break;
                    }
                }
            }
        }
    }

    @Transactional
    public synchronized void updateRules(List<PricingRuleUpdateDto> updates) {
        if (updates == null || updates.isEmpty()) {
            return;
        }
        for (PricingRuleUpdateDto update : updates) {
            if (update.getItemKey() != null && update.getSellingPrice() != null && update.getUnitProfit() != null) {
                updateRule(update.getItemKey(), update.getSellingPrice(), update.getUnitProfit());
            }
        }
    }

    @Transactional
    public synchronized void resetToDefaults() {
        List<PricingRule> defaults = defaultRuleList();
        if (pricingRuleRepository != null) {
            for (PricingRule def : defaults) {
                PricingRule existing = pricingRuleRepository.findByItemKey(def.getItemKey())
                        .orElse(new PricingRule(def.getItemKey(), def.getBedsheetType(), def.getBedSize(), def.getItemLabel(), def.getSellingPrice(), def.getUnitProfit()));
                existing.setSellingPrice(def.getSellingPrice());
                existing.setUnitProfit(def.getUnitProfit());
                existing.setItemLabel(def.getItemLabel());
                existing.setUpdatedAt(LocalDateTime.now());
                pricingRuleRepository.save(existing);
                applyRuleToCache(existing);
            }
        } else {
            initDefaultMaps();
        }
    }

    public synchronized List<PriceRowView> getPriceRows() {
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

    public synchronized Map<String, Map<String, BigDecimal>> getSellingPricesForJson() {
        return toStringKeyMap(sellingPrices);
    }

    public synchronized Map<String, Map<String, BigDecimal>> getUnitProfitsForJson() {
        return toStringKeyMap(unitProfits);
    }

    private static Map<String, Map<String, BigDecimal>> toStringKeyMap(
            Map<BedsheetType, Map<BedSize, BigDecimal>> source) {
        Map<String, Map<String, BigDecimal>> result = new LinkedHashMap<>();
        for (BedsheetType type : BedsheetType.values()) {
            Map<String, BigDecimal> bySize = new LinkedHashMap<>();
            for (BedSize size : BedSize.values()) {
                Map<BedSize, BigDecimal> inner = source.get(type);
                bySize.put(size.name(), inner != null ? inner.get(size) : BigDecimal.ZERO);
            }
            result.put(type.name(), bySize);
        }
        return result;
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
