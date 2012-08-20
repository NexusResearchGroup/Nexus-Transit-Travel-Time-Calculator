import java.io.*;
import java.util.*;
import java.util.concurrent.*;
public class RaptorMatrixCalculator {
	private GTFSData gtfsData;
	private RaptorResultMatrix stopMatrix;
	private RegionResultMatrix regionMatrix;

	public RaptorMatrixCalculator(String gtfsFileName, String pointsFileName, String regionsFileName, String serviceId) {
		this.gtfsData = new GTFSData(gtfsFileName, pointsFileName, regionsFileName, serviceId);
		stopMatrix = new RaptorResultMatrix();
		regionMatrix = new RegionResultMatrix();
	}

	public static void main (String[] args) {
		// Usage: GTFSData google_transit.zip points.csv SEP09-Multi-Weekday-01 25200 32400 2 900 results.csv
        String gtfsFileName = args[0];
        String pointsFileName = args[1];
        String regionsFileName = args[2];
        String serviceId = args[3];
        int startTime = Integer.parseInt(args[4]);
        int endTime = Integer.parseInt(args[5]);
        int maxTrips = Integer.parseInt(args[6]);
        int maxTransferTime = Integer.parseInt(args[7]);
        String outputFileName = args[8];
        
        RaptorMatrixCalculator r = new RaptorMatrixCalculator(gtfsFileName, pointsFileName, regionsFileName, serviceId);
        r.calculateStopMatrix(startTime, endTime, maxTrips, maxTransferTime);
        //r.calculateRegionMatrix();
	}
	
	public RaptorResultMatrix getStopMatrix() {
		return stopMatrix;
	}
	
	public RegionResultMatrix getRegionMatrix() {
	    return regionMatrix;
	}
	
	public void calculateRegionMatrix() {
	    Collection<ODRegion> regions = gtfsData.getRegions();
	    ExecutorService es = Executors.newFixedThreadPool(3);
	    
	    for (ODRegion region : regions) {
	        Future f = es.submit(new RegionRowCalculator(gtfsData, region, stopMatrix, regionMatrix);
	    }
	    es.shutdown();
	    
	    while (!es.isTerminated()) {
	        try {
	            Thread.sleep(1000);
	        } catch (Exception e) {
	            System.err.println(e.getMessage());
	        }	            
	    }
	}
		
	public void calculateStopMatrix(int startTime, int endTime, int maxTrips, int maxTransferTime) {
		int numStops = 100;
		int completedStops = 0;
		long totalCalcTime = 0;
		long startCalc;
		Random rng = new Random();
		ExecutorService es = Executors.newFixedThreadPool(3);
		
		startCalc = System.currentTimeMillis();
		while (completedStops < numStops) {
		    String randomStopId = ((Integer) rng.nextInt(60000)).toString();
		    if (gtfsData.getStopIds().contains(randomStopId)) {
		        Future f = es.submit(new RaptorRowCalculator(gtfsData, gtfsData.getStopForId(randomStopId), startTime, endTime, maxTrips, maxTransferTime, stopMatrix));
		        completedStops++;
		    }
		}
		es.shutdown();
		
		while (!es.isTerminated()) {
		    try {
		        Thread.sleep(1000);
		    } catch (Exception e) {
		        System.err.println(e.getMessage());
		    }
		}
        totalCalcTime = System.currentTimeMillis() - startCalc;
        System.out.println("Total calculation time for " + numStops + " stops: " + (totalCalcTime / 1000.0) + " seconds");
	}
}