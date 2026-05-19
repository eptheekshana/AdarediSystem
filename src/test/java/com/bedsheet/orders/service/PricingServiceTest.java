package com.bedsheet.orders.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bedsheet.orders.model.BedSize;
import com.bedsheet.orders.model.BedsheetType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PricingServiceTest {

    private final PricingService pricingService = new PricingService();

    @Test
    void flatDoublePrices() {
        assertEquals(new BigDecimal("1600"), pricingService.getSellingPrice(BedsheetType.FLAT, BedSize.DOUBLE));
        assertEquals(new BigDecimal("900"), pricingService.getUnitProfit(BedsheetType.FLAT, BedSize.DOUBLE));
    }

    @Test
    void fittedQueenUses2150NotTypo() {
        assertEquals(new BigDecimal("2150"), pricingService.getSellingPrice(BedsheetType.FITTED, BedSize.QUEEN));
        assertEquals(new BigDecimal("1320"), pricingService.getUnitProfit(BedsheetType.FITTED, BedSize.QUEEN));
    }
}
