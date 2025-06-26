# Chronos Couriers 

Chronos Couriers is an in-memory, single-threaded delivery dispatch simulation system implemented in Java. 

---

## 🚀 Getting Started

### 🔧 Requirements

- Java 11 or higher
- Terminal or command prompt access

---

###  CLI Commands

- registerRider <riderId> <canHandleFragile> <reliability>
  Registers a new rider into the system.
  Example - registerRider R1 true 0.9
  
- placeOrder <packageId> <EXPRESS|STANDARD> <deliveryDeadlineMillis> <isFragile>
  Places a new package into the system.
  Example - placeOrder P1 EXPRESS 1734567890000 true

- updateRiderStatus <riderId> <AVAILABLE|BUSY|OFFLINE>
  Updates a rider's current status.
  Example - updateRiderStatus R1 AVAILABLE

- completeDelivery <packageId>
  Marks a package as delivered and sets the rider back to AVAILABLE.
  completeDelivery P1

- missedExpressDeliveries
  Returns all Express packages which are missed deliveries.

- auditLog
  Prints a full audit log of all major actions

