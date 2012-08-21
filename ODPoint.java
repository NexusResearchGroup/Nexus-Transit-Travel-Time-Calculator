import java.io.*;
import java.util.*;

public class ODPoint {
    private String id;
    private GTFSStop closestStop;
    private int closestStopAccessTime;
    private GTFSStop secondClosestStop;
    private int secondClosestStopAccessTime;
    private Map<GTFSStop, Integer> backupStopAccessTimes;
    private GeoPoint location;
    
    public ODPoint(String inputPointId, GeoPoint inputLocation) {
        id = inputPointId;
        location = inputLocation;
        backupStopAccessTimes = new HashMap<GTFSStop, Integer>();
        closestStopAccessTime = Integer.MAX_VALUE;
        closestStop = null;
        secondClosestStopAccessTime = Integer.MAX_VALUE;
        secondClosestStop = null;
    }
    
    public GeoPoint getLocation() {
    	return location;
    }
    
    public String getId() { return id; }
    
    public void addStop(GTFSStop stop, int accessTime) {
        if (accessTime < closestStopAccessTime) {
            // new stop is now the closest stop
            //System.out.println("  Point " + id + ": stop " + stop.getId() + " is new closest stop");
            if (secondClosestStop != null) {
                secondClosestStop.removePoint(this);
            }
            secondClosestStop = closestStop;
            secondClosestStopAccessTime = closestStopAccessTime;
            closestStop = stop;
            closestStopAccessTime = accessTime;
            stop.addPoint(this, accessTime);
        } else if (accessTime < secondClosestStopAccessTime) {
            // new stop is now the second closest stop
            //System.out.println("  Point " + id + ": stop " + stop.getId() + " is new 2nd closest stop");
            if (secondClosestStop != null) {
                secondClosestStop.removePoint(this);
            }
            secondClosestStop = stop;
            secondClosestStopAccessTime = accessTime;
            stop.addPoint(this, accessTime);
        } else {
            // new stop is farther away than both current stops, ignore it
            return;
        }
    }
    
    public void addBackupStop(GTFSStop stop, int accessTime) {
        backupStopAccessTimes.put(stop, accessTime);
        stop.addPoint(this, accessTime);
    }
    
    public Map<GTFSStop, Integer> getStopAccessTimes() {
        Map<GTFSStop, Integer> map = new HashMap<GTFSStop, Integer>(backupStopAccessTimes);
        map.put(closestStop, closestStopAccessTime);
        map.put(secondClosestStop, secondClosestStopAccessTime);
        return map;
    }
    
    public Set<GTFSStop> getStops() {
        Set<GTFSStop> stops = new HashSet<GTFSStop>(backupStopAccessTimes.keySet());
        stops.add(closestStop);
        stops.add(secondClosestStop);
        return stops;
    }
}