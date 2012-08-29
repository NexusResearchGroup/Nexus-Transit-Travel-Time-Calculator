package edu.umn.nexus.tttcalc;

public class GeoPoint {
    public double lat;
    public double lon;
    
    public GeoPoint(double inputLat, double inputLon) {
        lat = inputLat;
        lon = inputLon;
    }
    
    public static String toString(GeoPoint point) {
        return Double.toString(point.lat) + ", " + Double.toString(point.lon);
    }
    
}