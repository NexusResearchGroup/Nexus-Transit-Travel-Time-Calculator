import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class RaptorResultMatrix {
	private Map<String, Map<String, RaptorResult>> matrix;
	
	public RaptorResultMatrix() {
		matrix = Collections.synchronizedMap(new HashMap<String, Map<String, RaptorResult>>());
	}
	
	public void putRow(String stopId, Map<String, RaptorResult> row) {
	    matrix.put(stopId, row);
	}
	
	public Map<String, RaptorResult> getRow(String stopId) {
        return matrix.get(stopId);
	}
	
	public RaptorResult getResult(String origin, String destination) {
		if (!matrix.containsKey(origin)) {
			return RaptorResult.EMPTY_RESULT();
		}
		
		Map<String, RaptorResult> row = matrix.get(origin);
		
		if (!row.containsKey(destination)) {
			return RaptorResult.EMPTY_RESULT();
		}
		
		return row.get(destination);
	}
	
	public void updateResult(String origin, String destination, int arrivalTime, int activeTime) {
		synchronized (matrix) {
			if (!matrix.containsKey(origin)) {
				matrix.put(origin, new HashMap<String, RaptorResult>());
			}
		}
		
		Map<String, RaptorResult> row = matrix.get(origin);
		
		synchronized (row) {
			if (!row.containsKey(destination)) {
				row.put(destination, new RaptorResult());
			}
		}
		
		RaptorResult result = row.get(destination);
		result.arrivalTime = arrivalTime;
		result.activeTime = activeTime;
	}
	
	public void writeResultsToCSVFile(String fileName, GTFSData gtfsData){
		String c = ",";
		System.out.println("Writing results...");
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(fileName));
			writer.write("ostop,dstop,mins");
			writer.newLine();
			
			List<String> originList = new ArrayList<String>(matrix.keySet());
			Collections.sort(originList);
			for (String originId : originList) {
				Map<String, RaptorResult> row = matrix.get(originId);
				List<String> destinationList = new ArrayList<String>(row.keySet());
				Collections.sort(destinationList);
				for (String destinationId : destinationList) {
					double activeMinutes = row.get(destinationId).activeTime / 60.0;
					writer.write(originId + c + destinationId + c + activeMinutes);
					writer.newLine();
				}
			}
			
			writer.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}