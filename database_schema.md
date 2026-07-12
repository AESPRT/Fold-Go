# Fold&Go: Database Schema Documentation

This document outlines the unified database schema used by the .NET Core backend (PostgreSQL) and the Android Mobile application (Room DB).

---

## 1. Overview
The system uses a **Sync-Down Configuration / Sync-Up Transaction** model. 
- **Configuration Tables:** Managed by the Web Admin, synced to Mobile.
- **Transaction Tables:** Created on Mobile, synced to the Server.

---

## 2. Configuration Tables (Sync-Down)

### `shops`
Stores the business details for each laundry branch.
| Column | Type | Description |
| :--- | :--- | :--- |
| `shopId` | `String` (PK) | Unique identifier for the shop |
| `name` | `String` | Registered name of the shop |
| `address` | `String` | Physical location |
| `ownerId` | `String` | ID of the shop owner |
| `pin` | `String` | 4-digit security PIN for mobile login |
| `settings` | `String` (JSON) | Shop-specific settings (e.g., currency, tax) |
| `createdAt` | `Long` | Timestamp of registration |

### `staff`
Stores operator and manager profiles.
| Column | Type | Description |
| :--- | :--- | :--- |
| `staffId` | `String` (PK) | Unique identifier |
| `shopId` | `String` (FK) | Reference to `shops.shopId` |
| `name` | `String` | Full name of the staff member |
| `role` | `String` | Role (e.g., "Operator", "Manager") |
| `isActive` | `Boolean` | Employment status |
| `createdAt` | `Long` | Timestamp of creation |

### `machine_categories`
Groupings for machines (e.g., "Industrial Washers").
| Column | Type | Description |
| :--- | :--- | :--- |
| `categoryId` | `String` (PK) | Unique identifier |
| `name` | `String` | Display name (e.g., "Stacked Dryers") |
| `type` | `Enum` | `WASHER`, `DRYER`, `COMBO`, `IRON`, `STEAMER` |
| `iconName` | `String?` | Name of the icon asset |
| `colorHex` | `String?` | UI accent color for this category |

### `machines`
Physical hardware registry.
| Column | Type | Description |
| :--- | :--- | :--- |
| `machineId` | `String` (PK) | Unique identifier |
| `shopId` | `String` (FK) | Reference to `shops.shopId` |
| `name` | `String` | Machine name/number (e.g., "W-01") |
| `type` | `Enum` | Machine base type |
| `capacityKg` | `Double` | Maximum load capacity |
| `status` | `Enum` | `IDLE`, `BUSY`, `OUT_OF_ORDER` |
| `lastMaintenanceDate`| `Long` | Last service timestamp |
| `endTime` | `Long?` | Estimated finish time of current cycle |
| `cyclesCount` | `Int` | Total cycles performed (for maintenance tracking) |

### `services`
The menu of services offered by the shop.
| Column | Type | Description |
| :--- | :--- | :--- |
| `serviceId` | `String` (PK) | Unique identifier |
| `shopId` | `String` (FK) | Reference to `shops.shopId` |
| `name` | `String` | Service name (e.g., "Wash & Dry") |
| `defaultQuantity` | `Double` | Default weight/unit for intake |
| `unit` | `String` | Measurement unit (e.g., "KG", "PCS") |
| `pricePerUnit` | `Double` | Unit price |
| `type` | `Enum` | `WASH`, `DRY`, `WASH_DRY`, `IRON`, `OTHER` |

### `inventory`
Consumables tracking (Detergents, Fabcon, etc.).
| Column | Type | Description |
| :--- | :--- | :--- |
| `itemId` | `String` (PK) | Unique identifier |
| `shopId` | `String` (FK) | Reference to `shops.shopId` |
| `name` | `String` | Item name |
| `currentStock` | `Double` | Amount currently in stock |
| `unit` | `String` | e.g., "Liters", "Scoops" |
| `lowStockThreshold` | `Double` | Alert level for restocking |

---

## 3. Transaction Tables (Sync-Up)

### `orders`
Primary table for customer transactions.
| Column | Type | Description |
| :--- | :--- | :--- |
| `orderId` | `String` (PK) | Unique identifier |
| `shopId` | `String` (FK) | Reference to `shops.shopId` |
| `customerId` | `String` | Identifier for the customer |
| `customerName` | `String` | Display name of the customer |
| `customerPhone` | `String` | Phone number for SMS notifications |
| `orderNumber` | `String` | Human-readable receipt ID (e.g., FG-1001) |
| `itemsJson` | `String` (JSON) | List of `ServiceItem` objects |
| `totalAmount` | `Double` | Final price inclusive of fees |
| `paidAmount` | `Double` | Total amount collected |
| `changeDue` | `Double` | Change returned to customer |
| `status` | `Enum` | `INTAKE` -> `WASHING` -> `READY` -> `DELIVERED` |
| `deliveryMethod` | `Enum` | `PICKUP`, `DELIVERY` |
| `paymentStatus` | `Enum` | `PENDING`, `PAID`, `PARTIAL` |
| `intakePhotosJson` | `String?` (JSON) | List of image URLs/Paths |
| `machineId` | `String?` (FK) | Currently assigned machine |
| `staffId` | `String` (FK) | Operator who took the order |
| `staffName` | `String` | Name of the staff member |
| `createdAt` | `Long` | Timestamp of intake |
| `updatedAt` | `Long` | Timestamp of last status change |
| `isSynced` | `Boolean` | Internal flag for Mobile Sync outbox |

### `sync_outbox` (Mobile Only)
Stores pending operations to be pushed to the server.
| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | `Long` (PK) | Auto-incrementing ID |
| `entityType` | `String` | e.g., "ORDER", "MACHINE_STATUS" |
| `entityId` | `String` | ID of the modified entity |
| `operation` | `String` | `CREATE`, `UPDATE`, `DELETE` |
| `payloadJson` | `String` | Full JSON object for the server update |
| `createdAt` | `Long` | Timestamp of the local change |

---

## 4. Enums & Constants

### OrderStatus
`INTAKE`, `WASHING`, `WASHED`, `DRYING`, `DRIED`, `IRONING`, `IRONED`, `FOLDING`, `READY`, `DELIVERED`

### MachineType
`WASHER`, `DRYER`, `WASHER_DRYER`, `IRON`, `STEAMER`

### ServiceType
`WASH`, `DRY`, `WASH_DRY`, `IRON`, `OTHER`
