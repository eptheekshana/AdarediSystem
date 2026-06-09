-- Minimal H2-compatible schema to allow the application to start when H2 fallback is used on Heroku.
-- This schema contains only the columns referenced by analytics queries and dashboard.

CREATE TABLE IF NOT EXISTS orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  created_at TIMESTAMP,
  waybill_no VARCHAR(255),
  customer_name VARCHAR(255),
  district VARCHAR(255),
  nearest_city VARCHAR(255),
  cod_value DECIMAL(15,2) DEFAULT 0,
  delivery_charge DECIMAL(15,2) DEFAULT 0,
  gross_profit DECIMAL(15,2) DEFAULT 0,
  net_profit DECIMAL(15,2) DEFAULT 0,
  discount DECIMAL(15,2) DEFAULT 0,
  total_selling_price DECIMAL(15,2) DEFAULT 0,
  payment_method VARCHAR(20) DEFAULT 'CASH_ON_DELIVERY'
);

CREATE TABLE IF NOT EXISTS order_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT,
  bedsheet_type VARCHAR(100),
  bed_size VARCHAR(100),
  quantity INT DEFAULT 0,
  line_selling_price DECIMAL(15,2) DEFAULT 0,
  line_profit DECIMAL(15,2) DEFAULT 0,
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Simple index to speed up date range queries used by analytics
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);

