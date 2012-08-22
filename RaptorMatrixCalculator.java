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
        r.calculateRegionMatrixToFile(outputFileName);
	}
	
	public RaptorResultMatrix getStopMatrix() {
		return stopMatrix;
	}
	
	public RegionResultMatrix getRegionMatrix() {
	    return regionMatrix;
	}
	
	public void writeStopMatrixToFile(String fileName) {
		stopMatrix.writeResultsToCSVFile(fileName, gtfsData);
	}
	
	public void writeRegionMatrixToFile(String fileName) {
		regionMatrix.writeResultsToCSVFile(fileName, gtfsData);
	}
	
	public void calculateRegionMatrixToFile(String fileName) {
		System.out.println("Calculating region travel time matrix...");
		BufferedWriter writer = null;
		long startCalc;
		long totalCalcTime;
		ExecutorService es = Executors.newFixedThreadPool(1);
		
		try {
			writer = new BufferedWriter(new FileWriter(fileName));
			//System.out.println("Writing headers...");
			writer.write("\"otaz\",\"dtaz\",\"mins\"");
			writer.newLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		List<ODRegion> regions = new ArrayList<ODRegion>(gtfsData.getRegions());
		Collections.sort(regions);
		startCalc = System.currentTimeMillis();
		for (ODRegion region : regions) {
			Future f = es.submit(new RegionRowCalculator(gtfsData, region, stopMatrix, regionMatrix, writer));		
		}
		es.shutdown();
	    
	    while (!es.isTerminated()) {
	        try {
	            Thread.sleep(10000);
	        } catch (Exception e) {
	            e.printStackTrace();;
	        }	            
	    }
	    totalCalcTime = System.currentTimeMillis() - startCalc;
        System.out.println("  Total calculation time for " + regions.size() + " regions: " + (totalCalcTime / 1000.0) + " seconds");
			
		try {
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void calculateRegionMatrix() {
		long startCalc;
		long totalCalcTime;
		BufferedWriter writer = null;
	    Collection<ODRegion> regions = gtfsData.getRegions();
	    //Collection<ODRegion> regions = new HashSet<ODRegion>();
	    //regions.add(gtfsData.getRegionForId("100"));
	    ExecutorService es = Executors.newFixedThreadPool(1);
	    
	    for (ODRegion region : regions) {
			RegionRowCalculator rrc = new RegionRowCalculator(gtfsData, region, stopMatrix, regionMatrix, writer);
			rrc.run();
		}
	    
// 	    startCalc = System.currentTimeMillis();
// 	    for (ODRegion region : regions) {
// 	        Future f = es.submit(new RegionRowCalculator(gtfsData, region, stopMatrix, regionMatrix));
// 	    }
// 	    es.shutdown();
// 	    
// 	    while (!es.isTerminated()) {
// 	        try {
// 	            Thread.sleep(10000);
// 	        } catch (Exception e) {
// 	            e.printStackTrace();;
// 	        }	            
// 	    }
// 	    totalCalcTime = System.currentTimeMillis() - startCalc;
//         System.out.println("Total calculation time for " + regions.size() + " regions: " + (totalCalcTime / 1000.0) + " seconds");
	}
	
	public void calculateStopMatrix(int startTime, int endTime, int maxTrips, int maxTransferTime) {
		System.out.println("Calculating stop travel time matrix...");
		int numStops = 100;
		int completedStops = 0;
		long totalCalcTime = 0;
		long startCalc;
		Random rng = new Random();
		ExecutorService es = Executors.newFixedThreadPool(4);
		
		startCalc = System.currentTimeMillis();
		for (GTFSStop stop : gtfsData.getStops()) {
		    //String randomStopId = ((Integer) rng.nextInt(60000)).toString();
		    Future f = es.submit(new RaptorRowCalculator(gtfsData, stop, startTime, endTime, maxTrips, maxTransferTime, stopMatrix));
		}
		es.shutdown();
		
		while (!es.isTerminated()) {
		    try {
		        Thread.sleep(10000);
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}
        totalCalcTime = System.currentTimeMillis() - startCalc;
        System.out.println("  Total calculation time for " + gtfsData.getStops().size() + " stops: " + (totalCalcTime / 1000.0) + " seconds");
	}
}