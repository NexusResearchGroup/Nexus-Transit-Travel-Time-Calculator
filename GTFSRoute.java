import java.io.*;
import java.util.*;

public class GTFSRoute {
    public String id;
    public String name;
    public Set<GTFSTrip> trips;
    public Set<GTFSStop> stops;
    
    public GTFSRoute(String inputId, String inputName) {
        id = inputId;
        name = inputName;
        trips = new HashSet<GTFSTrip>();
        stops = new HashSet<GTFSStop>();
    }
    
    public void addStop(GTFSStop stop) {
        stops.add(stop);
    }
    
    public void addTrip(GTFSTrip trip) {
        trips.add(trip);
    }
}