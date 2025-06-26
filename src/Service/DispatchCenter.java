package Service;
import models.*;
import models.Package;

import java.util.HashMap;
import java.util.*;
import java.util.stream.Collectors;

public class DispatchCenter {

    private List<Package> pendingPackages = new ArrayList<>();

    private List<Rider> riders = new ArrayList<>();

    private List<Assignment> assignments = new ArrayList<>();

    private final AuditTrail auditTrail = new AuditTrail();

    public void placeOrder(Package pkg) {
        pkg.setPackageStatus(PackageStatus.PENDING);
        pendingPackages.add(pkg);
        System.out.println("Order placed: " + pkg.getId());
        auditTrail.log("Order placed: " + pkg.getId());
        assignPackage();
    }

    public void registerRider(Rider rider) {
        riders.add(rider);
        System.out.println("Rider registered: " + rider.getId());
        auditTrail.log("Rider registered: " + rider.getId());
        assignPackage();
    }

    public void updateRiderStatus(String riderId, RiderStatus status) {
        for (Rider rider : riders) {
            if(rider.getId().equals(riderId)) {
                rider.setRiderStatus(status);
                break;
            }
        }
        System.out.println("Rider " + riderId + " status updated to " + status);
        auditTrail.log("Rider " + riderId + " status updated to " + status);
        assignPackage();
    }

    private void assignPackage() {
        pendingPackages.sort((p1, p2) -> {
            if (p1.getPriority() == PackagePriority.EXPRESS && p2.getPriority() != PackagePriority.EXPRESS) {
                return -1;
            } else if (p1.getPriority() != PackagePriority.EXPRESS && p2.getPriority() == PackagePriority.EXPRESS) {
                return 1;
            } else {
                return Long.compare(p1.getDeliveryDeadline(), p2.getDeliveryDeadline());
            }
        });

        Iterator<Package> pendingPkgIterator = pendingPackages.iterator();

        while(pendingPackages.iterator().hasNext()){
            Package pkg = pendingPkgIterator.next();

            Rider selectedRider = null;
            for (Rider rider : riders) {
                if (rider.getRiderStatus() == RiderStatus.AVAILABLE && (!pkg.isFragile() || rider.isCanHandleFragile())) {
                    selectedRider = rider;
                    break;
                }
            }

            if (selectedRider != null) {
                pkg.setPackageStatus(PackageStatus.ASSIGNED);
                assignments.add(new Assignment(pkg, selectedRider));
                selectedRider.setRiderStatus(RiderStatus.BUSY);
                System.out.println("Assigned package " + pkg.getId() + " to rider " + selectedRider.getId());
                auditTrail.log("Assigned package " + pkg.getId() + " to rider " + selectedRider.getId());
                pendingPkgIterator.remove();
            }
        }
    }

    public void completeDelivery(String packageId) {
        for(Assignment assignment : assignments) {
            if(assignment.getPkg().getId().equals(packageId)) {
                assignment.getPkg().setPackageStatus(PackageStatus.DELIVERED);
                assignment.getPkg().setDeliveryTime(System.currentTimeMillis());
                assignment.getRider().setRiderStatus(RiderStatus.AVAILABLE);
                System.out.println("Package " + packageId + " delivered by rider " + assignment.getRider().getId());
                auditTrail.log("Package " + packageId + " delivered by rider " + assignment.getRider().getId());
                break;
            }
        }
        assignPackage();
    }

    public List<Package> getPackagesDeliveredBy(String riderId, long sinceMillis) {
        List<Package> result = new ArrayList<>();
        for(Assignment assignment: assignments) {
            if (assignment.getRider().getId().equals(riderId)) {
                Package pkg = assignment.getPkg();
                if(pkg.getPackageStatus() == PackageStatus.DELIVERED && pkg.getDeliveryTime() >= sinceMillis) {
                    result.add(pkg);
                }
            }
        }
        return result;
    }

    public List<Package> getMissedExpressDeliveries() {
        long now = System.currentTimeMillis();
        List<Package> missedExpressDeliveries = new ArrayList<>();
        for (Assignment assignment : assignments) {
            Package pkg = assignment.getPkg();
            if (pkg.getPriority() == PackagePriority.EXPRESS && pkg.getDeliveryDeadline() < now && pkg.getPackageStatus() != PackageStatus.DELIVERED) {
                missedExpressDeliveries.add(pkg);
            }
        }
        return missedExpressDeliveries;
    }

    public List<String> getAuditLog() {
        return auditTrail.getLogs();
    }
}
