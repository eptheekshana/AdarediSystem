# Quick Start: TiDB Setup for Bedsheet Order Profit

## Step 1: Set Up TiDB Cluster

### Option A: TiDB Cloud (Recommended for Quick Start)

1. Go to [tidbcloud.com](https://tidbcloud.com) and sign up
2. Create a new project
3. Click **"Create a Cluster"**
4. Choose **Serverless Tier** (free tier available with automatic scaling)
5. Wait for cluster to be ready (5-10 minutes)
6. Click on your cluster to see connection details

### Option B: Self-Hosted TiDB

```bash
# Use Docker
docker run -d \
  --name tidb \
  -p 4000:4000 \
  pingcap/tidb:latest
```

## Step 2: Get Connection Details

From TiDB Cloud console or your self-hosted instance, note:
- **Host**: `your-cluster-id.tidbcloud.com` (Cloud) or `localhost` (Docker)
- **Port**: `4000`
- **User**: `root` or your username
- **Password**: Your set password
- **Database**: Create `bedsheet_db`

## Step 3: Configure Your Application

### Method A: Using Environment Variables (Recommended)

```bash
# For local development
export TIDB_HOST="your-host"
export TIDB_PORT="4000"
export TIDB_USER="root"
export TIDB_PASSWORD="your_password"
export TIDB_DB="bedsheet_db"

# The helper script will set SPRING_DATASOURCE_* variables
```

### Method B: Create .env File

```bash
cat > .env << 'EOF'
# TiDB Configuration
TIDB_HOST=your-cluster-id.tidbcloud.com
TIDB_PORT=4000
TIDB_USER=root
TIDB_PASSWORD=your_password
TIDB_DB=bedsheet_db

# Application
SPRING_DATASOURCE_URL=jdbc:mysql://your-cluster-id.tidbcloud.com:4000/bedsheet_db?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_password
EOF
```

### Method C: Direct Java Properties

Edit `src/main/resources/application-tidb.properties`:
```properties
spring.datasource.url=jdbc:mysql://your-host:4000/bedsheet_db?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
```

## Step 4: Initialize Database (Option A - Recommended)

Use the helper script:

```bash
# Make script executable
chmod +x docs/tidb-migrate.sh

# Run full migration (automated)
TIDB_HOST="your-host" \
TIDB_USER="root" \
TIDB_PASSWORD="your_password" \
TIDB_DB="bedsheet_db" \
bash docs/tidb-migrate.sh full-migration
```

## Step 4B: Initialize Database (Option B - Manual)

```bash
# Connect to TiDB
mysql -h your-host -P 4000 -u root -p

# Inside mysql client:
CREATE DATABASE IF NOT EXISTS bedsheet_db;
USE bedsheet_db;
```

Then run schema and data:
```bash
mysql -h your-host -P 4000 -u root -p bedsheet_db < sql/schema.sql
mysql -h your-host -P 4000 -u root -p bedsheet_db < src/main/resources/data.sql
```

## Step 5: Build the Application

```bash
cd /home/eptheekshana/Projects/bedsheet-order-profit
mvn clean install -DskipTests
```

## Step 6: Run the Application

```bash
# Set environment variables
export SPRING_DATASOURCE_URL="jdbc:mysql://your-host:4000/bedsheet_db?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true"
export SPRING_DATASOURCE_USERNAME="root"
export SPRING_DATASOURCE_PASSWORD="your_password"

# Run
mvn spring-boot:run

# OR use the built JAR
java -jar target/order-profit-1.0.0.jar
```

## Step 7: Verify Everything Works

### Check Application Startup
```
HikariPool - Starting pool
Hibernate: select 1
INFO  [c.b.o.c.EnvironmentPostProcessor] : Loaded from .env
```

### Test APIs

```bash
# Get dashboard
curl http://localhost:8080/dashboard

# Get all orders
curl http://localhost:8080/api/orders

# View in browser
# Dashboard: http://localhost:8080/dashboard
# New Order: http://localhost:8080/orders/new
# Orders List: http://localhost:8080/orders
```

## Troubleshooting

### Connection Error: "Connection refused"
```
✗ Verify TiDB cluster is running
✗ Check Host/Port is correct
✗ Ensure firewall allows outbound to TiDB port 4000
```

### SSL Error: "CERTIFICATE_VERIFY_FAILED"
```
✓ Add to connection string: useSSL=true
✓ Add: allowPublicKeyRetrieval=true
```

### Timeout Error
```
✓ Check internet connection
✓ Verify TiDB cluster status
✓ Increase connection timeout:
   spring.datasource.hikari.connection-timeout=60000
```

### Database Not Found
```
✓ Verify TIDB_DB variable is set
✓ Check database exists:
   mysql -h host -u user -p -e "SHOW DATABASES;"
```

## Performance Tips

1. **Local Development**: Use H2 (zero setup)
   ```bash
   # H2 is default if SPRING_DATASOURCE_URL is not set
   mvn spring-boot:run
   ```

2. **Production**: Use TiDB Cloud with proper connection pooling
   ```properties
   spring.datasource.hikari.maximum-pool-size=20
   spring.datasource.hikari.minimum-idle=5
   ```

3. **Query Optimization**: 
   - TiDB automatically analyzes queries
   - Use indexes (already created in schema.sql)
   - Monitor slow logs in TiDB console

## Next Steps

1. See **[TIDB_MIGRATION_GUIDE.md](TIDB_MIGRATION_GUIDE.md)** for detailed migration information
2. Configure **Curfox/Royal Express** integration in `.env` (optional)
3. Set up **logging** and **monitoring** for production

## Support Resources

- **TiDB Cloud Dashboard**: https://tidbcloud.com/console
- **TiDB Documentation**: https://docs.pingcap.com/tidb/stable
- **Connection Issues**: https://docs.pingcap.com/tidbcloud/select-cluster-tier
- **Performance Tuning**: https://docs.pingcap.com/tidb/stable/performance-tuning-overview

## Clean Up

To remove TiDB (if needed):
- **TiDB Cloud**: Delete cluster from console
- **Docker**: `docker stop tidb && docker rm tidb`


