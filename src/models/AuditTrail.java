package models;

import java.util.ArrayList;
import java.util.List;

public class AuditTrail {
    private final List<String> logs = new ArrayList<>();

    public void log(String message) {
        logs.add(System.currentTimeMillis() + ": " + message);
    }

    public List<String> getLogs() {
        return logs;
    }
}
