import java.io.*;
import java.util.*;
import java.util.zip.*;

public class GTFSData {
    
    private static final String tripsFileName = "trips.txt";
    private static final String stopTimesFileName = "stop_times.txt";
    private static final String stopsFileName = "stops.txt";
    private static final String routesFileName = "routes.txt";
    private static final double transferThreshold = 400.0; // meters
    private static final double stopJoinThreshold = 8000.0;; // meters
    private static final double walkSpeed = 5000.0; // m/h
    private static final double circuityAdjustment = 1.2;
    
    private String selectedServiceId = "";
	private Map<String, GTFSStop> stops = null;
	private Map<String, GTFSRoute> routes = null;
	private Map<String, GTFSTrip> trips = null;
	private Map<String, ODPoint> points = null;
	private Map<String, ODRegion> regions = null;

	public GTFSData(String gtfsFileName, String pointsFileName, String regionsFileName, String serviceId) {
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
            File regionsFile = new File(regionsFileName);
            loadODRegions(regionsFile);
            File pointsFile = new File(pointsFileName);
            loadODPoints(pointsFile);
        } catch (Exception e) {
            System.out.println ("exception:" + e);
        }
	}
	
//     public static void main (String[] args) {
//         // Usage: GTFSData google_transit.zip points.csv MAY12-Multi-Weekday-01 s stop_line.csv
//         String gtfsFileName = args[0];
//         String pointsFileName = args[1];
//         String selectedServiceId = args[2];
//         String command = args[3];
//         String outputFileName = args[4];
//         
//         GTFSData g = new GTFSData(gtfsFileName, pointsFileName, selectedServiceId);
//         
//         if (command.equals("s")) {
//             g.writeStopLineFile(outputFileName);
//         } else if (command.equals("t")) {
//             g.writeTransferFile(outputFileName);
//         } else if (command.equals("c")) {
//             g.writeStopPointFile(outputFileName);
//         }
//         
//     }
    
    private void writeStopLineFile(String outputFileName) {
        System.out.println("Writing stop-line file...");
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(outputFileName));        
            writer.write("stopID,lineno");
            writer.newLine();

            for (GTFSStop stop : getStops()) {
                //System.out.println(stop.id);
                for (GTFSRoute route : stop.getRoutes()) {
                    //System.out.println("  "+route.name);
                    writer.write(stop.getId() + "," + route.getName());
                    writer.newLine();
                }
            }
            writer.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
    private void writeTransferFile(String outputFileName) {
        System.out.println("Writing transfer file...");
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(outputFileName));        
            writer.write("line1,stop1,line2,stop2");
            writer.newLine();

            for (GTFSStop stop1 : getStops()) {
                for (GTFSRoute route1 : stop1.getRoutes()) {
                    for (GTFSStop stop2 : stop1.getTransferStops()) {
                        for (GTFSRoute route2 : stop2.getRoutes()) {
                            writer.write(route1.getName() +","+ stop1.getId() +","+ route2.getName() +","+ stop2.getId());
                            writer.newLine();
                        }
                    }
                }
            }
            writer.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
    private void writeStopPointFile(String outputFileName) {
        System.out.println("Writing stop-point file...");
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(outputFileName));        
            writer.write("stopID,pointID,access_time");
            writer.newLine();

            for (GTFSStop stop : getStops()) {
                for (ODPoint point : stop.getPoints()) {
                    writer.write(stop.getId() +","+ point.getId() +","+ (double)stop.getAccessTimeForPoint(point) / 60);
                    writer.newLine();
                }
            }
            writer.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
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
	        //System.out.println("Creating new trip " + tripId + " for route " + routeId);
	        GTFSRoute route = routes.get(routeId);
	        GTFSTrip trip = new GTFSTrip(tripId, route);
	        trips.put(tripId, trip);
	        route.addTrip(trip);
	    }
	}
	
	private void addTripStopTime(String tripId, String stopId, int time) throws Exception {
	    GTFSTrip trip = trips.get(tripId);
	    //System.out.println("This trip belongs to route " + trip.routeId);
	    GTFSRoute route = trip.getRoute();
	    GTFSStop stop = stops.get(stopId);
	    GTFSStopTime stopTime = new GTFSStopTime(trip, stop, time);
	    
        stop.addStopTime(stopTime);
        route.addStop(stop);
        route.addTrip(trip);
        trip.addStopTime(stopTime);
	}
	
	private void addTripStopTime(String tripId, String stopId, String timeString) throws Exception {
	    int time = GTFSData.gtfsTimeToSeconds(timeString);
	    addTripStopTime(tripId, stopId, time);
	}
	
	private void addODPoint(String pointId, String containerId, String lat, String lon) throws Exception {
        GeoPoint location = new GeoPoint(Double.parseDouble(lat), Double.parseDouble(lon));
        addODPoint(pointId, containerId, location);
	}
	
	private void addODPoint(String pointId, String regionId, GeoPoint location) throws Exception {
	    double stopPointDistance;
	    Integer accessTime;
	    ODPoint point = null;
	    
	    if (points.containsKey(pointId)) {
	        throw new Exception("Duplicate point ID: " + pointId);
	    } else {
	        point = new ODPoint(pointId, location);
	        points.put(pointId, point);
	        for (GTFSStop stop : stops.values()) {
	            stopPointDistance = Haversine.distanceBetween(stop.getLocation(), point.getLocation()) * circuityAdjustment;
	            if (stopPointDistance <= stopJoinThreshold) {
	                accessTime = (int)Math.round(stopPointDistance / walkSpeed * 3600);
	                point.addStop(stop, accessTime);
	            }
	        }
	    }
	    
	    if (!regions.containsKey(regionId)) {
	        throw new Exception("Tried to add point to nonexistent region ID: " + regionId);
	    } else {
	        regions.get(regionId).addPoint(point);
	    }
	}
	
	private void addODRegion(String regionId, String lat, String lon) throws Exception {
	    GeoPoint location = new GeoPoint(Double.parseDouble(lat), Double.parseDouble(lon));
	    addODRegion(regionId, location);
	}
	
	private void addODRegion(String regionId, GeoPoint location) throws Exception {
	    if (regions.containsKey(regionId)) {
	        throw new Exception("Duplicate region ID: " + regionId);
	    } else {
	        ODRegion region = new ODRegion(regionId, location);
	        regions.put(regionId, region);
	    }
	}
	
	private void connectOrphanStops() {
	    System.out.println("Connecting unconnected stops...");
	    double shortestDistance = Double.MAX_VALUE;
	    double distance;
	    ODPoint closestPoint = null;
	    int accessTime;
	    int numAdded = 0;
	    
	    for (GTFSStop stop : stops.values()) {
	        if (stop.getPoints().isEmpty()) {
	            for (ODPoint point : points.values()) {
	                distance = Haversine.distanceBetween(stop.getLocation(), point.getLocation()) * circuityAdjustment;
	                if (distance < shortestDistance) {
	                    shortestDistance = distance;
	                    closestPoint = point;
	                }
	            }
	            accessTime = (int)Math.round(shortestDistance / walkSpeed * 3600);
	            if (closestPoint != null) {
	                closestPoint.addBackupStop(stop, accessTime);
	                numAdded++;
	            }
	        }
	    }
	    System.out.println(numAdded + " out of " + stops.size() + " stops were unconnected");
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
	
	private void loadODRegions(File regionsFile) {
	    System.out.println("Loading OD regions...");
	    String[] row;
	    BufferedReader regionReader;
	    
	    if (regions == null) {regions = new HashMap<String, ODRegion>(); }
	    
	    try {
	        regionReader = new BufferedReader(new FileReader(regionsFile));
	    } catch (FileNotFoundException e) {
	        System.err.println(e.getMessage());
	        return;
	    }
	    
	    try {
	        //swallow the csv headers
	        regionReader.readLine();
	        while (regionReader.ready()) {
	            row = regionReader.readLine().split(",");
	            try {
	                addODRegion(row[0], row[1], row[2]);
	            } catch (Exception e) {
	                System.err.println(e.getMessage());
	            }
	        }
	    } catch (IOException e) {
	        System.err.println("Error reading regions; aborting");
	        return;
	    }
	}
	
	private void loadODPoints(File pointsFile) {
	    System.out.println("Loading OD points and connecting to stops...");
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
                    addODPoint(row[0], row[1], row[2], row[3]);
                }
                catch (Exception e) {
                    System.err.println("Error loading point " + row[0]);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading points; aborting");
            return;
        }
        
        connectOrphanStops();
	}
	
	private void updateStopTransfers(GTFSStop newStop) {
	    for (GTFSStop otherStop : stops.values()) {
	    	if (otherStop == newStop) continue;
	    	double distance = Haversine.distanceBetween(newStop.getLocation(), otherStop.getLocation()) * circuityAdjustment;
            if (distance <= transferThreshold) {
            	int transferTime = (int)Math.round(distance / walkSpeed * 3600);
                otherStop.addTransferStop(newStop, transferTime);
                newStop.addTransferStop(otherStop, transferTime);
            }
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
	
	public Set<String> getStopIds() {
		return stops.keySet();
	}
	
	public Collection<ODPoint> getPoints() {
	    return points.values();
	}
	
	public Collection<ODRegion> getRegions() {
	    return regions.values();
	}
	
	public Collection<GTFSTrip> getTrips() {
	    return trips.values();
	}
	
	public GTFSRoute getRouteForId(String id) {
	    return routes.get(id);
	}
	
	public GTFSTrip getTripForId(String id) {
		return trips.get(id);
	}
	
	public GTFSStop getStopForId(String id) {
		return stops.get(id);
	}
    
}