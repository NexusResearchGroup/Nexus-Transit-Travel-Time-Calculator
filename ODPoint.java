import java.io.*;
import java.util.*;

public class ODPoint {
    private String id;
    private GTFSStop closestStop;
    private GTFSStop secondClosestStop;
    private Map<GTFSStop, Integer> stopAccessTimes;
    private GeoPoint location;
    
    public ODPoint(String inputPointId, GeoPoint inputLocation) {
        id = inputPointId;
        location = inputLocation;
        stopAccessTimes = new HashMap<GTFSStop, Integer>();
    }
    
    public GeoPoint getLocation() {
    	return location;
    }
    
    public String getId() { return id; }
    
    public void addStop(GTFSStop stop, int accessTime) {
    	if (closestStop == null) {
    		// there is no closest stop, so use this one
    		closestStop = stop;
    		stopAccessTimes.put(closestStop, accessTime);
    		stop.addPoint(this, accessTime);
    	
    	} else if (secondClosestStop == null) {
    		// there is no second closest stop, so use this one
    		secondClosestStop = stop;
    		stopAccessTimes.put(secondClosestStop, accessTime);
    		stop.addPoint(this, accessTime);
    	
    	} else if (accessTime < stopAccessTimes.get(closestStop)) {
            // new stop replaces the closest stop
            
            // remove the old second closest stop
            stopAccessTimes.remove(secondClosestStop);
            secondClosestStop.removePoint(this);
            
            // make the old closest the second closest
            secondClosestStop = closestStop;

            // make the new stop the closest
            closestStop = stop;
            stopAccessTimes.put(closestStop, accessTime);
            closestStop.addPoint(this, accessTime);
            
        } else if (accessTime < stopAccessTimes.get(secondClosestStop)) {
            // new stop is now the second closest stop
            
            // remove the old second closest stop
            stopAccessTimes.remove(secondClosestStop);
            secondClosestStop.removePoint(this);
            
            // make the new stop the second closest
            secondClosestStop = stop;
            stopAccessTimes.put(secondClosestStop, accessTime);
            secondClosestStop.addPoint(this, accessTime);
        } else {
            // new stop is farther away than both current stops, ignore it
            return;
        }
    }
    
    public void addBackupStop(GTFSStop stop, int accessTime) {
        stopAccessTimes.put(stop, accessTime);
        stop.addPoint(this, accessTime);
    }
    
    public Map<GTFSStop, Integer> getStopAccessTimes() {
        return stopAccessTimes;
    }
    
    public Set<GTFSStop> getStops() {
        return stopAccessTimes.keySet();
    }
}