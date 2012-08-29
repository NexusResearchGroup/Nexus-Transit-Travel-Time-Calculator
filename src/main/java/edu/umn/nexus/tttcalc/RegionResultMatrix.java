package edu.umn.nexus.tttcalc;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.text.DecimalFormat;

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
    
    public void writeResultsToCSVFile(String fileName, GTFSData gtfsData) {
		String c = ",";
		String q = "\"";
		String originId;
		String destinationId;
		System.out.println("Writing results to " + fileName);
		BufferedWriter writer;
		DecimalFormat formatter = new DecimalFormat();
		formatter.setMaximumFractionDigits(2);
		formatter.setGroupingUsed(false);
		String formattedValue;
		
		try {
			writer = new BufferedWriter(new FileWriter(fileName));
			//System.out.println("Writing headers...");
			writer.write("\"otaz\",\"dtaz\",\"mins\"");
			writer.newLine();
			
			List<ODRegion> regionList = new ArrayList<ODRegion>(gtfsData.getRegions());
			Collections.sort(regionList);
			//System.out.println("Writing table...");
			for (ODRegion origin : regionList) {
				//System.out.println("Writing result for origin " + origin.getId());
			    originId = origin.getId();
				for (ODRegion destination : regionList) {
					//System.out.println("Writing result for destination " + destination.getId());
				    destinationId = destination.getId();
					formattedValue = formatter.format(getValue(origin, destination) / 60.0);
					writer.write(q + originId + q + c + q + destinationId + q + c + formattedValue);
					writer.newLine();
				}
			}
			
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}