package Service;
import models.*;
import models.Package;

import java.util.HashMap;
import java.util.*;
import java.util.stream.Collectors;

public class DispatchCenter {

    private final PriorityQueue<Package> pendingPackages;

    private final Map<String, Package> packages = new HashMap<>();

    private final Map<String, Rider> riders = new HashMap<>();

    private final Map<String, Assignment> assignments = new HashMap<>();

    private final AuditTrail auditTrail = new AuditTrail();

    public DispatchCenter() {
        pendingPackages = new PriorityQueue<>((p1, p2) -> {
            if(p1.getPriority() != p2.getPriority()) {
                return p1.getPriority() == PackagePriority.EXPRESS ? -1 : 1; // Express packages have higher priority
            }
            if(p1.getDeliveryDeadline() != p2.getDeliveryDeadline()) {
                return Long.compare(p1.getDeliveryDeadline(), p2.getDeliveryDeadline()); // Earlier deadlines have higher priority
            }
            return Long.compare(p1.getOrderTime(), p2.getOrderTime()); // Earlier order times have higher priority
        });
    }

    public void placeOrder(Package pkg) {
        if (pkg == null) {
            throw new IllegalArgumentException("Package cannot be null");
        }
        if (packages.containsKey(pkg.getId())) {
            throw new IllegalArgumentException("Package ID already exists: " + pkg.getId());
        }
        packages.put(pkg.getId(), pkg);
        pendingPackages.offer(pkg);
        System.out.println("Order placed: " + pkg.getId());
        auditTrail.log("Order placed: " + pkg.getId());
        assignPackage();
    }

    public void registerRider(Rider rider) {
        if (rider == null) {
            throw new IllegalArgumentException("Rider cannot be null");
        }
        if (riders.containsKey(rider.getId())) {
            throw new IllegalArgumentException("Rider ID already exists: " + rider.getId());
        }
        riders.put(rider.getId(), rider);
        System.out.println("Rider registered: " + rider.getId());
        auditTrail.log("Rider registered: " + rider.getId());
        assignPackage();
    }

    public void updateRiderStatus(String riderId, RiderStatus status) {
        if (riderId == null || riderId.isEmpty()) {
            throw new IllegalArgumentException("Rider ID cannot be null or empty");
        }
        if (status == null) {
            throw new IllegalArgumentException("RiderStatus cannot be null");
        }

        Rider rider = riders.get(riderId);
        if (rider != null) {
            rider.setRiderStatus(status);
            System.out.println("Rider " + riderId + " status updated to " + status);
            auditTrail.log("Rider " + riderId + " status updated to " + status);
            if (status == RiderStatus.AVAILABLE) {
                assignPackage();
            }
        } else {
             throw new IllegalArgumentException("Rider not found: " + riderId);
        }
    }

    private void assignPackage() {
        Iterator<Package> pkgIterator = pendingPackages.iterator();
        List<Package> toAssign = new ArrayList<>();
        while (pkgIterator.hasNext()) {
            Package pkg = pkgIterator.next();
            Optional<Rider> optRider = findSuitableRider(pkg);
            if (optRider.isPresent()) {
                Rider rider = optRider.get();
                pkg.setPackageStatus(PackageStatus.ASSIGNED);
                assignments.put(pkg.getId(), new Assignment(pkg, rider));
                rider.setRiderStatus(RiderStatus.BUSY);
                System.out.println("Assigned package " + pkg.getId() + " to rider " + rider.getId());
                auditTrail.log("Assigned package " + pkg.getId() + " to rider " + rider.getId());
                toAssign.add(pkg);
            }
        }
        toAssign.forEach(pendingPackages::remove);
    }

    private Optional<Rider> findSuitableRider(Package pkg) {
        return riders.values().stream()
                .filter(r -> r.getRiderStatus() == RiderStatus.AVAILABLE)
                .filter(r -> !pkg.isFragile() || r.isCanHandleFragile())
                .sorted(Comparator.comparingDouble(Rider::getReliabilityRating).reversed())
                .findFirst();
    }

    public void completeDelivery(String packageId) {
        if (packageId == null || packageId.isEmpty()) {
            throw new IllegalArgumentException("Package ID cannot be null or empty");
        }
        Package pkg = packages.get(packageId);
        if (pkg == null) {
            throw new IllegalArgumentException("Package not found: " + packageId);
        }
        if (pkg.getPackageStatus() != PackageStatus.ASSIGNED) {
            throw new IllegalStateException("Package is not assigned: " + packageId);
        }

        pkg.setPackageStatus(PackageStatus.DELIVERED);
        pkg.setDeliveryTime(System.currentTimeMillis());

        Rider rider = assignments.get(packageId).getRider();
        rider.setRiderStatus(RiderStatus.AVAILABLE);

        System.out.println("Package " + packageId + " delivered by rider " + rider.getId());
        auditTrail.log("Package " + packageId + " delivered by rider " + rider.getId());
    }

    public List<Package> getPackagesDeliveredBy(String riderId, long sinceMillis) {
        return assignments.values().stream()
                .filter(a -> a.getRider().getId().equals(riderId))
                .map(Assignment::getPkg)
                .filter(p -> p.getPackageStatus() == PackageStatus.DELIVERED && p.getDeliveryTime() >= sinceMillis)
                .collect(Collectors.toList());
    }

    public List<Package> getMissedExpressDeliveries() {
        long now = System.currentTimeMillis();
        return packages.values().stream()
                .filter(p -> p.getPriority() == PackagePriority.EXPRESS)
                .filter(p -> p.getDeliveryDeadline() < now && p.getPackageStatus() != PackageStatus.DELIVERED)
                .collect(Collectors.toList());
    }

    public List<String> getAuditLog() {
        return auditTrail.getLogs();
    }

    public Assignment getAssignment(String p1) {
        return assignments.get(p1);
    }
}
