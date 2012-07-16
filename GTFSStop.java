import java.io.*;
import java.util.*;

public class GTFSStop {
    public String id;
    public GeoPoint location;
    public Set<GTFSStop> transferStops;
    public Set<GTFSRoute> routes;
    public NavigableMap<Integer, Set<GTFSTrip>> tripTimes;
    public Map<String, Integer> pointAccessTimes;
    
    public GTFSStop(String inputId, GeoPoint inputLocation) {
        id = inputId;
        location = inputLocation;
        transferStops = new HashSet<GTFSStop>();
        routes = new HashSet<GTFSRoute>();
        tripTimes = new TreeMap<Integer, Set<GTFSTrip>>();
        pointAccessTimes = new HashMap<String, Integer>();
    }
    
    public void addTripTime(GTFSTrip trip, Integer time) {
        if (tripTimes.containsKey(time)) {
            tripTimes.get(time).add(trip);
        } else {
            tripTimes.put(time, new HashSet<GTFSTrip>());
            tripTimes.get(time).add(trip);
        }
    }
    
    public void addRoute(GTFSRoute route) {
        routes.add(route);
    }
    
    public void addTransferStop(GTFSStop stop) {
        transferStops.add(stop);
    }
    
    public void addPoint(String pointID, Integer accessTime) {
        pointAccessTimes.put(pointID, accessTime);
    }
    
    public Set<String> getPointIds() {
        return pointAccessTimes.keySet();
    }
    
    public SortedMap<Integer, Set<GTFSTrip>> tripsBetween(int startTime, int endTime) {
        return tripTimes.subMap(startTime, endTime);
    }
    
    public Set<Integer> getDepartureTimesBetween(int startTime, int endTime) {
        return tripTimes.subMap(startTime, endTime).keySet();
    }
    
    public void printDepartureTimes() {
        String times = "";
        for (int time : tripTimes.keySet()) {
            times = times + " " + time;
        }
        System.out.println(times);
    }
    
}