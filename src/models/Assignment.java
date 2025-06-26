package models;

public class Assignment {
    private final Package pkg;
    private final Rider rider;


    public Assignment(Package pkg, Rider rider) {
        this.pkg = pkg;
        this.rider = rider;
    }

    public Package getPkg() {
        return pkg;
    }

    public Rider getRider() {
        return rider;
    }
}
