package com.bedsheet.orders.controller;

import com.bedsheet.orders.dto.OrderRequest;
import com.bedsheet.orders.model.CustomerOrder;
import com.bedsheet.orders.service.OrderService;
import com.bedsheet.orders.service.PricingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bedsheet.orders.dto.PricingRuleUpdateDto;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final PricingService pricingService;

    public OrderController(OrderService orderService, PricingService pricingService) {
        this.orderService = orderService;
        this.pricingService = pricingService;
    }

    @GetMapping("/pricing")
    public Map<String, Object> pricing() {
        return Map.of(
                "sellingPrices", pricingService.getAllSellingPrices(),
                "unitProfits", pricingService.getAllUnitProfits(),
                "pillowcaseSellingPrice", pricingService.getPillowcaseSellingPrice(),
                "pillowcaseUnitProfit", pricingService.getPillowcaseUnitProfit(),
                "rules", pricingService.getAllRules());
    }

    @PutMapping("/pricing")
    public Map<String, Object> updatePricing(@RequestBody List<PricingRuleUpdateDto> updates) {
        pricingService.updateRules(updates);
        return pricing();
    }

    @PostMapping("/pricing/reset")
    public Map<String, Object> resetPricing() {
        pricingService.resetToDefaults();
        return pricing();
    }

    @GetMapping
    public List<CustomerOrder> list() {
        return orderService.findAll();
    }

    @GetMapping("/{id}")
    public CustomerOrder get(@PathVariable Long id) {
        return orderService.findById(id);
    }

    @GetMapping("/waybill/{waybillNo}")
    public CustomerOrder getByWaybill(@PathVariable String waybillNo) {
        return orderService.findByWaybill(waybillNo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerOrder create(@Valid @RequestBody OrderRequest request) {
        return orderService.create(request);
    }

    /**
     * Delete an order by its ID along with all associated order items.
     * @param id Order ID to delete
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        orderService.deleteById(id);
    }

    /**
     * Delete an order by its waybill number along with all associated order items.
     * @param waybillNo Waybill number to delete
     */
    @DeleteMapping("/waybill/{waybillNo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByWaybill(@PathVariable String waybillNo) {
        orderService.deleteByWaybillNo(waybillNo);
    }

    /**
     * Delete all orders and their items.
     * WARNING: This action cannot be undone! Use with extreme caution.
     * @return Confirmation message with count of deleted orders
     */
    @DeleteMapping
    public Map<String, Object> deleteAll() {
        long deletedCount = orderService.deleteAll();
        return Map.of(
                "message", "All orders have been deleted successfully",
                "deletedCount", deletedCount
        );
    }
}
