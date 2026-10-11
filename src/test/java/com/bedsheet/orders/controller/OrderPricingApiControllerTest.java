package com.bedsheet.orders.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bedsheet.orders.model.BedSize;
import com.bedsheet.orders.model.BedsheetType;
import com.bedsheet.orders.service.OrderService;
import com.bedsheet.orders.service.PricingService;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
class OrderPricingApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private PricingService pricingService;

    @Test
    void getPricingApiReturnsSellingPricesAndProfits() throws Exception {
        org.mockito.Mockito.when(pricingService.getAllSellingPrices())
                .thenReturn(Map.of(BedsheetType.FLAT, Map.of(BedSize.SINGLE, new BigDecimal("1400"))));
        org.mockito.Mockito.when(pricingService.getAllUnitProfits())
                .thenReturn(Map.of(BedsheetType.FLAT, Map.of(BedSize.SINGLE, new BigDecimal("750"))));
        org.mockito.Mockito.when(pricingService.getPillowcaseSellingPrice())
                .thenReturn(new BigDecimal("220"));
        org.mockito.Mockito.when(pricingService.getPillowcaseUnitProfit())
                .thenReturn(new BigDecimal("125"));
        org.mockito.Mockito.when(pricingService.getAllRules())
                .thenReturn(PricingService.defaultRuleList());

        mockMvc.perform(get("/api/orders/pricing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pillowcaseSellingPrice").value(220))
                .andExpect(jsonPath("$.pillowcaseUnitProfit").value(125))
                .andExpect(jsonPath("$.rules").isArray());
    }

    @Test
    void putPricingApiUpdatesRules() throws Exception {
        String json = """
                [
                  {"itemKey": "FLAT_SINGLE", "sellingPrice": 1500, "unitProfit": 850}
                ]
                """;

        org.mockito.Mockito.when(pricingService.getAllSellingPrices())
                .thenReturn(Map.of());
        org.mockito.Mockito.when(pricingService.getAllUnitProfits())
                .thenReturn(Map.of());
        org.mockito.Mockito.when(pricingService.getPillowcaseSellingPrice())
                .thenReturn(new BigDecimal("220"));
        org.mockito.Mockito.when(pricingService.getPillowcaseUnitProfit())
                .thenReturn(new BigDecimal("125"));
        org.mockito.Mockito.when(pricingService.getAllRules())
                .thenReturn(PricingService.defaultRuleList());

        mockMvc.perform(put("/api/orders/pricing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void postResetPricingApiResetsRules() throws Exception {
        org.mockito.Mockito.when(pricingService.getAllSellingPrices())
                .thenReturn(Map.of());
        org.mockito.Mockito.when(pricingService.getAllUnitProfits())
                .thenReturn(Map.of());
        org.mockito.Mockito.when(pricingService.getPillowcaseSellingPrice())
                .thenReturn(new BigDecimal("220"));
        org.mockito.Mockito.when(pricingService.getPillowcaseUnitProfit())
                .thenReturn(new BigDecimal("125"));
        org.mockito.Mockito.when(pricingService.getAllRules())
                .thenReturn(PricingService.defaultRuleList());

        mockMvc.perform(post("/api/orders/pricing/reset"))
                .andExpect(status().isOk());
    }
}
