public class StopRoutesPair {
    public GTFSStop stop;
    public Set<GTFSRoute> routes;
    
    public StopLinePair(GTFSStop inputStop) {
        stop = inputStop;
        routes = new HashSet<GTFSRoute>();
    }
    
    public StopLinePair(GTFSStop inputStop, Set<GTFSRoute> inputRoutes) {
        stop = inputStop;
        routes = inputRoutes;
    }
}