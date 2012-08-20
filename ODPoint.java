import java.io.*;
import java.util.*;

public class ODPoint {
    private String id;
    private GeoPoint location;
    
    public ODPoint(String inputPointId, GeoPoint inputLocation) {
        id = inputPointId;
        location = inputLocation;
    }
    
    public GeoPoint getLocation() {
    	return location;
    }
    
    public String getId() { return id; }
}