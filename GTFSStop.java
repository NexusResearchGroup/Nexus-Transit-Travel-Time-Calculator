import java.io.*;
import java.util.*;

public class GTFSStop {
    public String id;
    public GeoPoint location;
    public Set<GTFSStop> transferStops;
    public Set<GTFSRoute> routes;
    public NavigableMap<int, Set<GTFSTrip>> tripTimes;
    public Map<String, int> pointAccessTimes;
    
    public GTFSStop(String inputId, GeoPoint inputLocation){
        id = inputId;
        location = inputLocation;
        transferStops = new HashSet<GTFSStop>();
        routes = new HashSet<GTFSRoute>();
        tripTimes = new TreeMap<int, Set<GTFSTrip>>();
        pointAccessTimes = new HashMap<String, int>();
    }
    
    public addTripTime(GTFSTrip trip, int time) {
        if tripTimes.hasKey(time) {
            tripTimes.get(time).add(trip);
        } else {
            tripTimes.put(time, new HashSet<GTFSTrip>(trip));
        }
    }
    
}