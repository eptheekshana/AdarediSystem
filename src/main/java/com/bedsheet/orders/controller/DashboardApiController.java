package com.bedsheet.orders.controller;

import com.bedsheet.orders.dto.dashboard.DashboardView;
import com.bedsheet.orders.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    private final DashboardService dashboardService;

    public DashboardApiController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardView get(@RequestParam(defaultValue = "30") int days) {
        int periodDays = days <= 0 ? 0 : days;
        return dashboardService.buildDashboard(periodDays);
    }
}
