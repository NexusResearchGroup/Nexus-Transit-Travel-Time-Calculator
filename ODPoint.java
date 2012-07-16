import java.io.*;
import java.util.*;

public class ODPoint {
    public String id;
    public GeoPoint location;
    
    public ODPoint(String inputPointId, GeoPoint inputLocation) {
        id = inputPointId;
        location = inputLocation;
    }
}