# ADAREDI.COM — Order & Profit System

Spring Boot + MySQL application for **ADAREDI.COM** to track courier orders, multiple bedsheet line items, selling prices, and profit (with discount).

## Features

- **Order fields:** waybill no, name, primary/secondary phone, address, nearest city, district, weight, delivery charge, COD value, discount
- **Multiple products per order:** flat/fitted, bed size, bedding design, quantity
- **Auto pricing:** selling price and unit profit from your fixed price list
- **Profit:** `gross profit = sum(line profits)`, `net profit = gross profit − discount`
- **Analytics dashboard** at `/dashboard` (KPIs, charts, trends)
- **Web UI** at `/orders/new` and **REST API** at `/api/orders`

## Price list (LKR)

| Type   | Size       | Selling | Profit |
|--------|------------|---------|--------|
| Flat   | Single     | 1400    | 750    |
| Flat   | Double     | 1600    | 900    |
| Flat   | Queen      | 1750    | 920    |
| Flat   | King       | 1900    | 1020   |
| Flat   | Super King | 2050    | 1100   |
| Fitted | Single     | 1800    | 1150   |
| Fitted | Double     | 2000    | 1300   |
| Fitted | Queen      | 2150    | 1320   |
| Fitted | King       | 2300    | 1420   |
| Fitted | Super King | 2450    | 1500   |

> Fitted Queen selling price is **2150** (corrected from a common typo 21500).

## Prerequisites

- Java 17+
- MySQL 8+ or TiDB Cloud (MySQL-compatible)
- Maven (or use `~/.maven/maven-3.9.16/bin/mvn`)

## Setup

1. Create database (optional — app can create it if the user has permission):

```bash
mysql -u root -p < sql/schema.sql
```

2. **Database credentials** — copy the example env file and fill in values (or use the provided `.env` if already set up):

   ```bash
   cp .env.example .env
   ```

   Edit `.env` with your MySQL or TiDB details. The app loads this file automatically at startup (`.env` is gitignored).

   ```env
   SPRING_DATASOURCE_URL=jdbc:mysql://gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/test?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
   SPRING_DATASOURCE_USERNAME=oPm9e8BieWrryDE.root
   SPRING_DATASOURCE_PASSWORD=your_password
   SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
   ```

   Tables (`orders`, `order_items`) are created automatically in the target database on first run (`spring.jpa.hibernate.ddl-auto=update`).

   If you are moving data from DigitalOcean MySQL into TiDB Cloud, use the helper script:

   ```bash
   export SOURCE_DB_HOST="your-digitalocean-host"
   export SOURCE_DB_PORT="25060"
   export SOURCE_DB_NAME="defaultdb"
   export SOURCE_DB_USER="doadmin"
   export SOURCE_DB_PASSWORD="your_source_password"

   export TIDB_HOST="gateway01.ap-southeast-1.prod.aws.tidbcloud.com"
   export TIDB_PORT="4000"
   export TIDB_DB="test"
   export TIDB_USER="oPm9e8BieWrryDE.root"
   export TIDB_PASSWORD="your_tidb_password"

   bash docs/tidb-migrate.sh migrate-from-mysql
   ```

3. Build and run:

```bash
cd /home/eptheekshana/Projects/bedsheet-order-profit
~/.maven/maven-3.9.16/bin/mvn spring-boot:run
```

4. Open **http://localhost:8080/dashboard** (analytics) or **http://localhost:8080/orders/new** (new order)

## TiDB Cloud connection example

For the TiDB Cloud URI you provided, use this JDBC format in the application:

```bash
SPRING_DATASOURCE_URL="jdbc:mysql://gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/test?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true"
SPRING_DATASOURCE_USERNAME="oPm9e8BieWrryDE.root"
SPRING_DATASOURCE_PASSWORD="<PASSWORD>"
```

Set these in your deployment environment or `.env`, then restart the app.

## REST API examples

**Create order**

```bash
curl -s -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "waybillNo": "WB1001",
    "customerName": "John Doe",
    "primaryPhone": "0771234567",
    "address": "12 Temple Road",
    "nearestCity": "Kandy",
    "district": "Kandy",
    "deliveryCharge": 350,
    "codValue": 3350,
    "discount": 100,
    "items": [
      {
        "bedsheetType": "FLAT",
        "bedSize": "QUEEN",
        "beddingDesign": "Floral Blue",
        "quantity": 1
      },
      {
        "bedsheetType": "FITTED",
        "bedSize": "KING",
        "beddingDesign": "Stripe Grey",
        "quantity": 2
      }
    ]
  }'
```

## Royal Express (Curfox) integration

Orders can be pushed to [Royal Express](https://royalexpress.merchant.curfox.com) via the Curfox Merchant API.

Configure in `.env` (see `.env.example`). **Quote the password if it contains `#`** — otherwise everything after `#` is treated as a comment:

```env
CURFOX_PASSWORD="your#password"
```

- `CURFOX_TENANT=royalexpress`
- `CURFOX_EMAIL` / `CURFOX_PASSWORD` — merchant login
- `CURFOX_MERCHANT_BUSINESS_ID` — use **9212** for **Adaredi.com**
- `CURFOX_ORIGIN_CITY_NAME` / `CURFOX_ORIGIN_STATE_NAME` — pickup location (must match Curfox city names exactly)
- `CURFOX_LISTS_API_BASE_URL` — defaults to `https://v2-lists.api.curfox.com/api` (used to read `delivery_charge` after sync)

After an order is pushed to Royal Express, **delivery charge is fetched automatically** from Curfox and saved on the order. Use **Refresh tracking** on the order detail page to update it again later.

**UI:** checkbox on new order form · sync & tracking on order detail · courier status on order list  

**API:**

- `POST /api/orders/{id}/curfox/sync` — push order to Royal Express  
- `GET /api/orders/{id}/curfox/tracking` — tracking timeline  

**List orders:** `GET /api/orders`  
**Get by id:** `GET /api/orders/{id}`  
**Get by waybill:** `GET /api/orders/waybill/{waybillNo}`  
**Price matrix:** `GET /api/orders/pricing`

## Project location

`/home/eptheekshana/Projects/bedsheet-order-profit`
