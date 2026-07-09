# Fold&Go: Senior Technical Blueprint & UI/UX Specification
## Mobile-First Offline-First Laundry Management System

---

### 1. Senior Architectural Strategy: Modern Android Stack

**Fold&Go** leverages a "Clean Architecture" approach, ensuring the core business logic is independent of UI, database, or external agencies.

#### Core Technology Stack
*   **Language:** Kotlin with Coroutines and Flow for asynchronous operations.
*   **UI Framework:** Jetpack Compose with Material 3 (Material Design 3).
*   **Local Storage:** Room Persistence Library (SQLite) as the Absolute Source of Truth (ASOT).
*   **Remote Sync:** Firebase Cloud Firestore for real-time multi-device synchronization.
*   **Background Processing:** WorkManager for reliable data synchronization and scheduled tasks.
*   **Dependency Injection:** Koin for lightweight and pragmatic dependency management.
*   **Navigation:** Type-Safe Jetpack Compose Navigation.

#### Synchronization & Resiliency (The "Atomic Sync" Pattern)
*   **The Sync Outbox:** Every local mutation (Insert/Update/Delete) is wrapped in a Room Transaction that also records the change in a `sync_outbox` table.
*   **WorkManager Sync Engine:** A `CoroutineWorker` monitors the outbox. It performs exponential backoff and handles network constraints, ensuring data eventually reaches Firestore.
*   **Conflict Resolution:** Timestamp-based "Last Write Wins" (LWW) strategy implemented at the Repository level.
*   **Reactive UI:** ViewModels observe Room `Flows`. When WorkManager updates Room with remote changes, the UI reacts instantly without manual refreshes.

---

### 2. Senior UI/UX Design System: "Clean & Fresh"

#### Design Philosophy
The UI must be **Operator-Optimized**. Laundry environments are humid, busy, and often involve handheld use.
*   **Touch Targets:** Minimum 48dp for all interactive elements.
*   **Contrast:** High contrast ratios to ensure readability under bright shop lights.
*   **Visual Hierarchy:** Use color and size to emphasize the most common action (e.g., "New Order").

#### Color Palette (Clean & Fresh)
*   **Primary:** `Deep Ocean Blue (#005CBB)` - Reliability and Professionalism.
*   **Secondary:** `Mint Green (#00C853)` - Cleanliness and Completion.
*   **Status Colors:**
    *   `Intake/Pending:` Amber (#FFAB00)
    *   `Processing (Wash/Dry):` Sky Blue (#03A9F4)
    *   `Ready for Pickup:` Emerald Green (#4CAF50)
    *   `Alert/Error:` Crimson Red (#D32F2F)

#### Key UI Components
*   **Order Kanban Dashboard:** A high-level view of all orders grouped by stage (Intake, Washing, Drying, Ready).
*   **Machine Matrix Grid:** Visual representation of physical machines with countdown timers and status indicators.
*   **The "Quick Ingest" FAB:** A prominent Floating Action Button for 3-tap order creation.

---

### 3. Expanded Feature Set

#### A. Smart POS & Intake
*   **Dynamic Pricing Engine:** Support for weight-based (kg), piece-based, and service-based (e.g., "Dry Clean") pricing.
*   **Photo Evidence:** Capture photos of high-value items at intake to document pre-existing conditions (built-in CameraX integration).
*   **Loyalty & Customer CRM:** Automatic customer profile creation via phone number. Track visit frequency and implement "10th Wash Free" logic.

#### B. Intelligent Machine Lifecycle
*   **Utility Tracking:** Log which machine (Brand/ID) was used for each order.
*   **Maintenance Alerts:** Notify managers after X cycles to perform lint cleaning or descaling.
*   **Cycle Overlap Prevention:** Prevent assigning two orders to the same machine simultaneously.

#### C. Financial & Staff Governance
*   **Shift Handover Ledger:** Forced digital "Cash Count" at the start and end of shifts. Discrepancies are flagged immediately to the owner.
*   **Performance Metrics:** Track "Orders Processed" and "Average Turnaround Time" per staff member.
*   **Expense Tracker:** Log daily out-of-pocket expenses (e.g., buying detergent from a local store).

#### D. Customer Engagement
*   **Omnichannel Notifications:** Automated status updates via WhatsApp/SMS (Server-side triggers based on Firestore state changes).
*   **QR Code Tracking:** Print/Show a QR code that customers can scan to see their order progress on a simple web status page.

---

### 4. Refined Database Schema (Unified Entity Models)

#### Entity: `shops` (Owner's Root)
```kotlin
@Entity(tableName = "shops")
data class ShopEntity(
    @PrimaryKey val shopId: String,
    val name: String,
    val address: String,
    val ownerId: String,
    val settings: String, // JSON for business hours, tax rates, etc.
    val createdAt: Long
)
```

#### Entity: `machines` (Equipment Inventory)
```kotlin
@Entity(tableName = "machines")
data class MachineEntity(
    @PrimaryKey val machineId: String,
    val shopId: String,
    val name: String, // e.g., "Washer 01"
    val type: String, // WASHER | DRYER
    val capacityKg: Double,
    val status: String, // IDLE | BUSY | OUT_OF_ORDER
    val lastMaintenanceDate: Long
)
```

#### Entity: `orders` (Transaction Core)
```kotlin
@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val shopId: String,
    val customerId: String,
    val orderNumber: String, // Human-readable (e.g., "FG-1023")
    val itemsJson: String, // List of ServiceItem (Name, Qty, Price)
    val totalAmount: Double,
    val paidAmount: Double,
    val status: OrderStatus, // INTAKE, WASHING, DRYING, FOLDING, READY, DELIVERED
    val intakePhotosJson: String?, // List of Image URLs
    val machineId: String?, // Currently assigned machine
    val staffId: String, // Staff who created the order
    val createdAt: Long,
    val updatedAt: Long,
    val isSynced: Boolean = false
)
```

#### Entity: `inventory` (Supplies)
```kotlin
@Entity(tableName = "inventory")
data class InventoryEntity(
    @PrimaryKey val itemId: String,
    val shopId: String,
    val name: String, // e.g., "Detergent X"
    val currentStock: Double,
    val unit: String, // L, KG, PCS
    val lowStockThreshold: Double
)
```

#### Entity: `sync_outbox` (Resiliency Ledger)
```kotlin
@Entity(tableName = "sync_outbox")
data class SyncOutboxEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String, // "order", "machine", "inventory"
    val entityId: String,
    val operation: String, // INSERT, UPDATE, DELETE
    val payloadJson: String, // Full snapshot or delta
    val createdAt: Long
)
```
