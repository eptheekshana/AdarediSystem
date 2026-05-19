package com.bedsheet.orders.controller;

import com.bedsheet.orders.curfox.CurfoxOrderSyncService;
import com.bedsheet.orders.curfox.dto.CurfoxTrackingEvent;
import com.bedsheet.orders.model.CustomerOrder;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders/{orderId}/curfox")
public class CurfoxController {

    private final CurfoxOrderSyncService curfoxOrderSyncService;

    public CurfoxController(CurfoxOrderSyncService curfoxOrderSyncService) {
        this.curfoxOrderSyncService = curfoxOrderSyncService;
    }

    @PostMapping("/sync")
    public CustomerOrder sync(@PathVariable Long orderId) {
        return curfoxOrderSyncService.syncOrder(orderId);
    }

    @GetMapping("/tracking")
    public List<CurfoxTrackingEvent> tracking(@PathVariable Long orderId) {
        return curfoxOrderSyncService.fetchTracking(orderId);
    }

}
