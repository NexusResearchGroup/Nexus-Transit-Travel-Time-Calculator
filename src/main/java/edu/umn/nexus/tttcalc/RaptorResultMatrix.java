package edu.umn.nexus.tttcalc;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class RaptorResultMatrix {
	private Map<GTFSStop, Map<GTFSStop, RaptorResult>> matrix;
	
	public RaptorResultMatrix() {
		matrix = Collections.synchronizedMap(new HashMap<GTFSStop, Map<GTFSStop, RaptorResult>>());
	}
	
	public void putRow(GTFSStop stop, Map<GTFSStop, RaptorResult> row) {
	    matrix.put(stop, row);
	}
	
	public Map<GTFSStop, RaptorResult> getRow(GTFSStop stop) {
        return matrix.get(stop);
	}
	
	public RaptorResult getResult(GTFSStop origin, GTFSStop destination) {
		if (!matrix.containsKey(origin)) {
			return RaptorResult.EMPTY_RESULT();
		}
		
		Map<GTFSStop, RaptorResult> row = matrix.get(origin);
		
		if (!row.containsKey(destination)) {
			return RaptorResult.EMPTY_RESULT();
		}
		
		return row.get(destination);
	}
	
	public void updateResult(GTFSStop origin, GTFSStop destination, int arrivalTime, int activeTime) {
		synchronized (matrix) {
			if (!matrix.containsKey(origin)) {
				matrix.put(origin, new HashMap<GTFSStop, RaptorResult>());
			}
		}
		
		Map<GTFSStop, RaptorResult> row = matrix.get(origin);
		
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
		System.out.println("Writing results...");
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(fileName));
			writer.write("ostop,dstop,mins");
			writer.newLine();
			
			for (GTFSStop origin : matrix.keySet()) {
				Map<GTFSStop, RaptorResult> row = matrix.get(origin);
				for (GTFSStop destination : row.keySet()) {
					double activeMinutes = row.get(destination).activeTime / 60.0;
					writer.write(origin.getId() + "," + destination.getId() + "," + activeMinutes);
					writer.newLine();
				}
			}
			
			writer.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}