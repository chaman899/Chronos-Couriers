package test;

import Service.DispatchCenter;
import models.*;

import models.Package;
import org.junit.Test;
import org.junit.Before;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DispatchCenterTest {
    private DispatchCenter dispatchCenter;

    @Before
    public void setUp() {
        dispatchCenter = new DispatchCenter();
    }


    @Test
    public void testExpressPriorityOverStandard() {
        Package standard = new Package("p1", PackagePriority.STANDARD, System.currentTimeMillis(), System.currentTimeMillis() + 10000, false, null, 0, 0);
        Package express = new Package("p2", PackagePriority.EXPRESS, System.currentTimeMillis(), System.currentTimeMillis() + 5000, false, null, 0, 0);

        dispatchCenter.placeOrder(standard);
        dispatchCenter.placeOrder(express);

        Rider rider1 = new Rider("r1", RiderStatus.AVAILABLE, 4.5, true);
        Rider rider2 = new Rider("r2", RiderStatus.AVAILABLE, 4.5, true);
        dispatchCenter.registerRider(rider1);
        dispatchCenter.registerRider(rider2);

        // Both should be assigned, express should be assigned first
        Assignment assignmentExpress = dispatchCenter.getAssignment("p2");
        Assignment assignmentStandard = dispatchCenter.getAssignment("p1");

        assertNotNull(assignmentExpress);
        assertNotNull(assignmentStandard);
        assertEquals(PackageStatus.ASSIGNED, assignmentExpress.getPkg().getPackageStatus());
        assertEquals(PackageStatus.ASSIGNED, assignmentStandard.getPkg().getPackageStatus());

        // Complete express delivery
        dispatchCenter.completeDelivery("p2");
        assertEquals(PackageStatus.DELIVERED, assignmentExpress.getPkg().getPackageStatus());
    }

    @Test
    public void testDeadlinePriorityAmongSameType() {
        Package earlyDeadline = new Package("p1", PackagePriority.STANDARD, System.currentTimeMillis(), System.currentTimeMillis() + 5000, false, null, 0, 0);
        Package lateDeadline = new Package("p2", PackagePriority.STANDARD, System.currentTimeMillis(), System.currentTimeMillis() + 10000, false, null, 0, 0);
        dispatchCenter.placeOrder(lateDeadline);
        dispatchCenter.placeOrder(earlyDeadline);

        Rider rider1 = new Rider("r1", RiderStatus.AVAILABLE, 4.5, true);
        Rider rider2 = new Rider("r2", RiderStatus.AVAILABLE, 4.5, true);
        dispatchCenter.registerRider(rider1);
        dispatchCenter.registerRider(rider2);

        assertEquals(PackageStatus.ASSIGNED, earlyDeadline.getPackageStatus());
        assertEquals(PackageStatus.ASSIGNED, lateDeadline.getPackageStatus());
    }

    @Test
    public void testTiebreakerByOrderTime() {
        long now = System.currentTimeMillis();
        Package p1 = new Package("p1", PackagePriority.STANDARD, now, now + 10000, false, null, 0, 0);
        Package p2 = new Package("p2", PackagePriority.STANDARD, now-1000, now + 10000, false, null, 0, 0);
        dispatchCenter.placeOrder(p1);
        dispatchCenter.placeOrder(p2);

        Rider rider = new Rider("r1", RiderStatus.AVAILABLE, 4.5, true);
        dispatchCenter.registerRider(rider);

        Assignment assignment = dispatchCenter.getAssignment("p2");
        assertNotNull(assignment);
        assertEquals("p2", assignment.getPkg().getId());
    }

    @Test
    public void testRiderBecomesAvailableTriggersAssignment() {
        Rider rider = new Rider("r1", RiderStatus.OFFLINE, 4.5, true);
        dispatchCenter.registerRider(rider);

        Package pkg = new Package("p1", PackagePriority.EXPRESS, System.currentTimeMillis(), System.currentTimeMillis() + 5000, false, null, 0, 0);
        dispatchCenter.placeOrder(pkg);

        assertNull(dispatchCenter.getAssignment("p1"));

        dispatchCenter.updateRiderStatus("r1", RiderStatus.AVAILABLE);

        Assignment assignment = dispatchCenter.getAssignment("p1");
        assertNotNull(assignment);
        assertEquals("r1", assignment.getRider().getId());
    }



    @Test
    public void testFragileAssignment() {
        Rider r1 = new Rider("r1", RiderStatus.AVAILABLE, 4.8, false);
        Rider r2 = new Rider("r2", RiderStatus.AVAILABLE, 4.0, true);
        dispatchCenter.registerRider(r1);
        dispatchCenter.registerRider(r2);

        Package fragilePkg = new Package("p1", PackagePriority.EXPRESS, System.currentTimeMillis(), System.currentTimeMillis() + 10000, true, null, 0, 0);
        dispatchCenter.placeOrder(fragilePkg);

        Assignment assignment = dispatchCenter.getAssignment("p1");
        assertEquals("r2", assignment.getRider().getId());
    }

    @Test
    public void testRiderGoesOffline() {
        Rider rider = new Rider("r1", RiderStatus.AVAILABLE, 4.5, true);
        dispatchCenter.registerRider(rider);
        dispatchCenter.updateRiderStatus("r1", RiderStatus.OFFLINE);

        assertEquals(RiderStatus.OFFLINE, rider.getRiderStatus());
    }

    @Test
    public void testDeliveryCompletionFlow() {
        Rider rider = new Rider("r1", RiderStatus.AVAILABLE, 4.0, true);
        dispatchCenter.registerRider(rider);

        Package pkg = new Package("p1", PackagePriority.STANDARD, System.currentTimeMillis(), System.currentTimeMillis() + 15000, false, null, 0, 0);
        dispatchCenter.placeOrder(pkg);

        dispatchCenter.completeDelivery("p1");
        assertEquals(PackageStatus.DELIVERED, pkg.getPackageStatus());
    }

    @Test
    public void testGetDeliveredPackagesForRider() throws InterruptedException {
        Rider rider = new Rider("r1", RiderStatus.AVAILABLE, 4.5, true);
        dispatchCenter.registerRider(rider);

        Package pkg = new Package("p1", PackagePriority.STANDARD, System.currentTimeMillis(), System.currentTimeMillis() + 15000, false, null, 0, 0);
        dispatchCenter.placeOrder(pkg);
        dispatchCenter.completeDelivery("p1");

        List<Package> result = dispatchCenter.getPackagesDeliveredBy("r1", System.currentTimeMillis() - 10000);
        assertEquals(1, result.size());
        assertEquals("p1", result.get(0).getId());
    }

    @Test
    public void testMissedExpressDelivery() throws InterruptedException {
        Rider rider = new Rider("r1", RiderStatus.OFFLINE, 4.5, true);
        dispatchCenter.registerRider(rider);

        Package express = new Package("p1", PackagePriority.EXPRESS, System.currentTimeMillis(), System.currentTimeMillis() - 1000, false, null, 0, 0);
        dispatchCenter.placeOrder(express);

        List<Package> missed = dispatchCenter.getMissedExpressDeliveries();
        assertFalse(missed.isEmpty());
        assertEquals("p1", missed.get(0).getId());
    }

    @Test
    public void testAuditTrailLogging() {
        Rider rider = new Rider("r1", RiderStatus.AVAILABLE, 4.5, true);
        dispatchCenter.registerRider(rider);

        Package pkg = new Package("p1", PackagePriority.STANDARD, System.currentTimeMillis(), System.currentTimeMillis() + 5000, false, null, 0, 0);
        dispatchCenter.placeOrder(pkg);

        dispatchCenter.completeDelivery("p1");

        List<String> logs = dispatchCenter.getAuditLog();
        assertTrue(logs.stream().anyMatch(log -> log.contains("Order placed: p1")));
        assertTrue(logs.stream().anyMatch(log -> log.contains("Rider registered: r1")));
        assertTrue(logs.stream().anyMatch(log -> log.contains("delivered by rider r1")));
    }

}

