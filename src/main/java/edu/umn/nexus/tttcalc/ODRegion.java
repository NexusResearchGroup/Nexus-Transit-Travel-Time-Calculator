package edu.umn.nexus.tttcalc;

import java.io.*;
import java.util.*;

public class ODRegion implements Comparable<ODRegion> {
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
//        System.out.println("  Region " + id + ": added point " + point.getId());
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
    
    public int compareTo(ODRegion that) {
        return this.id.compareTo(that.id);
    }
    
    public boolean equalTo(ODRegion that) {
        return this.id.equals(that.id);
    }
}