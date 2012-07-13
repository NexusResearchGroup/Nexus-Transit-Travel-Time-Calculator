import java.io.*;
import java.util.*;
import java.util.zip.*;

public class StopLineListCreator {

    private static final String tripsFileName = "trips.txt";
    private static final String stop_timesFileName = "stop_times.txt";
    private static final String routesFileName = "routes.txt";

	public static void main(String args[]) {

        String gtfsFileName = args[0];
        File gtfsFile = new File(gtfsFileName);
        StopLineListCreator SLLC = new StopLineListCreator();
	    SLLC.printStopLineList(SLLC.stopLineListFromGTFS(gtfsFile));
	}
	
	public void printStopLineList(List<StopLinePair> stopLineList) {
	    //System.out.println("printStopLineList\n");
	    System.out.println("stopID,lineno");
	    for (StopLinePair pair : stopLineList) {
	        for (GTFSRoute route : pair.routes) {
	            System.out.println(pair.stop.id + "," + pair.route.id);
	    }
	}
	
	public List<StopLinePair> stopLineListFromGTFS(File GTFSFile) {
        //System.out.println("stopLineListFromGTFS\n");
        
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(GTFSFile);
        } catch (Exception e) {
            System.out.println ("exception:" + e );
            return new ArrayList<StopLinePair>();
        }
    
        BufferedReader tripsReader = this.bufferedReaderFromZipFileEntry(zipFile, StopLineListCreator.tripsFileName);
        BufferedReader stop_timesReader = this.bufferedReaderFromZipFileEntry(zipFile, StopLineListCreator.stop_timesFileName);
        BufferedReader routesReader = this.bufferedReaderFromZipFileEntry(zipFile, StopLineListCreator.routesFileName);
        
        Map<GTFSRoute, Set<String>> routeTripMap = this.routeTripMap(tripsReader);
        Map<String, Set<GTFSStop>> tripStopMap = this.tripStopMap(stop_timesReader);
        Map<String, String> routeIDShortIDMap = this.routeIDShortIDMap(routesReader);
        Map<GTFSRoute, Set<GTFSStop>> routeStopMap = this.routeStopMap(routeTripMap, tripStopMap, routeIDShortIDMap);
	    
	    return this.stopLineListFromRouteStopMap(routeStopMap);
        
	}

    private BufferedReader bufferedReaderFromZipFileEntry(ZipFile zipFile, String fileName) {
        //System.out.println("bufferedReaderFromZipFileEntry\n");
        
        BufferedReader reader;
        
        try {
            reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipFile.getEntry(fileName))));
        } catch (Exception e){
            reader = new BufferedReader(new StringReader(""));
        }
        
        return reader;
    }

    private Map<String, String> routeIDShortIDMap(BufferedReader routesReader) {
        //System.out.prinln("routeIDShortIDMap")
        
        Map<String, String> routeIDShortIDMap = new HashMap<String, String>();
        
        try {
            //swallow the csv headers
            routesReader.readLine();
            
            while (routesReader.ready()) {
                String row[] = routesReader.readLine().split(",");
                routeIDShortIDMap.put(row[0], row[2]);
            }
        } catch (Exception e) {
            System.out.println ("exception:" + e );
        }       
        return routeIDShortIDMap;
    }

    private Map<String, Set<String>> routeTripMap(BufferedReader tripsReader) {
        //System.out.println("routeTripMap\n");
        
        Map<String, Set<String>> routeTripMap = new HashMap<String, Set<String>>();
        
        try {
            // swallow the csv headers
            tripsReader.readLine();
        
            while (tripsReader.ready()) {
                String row[] = tripsReader.readLine().split(",");

                if (routeTripMap.containsKey(row[0])) {
                    routeTripMap.get(row[0]).add(row[2]);
                } else {
                    routeTripMap.put(row[0], new HashSet<String>());
                    routeTripMap.get(row[0]).add(row[2]);
                }
            }
 
        } catch (Exception e) {
            System.out.println ("exception:" + e );
        }       
        return routeTripMap;
        
    }
    
    private Map<String, Set<String>> tripStopMap(BufferedReader stop_timesReader) {
        //System.out.println("tripStopMap\n");
        
        Map<String, Set<String>> tripStopMap = new HashMap<String, Set<String>>();
        
        try {
            // swallow the csv headers
            stop_timesReader.readLine();
            
            while (stop_timesReader.ready()) {
                String row[] = stop_timesReader.readLine().split(",");
                if (tripStopMap.containsKey(row[0])) {
                    tripStopMap.get(row[0]).add(row[3]);
                } else {
                    tripStopMap.put(row[0], new HashSet<String>());
                    tripStopMap.get(row[0]).add(row[3]);
                }
            }
        } catch (Exception e) {
            System.out.println ("exception:" + e );
        }
        
        return tripStopMap;
    }

    private Map<String, Set<String>> routeStopMap(Map<String, Set<String>> routeTripMap, Map<String, Set<String>> tripStopMap, Map<String, String> routeIDShortIDMap) {
        //System.out.println("routeStopMap\n");
        
        Map<String, Set<String>> routeStopMap = new HashMap<String, Set<String>>();
        
        for (Map.Entry<String, Set<String>> routeTripPair : routeTripMap.entrySet()) {
            String routeID = routeTripPair.getKey();
            String shortRouteID = routeIDShortIDMap.get(routeID);
            Set<String> tripIDs = routeTripPair.getValue();
            
            Set<String> stops = new HashSet<String>();
            
            for (String tripID : tripIDs) {
                if (tripStopMap.containsKey(tripID)) {
                    stops.addAll(tripStopMap.get(tripID));
                } else {
                    //System.out.println("Trip " + tripID + " not in stop_times.txt");
                }
            }
            
            routeStopMap.put(shortRouteID, stops);
        }
        
        return routeStopMap;
    }
    
    private List<StopLinePair> stopLineListFromRouteStopMap(Map<String, Set<String>> routeStopMap) {
        //System.out.println("stopLineListFromRouteStopMap\n");
    
        List<StopLinePair> stopLineList = new ArrayList<StopLinePair>();
        
        for (Map.Entry<String,Set<String>> routeStopPair : routeStopMap.entrySet()) {
            String routeID = routeStopPair.getKey();
            Set<String> stops = routeStopPair.getValue();
            
            for (String stopID : stopIDs) {
                stopLineList.add(new StopLinePair(stopID, routeID));
            }
        }
        
        return stopLineList;
    }

}