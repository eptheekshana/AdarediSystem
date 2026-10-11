package com.bedsheet.orders.web;

import com.bedsheet.orders.curfox.CurfoxOrderSyncService;
import com.bedsheet.orders.curfox.CurfoxProperties;
import com.bedsheet.orders.curfox.dto.CurfoxTrackingEvent;
import com.bedsheet.orders.dto.OrderItemRequest;
import com.bedsheet.orders.dto.OrderRequest;
import com.bedsheet.orders.model.BedSize;
import com.bedsheet.orders.model.BedsheetType;
import com.bedsheet.orders.model.CurfoxSyncStatus;
import com.bedsheet.orders.model.CustomerOrder;
import com.bedsheet.orders.model.OrderItem;
import com.bedsheet.orders.service.DuplicateWaybillException;
import com.bedsheet.orders.service.OrderNotFoundException;
import com.bedsheet.orders.service.OrderService;
import com.bedsheet.orders.service.PricingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderPageController {

    private final OrderService orderService;
    private final PricingService pricingService;
    private final CurfoxOrderSyncService curfoxOrderSyncService;
    private final CurfoxProperties curfoxProperties;
    private final ObjectMapper objectMapper;

    public OrderPageController(
            OrderService orderService,
            PricingService pricingService,
            CurfoxOrderSyncService curfoxOrderSyncService,
            CurfoxProperties curfoxProperties,
            ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.pricingService = pricingService;
        this.curfoxOrderSyncService = curfoxOrderSyncService;
        this.curfoxProperties = curfoxProperties;
        this.objectMapper = objectMapper;
    }

    @GetMapping({"/", "/home", "/index"})
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/orders")
    public String list(Model model) {
        List<CustomerOrder> orders = orderService.findAll();
        model.addAttribute("orders", orders);
        model.addAttribute("curfoxEnabled", curfoxOrderSyncService.isIntegrationAvailable());
        return "orders/list";
    }

    @GetMapping("/orders/new")
    public String newOrderForm(Model model) throws JsonProcessingException {
        prepareFormModel(model, sampleOrder(), false, null);
        return "orders/form";
    }

    @PostMapping("/orders")
    public String create(
            @Valid @ModelAttribute("order") OrderRequest order,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) throws JsonProcessingException {
        if (bindingResult.hasErrors()) {
            prepareFormModel(model, order, false, null);
            return "orders/form";
        }

        try {
            CustomerOrder saved = orderService.create(order);
            String message = "Order saved. Total selling: LKR " + saved.getTotalSellingPrice()
                    + " | Net profit: LKR " + saved.getNetProfit();
            if (saved.getCurfoxSyncStatus() == CurfoxSyncStatus.SYNCED) {
                message += " | Pushed to Royal Express";
                if (saved.getDeliveryCharge() != null && saved.getDeliveryCharge().signum() > 0) {
                    message += " | Delivery charge: LKR " + saved.getDeliveryCharge();
                }
            } else if (saved.getCurfoxSyncStatus() == CurfoxSyncStatus.FAILED) {
                message += " | Royal Express sync failed: " + saved.getCurfoxSyncMessage();
            }
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/orders/" + saved.getId();
        } catch (DuplicateWaybillException ex) {
            bindingResult.rejectValue("waybillNo", "duplicate", ex.getMessage());
            prepareFormModel(model, order, false, null);
            return "orders/form";
        }
    }

    @GetMapping("/orders/{id}/edit")
    public String edit(@PathVariable Long id, Model model) throws JsonProcessingException {
        CustomerOrder order = orderService.findById(id);
        prepareFormModel(model, toFormOrder(order), true, id);
        return "orders/form";
    }

    @PostMapping("/orders/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("order") OrderRequest order,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) throws JsonProcessingException {
        if (bindingResult.hasErrors()) {
            prepareFormModel(model, order, true, id);
            return "orders/form";
        }

        try {
            CustomerOrder saved = orderService.update(id, order);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Order updated successfully. Courier sync was reset; re-sync if needed.");
            return "redirect:/orders/" + saved.getId();
        } catch (DuplicateWaybillException ex) {
            bindingResult.rejectValue("waybillNo", "duplicate", ex.getMessage());
            prepareFormModel(model, order, true, id);
            return "orders/form";
        }
    }

    @GetMapping("/orders/{id}")
    public String detail(@PathVariable Long id, Model model) {
        CustomerOrder order = orderService.findById(id);
        model.addAttribute("order", order);
        model.addAttribute("curfoxEnabled", curfoxOrderSyncService.isIntegrationAvailable());
        return "orders/detail";
    }

    @PostMapping("/orders/{id}/curfox/sync")
    public String syncCurfox(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        CustomerOrder order = curfoxOrderSyncService.syncOrder(id);
        if (order.getCurfoxSyncStatus() == CurfoxSyncStatus.SYNCED) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Synced to Royal Express (Curfox). " + order.getCurfoxSyncMessage());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Royal Express sync failed: " + order.getCurfoxSyncMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/orders/{id}/curfox/tracking")
    public String refreshTracking(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            List<CurfoxTrackingEvent> tracking = curfoxOrderSyncService.fetchTracking(id);
            CustomerOrder order = orderService.findById(id);
            model.addAttribute("order", order);
            model.addAttribute("tracking", tracking);
            model.addAttribute("curfoxEnabled", curfoxOrderSyncService.isIntegrationAvailable());
            return "orders/detail";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/orders/" + id;
        }
    }

    @PostMapping("/orders/{id}/delete")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            CustomerOrder order = orderService.findById(id);
            String waybillNo = order.getWaybillNo();
            orderService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Order '" + waybillNo + "' has been deleted successfully.");
            return "redirect:/orders";
        } catch (OrderNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/orders";
        }
    }

    private void prepareFormModel(Model model, OrderRequest order, boolean editMode, Long orderId) throws JsonProcessingException {
        model.addAttribute("order", order);
        model.addAttribute("bedsheetTypes", BedsheetType.values());
        model.addAttribute("bedSizes", BedSize.values());
        model.addAttribute("priceRows", pricingService.getPriceRows());
        model.addAttribute("isEditMode", editMode);
        model.addAttribute("pageTitle", editMode ? "Edit order" : "Create order");
        model.addAttribute("submitLabel", editMode ? "Update order" : "Save order");
        model.addAttribute("formAction", editMode ? "/orders/" + orderId : "/orders");
        model.addAttribute("cancelHref", editMode ? "/orders/" + orderId : "/orders");
        addCurfoxContext(model);
        model.addAttribute("sellingPricesJson",
                objectMapper.writeValueAsString(pricingService.getSellingPricesForJson()));
        model.addAttribute("unitProfitsJson",
                objectMapper.writeValueAsString(pricingService.getUnitProfitsForJson()));
        model.addAttribute("pillowcasePrice", pricingService.getPillowcaseSellingPrice());
        model.addAttribute("pillowcaseProfit", pricingService.getPillowcaseUnitProfit());
    }

    private void addCurfoxContext(Model model) {
        boolean enabled = curfoxOrderSyncService.isIntegrationAvailable();
        model.addAttribute("curfoxEnabled", enabled);
        if (enabled) {
            String business = curfoxProperties.getMerchantBusinessName();
            if (business == null || business.isBlank()) {
                business = "Business ID " + curfoxProperties.getMerchantBusinessId();
            }
            model.addAttribute("curfoxBusinessName", business);
            model.addAttribute("curfoxOrigin",
                    curfoxProperties.getOriginCityName() + ", " + curfoxProperties.getOriginStateName());
        }
    }

    private static OrderRequest sampleOrder() {
        OrderRequest order = new OrderRequest();
        order.setPushToCurfox(true);
        order.setCreatedAt(java.time.LocalDateTime.now());
        OrderItemRequest item = new OrderItemRequest();
        item.setBedsheetType(BedsheetType.FLAT);
        item.setBedSize(BedSize.DOUBLE);
        item.setQuantity(1);
        order.setItems(Arrays.asList(item));
        return order;
    }

    private static OrderRequest toFormOrder(CustomerOrder order) {
        OrderRequest request = new OrderRequest();
        request.setWaybillNo(order.getWaybillNo());
        request.setCustomerName(order.getCustomerName());
        request.setPrimaryPhone(order.getPrimaryPhone());
        request.setSecondaryPhone(order.getSecondaryPhone());
        request.setAddress(order.getAddress());
        request.setNearestCity(order.getNearestCity());
        request.setDistrict(order.getDistrict());
        request.setWeight(order.getWeight());
        request.setDeliveryCharge(order.getDeliveryCharge());
        request.setCodValue(order.getCodValue());
        request.setDiscount(order.getDiscount());
        request.setCreatedAt(order.getCreatedAt());
        request.setPushToCurfox(false);
        request.setItems(order.getItems().stream()
                .map(OrderPageController::toFormItem)
                .collect(java.util.stream.Collectors.toList()));
        return request;
    }

    private static OrderItemRequest toFormItem(OrderItem item) {
        OrderItemRequest request = new OrderItemRequest();
        request.setBedsheetType(item.getBedsheetType());
        request.setBedSize(item.getBedSize());
        request.setBeddingDesign(item.getBeddingDesign());
        request.setQuantity(item.getQuantity());
        request.setPillowcaseQuantity(item.getPillowcaseQuantity());
        request.setUnitSellingPrice(item.getUnitSellingPrice());
        request.setUnitProfit(item.getUnitProfit());
        return request;
    }
}
