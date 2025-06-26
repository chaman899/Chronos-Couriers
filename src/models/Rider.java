package models;

public class Rider {
    private final String id;
    private RiderStatus riderStatus;
    private final double reliabilityRating;
    private final boolean canHandleFragile;


    public Rider(String id, RiderStatus riderStatus, double reliabilityRating, boolean canHandleFragile) {
        this.id = id;
        this.riderStatus = riderStatus; // Default status
        this.reliabilityRating = reliabilityRating;
        this.canHandleFragile = canHandleFragile;
    }

    public String getId() {
        return id;
    }

    public RiderStatus getRiderStatus() {
        return riderStatus;
    }

    public void setRiderStatus(RiderStatus riderStatus) {
        this.riderStatus = riderStatus;
    }

    public double getReliabilityRating() {
        return reliabilityRating;
    }

    public boolean isCanHandleFragile() {
        return canHandleFragile;
    }
}
