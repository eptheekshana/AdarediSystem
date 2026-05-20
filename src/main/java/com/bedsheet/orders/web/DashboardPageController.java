package com.bedsheet.orders.web;

import com.bedsheet.orders.dto.dashboard.DashboardView;
import com.bedsheet.orders.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardPageController {

    private final DashboardService dashboardService;
    private static final Logger log = LoggerFactory.getLogger(DashboardPageController.class);

    public DashboardPageController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "30") int days, Model model) {
        int periodDays = normalizeDays(days);
        try {
            DashboardView dashboard = dashboardService.buildDashboard(periodDays);
            model.addAttribute("dashboard", dashboard);
        } catch (Exception ex) {
            // Log with stacktrace so Heroku logs include the underlying cause for the 500
            log.error("Failed to build dashboard for periodDays={}", periodDays, ex);
            throw ex;
        }
        model.addAttribute("periodDays", periodDays);
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
