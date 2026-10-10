-- Realistic sample data for local development & H2 preview
INSERT INTO orders (id, created_at, waybill_no, customer_name, primary_phone, secondary_phone, address, nearest_city, district, weight, delivery_charge, cod_value, discount, total_selling_price, gross_profit, net_profit, curfox_sync_status, curfox_sync_message, curfox_synced_at, curfox_last_status)
VALUES 
(1, '2026-10-05 10:15:00', 'WB89210', 'Kasun Perera', '0771234567', '0719876543', 'No 45, Temple Road', 'Kadawatha', 'Gampaha', 1.50, 450.00, 4350.00, 100.00, 4000.00, 2440.00, 2340.00, 'SYNCED', 'Dispatched successfully', '2026-10-05 10:20:00', 'DELIVERED'),
(2, '2026-10-06 14:30:00', 'WB89211', 'Dilani Jayawardena', '0763456789', NULL, '12/B Lake Crescent', 'Kandy', 'Kandy', 2.10, 550.00, 4850.00, 0.00, 4300.00, 2740.00, 2740.00, 'SYNCED', 'Out for delivery', '2026-10-06 14:35:00', 'OUT_FOR_DELIVERY'),
(3, '2026-10-07 11:00:00', 'WB89212', 'Nimal Shantha', '0714567890', '0772345678', '88 Main Street', 'Maharagama', 'Colombo', 1.20, 400.00, 2550.00, 50.00, 2200.00, 1370.00, 1320.00, 'SYNCED', 'In transit', '2026-10-07 11:05:00', 'IN_TRANSIT'),
(4, '2026-10-08 16:45:00', 'WB89213', 'Amali Fernando', '0755678901', NULL, '34 Galle Road', 'Panadura', 'Kalutara', 3.00, 500.00, 6800.00, 200.00, 6500.00, 3960.00, 3760.00, 'NOT_SYNCED', NULL, NULL, NULL),
(5, '2026-10-09 09:20:00', 'WB89214', 'Chathura Silva', '0786789012', '0701239874', '56 Flower Terrace', 'Kurunegala', 'Kurunegala', 1.80, 550.00, 4650.00, 0.00, 4100.00, 2520.00, 2520.00, 'SYNCED', 'Picked up by courier', '2026-10-09 09:25:00', 'PICKED_UP'),
(6, '2026-10-10 12:10:00', 'WB89215', 'Ruwanthi Wickramasinghe', '0727890123', NULL, '102 Beach Road', 'Galle', 'Galle', 2.40, 500.00, 5200.00, 150.00, 4850.00, 2840.00, 2690.00, 'SYNCED', 'Manifest created', '2026-10-10 12:15:00', 'PROCESSING');

INSERT INTO order_items (id, order_id, bedsheet_type, bed_size, bedding_design, quantity, pillowcase_quantity, unit_selling_price, unit_profit, line_selling_price, line_profit)
VALUES
(1, 1, 'FITTED', 'QUEEN', 'Royal Mandala', 1, 2, 2150.00, 1320.00, 2590.00, 1570.00),
(2, 1, 'FLAT', 'SINGLE', 'Royal Mandala', 1, 0, 1400.00, 750.00, 1400.00, 750.00),
(3, 2, 'FITTED', 'KING', 'Midnight Flora', 1, 2, 2300.00, 1420.00, 2740.00, 1670.00),
(4, 2, 'FLAT', 'DOUBLE', 'Ocean Waves', 1, 0, 1600.00, 900.00, 1600.00, 900.00),
(5, 3, 'FITTED', 'DOUBLE', 'Nordic Fern', 1, 1, 2000.00, 1300.00, 2220.00, 1425.00),
(6, 4, 'FITTED', 'SUPER_KING', 'Royal Mandala', 1, 2, 2450.00, 1500.00, 2890.00, 1750.00),
(7, 4, 'FLAT', 'KING', 'Velvet Bloom', 1, 2, 1900.00, 1020.00, 2340.00, 1270.00),
(8, 4, 'FLAT', 'SINGLE', 'Nordic Fern', 1, 0, 1400.00, 750.00, 1400.00, 750.00),
(9, 5, 'FITTED', 'QUEEN', 'Ocean Waves', 1, 2, 2150.00, 1320.00, 2590.00, 1570.00),
(10, 5, 'FLAT', 'DOUBLE', 'Midnight Flora', 1, 0, 1600.00, 900.00, 1600.00, 900.00),
(11, 6, 'FITTED', 'KING', 'Royal Mandala', 1, 2, 2300.00, 1420.00, 2740.00, 1670.00),
(12, 6, 'FLAT', 'QUEEN', 'Velvet Bloom', 1, 2, 1750.00, 920.00, 2190.00, 1170.00);
