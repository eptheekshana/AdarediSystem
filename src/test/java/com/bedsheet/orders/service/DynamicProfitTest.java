package com.bedsheet.orders.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.bedsheet.orders.dto.OrderItemRequest;
import com.bedsheet.orders.dto.OrderRequest;
import com.bedsheet.orders.dto.PricingRuleUpdateDto;
import com.bedsheet.orders.model.BedSize;
import com.bedsheet.orders.model.BedsheetType;
import com.bedsheet.orders.model.CustomerOrder;
import com.bedsheet.orders.model.OrderItem;
import com.bedsheet.orders.model.PricingRule;
import com.bedsheet.orders.repository.OrderRepository;
import com.bedsheet.orders.repository.PricingRuleRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DynamicProfitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PricingRuleRepository pricingRuleRepository;

    @Mock
    private com.bedsheet.orders.curfox.CurfoxOrderSyncService curfoxOrderSyncService;

    @Mock
    private com.bedsheet.orders.curfox.CurfoxProperties curfoxProperties;

    private PricingService pricingService;
    private OrderService orderService;
    private final List<PricingRule> databaseRules = new ArrayList<>();

    @BeforeEach
    void setUp() {
        databaseRules.clear();
        when(curfoxProperties.isAutoSyncOnCreate()).thenReturn(false);
        when(curfoxOrderSyncService.isIntegrationAvailable()).thenReturn(false);

        // Mock in-memory behavior for pricingRuleRepository
        when(pricingRuleRepository.findAllByOrderByIdAsc()).thenAnswer(inv -> new ArrayList<>(databaseRules));
        when(pricingRuleRepository.findByItemKey(any())).thenAnswer(inv -> {
            String key = inv.getArgument(0);
            return databaseRules.stream().filter(r -> r.getItemKey().equalsIgnoreCase(key)).findFirst();
        });
        when(pricingRuleRepository.save(any(PricingRule.class))).thenAnswer(inv -> {
            PricingRule rule = inv.getArgument(0);
            databaseRules.removeIf(r -> r.getItemKey().equalsIgnoreCase(rule.getItemKey()));
            databaseRules.add(rule);
            return rule;
        });

        pricingService = new PricingService(pricingRuleRepository);
        orderService = new OrderService(orderRepository, pricingService, curfoxOrderSyncService, curfoxProperties);
    }

    @Test
    void userCanChangeProfitAndOnlyNewOrdersReflectChangeWhilePastOrdersRemainUnchanged() {
        when(orderRepository.existsByWaybillNo(any())).thenReturn(false);
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        // 1. Initial State: Flat Single has standard profit of 750
        assertEquals(new BigDecimal("750"), pricingService.getUnitProfit(BedsheetType.FLAT, BedSize.SINGLE));

        // 2. Customer creates Order 1 with 1x Flat Single
        OrderRequest order1Req = new OrderRequest();
        order1Req.setWaybillNo("WB-PAST-001");
        order1Req.setCustomerName("Customer Past");
        order1Req.setPrimaryPhone("0771112222");
        order1Req.setAddress("Colombo");
        order1Req.setNearestCity("Colombo");
        order1Req.setDistrict("Colombo");

        OrderItemRequest item1Req = new OrderItemRequest();
        item1Req.setBedsheetType(BedsheetType.FLAT);
        item1Req.setBedSize(BedSize.SINGLE);
        item1Req.setBeddingDesign("Rose Pattern");
        item1Req.setQuantity(1);
        order1Req.setItems(List.of(item1Req));

        CustomerOrder pastOrder = orderService.create(order1Req);
        pastOrder.setId(101L);

        // Verify past order has profit 750
        assertEquals(new BigDecimal("1400"), pastOrder.getTotalSellingPrice());
        assertEquals(new BigDecimal("750"), pastOrder.getGrossProfit());
        assertEquals(new BigDecimal("750"), pastOrder.getNetProfit());
        assertEquals(new BigDecimal("750"), pastOrder.getItems().get(0).getUnitProfit());

        // 3. User changes Flat Single profit from 750 to 950
        pricingService.updateRule("FLAT_SINGLE", new BigDecimal("1500"), new BigDecimal("950"));

        // Verify PricingService now serves the new profit rate
        assertEquals(new BigDecimal("1500"), pricingService.getSellingPrice(BedsheetType.FLAT, BedSize.SINGLE));
        assertEquals(new BigDecimal("950"), pricingService.getUnitProfit(BedsheetType.FLAT, BedSize.SINGLE));

        // 4. VERIFY PREVIOUS ORDER PROFIT DID NOT CHANGE
        assertEquals(new BigDecimal("750"), pastOrder.getGrossProfit(), "Past order gross profit must remain 750!");
        assertEquals(new BigDecimal("750"), pastOrder.getNetProfit(), "Past order net profit must remain 750!");
        assertEquals(new BigDecimal("750"), pastOrder.getItems().get(0).getUnitProfit(), "Past order item unit profit must remain 750!");

        // 5. New Order 2 is created after profit change
        OrderRequest order2Req = new OrderRequest();
        order2Req.setWaybillNo("WB-NEW-002");
        order2Req.setCustomerName("Customer New");
        order2Req.setPrimaryPhone("0773334444");
        order2Req.setAddress("Kandy");
        order2Req.setNearestCity("Kandy");
        order2Req.setDistrict("Kandy");

        OrderItemRequest item2Req = new OrderItemRequest();
        item2Req.setBedsheetType(BedsheetType.FLAT);
        item2Req.setBedSize(BedSize.SINGLE);
        item2Req.setBeddingDesign("Rose Pattern");
        item2Req.setQuantity(1);
        order2Req.setItems(List.of(item2Req));

        CustomerOrder newOrder = orderService.create(order2Req);

        // Verify new order uses the NEW profit 950 and new selling price 1500
        assertEquals(new BigDecimal("1500"), newOrder.getTotalSellingPrice());
        assertEquals(new BigDecimal("950"), newOrder.getGrossProfit());
        assertEquals(new BigDecimal("950"), newOrder.getNetProfit());
        assertEquals(new BigDecimal("950"), newOrder.getItems().get(0).getUnitProfit());

        // Verify past order still has 750
        assertEquals(new BigDecimal("750"), pastOrder.getNetProfit());

        // 6. User edits Past Order 1 (e.g. updating phone number, keeping existing line items)
        when(orderRepository.findWithItemsById(101L)).thenReturn(Optional.of(pastOrder));

        OrderRequest editPastOrderReq = new OrderRequest();
        editPastOrderReq.setWaybillNo("WB-PAST-001");
        editPastOrderReq.setCustomerName("Customer Past (Edited Phone)");
        editPastOrderReq.setPrimaryPhone("0779998888");
        editPastOrderReq.setAddress("Colombo");
        editPastOrderReq.setNearestCity("Colombo");
        editPastOrderReq.setDistrict("Colombo");

        // The edit form retains the historical item's unit selling and profit
        OrderItem pastItem = pastOrder.getItems().get(0);
        OrderItemRequest editItemReq = new OrderItemRequest();
        editItemReq.setBedsheetType(pastItem.getBedsheetType());
        editItemReq.setBedSize(pastItem.getBedSize());
        editItemReq.setBeddingDesign(pastItem.getBeddingDesign());
        editItemReq.setQuantity(pastItem.getQuantity());
        editItemReq.setUnitSellingPrice(pastItem.getUnitSellingPrice()); // 1400
        editItemReq.setUnitProfit(pastItem.getUnitProfit());             // 750
        editPastOrderReq.setItems(List.of(editItemReq));

        CustomerOrder updatedPastOrder = orderService.update(101L, editPastOrderReq);

        // Verify that even after updating the past order, its profit is NOT overwritten with 950!
        assertEquals("0779998888", updatedPastOrder.getPrimaryPhone());
        assertEquals(new BigDecimal("1400"), updatedPastOrder.getTotalSellingPrice());
        assertEquals(new BigDecimal("750"), updatedPastOrder.getGrossProfit(), "Past order profit must NOT change to 950 on edit!");
        assertEquals(new BigDecimal("750"), updatedPastOrder.getNetProfit(), "Past order net profit must NOT change to 950 on edit!");
        assertEquals(new BigDecimal("750"), updatedPastOrder.getItems().get(0).getUnitProfit());
    }

    @Test
    void userCanBulkUpdateRulesAndResetToDefaults() {
        List<PricingRuleUpdateDto> updates = List.of(
                new PricingRuleUpdateDto("FLAT_DOUBLE", new BigDecimal("1700"), new BigDecimal("1000")),
                new PricingRuleUpdateDto("PILLOWCASE", new BigDecimal("250"), new BigDecimal("150"))
        );

        pricingService.updateRules(updates);

        assertEquals(new BigDecimal("1700"), pricingService.getSellingPrice(BedsheetType.FLAT, BedSize.DOUBLE));
        assertEquals(new BigDecimal("1000"), pricingService.getUnitProfit(BedsheetType.FLAT, BedSize.DOUBLE));
        assertEquals(new BigDecimal("250"), pricingService.getPillowcaseSellingPrice());
        assertEquals(new BigDecimal("150"), pricingService.getPillowcaseUnitProfit());

        // Reset to factory defaults
        pricingService.resetToDefaults();

        assertEquals(new BigDecimal("1600"), pricingService.getSellingPrice(BedsheetType.FLAT, BedSize.DOUBLE));
        assertEquals(new BigDecimal("900"), pricingService.getUnitProfit(BedsheetType.FLAT, BedSize.DOUBLE));
        assertEquals(new BigDecimal("220"), pricingService.getPillowcaseSellingPrice());
        assertEquals(new BigDecimal("125"), pricingService.getPillowcaseUnitProfit());
    }
}
