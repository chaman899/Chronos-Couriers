import Service.DispatchCenter;
import models.PackagePriority;
import models.Package;
import models.Rider;
import models.RiderStatus;

import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        DispatchCenter dispatchCenter = new DispatchCenter();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String[] cmd = scanner.nextLine().split(" ");
            switch (cmd[0]) {
                case "placeOrder":
                    Package pkg = new Package(cmd[1], PackagePriority.valueOf(cmd[2]), System.currentTimeMillis(), Long.parseLong(cmd[3]), Boolean.parseBoolean(cmd[4]), null, 0, 0);
                    dispatchCenter.placeOrder(pkg);
                    break;

                case "registerRider":
                    Rider rider = new Rider(cmd[1], RiderStatus.AVAILABLE, Double.parseDouble(cmd[3]), Boolean.parseBoolean(cmd[2]));
                    dispatchCenter.registerRider(rider);
                    break;

                case "updateRiderStatus":
                    dispatchCenter.updateRiderStatus(cmd[1], RiderStatus.valueOf(cmd[2]));
                    break;

                case "completeDelivery":
                    dispatchCenter.completeDelivery(cmd[1]);
                    break;

                case "missedExpressDeliveries":
                    List<Package> missed = dispatchCenter.getMissedExpressDeliveries();
                    if (missed.isEmpty()) {
                        System.out.println("No missed EXPRESS deliveries.");
                    } else {
                        System.out.println("Missed EXPRESS Deliveries:");
                        for (Package p : missed) {
                            System.out.println("ID: " + p.getId() + ", Deadline: " + new Date(p.getDeliveryDeadline()) + ", Status: " + p.getPackageStatus() + ", Ordered At: " + new Date(p.getOrderTime()));
                        }
                    }
                    break;

                case "auditLog":
                    dispatchCenter.getAuditLog().forEach(System.out::println);
                    break;
            }
        }
    }
}