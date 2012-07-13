import java.lang.Math;

public class Haversine {
    private static double defaultRadius = 6371.0; //meters
    
    public static void main(String args[]) {
        GeoPoint pointA = new GeoPoint(Double.parseDouble(args[0]), Double.parseDouble(args[1]));
        GeoPoint pointB = new GeoPoint(Double.parseDouble(args[2]), Double.parseDouble(args[3]));
        System.out.println(GeoPoint.toString(pointA));
        System.out.println(GeoPoint.toString(pointB));
        double result = distanceBetween(pointA, pointB);
        System.out.println(Double.toString(result));
    }
    
    public static double distanceBetween(GeoPoint pointA, GeoPoint pointB) {
        return distance(pointA, pointB, defaultRadius);
    }
    
    public static double distanceBetween(GeoPoint pointA, GeoPoint pointB, double radius) {
        System.out.println(Double.toString(defaultRadius));
        double lat1 = pointA.lat;
        double lat2 = pointB.lat;
        double lon1 = pointA.lon;
        double lon2 = pointB.lon;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return radius * c;
    }
}
