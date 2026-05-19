#!/bin/bash

# Bedsheet Order Profit - Delete Order Testing Script
# This script provides useful curl commands to test the delete order features

BASE_URL="http://localhost:8080"
API_URL="$BASE_URL/api/orders"

echo "======================================"
echo "Delete Order Feature - Testing Script"
echo "======================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print section headers
print_section() {
    echo -e "${BLUE}===============================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}===============================================${NC}"
    echo ""
}

# Function to print command
print_command() {
    echo -e "${YELLOW}Command:${NC}"
    echo "  $1"
    echo ""
}

# Function to print explanation
print_explanation() {
    echo -e "${GREEN}Description:${NC}"
    echo "  $1"
    echo ""
}

# Function to print example
print_example() {
    echo -e "${YELLOW}Example:${NC}"
    echo "  $1"
    echo ""
}

# ============================================
# 1. LIST ALL ORDERS
# ============================================
print_section "1. LIST ALL ORDERS (GET)"

print_explanation "Retrieve all orders before testing deletes"

print_command "curl http://localhost:8080/api/orders | jq"

echo "Or in a terminal:"
echo "  curl -s http://localhost:8080/api/orders | jq '.[] | {id, waybillNo, customerName}'"
echo ""

# ============================================
# 2. CREATE TEST ORDER
# ============================================
print_section "2. CREATE TEST ORDER (POST)"

print_explanation "Create a sample order for testing delete functionality"

print_command "curl -X POST http://localhost:8080/api/orders"

cat << 'EOF'
With request body:

{
  "waybillNo": "TEST-2024-001",
  "customerName": "John Doe",
  "primaryPhone": "9876543210",
  "address": "123 Main St, City",
  "nearestCity": "Mumbai",
  "district": "Mumbai",
  "deliveryCharge": 100,
  "codValue": 2000,
  "items": [
    {
      "bedsheetType": "COTTON",
      "bedSize": "QUEEN",
      "beddingDesign": "Floral",
      "quantity": 2,
      "pillowcaseQuantity": 2
    }
  ]
}

Full curl command:

curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "waybillNo": "TEST-2024-001",
    "customerName": "John Doe",
    "primaryPhone": "9876543210",
    "address": "123 Main St, City",
    "nearestCity": "Mumbai",
    "district": "Mumbai",
    "deliveryCharge": 100,
    "codValue": 2000,
    "items": [
      {
        "bedsheetType": "COTTON",
        "bedSize": "QUEEN",
        "beddingDesign": "Floral",
        "quantity": 2,
        "pillowcaseQuantity": 2
      }
    ]
  }' | jq
EOF

echo ""
echo ""

# ============================================
# 3. DELETE ORDER BY ID
# ============================================
print_section "3. DELETE ORDER BY ID (DELETE)"

print_explanation "Delete a single order using its ID"

print_command "curl -X DELETE http://localhost:8080/api/orders/{id}"

print_example "curl -X DELETE http://localhost:8080/api/orders/1"

echo "Expected Response: 204 No Content (empty response body)"
echo ""
echo "After deletion, attempting to retrieve the order should return 404:"
echo "  curl http://localhost:8080/api/orders/1"
echo ""

# ============================================
# 4. DELETE ORDER BY WAYBILL
# ============================================
print_section "4. DELETE ORDER BY WAYBILL (DELETE)"

print_explanation "Delete a single order using its waybill number"

print_command "curl -X DELETE http://localhost:8080/api/orders/waybill/{waybillNo}"

print_example "curl -X DELETE http://localhost:8080/api/orders/waybill/TEST-2024-001"

echo "Expected Response: 204 No Content (empty response body)"
echo ""
echo "After deletion, attempting to retrieve by waybill should return 404:"
echo "  curl http://localhost:8080/api/orders/waybill/TEST-2024-001"
echo ""

# ============================================
# 5. DELETE ALL ORDERS
# ============================================
print_section "5. DELETE ALL ORDERS (DELETE)"

print_explanation "Delete ALL orders and their items from the system"
echo -e "${RED}WARNING: This action cannot be undone!${NC}"

print_command "curl -X DELETE http://localhost:8080/api/orders"

print_example "curl -X DELETE http://localhost:8080/api/orders | jq"

echo "Expected Response:"
echo -e "${YELLOW}{NC}"
echo "  {\"message\": \"All orders have been deleted successfully\", \"deletedCount\": N}"
echo -e "${YELLOW}}${NC}"
echo ""

# ============================================
# 6. ERROR SCENARIOS
# ============================================
print_section "6. ERROR SCENARIOS"

echo -e "${YELLOW}Scenario A: Delete non-existent order${NC}"
print_command "curl -X DELETE http://localhost:8080/api/orders/99999"
echo "Expected Response: 404 Not Found"
echo "Body: \"Order not found: 99999\""
echo ""

echo -e "${YELLOW}Scenario B: Delete by invalid waybill${NC}"
print_command "curl -X DELETE http://localhost:8080/api/orders/waybill/INVALID-123"
echo "Expected Response: 404 Not Found"
echo "Body: \"Order not found for waybill: INVALID-123\""
echo ""

# ============================================
# 7. VERIFICATION STEPS
# ============================================
print_section "7. VERIFICATION STEPS AFTER DELETE"

echo "After deleting an order, verify it's gone:"
echo ""
echo "1. List all orders:"
echo "   curl http://localhost:8080/api/orders | jq"
echo ""
echo "2. Try to get deleted order by ID (should get 404):"
echo "   curl http://localhost:8080/api/orders/1"
echo ""
echo "3. Try to get deleted order by waybill (should get 404):"
echo "   curl http://localhost:8080/api/orders/waybill/TEST-001"
echo ""

# ============================================
# 8. BASH FUNCTION FOR QUICK TESTING
# ============================================
print_section "8. BASH FUNCTIONS FOR QUICK TESTING"

cat << 'EOF'
Add these to your ~/.bashrc or run them in terminal:

# Delete order by ID
delete_order() {
  local id=$1
  if [ -z "$id" ]; then
    echo "Usage: delete_order <order_id>"
    return 1
  fi
  curl -X DELETE "http://localhost:8080/api/orders/$id" -w "\nStatus: %{http_code}\n"
}

# Delete order by waybill
delete_order_by_waybill() {
  local waybill=$1
  if [ -z "$waybill" ]; then
    echo "Usage: delete_order_by_waybill <waybill_no>"
    return 1
  fi
  curl -X DELETE "http://localhost:8080/api/orders/waybill/$waybill" -w "\nStatus: %{http_code}\n"
}

# Delete all orders
delete_all_orders() {
  echo "Are you sure you want to delete ALL orders? (yes/no)"
  read -r confirm
  if [ "$confirm" = "yes" ]; then
    curl -X DELETE "http://localhost:8080/api/orders" -w "\nStatus: %{http_code}\n" | jq
  else
    echo "Cancelled."
  fi
}

# List all orders
list_orders() {
  curl -s http://localhost:8080/api/orders | jq '.[] | {id, waybillNo, customerName, createdAt}'
}

# Usage examples:
# delete_order 5
# delete_order_by_waybill "WB-2024-001"
# delete_all_orders
# list_orders
EOF

echo ""

# ============================================
# 9. VERBOSE CURL COMMANDS
# ============================================
print_section "9. VERBOSE CURL COMMANDS (for debugging)"

echo "Delete with verbose output to see request/response headers:"
echo ""
echo "curl -v -X DELETE http://localhost:8080/api/orders/1"
echo ""
echo "Delete with detailed timing information:"
echo ""
echo "curl -w 'Time: %{time_total}s\\nStatus: %{http_code}\\n' -X DELETE http://localhost:8080/api/orders/1"
echo ""

# ============================================
# 10. COMPLETE TEST WORKFLOW
# ============================================
print_section "10. COMPLETE TEST WORKFLOW"

cat << 'EOF'
Step-by-step testing:

1. Start the application
   mvn spring-boot:run
   (or run from IDE)

2. Create a test order:
   curl -X POST http://localhost:8080/api/orders \
     -H "Content-Type: application/json" \
     -d '{
       "waybillNo": "TEST-1",
       "customerName": "Test Customer",
       "primaryPhone": "9876543210",
       "address": "Test Address",
       "nearestCity": "Mumbai",
       "district": "Mumbai",
       "items": [
         {
           "bedsheetType": "COTTON",
           "bedSize": "QUEEN",
           "beddingDesign": "Test Design",
           "quantity": 1
         }
       ]
     }' | jq

3. Note the order ID (e.g., 1) from the response

4. Verify order exists:
   curl http://localhost:8080/api/orders/1 | jq

5. Delete the order:
   curl -X DELETE http://localhost:8080/api/orders/1

6. Verify it's deleted (should get 404):
   curl http://localhost:8080/api/orders/1

7. Try delete again (should get 404):
   curl -X DELETE http://localhost:8080/api/orders/1

Success! The delete feature is working correctly.
EOF

echo ""

# ============================================
# END
# ============================================
print_section "END OF TESTING GUIDE"

echo "For more information, see:"
echo "  - docs/DELETE_ORDER_FEATURE.md (comprehensive documentation)"
echo "  - docs/DELETE_FEATURE_SUMMARY.md (quick reference)"
echo "  - docs/IMPLEMENTATION_GUIDE.md (technical details)"
echo ""

