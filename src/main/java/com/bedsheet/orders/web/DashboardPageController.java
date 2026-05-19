package com.bedsheet.orders.web;

import com.bedsheet.orders.dto.dashboard.DashboardView;
import com.bedsheet.orders.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardPageController {

    private final DashboardService dashboardService;

    public DashboardPageController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "30") int days, Model model) {
        int periodDays = normalizeDays(days);
        DashboardView dashboard = dashboardService.buildDashboard(periodDays);
        model.addAttribute("dashboard", dashboard);
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
