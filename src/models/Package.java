package models;


public class Package {
    private final String id;
    private final PackagePriority priority;
    private final long orderTime;
    private final long deliveryDeadline;
    private final boolean isFragile;

    public PackageStatus getPackageStatus() {
        return packageStatus;
    }

    public void setPackageStatus(PackageStatus packageStatus) {
        this.packageStatus = packageStatus;
    }

    public long getPickUpTime() {
        return pickUpTime;
    }

    public void setPickUpTime(long pickUpTime) {
        this.pickUpTime = pickUpTime;
    }

    public long getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(long deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    private PackageStatus packageStatus;
    private long pickUpTime;
    private long deliveryTime;

    public Package(String id, PackagePriority priority, long orderTime, long deliveryDeadline, boolean isFragile, PackageStatus  packageStatus, long pickUpTime, long deliveryTime) {
        this.id = id;
        this.priority = priority;
        this.orderTime = orderTime;
        this.deliveryDeadline = deliveryDeadline;
        this.isFragile = isFragile;
        this.packageStatus = packageStatus;
        this.pickUpTime = pickUpTime;
        this.deliveryTime = deliveryTime;
    }

    public String getId() {
        return id;
    }

    public PackagePriority getPriority() {
        return priority;
    }

    public PackagePriority setPriority(PackagePriority priority) {
        return this.priority;
    }

    public long getOrderTime() {
        return orderTime;
    }

    public long getDeliveryDeadline() {
        return deliveryDeadline;
    }

    public boolean isFragile() {
        return isFragile;
    }

}
