-- Sample data to populate in-memory H2 for quick verification on Heroku
INSERT INTO orders (id, created_at, waybill_no, customer_name, district, nearest_city, cod_value, delivery_charge, gross_profit, net_profit, discount)
VALUES (1, '2026-05-18 09:30:00', 'WB1001', 'Alice', 'Colombo', 'Colombo', 2500.00, 200.00, 600.00, 400.00, 0.00);

INSERT INTO orders (id, created_at, waybill_no, customer_name, district, nearest_city, cod_value, delivery_charge, gross_profit, net_profit, discount)
VALUES (2, '2026-05-19 11:15:00', 'WB1002', 'Bob', 'Galle', 'Galle', 1800.00, 150.00, 400.00, 300.00, 50.00);

INSERT INTO order_items (id, order_id, bedsheet_type, bed_size, quantity, line_selling_price, line_profit)
VALUES (1, 1, 'Plain', 'Queen', 2, 1250.00, 300.00);

INSERT INTO order_items (id, order_id, bedsheet_type, bed_size, quantity, line_selling_price, line_profit)
VALUES (2, 2, 'Printed', 'Double', 1, 1800.00, 200.00);

