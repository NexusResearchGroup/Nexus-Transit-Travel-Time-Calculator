import java.io.*;
import java.util.*;
import java.util.concurrent.*;
public class RaptorMatrixCalculator {
	private GTFSData gtfsData;
	private RaptorResultMatrix resultMatrix;

	public RaptorMatrixCalculator(String gtfsFileName, String pointsFileName, String serviceId) {
		this.gtfsData = new GTFSData(gtfsFileName, pointsFileName, serviceId);
		resultMatrix = new RaptorResultMatrix();
	}

	public static void main (String[] args) {
		// Usage: GTFSData google_transit.zip points.csv SEP09-Multi-Weekday-01 25200 32400 2 900 results.csv
        String gtfsFileName = args[0];
        String pointsFileName = args[1];
        String serviceId = args[2];
        int startTime = Integer.parseInt(args[3]);
        int endTime = Integer.parseInt(args[4]);
        int maxTrips = Integer.parseInt(args[5]);
        int maxTransferTime = Integer.parseInt(args[6]);
        String outputFileName = args[7];
        
        RaptorMatrixCalculator r = new RaptorMatrixCalculator(gtfsFileName, pointsFileName, serviceId);
        r.calculateResults(startTime, endTime, maxTrips, maxTransferTime);
        r.getResultMatrix().writeResultsToCSVFile(outputFileName);
	}
	
	public RaptorResultMatrix getResultMatrix() {
		return resultMatrix;
	}
	
	private void calculateResults(int startTime, int endTime, int maxTrips, int maxTransferTime) {
		int numStops = 100;
		int completedStops = 0;
		long totalCalcTime = 0;
		long startCalc;
		List<RaptorRowCalculator> rowCalculators = new ArrayList<RaptorRowCalculator>();
		Random rng = new Random();
		ExecutorService es = Executors.newFixedThreadPool(3);
		
		startCalc = System.currentTimeMillis();
		while (completedStops < numStops) {
		    String randomStopId = ((Integer) rng.nextInt(60000)).toString();
		    if (gtfsData.getStopIds().contains(randomStopId)) {
		        Future f = es.submit(new RaptorRowCalculator(gtfsData, gtfsData.getStopForId(randomStopId), startTime, endTime, maxTrips, maxTransferTime, resultMatrix));
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