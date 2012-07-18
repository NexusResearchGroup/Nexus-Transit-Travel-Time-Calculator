import java.io.*;
import java.util.*;

public class GTFSTrip {
    public String id;
    public String routeId;
    public NavigableMap<Integer, Set<GTFSStop>> stopTimes;
    
    public GTFSTrip(String inputId, String inputRouteId) {
        id = inputId;
        routeId = inputRouteId;
        //System.out.println("Trip " +id + " belongs to route " + routeId);
        stopTimes = new TreeMap<Integer, Set<GTFSStop>>();
    }
    
    public NavigableMap<Integer, Set<GTFSStop>> stopTimesAfter(Integer time) {
        return stopTimes.tailMap(time, false);
    }
    
    public int firstStopTime() {
        Map.Entry<Integer, Set<GTFSStop>> firstEntry = stopTimes.firstEntry();
        return firstEntry.getKey();
    }
    
    public int lastStopTime() {
        Map.Entry<Integer, Set<GTFSStop>> lastEntry = stopTimes.lastEntry();
        return lastEntry.getKey();
    }
    
    public void addStopTime(GTFSStop stop, int time) {
        if (stopTimes.containsKey(time)) {
            stopTimes.get(time).add(stop);
        } else {
            stopTimes.put(time, new HashSet<GTFSStop>());
            stopTimes.get(time).add(stop);
        }
    }
    
}