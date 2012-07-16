import java.io.*;
import java.util.*;
import java.util.zip.*;

public class GTFSData {
    
    private static final String tripsFileName = "trips.txt";
    private static final String stopTimesFileName = "stop_times.txt";
    private static final String stopsFileName = "stops.txt";
    private static final String routesFileName = "routes.txt";
    private static final double transferThreshold = 400.0; //meters
    
    private String selectedServiceId = "";
    private int beginTime;
    private int endTime;
	private Map<String, GTFSStop> stops = null;
	private Map<String, GTFSRoute> routes = null;
	private Map<String, GTFSTrip> trips = null;
	private Map<String, TTTODPoint> points = null;
	
    public static void main (String[] args) {
        GTFSData g = new GTFSData(new File("google_transit.zip"), "MAY12-Multi-Weekday-01");
        for (GTFSStop s : g.transferStopsForStopId("1318")) {
            System.out.println(s.id);
        }
    }
    
    public static int gtfsTimeToSeconds(String timeString) {
        String[] timePieces = timeString.split(":");
        return (3600 * Integer.parseInt(timePieces[0])) + (60 * Integer.parseInt(timePieces[1])) + Integer.parseInt(timePieces[2]);
    }
	
	private void addStop(GTFSStop newStop) throws Exception {
        if (stops.containsKey(newStop.id)) {
            throw new Exception("Skipped duplicate stop ID: " + newStop.id);
        } else {
            stops.put(newStop.id, newStop);
            updateStopTransfers(newStop);
        }
	}
	
	private void addRoute(GTFSRoute newRoute) throws Exception {
	    if (routes.containsKey(newRoute.id)) {
	        throw new Exception("Duplicate route ID: " + newRoute.id);
	    } else {
	        routes.put(newRoute.id, newRoute);
	    }
	}
	
	private void addTrip(GTFSTrip newTrip) throws Exception {
	    if (trips.containsKey(newTrip.id)) {
	        throw new Exception("Duplicate trip ID: " + newTrip.id);
	    } else {
	        trips.put(newTrip.id, newTrip);
	        routes.get(newTrip.routeId).trips.add(newTrip);
	    }
	}
	
	private void addStopTime(GTFSStopTime newStopTime) throws Exception {
	    if ( !(stops.containsKey(newStopTime.stopId)) ) {
	        throw new Exception("Trying to add stop time for invalid stop ID: " + newStopTime.stopId);
	    } else if ( !(trips.containsKey(newStopTime.tripId)) ) {
	        throw new Exception("Trying to add stop time for invalid trip ID: " + newStopTime.tripId);
	    } else {
	        GTFSTrip trip = trips.get(newStopTime.tripId);
	        GTFSRoute route = routes.get(trip.routeId);
	        GTFSStop stop = stops.get(newStopTime.stopId);
	        
	        stop.routes.add(route);
	        stop.addTripTime(trip, newStopTime.time);
	        route.stops.add(stop);
	        route.trips.add(trip);
	        trip.stopTimes.add(newStopTime);
	    }
	}
	
    private BufferedReader bufferedReaderFromZipFileEntry(ZipFile zipFile, String fileName) {        
        BufferedReader reader;
        
        try {
            reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipFile.getEntry(fileName))));
        } catch (Exception e){
            reader = new BufferedReader(new StringReader(""));
            System.out.println("Error reading zip entry: " + fileName);
        }
        
        return reader;
    }	

	private void loadStops(ZipFile zipFile) {
	    System.out.println("Loading stops...");
	    GeoPoint location;
	    GTFSStop newStop;
	    double stopLat;
	    double stopLon;
	    String stopID;
	    String[] row;
	    BufferedReader stopReader = bufferedReaderFromZipFileEntry(zipFile, stopsFileName);
	    
	    if (stops == null) { stops = new HashMap<String, GTFSStop>(); }
	    
        try {
            //swallow the csv headers
            stopReader.readLine();
            
            while (stopReader.ready()) {
                row = stopReader.readLine().split(",");
                stopID = row[0];
                stopLat = Double.parseDouble(row[3]);
                stopLon = Double.parseDouble(row[4]);
                location = new GeoPoint(stopLat, stopLon);
                newStop = new GTFSStop(stopID, location);
                addStop(newStop);
            }
        } catch (Exception e) {
            System.out.println ("exception:" + e );
        }    
	}
	
	private void loadRoutes(ZipFile zipFile) {
        System.out.println("Loading routes...");
	    String[] row;
	    GTFSRoute newRoute;
	    BufferedReader routeReader = bufferedReaderFromZipFileEntry(zipFile, routesFileName);
	    
	    if (routes == null) { routes = new HashMap<String, GTFSRoute>(); }
	    
	    try {
	        //swallow the csv headers
	        routeReader.readLine();
	        
	        while (routeReader.ready()) {
	            row = routeReader.readLine().split(",");
                newRoute = new GTFSRoute(row[0], row[2]);
                addRoute(newRoute);
            }
        } catch (Exception e) {
            System.out.println ("exception:" + e );
        }  
	}
	
	private void loadTrips(ZipFile zipFile) {
	    System.out.println("Loading trips...");
	    String[] row;
	    GTFSTrip newTrip;
	    BufferedReader tripReader = bufferedReaderFromZipFileEntry(zipFile, tripsFileName);
	    
	    if (trips == null) { trips = new HashMap<String, GTFSTrip>(); }
	    
	    try {
	        //swallow the csv headers
	        tripReader.readLine();
	        
	        while (tripReader.ready()) {
	            row = tripReader.readLine().split(",");
	            if (row[1].equals(selectedServiceId) &&
	                ) { // only process trips for the selected service Id
                    newTrip = new GTFSTrip(row[2], row[0]);
	                addTrip(newTrip);
	            }
	        }
        } catch (Exception e) {
            System.out.println ("exception:" + e );
        }  
	}
	
	private void loadStopTimes(ZipFile zipFile) {
	    System.out.println("Loading stop times...");
	    String[] row;
	    GTFSStopTime newStopTime;
	    BufferedReader stopTimeReader = bufferedReaderFromZipFileEntry(zipFile, stopTimesFileName);
	    	    
	    try {
	        //swallow the csv headers
	        stopTimeReader.readLine();
	        
	        while (stopTimeReader.ready()) {
	            row = stopTimeReader.readLine().split(",");
                if (trips.containsKey(row[0])) { // only process stoptimes for already-loaded trips
                    newStopTime = new GTFSStopTime(row[0], row[3], row[2]);
                    addStopTime(newStopTime);
                }
	        }
        } catch (Exception e) {
            System.out.println ("exception:" + e );
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
	
	public GTFSData(String gtfsFileName, String serviceId, int inputBeginTime, int inputEndTime) {
        selectedServiceId = serviceId;
        beginTime = inputBeginTime;
        endTime = inputEndTime;
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(gtfsFileName);
            loadStops(zipFile);
            loadRoutes(zipFile);
            loadTrips(zipFile);
            loadStopTimes(zipFile);
        } catch (Exception e) {
            System.out.println ("exception:" + e );
        }
	}
	
    public Set<GTFSStop> transferStopsForStopId(String stopId) {
        return stops.get(stopId).transferStops;
    }
    
    public Set<String> stopIds() {
        return stops.keySet();
    }
    
    
}