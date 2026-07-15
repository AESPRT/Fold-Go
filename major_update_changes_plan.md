# FoldGo — Updated Implementation Spec (Tablet-First)

This document captures the changes discussed for FoldGo's staff app: machine creation simplification, the 2-button machine status control, add-ons, machine assignment during order creation, the batch/cycle status flow, and the tablet-first adaptive layout strategy.

---

## 1. Machine Creation — Device Type Removed

**Change:** The "Machine Type" (Dryer / Washer / Washer-Dryer) field is removed from machine creation. Every machine is now treated as a unified unit capable of a full wash → dry cycle.

**New `AddMachine` form fields:**
| Field | Required | Notes |
|---|---|---|
| Machine Name | Yes | e.g. "Dryer 3" — name is just a label now, not a type |
| Capacity (kg) | Yes | Numeric, used for batch splitting |
| Active | Auto (true) | New machines default to `IDLE` |

**Removed:** `MachineType` enum selection at creation time.

**Data model change:**
```kotlin
// Before
data class Machine(
    val id: String,
    val name: String,
    val type: MachineType, // DRYER, WASHER, WASHER_DRYER  <-- removed
    val capacityKg: Double,
    val status: MachineStatus
)

// After
data class Machine(
    val id: String,
    val name: String,
    val capacityKg: Double,
    val status: MachineStatus,      // IDLE, WASHING, DRYING, FOLDING, OUT_OF_ORDER
    val assignedOrderId: String?    // null when unassigned
)
```

> Note: since type is gone, the "Dryers / Washers / Washer_dryers" filter chips on the Machines screen are no longer meaningful and should be removed or replaced with a status filter (Idle / In Use / Out of Order) instead.

**Tablet UI:** `FoldGo_Tablet_EquipmentSetup.png` — list of existing machines on the left, simplified add-machine form (Name + Capacity only) on the right, with an inline note explaining the removed field.

---

## 2. Machine Status Control — 2-Button Logic

**Change:** Clicking "Update Status" on a machine no longer opens a full status form. It shows exactly **two buttons**: `Start Cycle` and `Idle`.

**Logic:**

```kotlin
fun onUpdateStatusOpened(machine: Machine) {
    val hasAssignedOrder = machine.assignedOrderId != null
    startCycleButton.isEnabled = hasAssignedOrder
    if (!hasAssignedOrder) {
        showWarning("No order assigned to this machine. Create a new order and assign it here before starting a cycle.")
    }
}

fun onStartCycleClicked(machine: Machine) {
    require(machine.assignedOrderId != null) { "Cannot start cycle without an assigned order" }
    machine.status = MachineStatus.WASHING
    order.batchStatus = BatchStatus.WASHING
}

fun onIdleClicked(machine: Machine) {
    machine.status = MachineStatus.IDLE
    // does NOT clear assignedOrderId — idle is a pause/reset state, not a release
}
```

**Button states:**

| Condition | Start Cycle | Idle |
|---|---|---|
| No order assigned | Disabled (gray) + warning banner | Enabled |
| Order assigned, not yet started | Enabled (green) | Outlined/inactive |
| Cycle running | N/A (already started) | Enabled — allows manual override/reset |

**Tablet UI:**
- `FoldGo_Tablet_Machines_UpdateStatus.png` — no order assigned → Start Cycle disabled, amber warning shown
- `FoldGo_Tablet_Machines_UpdateStatus_Assigned.png` — order assigned → Start Cycle enabled (green), warning replaced with green "order assigned" confirmation

---

## 3. New Order — Add-Ons + Machine Assignment

The New Order form gains two new sections between **Service Items** and **Order Summary**:

### 3.1 Add-Ons
Optional extra services selectable as toggle chips, each with its own price. Selected add-ons are itemized separately in the order summary and invoice.

```kotlin
data class AddOn(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val appliesTo: ServiceScope, // ALL, WASH_ONLY, DRY_ONLY, FOLD_ONLY
    val isActive: Boolean
)

data class OrderAddOnSelection(
    val orderId: String,
    val addOnId: String,
    val priceAtTimeOfOrder: Double // snapshot, so later price changes don't affect past orders
)
```

### 3.2 Machine Assignment
Staff must assign an idle, unassigned machine before the order can be created.

```kotlin
fun availableMachinesForAssignment(machines: List<Machine>): List<Machine> =
    machines.filter { it.status == MachineStatus.IDLE && it.assignedOrderId == null }

fun assignMachineToOrder(machine: Machine, order: Order) {
    require(machine.status == MachineStatus.IDLE && machine.assignedOrderId == null)
    machine.assignedOrderId = order.id
    order.assignedMachineId = machine.id
    // machine.status stays IDLE until Start Cycle is pressed — see section 2
}

fun createOrderValidation(order: OrderDraft): Boolean =
    order.customerName.isNotBlank() &&
    order.serviceItems.isNotEmpty() &&
    order.assignedMachineId != null   // machine is now a required field
```

**Machine card states in the picker:**
| State | Visual | Selectable |
|---|---|---|
| IDLE + unassigned | Green "IDLE" badge | Yes |
| IDLE + assigned to another order | Amber "IN USE" badge, grayed, lock icon | No |
| WASHING / DRYING | Amber "IN USE" badge, grayed, lock icon | No |

**Tablet UI:** `FoldGo_Tablet_NewOrder_v3.png` — full form: customer info → delivery method → service items → add-ons (toggle chips) → assign machine (grid, disabled cards locked) → order summary panel with itemized services + add-ons + assigned machine + total.

---

## 4. Add-Ons Management Screen (new)

A dedicated Settings sub-screen for creating and managing add-ons, following the same list-detail pattern as the rest of the tablet UI.

**Left panel:** list of existing add-ons with an on/off toggle (deactivating hides it from the New Order picker without deleting history).

**Right panel — Create/Edit Add-On form:**
| Field | Type |
|---|---|
| Add-On Name | Text |
| Description | Text |
| Price (PHP) | Decimal |
| Applies To | Chip select: All Services / Wash Only / Dry Only / Fold Only |
| Active | Toggle |

```kotlin
fun saveAddOn(draft: AddOnDraft): AddOn {
    require(draft.name.isNotBlank())
    require(draft.price >= 0.0)
    return addOnRepository.upsert(draft.toAddOn())
}

fun toggleAddOnActive(addOnId: String, active: Boolean) {
    addOnRepository.updateActive(addOnId, active)
    // existing orders that already reference this add-on are unaffected (price snapshot)
}
```

**Tablet UI:** `FoldGo_Tablet_AddOns.png`

---

## 5. Batch Flow — Retained

The batch-splitting logic is unchanged. An order can still be split into multiple batches when it exceeds a single machine's capacity, and each batch tracks its own state independently.

```kotlin
data class OrderBatch(
    val id: String,
    val orderId: String,
    val batchIndex: Int,      // 1 of 2, 2 of 2, etc.
    val weightKg: Double,
    val assignedMachineId: String?,
    val status: BatchStatus
)
```

Batch assignment continues to happen right after order creation (`Batch Assignment` step in the flowchart), independent of the single "Assign Machine" picker used at order-creation time — for multi-batch orders, each batch gets matched to an available machine as machines free up.

---

## 6. Order/Batch Status Flow — Start Cycle Trigger

Order and batch status now formally follow this five-state sequence:

```
Queued → Washing → Drying → Folding → Ready
```

**Transition rules:**

```kotlin
enum class BatchStatus { QUEUED, WASHING, DRYING, FOLDING, READY }

fun onStartCycleClicked(machine: Machine) {
    val batch = batchRepository.findByMachine(machine.id)
    require(batch.status == BatchStatus.QUEUED)
    batch.status = BatchStatus.WASHING
    machine.status = MachineStatus.WASHING
}

// WorkManager job advances the batch automatically as each phase's timer completes
fun advanceBatchStatus(batch: OrderBatch) {
    batch.status = when (batch.status) {
        BatchStatus.QUEUED  -> BatchStatus.WASHING   // triggered manually via Start Cycle
        BatchStatus.WASHING -> BatchStatus.DRYING
        BatchStatus.DRYING  -> BatchStatus.FOLDING
        BatchStatus.FOLDING -> BatchStatus.READY
        BatchStatus.READY   -> BatchStatus.READY     // terminal
    }
    if (batch.status == BatchStatus.READY) {
        notificationHelper.sendOrderReady(batch.orderId)
    }
    if (batch.status == BatchStatus.FOLDING) {
        // folding doesn't occupy the machine — release it back to IDLE + unassigned
        machineRepository.release(batch.assignedMachineId)
    }
}
```

- `Queued`: batch created, waiting on a machine or on staff to press Start Cycle
- `Washing`: machine occupied, `MachineStatus.WASHING`
- `Drying`: machine occupied, `MachineStatus.DRYING` (or a second machine if wash/dry are separate units)
- `Folding`: machine is released back to `IDLE` + `assignedOrderId = null` — folding is manual/off-machine
- `Ready`: triggers the customer notification and appears in Dashboard/History as `READY`

---

## 7. Tablet-First, Mobile-Optional Layout Strategy

**Direction:** Tablet becomes the primary reference UI for the staff app. Mobile phone layouts are **not replaced** — they stay exactly as they are today — but every new screen should be built so it *can* adapt down to a compact phone width later, without a rewrite.

**How to implement this without duplicating UI:**

```kotlin
@Composable
fun MachinesScreen(windowSizeClass: WindowSizeClass) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Expanded -> MachinesTabletLayout()   // primary, built first
        WindowWidthSizeClass.Medium,
        WindowWidthSizeClass.Compact -> MachinesPhoneLayout()      // existing phone UI, untouched
    }
}
```

- Shared `ViewModel`, `Room` DAOs, `WorkManager` jobs, and `Koin` modules stay identical across both layouts — only the Composable layer branches.
- Build the tablet composables first (`NavigationRail`, list-detail panes, multi-column forms) since that's now the primary target.
- Phone composables are optional/deferred — reuse the current mobile screens as-is until there's time to adapt them; they don't block tablet rollout.
- New shared components to build once and reuse across tablet layouts: `TabletScaffold` (nav rail + header), `ListDetailPane`, `MachinePickerGrid`, `AddOnChipGroup`, `TwoButtonStatusControl`.

**Updated tablet screens (this round):**
- `FoldGo_Tablet_EquipmentSetup.png`
- `FoldGo_Tablet_AddOns.png`
- `FoldGo_Tablet_NewOrder_v3.png`
- `FoldGo_Tablet_Machines_UpdateStatus.png` / `_Assigned.png`

(Previously delivered: Dashboard, Machines grid, Settings, Order History — all still valid, unchanged by this round's logic.)

---

## 8. Updated System Flowchart
`FoldGo_Flowchart_v2.png` reflects all changes in this document: add-ons selection, machine assignment at order creation, the machine-locking rule, the Start Cycle decision gate (with the warning path), the five-state batch cycle (Queued → Washing → Drying → Folding → Ready), and machine release back to idle after payment/completion.

---

## Summary of Files in This Round

| File | Purpose |
|---|---|
| `FoldGo_Tablet_EquipmentSetup.png` | Add-machine form, no device type field |
| `FoldGo_Tablet_AddOns.png` | Add-ons management (list + create form) |
| `FoldGo_Tablet_NewOrder_v3.png` | New Order with add-ons + machine assignment |
| `FoldGo_Tablet_Machines_UpdateStatus.png` | 2-button status control — no order assigned |
| `FoldGo_Tablet_Machines_UpdateStatus_Assigned.png` | 2-button status control — order assigned |
| `FoldGo_Flowchart_v2.png` | Updated end-to-end system flowchart |