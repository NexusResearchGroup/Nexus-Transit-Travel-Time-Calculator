import java.lang.Math;
import java.util.*;
import java.io.*;

public class Haversine {
    private static double defaultRadius = 6371000.0; //meters
    
    public static void main(String args[]) {
        // GeoPoint pointA = new GeoPoint(Double.parseDouble(args[0]), Double.parseDouble(args[1]));
//         GeoPoint pointB = new GeoPoint(Double.parseDouble(args[2]), Double.parseDouble(args[3]));
//         System.out.println(GeoPoint.toString(pointA));
//         System.out.println(GeoPoint.toString(pointB));
//         double result = distanceBetween(pointA, pointB);
//         System.out.println(Double.toString(result));
//
		try {
			File stopFile = new File("stops.txt");
			BufferedReader stopFileReader = new BufferedReader(new FileReader(stopFile));
			stopFileReader.readLine();
			String row[] = stopFileReader.readLine().split(",");
			GeoPoint lastPoint = new GeoPoint(Double.parseDouble(row[3]), Double.parseDouble(row[4]));
			GeoPoint thisPoint = null;
			while (stopFileReader.ready()) {
				row = stopFileReader.readLine().split(",");
				thisPoint = new GeoPoint(Double.parseDouble(row[3]), Double.parseDouble(row[4]));
				System.out.println(Double.toString(distanceBetween(lastPoint, thisPoint)));
				lastPoint = thisPoint;
			}
		} catch (Exception e) {
		}
			
    }
    
    public static double distanceBetween(GeoPoint pointA, GeoPoint pointB) {
        return distanceBetween(pointA, pointB, defaultRadius);
    }
    
    public static double secondsBetween(GeoPoint pointA, GeoPoint pointB, double speed) {
        // Speed is in m/h
        return (distanceBetween(pointA, pointB) / speed * 3600.0);
    }
    
    public static double distanceBetween(GeoPoint pointA, GeoPoint pointB, double radius) {
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
