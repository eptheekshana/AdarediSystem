package com.bedsheet.orders.web;

import com.bedsheet.orders.dto.dashboard.DashboardView;
import com.bedsheet.orders.service.DashboardService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardPageController {

    private final DashboardService dashboardService;
    private final ObjectMapper objectMapper;

    public DashboardPageController(DashboardService dashboardService, ObjectMapper objectMapper) {
        this.dashboardService = dashboardService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "30") int days, Model model)
            throws JsonProcessingException {
        int periodDays = normalizeDays(days);
        DashboardView dashboard = dashboardService.buildDashboard(periodDays);
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("periodDays", periodDays);
        model.addAttribute("chartDataJson", objectMapper.writeValueAsString(dashboard));
        return "dashboard/index";
    }

    private static int normalizeDays(int days) {
        if (days <= 0) {
            return 0;
        }
        return switch (days) {
            case 7, 30, 90, 365 -> days;
            default -> 30;
        };
    }
}
