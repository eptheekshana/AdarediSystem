package com.bedsheet.orders.service;

import com.bedsheet.orders.curfox.CurfoxOrderSyncService;
import com.bedsheet.orders.curfox.CurfoxProperties;
import com.bedsheet.orders.dto.OrderItemRequest;
import com.bedsheet.orders.dto.OrderRequest;
import com.bedsheet.orders.model.CurfoxSyncStatus;
import com.bedsheet.orders.model.CustomerOrder;
import com.bedsheet.orders.model.OrderItem;
import com.bedsheet.orders.model.PaymentMethod;
import com.bedsheet.orders.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final BigDecimal PILLOWCASE_UNIT_PRICE = BigDecimal.valueOf(220);
    private static final BigDecimal PILLOWCASE_UNIT_PROFIT = BigDecimal.valueOf(125);
    private static final BigDecimal DELIVERY_CHARGE_FREE_LIMIT = BigDecimal.valueOf(400);

    private final OrderRepository orderRepository;
    private final PricingService pricingService;
    private final CurfoxOrderSyncService curfoxOrderSyncService;
    private final CurfoxProperties curfoxProperties;

    public OrderService(
            OrderRepository orderRepository,
            PricingService pricingService,
            CurfoxOrderSyncService curfoxOrderSyncService,
            CurfoxProperties curfoxProperties) {
        this.orderRepository = orderRepository;
        this.pricingService = pricingService;
        this.curfoxOrderSyncService = curfoxOrderSyncService;
        this.curfoxProperties = curfoxProperties;
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> findAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public CustomerOrder findById(Long id) {
        return orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));
    }

    @Transactional(readOnly = true)
    public CustomerOrder findByWaybill(String waybillNo) {
        return orderRepository.findWithItemsByWaybillNo(waybillNo)
                .orElseThrow(() -> new OrderNotFoundException("Order not found for waybill: " + waybillNo));
    }

    @Transactional
    public CustomerOrder create(OrderRequest request) {
        if (orderRepository.existsByWaybillNo(request.getWaybillNo())) {
            throw new DuplicateWaybillException("Waybill already exists: " + request.getWaybillNo());
        }

        CustomerOrder order = new CustomerOrder();
        applyOrderFields(order, request);

        for (OrderItemRequest itemRequest : request.getItems()) {
            order.addItem(buildItem(itemRequest));
        }

        calculateTotals(order);
        CustomerOrder saved = orderRepository.save(order);

        if (shouldSyncToCurfox(request)) {
            return curfoxOrderSyncService.syncOrder(saved.getId());
        }

        return saved;
    }

    @Transactional
    public CustomerOrder update(Long id, OrderRequest request) {
        CustomerOrder order = findById(id);
        String requestedWaybill = request.getWaybillNo().trim();
        if (!order.getWaybillNo().equals(requestedWaybill) && orderRepository.existsByWaybillNo(requestedWaybill)) {
            throw new DuplicateWaybillException("Waybill already exists: " + request.getWaybillNo());
        }

        applyOrderFields(order, request);
        order.getItems().clear();
        for (OrderItemRequest itemRequest : request.getItems()) {
            order.addItem(buildItem(itemRequest));
        }

        calculateTotals(order);
        resetCurfoxState(order);
        return orderRepository.save(order);
    }

    private boolean shouldSyncToCurfox(OrderRequest request) {
        return request.isPushToCurfox()
                && curfoxProperties.isAutoSyncOnCreate()
                && curfoxOrderSyncService.isIntegrationAvailable();
    }

    private void applyOrderFields(CustomerOrder order, OrderRequest request) {
        order.setWaybillNo(request.getWaybillNo().trim());
        order.setCustomerName(request.getCustomerName().trim());
        order.setPrimaryPhone(request.getPrimaryPhone().trim());
        order.setSecondaryPhone(trimToNull(request.getSecondaryPhone()));
        order.setAddress(request.getAddress().trim());
        order.setNearestCity(request.getNearestCity().trim());
        order.setDistrict(request.getDistrict().trim());
        order.setWeight(request.getWeight());
        if (request.getCreatedAt() != null) {
            order.setCreatedAt(request.getCreatedAt());
        }
        order.setDeliveryCharge(zeroIfNull(request.getDeliveryCharge()));
        order.setCodValue(zeroIfNull(request.getCodValue()));
        order.setDiscount(zeroIfNull(request.getDiscount()));
        order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.CASH_ON_DELIVERY);
    }

    private OrderItem buildItem(OrderItemRequest request) {
        BigDecimal unitSelling = pricingService.getSellingPrice(request.getBedsheetType(), request.getBedSize());
        BigDecimal unitProfit = pricingService.getUnitProfit(request.getBedsheetType(), request.getBedSize());
        int quantity = Math.max(request.getQuantity(), 1);

        OrderItem item = new OrderItem();
        item.setBedsheetType(request.getBedsheetType());
        item.setBedSize(request.getBedSize());
        item.setBeddingDesign(request.getBeddingDesign().trim());
        item.setQuantity(quantity);
        int pillowcaseQuantity = Math.max(request.getPillowcaseQuantity(), 0);
        item.setPillowcaseQuantity(pillowcaseQuantity);
        item.setUnitSellingPrice(unitSelling);
        item.setUnitProfit(unitProfit);
        BigDecimal bedsheetSell = unitSelling.multiply(BigDecimal.valueOf(quantity));
        BigDecimal bedsheetProfit = unitProfit.multiply(BigDecimal.valueOf(quantity));
        BigDecimal pillowcaseSell = PILLOWCASE_UNIT_PRICE.multiply(BigDecimal.valueOf(pillowcaseQuantity));
        BigDecimal pillowcaseProfit = PILLOWCASE_UNIT_PROFIT.multiply(BigDecimal.valueOf(pillowcaseQuantity));
        item.setLineSellingPrice(bedsheetSell.add(pillowcaseSell));
        item.setLineProfit(bedsheetProfit.add(pillowcaseProfit));
        return item;
    }

    private void calculateTotals(CustomerOrder order) {
        BigDecimal totalSelling = BigDecimal.ZERO;
        BigDecimal grossProfit = BigDecimal.ZERO;

        for (OrderItem item : order.getItems()) {
            totalSelling = totalSelling.add(item.getLineSellingPrice());
            grossProfit = grossProfit.add(item.getLineProfit());
        }

        BigDecimal discount = zeroIfNull(order.getDiscount());
        BigDecimal deliveryCharge = zeroIfNull(order.getDeliveryCharge());
        BigDecimal deliveryDeduction = deliveryCharge.subtract(DELIVERY_CHARGE_FREE_LIMIT);
        if (deliveryDeduction.compareTo(BigDecimal.ZERO) < 0) {
            deliveryDeduction = BigDecimal.ZERO;
        }

        // Calculate payment method deduction
        BigDecimal paymentMethodDeduction = calculatePaymentMethodDeduction(order, totalSelling, deliveryCharge);

        BigDecimal netProfit = grossProfit.subtract(discount).subtract(deliveryDeduction).subtract(paymentMethodDeduction);
        if (netProfit.compareTo(BigDecimal.ZERO) < 0) {
            netProfit = BigDecimal.ZERO;
        }

        order.setTotalSellingPrice(totalSelling);
        order.setGrossProfit(grossProfit);
        order.setNetProfit(netProfit);
    }

    private BigDecimal calculatePaymentMethodDeduction(CustomerOrder order, BigDecimal totalSelling, BigDecimal deliveryCharge) {
        PaymentMethod paymentMethod = order.getPaymentMethod();
        if (paymentMethod == null || paymentMethod == PaymentMethod.CASH_ON_DELIVERY || paymentMethod == PaymentMethod.BANK_TRANSFER) {
            return BigDecimal.ZERO;
        }

        // Total value = product prices + delivery charge
        BigDecimal totalValue = totalSelling.add(deliveryCharge);
        double deductionPercent = paymentMethod.getProfitDeductionPercent();
        BigDecimal deduction = totalValue.multiply(BigDecimal.valueOf(deductionPercent)).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        return deduction;
    }

    private static void resetCurfoxState(CustomerOrder order) {
        order.setCurfoxSyncStatus(CurfoxSyncStatus.NOT_SYNCED);
        order.setCurfoxSyncedAt(null);
        order.setCurfoxSyncMessage(null);
        order.setCurfoxLastStatus(null);
    }

    private static BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Delete an order by ID along with all its items.
     * @param id Order ID to delete
     * @throws OrderNotFoundException if order doesn't exist
     */
    @Transactional
    public void deleteById(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException("Order not found: " + id);
        }
        orderRepository.deleteById(id);
    }

    /**
     * Delete an order by waybill number along with all its items.
     * @param waybillNo Waybill number to delete
     * @throws OrderNotFoundException if order doesn't exist
     */
    @Transactional
    public void deleteByWaybillNo(String waybillNo) {
        CustomerOrder order = orderRepository.findWithItemsByWaybillNo(waybillNo)
                .orElseThrow(() -> new OrderNotFoundException("Order not found for waybill: " + waybillNo));
        orderRepository.delete(order);
    }

    /**
     * Delete all orders and their items.
     * WARNING: This action cannot be undone! Use with caution.
     * @return number of orders deleted
     */
    @Transactional
    public long deleteAll() {
        long count = orderRepository.count();
        orderRepository.deleteAll();
        return count;
    }
}
