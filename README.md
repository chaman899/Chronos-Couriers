# Chronos Couriers 

Chronos Couriers is an in-memory, single-threaded delivery dispatch simulation system implemented in Java. 

---

## Getting Started

### 🔧 Requirements

- Java 11 or higher  
- Terminal or command prompt access

---

### 💻 CLI Commands

**registerRider `<riderId>` `<canHandleFragile>` `<reliability>`**  
Registers a new rider into the system.  
**Example:** `registerRider R1 true 0.9`

---

**placeOrder `<packageId>` `<EXPRESS|STANDARD>` `<deliveryDeadlineMillis>` `<isFragile>`**  
Places a new package into the system.  
**Example:** `placeOrder P1 EXPRESS 1734567890000 true`

---

**updateRiderStatus `<riderId>` `<AVAILABLE|BUSY|OFFLINE>`**  
Updates a rider's current status.  
**Example:** `updateRiderStatus R1 AVAILABLE`

---

**completeDelivery `<packageId>`**  
Marks a package as delivered and sets the rider back to AVAILABLE.  
**Example:** `completeDelivery P1`

---

**missedExpressDeliveries**  
Returns all EXPRESS packages that missed their delivery deadline.  
**Example:** `missedExpressDeliveries`

---

**auditLog**  
Prints a full audit log of all major actions.  
**Example:** `auditLog`

---

## 🏗️ Design & Data Structures

**Design Choices:**
- **Separation of Concerns:** Riders, packages, assignments, and audit logs are managed in separate classes and collections for modularity and maintainability.
- **Priority Assignment:** Uses a `PriorityQueue` to always assign the highest-priority package first.

**Data Structures:**
- `PriorityQueue<Package>`: Orders pending packages by priority (`EXPRESS` > `STANDARD`), then by earliest deadline, then by earliest order time.
- `HashMap<String, Package>`: Fast lookup of packages by ID.
- `HashMap<String, Rider>`: Fast lookup of riders by ID.
- `HashMap<String, Assignment>`: Tracks which rider is assigned to which package.
- `AuditTrail`: Logs all significant actions for traceability.

**Algorithms:**
- **Assignment:** When a package or rider is added/updated, the system assigns the highest-priority pending package to the most suitable available rider (highest reliability, can handle fragile if needed).
- **Priority Logic:** The custom comparator in the `PriorityQueue` ensures correct order for assignment.
- **Validation:** Input validation and exception handling ensure data integrity and clear error messages.

---
