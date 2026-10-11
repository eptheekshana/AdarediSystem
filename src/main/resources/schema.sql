-- H2 & MySQL compatible schema for ADAREDI.COM
CREATE TABLE IF NOT EXISTS orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  created_at TIMESTAMP NOT NULL,
  waybill_no VARCHAR(50) NOT NULL UNIQUE,
  customer_name VARCHAR(150) NOT NULL,
  primary_phone VARCHAR(20) NOT NULL,
  secondary_phone VARCHAR(20),
  address VARCHAR(500) NOT NULL,
  nearest_city VARCHAR(100) NOT NULL,
  district VARCHAR(100) NOT NULL,
  weight DECIMAL(10,2),
  delivery_charge DECIMAL(12,2) DEFAULT 0 NOT NULL,
  cod_value DECIMAL(12,2) DEFAULT 0 NOT NULL,
  discount DECIMAL(12,2) DEFAULT 0 NOT NULL,
  total_selling_price DECIMAL(12,2) DEFAULT 0 NOT NULL,
  gross_profit DECIMAL(12,2) DEFAULT 0 NOT NULL,
  net_profit DECIMAL(12,2) DEFAULT 0 NOT NULL,
  curfox_sync_status VARCHAR(20) DEFAULT 'NOT_SYNCED' NOT NULL,
  curfox_sync_message VARCHAR(500),
  curfox_synced_at TIMESTAMP,
  curfox_last_status VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS order_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  bedsheet_type VARCHAR(10) NOT NULL,
  bed_size VARCHAR(15) NOT NULL,
  bedding_design VARCHAR(200) NOT NULL,
  quantity INT DEFAULT 1 NOT NULL,
  pillowcase_quantity INT DEFAULT 0 NOT NULL,
  unit_selling_price DECIMAL(12,2) NOT NULL,
  unit_profit DECIMAL(12,2) NOT NULL,
  line_selling_price DECIMAL(12,2) NOT NULL,
  line_profit DECIMAL(12,2) NOT NULL,
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);

CREATE TABLE IF NOT EXISTS pricing_rules (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  item_key VARCHAR(50) NOT NULL UNIQUE,
  bedsheet_type VARCHAR(20),
  bed_size VARCHAR(20),
  item_label VARCHAR(100) NOT NULL,
  selling_price DECIMAL(12,2) NOT NULL,
  unit_profit DECIMAL(12,2) NOT NULL,
  updated_at TIMESTAMP
);
