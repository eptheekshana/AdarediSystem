package com.bedsheet.orders.controller;

import com.bedsheet.orders.curfox.CurfoxClient;
import com.bedsheet.orders.curfox.CurfoxOrderSyncService;
import com.bedsheet.orders.curfox.dto.CurfoxCity;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/curfox")
public class CurfoxCityController {

    private final CurfoxClient curfoxClient;
    private final CurfoxOrderSyncService curfoxOrderSyncService;

    public CurfoxCityController(CurfoxClient curfoxClient, CurfoxOrderSyncService curfoxOrderSyncService) {
        this.curfoxClient = curfoxClient;
        this.curfoxOrderSyncService = curfoxOrderSyncService;
    }

    @GetMapping("/cities")
    public List<CurfoxCity> search(@RequestParam("q") String query) {
        if (!curfoxOrderSyncService.isIntegrationAvailable()) {
            return List.of();
        }
        if (query == null || query.length() < 2) {
            return List.of();
        }
        return curfoxClient.findCitiesByName(query).stream().limit(15).toList();
    }
}
