import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.sql.Timestamp;
import java.text.DateFormat;

public class StopLineCreator {

    private static final String tripsFileName = "trips.txt";
    private static final String stop_timesFileName = "stop_times.txt";
    private static final String routesFileName = "routes.txt";

	public static void main(String args[]) {
	    
	}
	
	public String[] stopLineFromGTFS(File GTFSFile) {
	    ZipFile zipFile = new ZipFile(GTFSFile);
	    Map<String, Set> routeTripMap = StopLineCreator.createRouteTripMap(new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipFile.getEntry(StopLineCreator.tripsFileName)))));
	    Map<String, Set> tripStopMap = StopLineCreator.createTripStopMap(new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipFile.getEntry(StopLineCreator.stop_timesFileName)))));

        
	}

    private static Map<String, Set> createRouteTripMap(BufferedReader tripsReader) {
    
        Map<String, Set> routeTripMap = new HashMap<String, Set>();
        
        // swallow the csv headers
        tripsReader.readLine();
        
        while (tripsReader.ready()) {
            String row[] = tripsReader.readLine().split(",");
            if (routeTripMap.containsKey(row[0])) {
                ((HashSet)routeTripMap.get(row[0])).add(row[2]);
            } else {
                routeTripMap.put(row[0], new HashSet());
                ((HashSet)routeTripMap.get(row[0])).add(row[2]);
            }
        }
        
        return routeTripMap;
        
    }
    
    private static Map<String, Set> createTripStopMap(BufferedReader stop_timesReader) {
        
        Map<String, Set> tripStopMap = new HashMap<String, Set>();
        
        // swallow the csv headers
        stop_timesReader.readLine();
        
        while (stop_timesReader.ready()) {
            String row[] = stop_timesReader.readLine().split(",");
            if (tripStopMap.containsKey(row[0])) {
                ((HashSet)tripStopMap.get(row[0])).add(row[3]);
            } else {
                tripStopMap.put(row[0], new HashSet());
                ((HashSet)tripStopMap.get(row[0])).add(row[3]);
            }
        }
        
        return tripStopMap;
    }

    private static HashMap createRouteStopMap(HashMap routeTripMap, HashMap tripStopMap) {
    
    }

}