# Delete Order Details Feature

This document describes the new delete order functionality added to the Bedsheet Order Profit system.

## Overview

The delete order feature allows you to remove order records from the system. When an order is deleted, all associated order items are automatically deleted as well (cascading delete). This ensures data consistency and integrity.

## API Endpoints

### 1. Delete Order by ID

**Endpoint:** `DELETE /api/orders/{id}`

**Description:** Delete a single order by its ID along with all associated order items.

**Path Parameter:**
- `id` (Long): The ID of the order to delete

**Response:**
- **Status Code:** `204 No Content` - Order successfully deleted
- **Status Code:** `404 Not Found` - Order not found

**Example:**
```bash
curl -X DELETE http://localhost:8080/api/orders/1
```

---

### 2. Delete Order by Waybill Number

**Endpoint:** `DELETE /api/orders/waybill/{waybillNo}`

**Description:** Delete a single order by its waybill number along with all associated order items.

**Path Parameter:**
- `waybillNo` (String): The waybill number of the order to delete

**Response:**
- **Status Code:** `204 No Content` - Order successfully deleted
- **Status Code:** `404 Not Found` - Order not found

**Example:**
```bash
curl -X DELETE http://localhost:8080/api/orders/waybill/WB-2024-001
```

---

### 3. Delete All Orders

**Endpoint:** `DELETE /api/orders`

**Description:** Delete all orders and their associated items from the system.

**⚠️ WARNING:** This action cannot be undone! All orders will be permanently deleted from the database.

**Response:**
- **Status Code:** `200 OK`
- **Response Body:**
```json
{
  "message": "All orders have been deleted successfully",
  "deletedCount": 42
}
```

**Example:**
```bash
curl -X DELETE http://localhost:8080/api/orders
```

---

## Service Methods

The following methods are available in `OrderService`:

### deleteById(Long id)
```java
/**
 * Delete an order by ID along with all its items.
 * @param id Order ID to delete
 * @throws OrderNotFoundException if order doesn't exist
 */
@Transactional
public void deleteById(Long id)
```

### deleteByWaybillNo(String waybillNo)
```java
/**
 * Delete an order by waybill number along with all its items.
 * @param waybillNo Waybill number to delete
 * @throws OrderNotFoundException if order doesn't exist
 */
@Transactional
public void deleteByWaybillNo(String waybillNo)
```

### deleteAll()
```java
/**
 * Delete all orders and their items.
 * WARNING: This action cannot be undone! Use with caution.
 * @return number of orders deleted
 */
@Transactional
public long deleteAll()
```

---

## How It Works

1. **Cascading Deletes**: When an order is deleted, all associated `OrderItem` records are automatically deleted due to:
   - `@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)`
   - This ensures referential integrity

2. **Transaction Safety**: All delete operations are wrapped in Spring transactions (`@Transactional`)
   - If any error occurs during deletion, the entire operation is rolled back
   - Data consistency is guaranteed

3. **Exception Handling**: 
   - `OrderNotFoundException` is thrown if you try to delete a non-existent order
   - This is handled globally by `GlobalExceptionHandler`

---

## Examples

### Using cURL

```bash
# Delete order with ID 5
curl -X DELETE http://localhost:8080/api/orders/5

# Delete order by waybill number
curl -X DELETE "http://localhost:8080/api/orders/waybill/WB-2024-001"

# Delete all orders (use with caution!)
curl -X DELETE http://localhost:8080/api/orders
```

### Using JavaScript/Fetch

```javascript
// Delete order by ID
async function deleteOrder(orderId) {
  try {
    const response = await fetch(`/api/orders/${orderId}`, {
      method: 'DELETE'
    });
    if (response.ok) {
      console.log('Order deleted successfully');
    } else {
      console.error('Failed to delete order');
    }
  } catch (error) {
    console.error('Error:', error);
  }
}

// Delete order by waybill
async function deleteOrderByWaybill(waybillNo) {
  try {
    const response = await fetch(`/api/orders/waybill/${waybillNo}`, {
      method: 'DELETE'
    });
    if (response.ok) {
      console.log('Order deleted successfully');
    }
  } catch (error) {
    console.error('Error:', error);
  }
}

// Delete all orders
async function deleteAllOrders() {
  if (!confirm('Are you sure? This will delete ALL orders. This action cannot be undone.')) {
    return;
  }
  
  try {
    const response = await fetch('/api/orders', {
      method: 'DELETE'
    });
    if (response.ok) {
      const data = await response.json();
      console.log(`Deleted ${data.deletedCount} orders`);
    }
  } catch (error) {
    console.error('Error:', error);
  }
}
```

### Using Java

```java
@Autowired
private OrderService orderService;

// Delete by ID
public void deleteOrder(Long orderId) {
    try {
        orderService.deleteById(orderId);
        System.out.println("Order deleted successfully");
    } catch (OrderNotFoundException e) {
        System.out.println("Order not found: " + e.getMessage());
    }
}

// Delete by Waybill
public void deleteOrderByWaybill(String waybillNo) {
    try {
        orderService.deleteByWaybillNo(waybillNo);
        System.out.println("Order deleted successfully");
    } catch (OrderNotFoundException e) {
        System.out.println("Order not found: " + e.getMessage());
    }
}
```

---

## Security Considerations

### Important Notes:
1. **No Authentication/Authorization**: The current implementation has no security restrictions
2. **Production Recommendation**: Add proper security controls:
   - Require admin role for delete operations
   - Implement request validation
   - Add audit logging to track who deleted what and when

### Example Security Enhancement:
```java
@DeleteMapping("/{id}")
@Secured("ROLE_ADMIN")  // Only admin can delete
public void deleteById(@PathVariable Long id) {
    orderService.deleteById(id);
}
```

---

## Error Handling

### OrderNotFoundException
- **Thrown when:** Attempting to delete an order that doesn't exist
- **HTTP Status:** 404 Not Found
- **Response Example:**
```json
{
  "error": "Order not found: 999"
}
```

---

## Related Features

- **Database Schema**: See `sql/schema.sql`
- **Clear All Data**: See `sql/clear-data.sql` for SQL-based bulk deletion
- **Database Reset Service**: See `DatabaseResetService` for programmatic database clearing

---

## Testing

### Manual Testing Steps

1. **Setup**: Create some test orders using POST `/api/orders`

2. **Delete Single Order**:
   ```bash
   # Get order ID first
   curl http://localhost:8080/api/orders
   
   # Delete it
   curl -X DELETE http://localhost:8080/api/orders/1
   
   # Verify it's gone
   curl http://localhost:8080/api/orders/1
   # Should return 404
   ```

3. **Delete by Waybill**:
   ```bash
   curl -X DELETE http://localhost:8080/api/orders/waybill/TEST-001
   ```

4. **Verify Cascading Delete**: 
   - Delete an order and verify its items are also deleted
   - Check the database directly

---

## FAQ

**Q: What happens if I delete an order?**
A: The order and all its items are permanently deleted from the database. There is no "soft delete" or trash feature.

**Q: Can I undo a delete?**
A: No, deletes are permanent. You would need to restore from a backup or re-enter the data.

**Q: What if the delete operation fails?**
A: The entire transaction is rolled back, so no partial data is deleted.

**Q: Are there any rate limits on delete operations?**
A: No, but we recommend adding them in production for safety.

**Q: Can I delete multiple specific orders?**
A: Currently, you need to delete them one at a time by ID or waybill. You can use a loop to delete multiple orders.

---

## Future Enhancements

- [ ] Soft delete (mark as deleted, don't remove)
- [ ] Bulk delete by date range
- [ ] Delete with confirmation token
- [ ] Audit logging for all deletes
- [ ] Role-based access control
- [ ] Delete recovery/restore functionality
- [ ] Archive orders instead of delete

