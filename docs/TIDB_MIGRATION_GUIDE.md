# TiDB Migration Guide

This guide explains how to migrate your Bedsheet Order Profit application from H2 database to TiDB.

## What is TiDB?

TiDB is a distributed MySQL-compatible SQL database. Since it's MySQL compatible, you can leverage your existing MySQL connector and JPA configuration with minimal changes.

## Prerequisites

1. **TiDB Cluster**: Set up a TiDB cluster (Cloud or Self-hosted)
   - [TiDB Cloud](https://tidbcloud.com/) - Managed service (recommended for quick start)
   - [Self-hosted TiDB](https://docs.pingcap.com/tidb/stable/quick-start-with-tidb) - On your own infrastructure

2. **Connection Details**: You'll need:
   - Host/Endpoint
   - Port (typically 4000 for TiDB)
   - Database name
   - Username
   - Password

## Migration Steps

### Step 1: Update Application Dependencies

Your project already has `mysql-connector-j` which is compatible with TiDB. No new dependencies are required, but verify the version is up-to-date:

Current in `pom.xml`:
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.1.0</version>
    <scope>runtime</scope>
</dependency>
```

**Optional**: Update to latest version if needed:
```bash
mvn versions:display-dependency-updates | grep mysql-connector-j
```

### Step 2: Configure Application Properties

The application already uses `spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect`, which is perfect for TiDB.

**Update your database connection** in `application.properties` or via environment variables:

#### Option A: Using Application Properties
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://tidb-host:4000/bedsheet_db?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=your_tidb_user
spring.datasource.password=your_tidb_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

#### Option B: Using Environment Variables (Recommended)
Set environment variables before starting the application:

```bash
# For local development
export SPRING_DATASOURCE_URL="jdbc:mysql://tidb-host:4000/bedsheet_db?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true"
export SPRING_DATASOURCE_USERNAME="your_tidb_user"
export SPRING_DATASOURCE_PASSWORD="your_tidb_password"
```

#### Option C: On Heroku/Cloud Deployment
Add config vars:
```bash
# Using Heroku CLI:
heroku config:set SPRING_DATASOURCE_URL="jdbc:mysql://tidb-host:4000/bedsheet_db?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true"
heroku config:set SPRING_DATASOURCE_USERNAME="your_tidb_user"
heroku config:set SPRING_DATASOURCE_PASSWORD="your_tidb_password"
```

### Step 3: Create TiDB Database

Connect to your TiDB cluster and create the database:

```sql
CREATE DATABASE IF NOT EXISTS bedsheet_db;
USE bedsheet_db;
```

### Step 4: Schema Migration

TiDB is MySQL-compatible, so your existing schema works without modification.

The application will automatically create/update the schema using Hibernate since `spring.jpa.hibernate.ddl-auto=update` is set.

**However, for better control**, you can manually run the schema:

```bash
# Connect to TiDB
mysql -h <tidb-host> -P 4000 -u <user> -p bedsheet_db < sql/schema.sql
```

### Step 5: Data Migration (Optional)

If you have existing data in H2 that you want to migrate:

#### Option A: Using Flyway or Liquibase
Add migration versioning tool to pom.xml:

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

#### Option B: Manual Export/Import
1. **Export from H2**:
```sql
-- Connect to H2 and export data
SELECT * FROM orders;
SELECT * FROM order_items;
```

2. **Insert into TiDB**:
```sql
-- Use the data.sql format and execute against TiDB
INSERT INTO orders (...) VALUES (...);
INSERT INTO order_items (...) VALUES (...);
```

#### Option C: Using mysqldump (if migrating from MySQL)
```bash
mysqldump -h source-host -u user -p database | mysql -h tidb-host -P 4000 -u tidb-user -p bedsheet_db
```

#### Option D: DigitalOcean MySQL → TiDB Cloud (recommended for this app)
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

After the import finishes, connect the Spring Boot application to TiDB by setting:

```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/test?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true"
export SPRING_DATASOURCE_USERNAME="oPm9e8BieWrryDE.root"
export SPRING_DATASOURCE_PASSWORD="your_tidb_password"
```

### Step 6: Update Hibernate Dialect (Optional Enhancement)

Consider updating to a more modern TiDB-aware dialect. Create a custom dialect or use:

Update `pom.xml` to use TiDB-specific connector (optional):
```xml
<!-- Optional: TiDB specific connector for better compatibility -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.2.0</version>
    <scope>runtime</scope>
</dependency>
```

### Step 7: Test the Connection

1. **Build the application**:
```bash
mvn clean install
```

2. **Run locally**:
```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:4000/bedsheet_db?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true"
export SPRING_DATASOURCE_USERNAME="root"
export SPRING_DATASOURCE_PASSWORD="your_password"

mvn spring-boot:run
```

3. **Verify connectivity**: Check logs for:
```
HikariPool - Starting pool
Hibernate: show tables
```

4. **Test the API**:
```bash
curl http://localhost:8080/orders
curl http://localhost:8080/dashboard
```

## TiDB Connection String Details

The JDBC URL pattern for TiDB:
```
jdbc:mysql://host:port/database?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

**Key Parameters**:
- `host`: TiDB cluster host
- `port`: TiDB cluster port (default: 4000)
- `database`: Your database name
- `useSSL=true`: Enable SSL/TLS (recommended for TiDB Cloud)
- `serverTimezone=UTC`: Prevent timezone issues
- `allowPublicKeyRetrieval=true`: Allow public key retrieval for authentication

## TiDB Cloud Setup (Quick Start)

1. Create account at [tidbcloud.com](https://tidbcloud.com)
2. Create a cluster (Serverless or Dedicated)
3. Get connection string from cluster console
4. Database will be pre-created; get the name from connection details
5. Set environment variables and restart application

## Important Notes

1. **Connection String**: TiDB uses port 4000, not the standard MySQL 3306
2. **SSL**: TiDB Cloud requires SSL by default
3. **Transaction Isolation**: TiDB supports ACID transactions like MySQL
4. **Compatibility**: Your current JPA queries should work without modification
5. **Fallback**: Keep H2 as fallback for local development by checking if `SPRING_DATASOURCE_URL` is set

## Rollback Strategy

If you need to revert:
1. Backup TiDB data
2. Reset environment variables to H2
3. Application will fall back to H2

## Performance Optimization

For TiDB, consider:
1. Increase connection pool size (for distributed queries)
2. Use read-only replicas for analytics queries
3. Enable query result cache for frequently accessed data

```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

## Troubleshooting

### Connection Refused
```
Check TiDB cluster status and firewall rules
Verify host, port, and credentials
```

### SSL/TLS Errors
```
Ensure useSSL=true in connection string
Add allowPublicKeyRetrieval=true parameter
```

### Timezone Issues
```
Add serverTimezone=UTC to connection string
```

### Slow Queries
```
Check TiDB Slow Query Log
Verify indexes are created (CREATE INDEX IF NOT EXISTS)
```

## See Also

- [TiDB Documentation](https://docs.pingcap.com/tidb/stable)
- [TiDB MySQL Compatibility](https://docs.pingcap.com/tidb/stable/mysql-compatibility)
- [TiDB Connection Troubleshooting](https://docs.pingcap.com/tidbcloud/select-cluster-tier)


