import java.io.*;
import java.util.*;

public class RegionResultMatrix {
    private Map<ODRegion, Map<ODRegion, Integer>> matrix;
    
    public RegionResultMatrix() {
        this.matrix = Collections.synchronizedMap(new HashMap<ODRegion, Map<ODRegion, Integer>>());
    }
    
    public RegionResultMatrix(RaptorResultMatrix stopMatrix, GTFSData gtfsData) {
        this.matrix = new HashMap<ODRegion, Map<ODRegion, Integer>>();
        calculateMatrix(stopMatrix, gtfsData);
    }
    
    public void calculateMatrix(RaptorResultMatrix stopMatrix, GTFSData gtfsData) {
        Collection<ODRegion> regions = gtfsData.getRegions();
    }
    
    public Map<ODRegion, Map<ODRegion, Integer>> getMatrix() {
        return matrix;
    }
    
    public void putRow(ODRegion region, Map<ODRegion, Integer> row) {
	    matrix.put(region, row);
	}
    
    public Map<ODRegion, Integer> getRow(ODRegion region) {
        return matrix.get(region);
    }
    
    public int getValue(ODRegion origin, ODRegion destination) {
        if (!matrix.containsKey(origin)) {
            return Integer.MAX_VALUE;
        }
        Map<ODRegion, Integer> row = matrix.get(origin);
        if (!row.containsKey(destination)) {
            return Integer.MAX_VALUE;
        }
        return row.get(destination);
    }
    
    public void updateValue(ODRegion origin, ODRegion destination, int value) {
        if (!matrix.containsKey(origin)) {
            matrix.put(origin, new HashMap<ODRegion, Integer>());
        }
        Map<ODRegion, Integer> row = matrix.get(origin);
        row.put(destination, value);
    }
    
//     	public void writeResultsToCSVFile(String fileName){
// 		String c = ",";
// 		System.out.println("Writing results...");
// 		BufferedWriter writer;
// 		try {
// 			writer = new BufferedWriter(new FileWriter(fileName));
// 			writer.write("ostop,dstop,mins");
// 			writer.newLine();
// 			
// 			List<String> originList = new ArrayList<String>(matrix.keySet());
// 			Collections.sort(originList);
// 			for (String originId : originList) {
// 				Map<String, RaptorResult> row = matrix.get(originId);
// 				List<String> destinationList = new ArrayList<String>(row.keySet());
// 				Collections.sort(destinationList);
// 				for (String destinationId : destinationList) {
// 					double activeMinutes = row.get(destinationId).activeTime / 60.0;
// 					writer.write(originId + c + destinationId + c + activeMinutes);
// 					writer.newLine();
// 				}
// 			}
// 			
// 			writer.close();
// 		} catch (Exception e) {
// 			System.err.println(e.getMessage());
// 		}
// 	}
}