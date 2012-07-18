import java.io.*;
import java.util.*;
import java.util.zip.*;

public class GTFSData {
    
    private static final String tripsFileName = "trips.txt";
    private static final String stopTimesFileName = "stop_times.txt";
    private static final String stopsFileName = "stops.txt";
    private static final String routesFileName = "routes.txt";
    private static final double transferThreshold = 400.0; // meters
    private static final double stopJoinThreshold = 5000.0;; // meters
    private static final double walkSpeed = 5000.0; // m/h
    private static final double circuityAdjustment = 1.2;
    
    private String selectedServiceId = "";
	private Map<String, GTFSStop> stops = null;
	private Map<String, GTFSRoute> routes = null;
	private Map<String, GTFSTrip> trips = null;
	private Map<String, ODPoint> points = null;
	
    public static void main (String[] args) {
        // Usage: GTFSData google_transit.zip points.csv MAY12-Multi-Weekday-01
        String gtfsFileName = args[0];
        String pointsFileName = args[1];
        String selectedServiceId = args[2];
//         int beginTime = Integer.parseInt(args[2]);
//         int endTime = Integer.parseInt(args[3]);
//         Set<String> selectedRoutes = new HashSet<String>(Arrays.asList(args[4].split(",")));
        
        GTFSData g = new GTFSData(gtfsFileName, pointsFileName, selectedServiceId);
        
//         for (GTFSStop stop : g.getStops()) {
//             System.out.println(stop.id);
//             for (String pointId : stop.getPoints()) {
//                 System.out.println("    " + pointId);
//             }
//         }
    }
    
    public static int gtfsTimeToSeconds(String timeString) {
        String[] timePieces = timeString.split(":");
        return (3600 * Integer.parseInt(timePieces[0])) + (60 * Integer.parseInt(timePieces[1])) + Integer.parseInt(timePieces[2]);
    }

    private static BufferedReader bufferedReaderFromZipFileEntry(ZipFile zipFile, String fileName) {        
        BufferedReader reader;
        
        try {
            reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipFile.getEntry(fileName))));
        } catch (Exception e){
            reader = new BufferedReader(new StringReader(""));
            System.out.println("Error reading zip entry: " + fileName);
        }
        
        return reader;
    }	
	
	private void addStop(String stopId, String lat, String lon) throws Exception {
        GeoPoint location = new GeoPoint(Double.parseDouble(lat), Double.parseDouble(lon));
        addStop(stopId, location);
	}
	
	private void addStop(String stopId, GeoPoint location) throws Exception {
	    if (stops.containsKey(stopId)) {
	        throw new Exception("Duplicate stop ID: " + stopId);
	    } else {
	        GTFSStop stop = new GTFSStop(stopId, location);
	        stops.put(stopId, stop);
	        updateStopTransfers(stop);
	    }
	}
	
	private void addRoute(String routeId, String routeName) throws Exception {
	    if (routes.containsKey(routeId)) {
	        throw new Exception("Duplicate route ID: " + routeId);
	    } else {
	        GTFSRoute route = new GTFSRoute(routeId, routeName);
	        routes.put(routeId, route);
	    }
	}
	
	private void addTrip(String tripId, String routeId) throws Exception {
	    if (trips.containsKey(tripId)) {
	        throw new Exception("Duplicate trip ID: " + tripId);
	    } else {
	        GTFSTrip trip = new GTFSTrip(tripId, routeId);
	        trips.put(tripId, trip);
	        routes.get(routeId).addTrip(trip);
	    }
	}
	
	private void addTripStopTime(String tripId, String stopId, int time) throws Exception {
	    GTFSTrip trip = trips.get(tripId);
	    GTFSRoute route = routes.get(trip.routeId);
	    GTFSStop stop = stops.get(stopId);
	    
        stop.addRoute(route);
        stop.addTripTime(trip, time);
        route.addStop(stop);
        route.addTrip(trip);
        trip.addStopTime(stop, time);

	}
	
	private void addTripStopTime(String tripId, String stopId, String timeString) throws Exception {
	    int time = GTFSData.gtfsTimeToSeconds(timeString);
	    addTripStopTime(tripId, stopId, time);
	}
	
	private void addODPoint(String pointId, String lat, String lon) throws Exception {
        GeoPoint location = new GeoPoint(Double.parseDouble(lat), Double.parseDouble(lon));
        addODPoint(pointId, location);
	}
	
	private void addODPoint(String pointId, GeoPoint location) throws Exception {
	    double stopPointDistance;
	    Integer accessTime;
	    
	    if (points.containsKey(pointId)) {
	        throw new Exception("Duplicate point ID: " + pointId);
	    } else {
	        ODPoint point = new ODPoint(pointId, location);
	        points.put(pointId, point);
	        for (GTFSStop stop : stops.values()) {
	            stopPointDistance = Haversine.distanceBetween(stop.location, point.location) * circuityAdjustment;
	            if (stopPointDistance <= stopJoinThreshold) {
	                accessTime = (int)Math.round(stopPointDistance / walkSpeed * 3600);
	                stop.addPoint(pointId, accessTime);
	            }
	        }
	    }
	}

	private void loadStops(ZipFile zipFile) {
	    System.out.println("Loading stops...");
	    String[] row;
	    BufferedReader stopReader = GTFSData.bufferedReaderFromZipFileEntry(zipFile, stopsFileName);
	    
	    if (stops == null) { stops = new HashMap<String, GTFSStop>(); }
	    
	    try {
	        //swallow the csv headers
	        stopReader.readLine();
            while (stopReader.ready()) {
                row = stopReader.readLine().split(",");
                try {
                    addStop(row[0], row[3], row[4]);
                }
                catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading stops; aborting");
            return;
        }

	}
	
	private void loadRoutes(ZipFile zipFile) {
        System.out.println("Loading routes...");
	    String[] row;
	    BufferedReader routeReader = GTFSData.bufferedReaderFromZipFileEntry(zipFile, routesFileName);
	    
	    if (routes == null) { routes = new HashMap<String, GTFSRoute>(); }
	    
	    try {
	        //swallow the csv headers
	        routeReader.readLine();
            while (routeReader.ready()) {
                row = routeReader.readLine().split(",");
                try {
                    addRoute(row[0], row[2]);
                }
                catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading routes; aborting");
            return;
        }

	}
	
	private void loadTrips(ZipFile zipFile) {
	    System.out.println("Loading trips...");
	    String[] row;
	    BufferedReader tripReader = GTFSData.bufferedReaderFromZipFileEntry(zipFile, tripsFileName);
	    
	    if (trips == null) { trips = new HashMap<String, GTFSTrip>(); }
	    
	    try {
	        //swallow the csv headers
	        tripReader.readLine();
            while (tripReader.ready()) {
                row = tripReader.readLine().split(",");
                if (row[1].equals(selectedServiceId)) { // only process trips for the selected service Id
                    try {
                        addTrip(row[2], row[0]);
                    }
                    catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading routes; aborting");
            return;
        }
	}
	
	private void loadTripStopTimes(ZipFile zipFile) {
	    System.out.println("Loading stop times...");
	    String[] row;
	    BufferedReader stopTimeReader = GTFSData.bufferedReaderFromZipFileEntry(zipFile, stopTimesFileName);
	    
	    try {
	        //swallow the csv headers
	        stopTimeReader.readLine();
            while (stopTimeReader.ready()) {
                row = stopTimeReader.readLine().split(",");
                if (trips.containsKey(row[0])) { // only process trips for the selected service Id
                    try {
                        addTripStopTime(row[0], row[3], row[2]);
                    }
                    catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading routes; aborting");
            return;
        }
	}
	
	private void loadODPoints(File pointsFile) {
	    System.out.println("Loading OD points...");
	    String[] row;
	    BufferedReader pointReader;

	    if (points == null) { points = new HashMap<String, ODPoint>(); }


	    try {
	        pointReader = new BufferedReader(new FileReader(pointsFile));
	    } catch (FileNotFoundException e) {
	        System.err.println(e.getMessage());
	        return;
	    }
	    	    
	    try {
	        //swallow the csv headers
	        pointReader.readLine();
            while (pointReader.ready()) {
                row = pointReader.readLine().split(",");
                try {
                    addODPoint(row[0], row[1], row[2]);
                }
                catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading points; aborting");
            return;
        }
	}
	
	private void updateStopTransfers(GTFSStop newStop) {
	    for (GTFSStop otherStop : stops.values()) {
            if (Haversine.distanceBetween(newStop.location, otherStop.location) <= transferThreshold) {
                otherStop.transferStops.add(newStop);
                newStop.transferStops.add(otherStop);
            }
	    }
	}
	
	public GTFSData(String gtfsFileName, String pointsFileName, String serviceId) {
        selectedServiceId = serviceId;
        
        try {
            ZipFile zipFile = new ZipFile(gtfsFileName);
            loadStops(zipFile);
            loadRoutes(zipFile);
            loadTrips(zipFile);
            loadTripStopTimes(zipFile);
        } catch (Exception e) {
            System.out.println ("exception:" + e );
        }
        
        try {
            File pointsFile = new File(pointsFileName);
            loadODPoints(pointsFile);
        } catch (Exception e) {
            System.out.println ("exception:" + e);
        }
	}
	
	public Collection<GTFSRoute> getRoutes() {
	    return routes.values();
	}
	
	public Set<String> getRouteIds() {
	    return routes.keySet();
	}
	
	public Collection<GTFSStop> getStops() {
	    return stops.values();
	}
	
	public Collection<ODPoint> getPoints() {
	    return points.values();
	}
	
	public Collection<GTFSTrip> getTrips() {
	    return trips.values();
	}
	
	public GTFSRoute routeWithId(String id) {
	    return routes.get(id);
	}
    
}