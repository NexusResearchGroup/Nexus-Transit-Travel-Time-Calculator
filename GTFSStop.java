public class GTFSStop {
    public String id;
    public GeoPoint location;
    public Set<GTFSRoute> routes;
    
    public GTFSStop(String inputId, GeoPoint inputLocation){
        id = inputId;
        location = inputLocation;
        routes = new HashSet<GTFSRoute> routes;
    }
    
    public GTFSStop(String inputId, GeoPoint inputLocation, Set<GTFSRoute> inputRoutes) {
        id = inputId;
        location = inputLocation;
        routes = inputRoutes;
    }
    
}