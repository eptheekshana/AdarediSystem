package com.bedsheet.orders.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.bedsheet.orders.service.PricingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PricingPageController.class)
class PricingPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PricingService pricingService;

    @Test
    void getPricingPageReturns200AndRendersView() throws Exception {
        org.mockito.Mockito.when(pricingService.getAllRules())
                .thenReturn(PricingService.defaultRuleList());

        mockMvc.perform(get("/pricing"))
                .andExpect(status().isOk())
                .andExpect(view().name("pricing/index"))
                .andExpect(model().attributeExists("flatRules"))
                .andExpect(model().attributeExists("fittedRules"))
                .andExpect(model().attributeExists("addonRules"))
                .andExpect(model().attributeExists("pricingForm"))
                .andExpect(model().attributeExists("avgMargin"));
    }

    @Test
    void postPricingUpdatesAndRedirects() throws Exception {
        mockMvc.perform(post("/pricing")
                        .param("rules[0].itemKey", "FLAT_SINGLE")
                        .param("rules[0].sellingPrice", "1500")
                        .param("rules[0].unitProfit", "850"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pricing"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void postResetPricingRedirects() throws Exception {
        mockMvc.perform(post("/pricing/reset"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pricing"))
                .andExpect(flash().attributeExists("successMessage"));
    }
}
