import java.io.*;
import java.util.*;

public class ODRegion {
    private final String id;
    private final GeoPoint location;
    private Set<ODPoint> points;
    
    public ODRegion(String id, GeoPoint location) {
        this.id = id;
        this.points = new HashSet<ODPoint>();
        this.location = location;
    }
    
    public void addPoint(ODPoint point) {
        points.add(point);
    }
    
    public Set<ODPoint> getPoints() {
        return points;
    }
    
    public String getId() {
        return id;
    }
    
    public GeoPoint getLocation() {
        return location;
    }
}