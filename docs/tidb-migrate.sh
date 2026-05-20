#!/bin/bash
# TiDB Migration Helper Script
# This script automates common TiDB migration tasks

set -e

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default connection settings
: "${SOURCE_DB_PORT:=3306}"
: "${SOURCE_DB_SSLMODE:=REQUIRED}"
: "${TIDB_PORT:=4000}"
: "${TIDB_SSLMODE:=REQUIRED}"
: "${TIDB_DB:=bedsheet_db}"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}TiDB Migration Helper${NC}"
echo -e "${GREEN}========================================${NC}"

# Function to print colored output
print_step() {
    echo -e "${YELLOW}[STEP]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

require_var() {
    local name="$1"
    local value="${!name:-}"
    if [ -z "$value" ]; then
        print_error "$name environment variable is not set"
        exit 1
    fi
}

# Check if required tools are installed
check_requirements() {
    print_step "Checking requirements..."

    if ! command -v mysql &> /dev/null; then
        print_error "mysql CLI is not installed"
        echo "Install it with: sudo apt-get install -y mysql-client"
        exit 1
    fi

    if ! command -v mysqldump &> /dev/null; then
        print_error "mysqldump CLI is not installed"
        echo "Install it with: sudo apt-get install -y mysql-client"
        exit 1
    fi

    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed"
        echo "Install it with: sudo apt-get install -y maven"
        exit 1
    fi

    print_success "All requirements are met"
}

# Function to test source MySQL connection
test_source_connection() {
    print_step "Testing source MySQL connection..."

    require_var SOURCE_DB_HOST
    require_var SOURCE_DB_USER
    require_var SOURCE_DB_PASSWORD
    require_var SOURCE_DB_NAME

    if mysql --host "$SOURCE_DB_HOST" --port "$SOURCE_DB_PORT" --user "$SOURCE_DB_USER" --password="$SOURCE_DB_PASSWORD" \
        --ssl-mode="$SOURCE_DB_SSLMODE" --default-character-set=utf8mb4 \
        "$SOURCE_DB_NAME" -e "SELECT 1" &> /dev/null; then
        print_success "Source MySQL connection successful"
    else
        print_error "Failed to connect to source MySQL"
        exit 1
    fi
}

# Function to test TiDB connection
test_connection() {
    print_step "Testing TiDB connection..."

    require_var TIDB_HOST
    require_var TIDB_USER
    require_var TIDB_PASSWORD

    if mysql --host "$TIDB_HOST" --port "$TIDB_PORT" --user "$TIDB_USER" --password="$TIDB_PASSWORD" \
        --ssl-mode="$TIDB_SSLMODE" --default-character-set=utf8mb4 \
        -e "SELECT 1" &> /dev/null; then
        print_success "TiDB connection successful"
    else
        print_error "Failed to connect to TiDB"
        exit 1
    fi
}

# Function to create database
create_database() {
    print_step "Creating database in TiDB..."

    require_var TIDB_HOST
    require_var TIDB_USER
    require_var TIDB_PASSWORD

    mysql --host "$TIDB_HOST" --port "$TIDB_PORT" --user "$TIDB_USER" --password="$TIDB_PASSWORD" \
        --ssl-mode="$TIDB_SSLMODE" --default-character-set=utf8mb4 <<EOF
CREATE DATABASE IF NOT EXISTS ${TIDB_DB} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ${TIDB_DB};
EOF

    print_success "Database created/verified"
}

# Function to create schema
create_schema() {
    print_step "Creating schema in TiDB..."

    require_var TIDB_HOST
    require_var TIDB_USER
    require_var TIDB_PASSWORD

    mysql --host "$TIDB_HOST" --port "$TIDB_PORT" --user "$TIDB_USER" --password="$TIDB_PASSWORD" \
        --ssl-mode="$TIDB_SSLMODE" --default-character-set=utf8mb4 "$TIDB_DB" < sql/schema.sql

    print_success "Schema created successfully"
}

# Function to load sample data
load_sample_data() {
    print_step "Loading sample data into TiDB..."

    require_var TIDB_HOST
    require_var TIDB_USER
    require_var TIDB_PASSWORD

    mysql --host "$TIDB_HOST" --port "$TIDB_PORT" --user "$TIDB_USER" --password="$TIDB_PASSWORD" \
        --ssl-mode="$TIDB_SSLMODE" --default-character-set=utf8mb4 "$TIDB_DB" < src/main/resources/data.sql

    print_success "Sample data loaded successfully"
}

# Function to migrate data from a source MySQL database into TiDB
migrate_from_mysql() {
    print_step "Migrating source MySQL data into TiDB..."

    require_var SOURCE_DB_HOST
    require_var SOURCE_DB_USER
    require_var SOURCE_DB_PASSWORD
    require_var SOURCE_DB_NAME
    require_var TIDB_HOST
    require_var TIDB_USER
    require_var TIDB_PASSWORD

    local dump_file
    dump_file=$(mktemp /tmp/bedsheet-mysql-dump.XXXXXX.sql)

    cleanup() {
        rm -f "$dump_file"
    }
    trap cleanup EXIT

    print_step "Exporting from source database ${SOURCE_DB_HOST}:${SOURCE_DB_PORT}/${SOURCE_DB_NAME}..."
    mysqldump --host "$SOURCE_DB_HOST" --port "$SOURCE_DB_PORT" --user "$SOURCE_DB_USER" \
        --password="$SOURCE_DB_PASSWORD" --ssl-mode="$SOURCE_DB_SSLMODE" \
        --single-transaction --skip-lock-tables --quick --hex-blob --default-character-set=utf8mb4 \
        --column-statistics=0 --set-gtid-purged=OFF "$SOURCE_DB_NAME" > "$dump_file"

    print_step "Creating target TiDB database ${TIDB_HOST}:${TIDB_PORT}/${TIDB_DB}..."
    mysql --host "$TIDB_HOST" --port "$TIDB_PORT" --user "$TIDB_USER" --password="$TIDB_PASSWORD" \
        --ssl-mode="$TIDB_SSLMODE" --default-character-set=utf8mb4 \
        -e "CREATE DATABASE IF NOT EXISTS \`$TIDB_DB\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

    print_step "Importing SQL dump into TiDB..."
    mysql --host "$TIDB_HOST" --port "$TIDB_PORT" --user "$TIDB_USER" --password="$TIDB_PASSWORD" \
        --ssl-mode="$TIDB_SSLMODE" --default-character-set=utf8mb4 "$TIDB_DB" < "$dump_file"

    print_step "Verifying row counts..."
    local source_orders source_items target_orders target_items
    source_orders=$(mysql --host "$SOURCE_DB_HOST" --port "$SOURCE_DB_PORT" --user "$SOURCE_DB_USER" \
        --password="$SOURCE_DB_PASSWORD" --ssl-mode="$SOURCE_DB_SSLMODE" --default-character-set=utf8mb4 \
        -Nse "SELECT COUNT(*) FROM orders" "$SOURCE_DB_NAME")
    source_items=$(mysql --host "$SOURCE_DB_HOST" --port "$SOURCE_DB_PORT" --user "$SOURCE_DB_USER" \
        --password="$SOURCE_DB_PASSWORD" --ssl-mode="$SOURCE_DB_SSLMODE" --default-character-set=utf8mb4 \
        -Nse "SELECT COUNT(*) FROM order_items" "$SOURCE_DB_NAME")
    target_orders=$(mysql --host "$TIDB_HOST" --port "$TIDB_PORT" --user "$TIDB_USER" \
        --password="$TIDB_PASSWORD" --ssl-mode="$TIDB_SSLMODE" --default-character-set=utf8mb4 \
        -Nse "SELECT COUNT(*) FROM orders" "$TIDB_DB")
    target_items=$(mysql --host "$TIDB_HOST" --port "$TIDB_PORT" --user "$TIDB_USER" \
        --password="$TIDB_PASSWORD" --ssl-mode="$TIDB_SSLMODE" --default-character-set=utf8mb4 \
        -Nse "SELECT COUNT(*) FROM order_items" "$TIDB_DB")

    print_success "Source orders: $source_orders | Imported orders: $target_orders"
    print_success "Source items: $source_items | Imported items: $target_items"
    print_success "MySQL -> TiDB migration completed successfully"
}

# Function to build the application
build_app() {
    print_step "Building Spring Boot application..."

    mvn clean install -DskipTests

    print_success "Application built successfully"
}

# Function to setup environment
setup_environment() {
    print_step "Setting up environment variables..."

    require_var TIDB_HOST
    require_var TIDB_USER
    require_var TIDB_PASSWORD

    export SPRING_DATASOURCE_URL="jdbc:mysql://${TIDB_HOST}:${TIDB_PORT}/${TIDB_DB}?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true"
    export SPRING_DATASOURCE_USERNAME="${TIDB_USER}"
    export SPRING_DATASOURCE_PASSWORD="${TIDB_PASSWORD}"
    export SPRING_DATASOURCE_DRIVER_CLASS_NAME="com.mysql.cj.jdbc.Driver"
    export SPRING_JPA_DATABASE_PLATFORM="org.hibernate.dialect.MySQLDialect"

    print_success "Environment variables set:"
    echo "  SPRING_DATASOURCE_URL=$SPRING_DATASOURCE_URL"
    echo "  SPRING_DATASOURCE_USERNAME=$SPRING_DATASOURCE_USERNAME"
}

# Function to run complete migration
full_migration() {
    print_step "Starting full TiDB migration..."

    check_requirements
    test_source_connection
    test_connection
    create_database
    migrate_from_mysql
    build_app
    setup_environment

    print_success "Full migration completed!"
    echo ""
    echo -e "${GREEN}Next steps:${NC}"
    echo "1. Start the application: mvn spring-boot:run"
    echo "2. Access dashboard: http://localhost:8080/dashboard"
    echo "3. View orders: http://localhost:8080/orders"
}

# Menu
show_menu() {
    echo ""
    echo "Select operation:"
    echo "1) Check requirements"
    echo "2) Test source MySQL connection"
    echo "3) Test TiDB connection"
    echo "4) Create TiDB database"
    echo "5) Create schema"
    echo "6) Load sample data"
    echo "7) Migrate source MySQL to TiDB"
    echo "8) Build application"
    echo "9) Setup environment"
    echo "10) Full migration (all steps)"
    echo "11) Exit"
    echo ""
    read -p "Enter your choice [1-11]: " choice
}

# Main
if [ $# -eq 0 ]; then
    # Interactive mode
    while true; do
        show_menu
        case $choice in
            1) check_requirements ;;
            2) test_source_connection ;;
            3) test_connection ;;
            4) create_database ;;
            5) create_schema ;;
            6) load_sample_data ;;
            7) migrate_from_mysql ;;
            8) build_app ;;
            9) setup_environment ;;
            10) full_migration ;;
            11)
                print_success "Exiting..."
                exit 0
                ;;
            *)
                print_error "Invalid choice"
                ;;
        esac
    done
else
    # Command line mode
    case "$1" in
        "check-requirements") check_requirements ;;
        "test-source-connection") test_source_connection ;;
        "test-connection") test_connection ;;
        "create-db") create_database ;;
        "create-schema") create_schema ;;
        "load-data") load_sample_data ;;
        "migrate-from-mysql") migrate_from_mysql ;;
        "build") build_app ;;
        "setup-env") setup_environment ;;
        "full-migration") full_migration ;;
        *)
            echo "Usage: $0 [check-requirements|test-source-connection|test-connection|create-db|create-schema|load-data|migrate-from-mysql|build|setup-env|full-migration]"
            exit 1
            ;;
    esac
fi

