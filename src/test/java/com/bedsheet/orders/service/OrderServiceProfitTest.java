package com.bedsheet.orders.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.bedsheet.orders.dto.OrderItemRequest;
import com.bedsheet.orders.dto.OrderRequest;
import com.bedsheet.orders.model.BedSize;
import com.bedsheet.orders.model.BedsheetType;
import com.bedsheet.orders.model.CustomerOrder;
import com.bedsheet.orders.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceProfitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private com.bedsheet.orders.curfox.CurfoxOrderSyncService curfoxOrderSyncService;

    @Mock
    private com.bedsheet.orders.curfox.CurfoxProperties curfoxProperties;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        // ensure curfox integration flags are mocked to avoid NPEs during tests
        when(curfoxProperties.isAutoSyncOnCreate()).thenReturn(false);
        when(curfoxOrderSyncService.isIntegrationAvailable()).thenReturn(false);

        orderService = new OrderService(orderRepository, new PricingService(), curfoxOrderSyncService, curfoxProperties);
    }

    @Test
    void discountSubtractsFromProfit() {
        when(orderRepository.existsByWaybillNo("WB-1")).thenReturn(false);
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderRequest request = baseOrder();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setBedsheetType(BedsheetType.FLAT);
        item1.setBedSize(BedSize.SINGLE);
        item1.setBeddingDesign("Rose");
        item1.setQuantity(1);

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setBedsheetType(BedsheetType.FITTED);
        item2.setBedSize(BedSize.KING);
        item2.setBeddingDesign("Stripe");
        item2.setQuantity(1);

        request.setItems(List.of(item1, item2));
        request.setDiscount(new BigDecimal("200"));

        CustomerOrder saved = orderService.create(request);

        assertEquals(new BigDecimal("3700"), saved.getTotalSellingPrice());
        assertEquals(new BigDecimal("2170"), saved.getGrossProfit());
        assertEquals(new BigDecimal("1970"), saved.getNetProfit());
    }

    private static OrderRequest baseOrder() {
        OrderRequest request = new OrderRequest();
        request.setWaybillNo("WB-1");
        request.setCustomerName("Test Customer");
        request.setPrimaryPhone("0771234567");
        request.setAddress("123 Main St");
        request.setNearestCity("Colombo");
        request.setDistrict("Colombo");
        return request;
    }
}
