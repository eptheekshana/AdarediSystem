package com.bedsheet.orders.curfox;

import com.bedsheet.orders.curfox.dto.CurfoxCity;
import com.bedsheet.orders.curfox.dto.CurfoxTrackingEvent;
import com.bedsheet.orders.model.CurfoxSyncStatus;
import com.bedsheet.orders.model.CustomerOrder;
import com.bedsheet.orders.repository.OrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurfoxOrderSyncService {

    private static final BigDecimal DELIVERY_CHARGE_FREE_LIMIT = BigDecimal.valueOf(400);

    private final CurfoxProperties properties;
    private final CurfoxClient curfoxClient;
    private final OrderRepository orderRepository;

    public CurfoxOrderSyncService(
            CurfoxProperties properties,
            CurfoxClient curfoxClient,
            OrderRepository orderRepository) {
        this.properties = properties;
        this.curfoxClient = curfoxClient;
        this.orderRepository = orderRepository;
    }

    public boolean isIntegrationAvailable() {
        return properties.isConfigured();
    }

    @Transactional
    public CustomerOrder syncOrder(Long orderId) {
        CustomerOrder order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (!properties.isConfigured()) {
            markFailed(order, "Curfox is not configured. Set CURFOX_* variables in .env");
            return orderRepository.save(order);
        }

        try {
            Map<String, Object> general = buildGeneralData();
            Map<String, Object> line = buildOrderLine(order);
            List<String> created = curfoxClient.createSingleOrder(general, line);

            order.setCurfoxSyncStatus(CurfoxSyncStatus.SYNCED);
            order.setCurfoxSyncedAt(LocalDateTime.now());
            order.setCurfoxSyncMessage(
                    created.isEmpty() ? "Synced to Royal Express" : "Synced: " + String.join(", ", created));
            refreshLatestStatus(order);
            syncDeliveryChargeFromRoyalExpress(order);
            recalculateNetProfit(order);
        } catch (Exception ex) {
            markFailed(order, ex.getMessage());
        }

        return orderRepository.save(order);
    }

    @Transactional
    public List<CurfoxTrackingEvent> fetchTracking(Long orderId) {
        CustomerOrder order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (!properties.isConfigured()) {
            throw new CurfoxApiException("Curfox is not configured");
        }
        if (order.getCurfoxSyncStatus() != CurfoxSyncStatus.SYNCED) {
            throw new CurfoxApiException("Order is not synced to Royal Express yet");
        }

        List<CurfoxTrackingEvent> events = curfoxClient.getTracking(order.getWaybillNo());
        if (!events.isEmpty()) {
            order.setCurfoxLastStatus(events.get(0).getStatusName());
        }
        syncDeliveryChargeFromRoyalExpress(order);
        recalculateNetProfit(order);
        orderRepository.save(order);
        return events;
    }

    @Transactional
    public void refreshLatestStatus(CustomerOrder order) {
        try {
            List<CurfoxTrackingEvent> events = curfoxClient.getTracking(order.getWaybillNo());
            if (!events.isEmpty()) {
                order.setCurfoxLastStatus(events.get(0).getStatusName());
            }
        } catch (Exception ignored) {
            // tracking optional after sync
        }
    }

    private Map<String, Object> buildGeneralData() {
        Map<String, Object> general = new LinkedHashMap<>();
        general.put("merchant_business_id", properties.getMerchantBusinessId());
        general.put("origin_city_name", properties.getOriginCityName());
        general.put("origin_state_name", properties.getOriginStateName());
        return general;
    }

    private Map<String, Object> buildOrderLine(CustomerOrder order) {
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("waybill_number", order.getWaybillNo());
        line.put("order_no", String.valueOf(order.getId() != null ? order.getId() : order.getWaybillNo()));
        line.put("customer_name", order.getCustomerName());
        line.put("customer_address", order.getAddress());
        line.put("customer_phone", order.getPrimaryPhone());
        if (order.getSecondaryPhone() != null && !order.getSecondaryPhone().isBlank()) {
            line.put("customer_secondary_phone", order.getSecondaryPhone());
        }
        applyDestination(line, order);
        line.put("cod", toCodInteger(order.getCodValue()));
        line.put("weight", toWeight(order.getWeight()));
        line.put("description", buildDescription(order));
        line.put("remark", "ADAREDI.COM order");
        return line;
    }

    private static int toCodInteger(BigDecimal cod) {
        if (cod == null) {
            return 0;
        }
        return cod.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private static double toWeight(BigDecimal weight) {
        if (weight == null || weight.compareTo(BigDecimal.ZERO) <= 0) {
            return 1.0;
        }
        return weight.doubleValue();
    }

    private void applyDestination(Map<String, Object> line, CustomerOrder order) {
        Optional<CurfoxCity> resolved = curfoxClient.findBestCityMatch(order.getNearestCity());
        if (resolved.isPresent()) {
            CurfoxCity city = resolved.get();
            line.put("destination_city_id", city.getId());
            line.put("destination_city_name", city.getName());
            line.put("destination_state_name", city.getStateName());
            return;
        }
        line.put("destination_city_name", order.getNearestCity());
        line.put("destination_state_name", order.getDistrict());
    }

    private void syncDeliveryChargeFromRoyalExpress(CustomerOrder order) {
        try {
            Optional<BigDecimal> charge = curfoxClient.fetchDeliveryChargeByWaybill(order.getWaybillNo());
            if (charge.isEmpty()) {
                return;
            }
            order.setDeliveryCharge(charge.get());
            String base = order.getCurfoxSyncMessage();
            String suffix = "Delivery charge from Royal Express: LKR " + charge.get().stripTrailingZeros().toPlainString();
            order.setCurfoxSyncMessage(truncate(base == null || base.isBlank() ? suffix : base + " | " + suffix));
        } catch (Exception ex) {
            String base = order.getCurfoxSyncMessage();
            String suffix = "Could not fetch delivery charge: " + ex.getMessage();
            order.setCurfoxSyncMessage(truncate(base == null || base.isBlank() ? suffix : base + " | " + suffix));
        }
    }

    private void recalculateNetProfit(CustomerOrder order) {
        BigDecimal grossProfit = order.getGrossProfit() == null ? BigDecimal.ZERO : order.getGrossProfit();
        BigDecimal discount = order.getDiscount() == null ? BigDecimal.ZERO : order.getDiscount();
        BigDecimal deliveryCharge = order.getDeliveryCharge() == null ? BigDecimal.ZERO : order.getDeliveryCharge();
        BigDecimal deliveryDeduction = deliveryCharge.subtract(DELIVERY_CHARGE_FREE_LIMIT);
        if (deliveryDeduction.compareTo(BigDecimal.ZERO) < 0) {
            deliveryDeduction = BigDecimal.ZERO;
        }
        BigDecimal netProfit = grossProfit.subtract(discount).subtract(deliveryDeduction);
        if (netProfit.compareTo(BigDecimal.ZERO) < 0) {
            netProfit = BigDecimal.ZERO;
        }
        order.setNetProfit(netProfit);
    }

    private static String buildDescription(CustomerOrder order) {
        return order.getItems().stream()
                .map(item -> item.getQuantity() + "x "
                        + item.getBedsheetType() + " "
                        + item.getBedSize().name().replace('_', ' ')
                        + " — "
                        + item.getBeddingDesign())
                .collect(Collectors.joining("; "));
    }

    private static void markFailed(CustomerOrder order, String message) {
        order.setCurfoxSyncStatus(CurfoxSyncStatus.FAILED);
        order.setCurfoxSyncMessage(truncate(message));
    }

    private static String truncate(String value) {
        if (value == null) {
            return null;
        }
        int max = 500;
        return value.length() <= max ? value : value.substring(0, max);
    }
}
