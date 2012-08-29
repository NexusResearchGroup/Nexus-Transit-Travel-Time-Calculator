package edu.umn.nexus.tttcalc;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import edu.umn.nexus.tttcalc.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class RaptorMatrixCalculator {
	private GTFSData gtfsData;
	private RaptorResultMatrix stopMatrix;
	private RegionResultMatrix regionMatrix;
	
	// Command-line arguments handled by args4j
	@Option(name="-g",usage="GTFS file")
	private String gtfsFileName;
	
	@Option(name="-p",usage="Points file")
	private String pointsFileName;
	
	@Option(name="-r",usage="Regions file")
	private String regionsFileName;
	
	@Option(name="-o",usage="Output file (will be overwritten). Default is results.csv")
	private String outputFileName = "results.csv";
	
	@Option(name="-id",usage="Service ID in GTFS file")
	private String serviceId;
	
	@Option(name="-s",usage="Start time of day, in seconds past midnight (e.g. 7AM = 25200)")
	private int startTime;
	
	@Option(name="-e",usage="End time of day, in seconds past midnight (e.g. 9AM = 32400)")
	private int endTime;
	
	@Option(name="-b",usage="Maximum number of boardings to allow")
	private int maxTrips = 2;
	
	@Option(name="-w",usage="Maximum allowable time to wait for transfers, in seconds")
	private int maxTransferTime = 900;
	
	@Option(name="-mp",usage="Maximum number of threads. Default is 1; increasing this will significantly speed up processing on multi-core computers")
	private int maxThreads = 1;

	public RaptorMatrixCalculator(String gtfsFileName, String pointsFileName, String regionsFileName, String serviceId) {
		this.gtfsData = new GTFSData(gtfsFileName, pointsFileName, regionsFileName, serviceId);
		stopMatrix = new RaptorResultMatrix();
		regionMatrix = new RegionResultMatrix();
	}
	
	public RaptorMatrixCalculator() {
	    this.stopMatrix = new RaptorResultMatrix();
	    this.regionMatrix = new RegionResultMatrix();
	}

	public static void main (String[] args) {
	    new RaptorMatrixCalculator().start(args);
	
		// Usage: GTFSData google_transit.zip points.csv SEP09-Multi-Weekday-01 25200 32400 2 900 results.csv
//         String gtfsFileName = args[0];
//         String pointsFileName = args[1];
//         String regionsFileName = args[2];
//         String serviceId = args[3];
//         int startTime = Integer.parseInt(args[4]);
//         int endTime = Integer.parseInt(args[5]);
//         int maxTrips = Integer.parseInt(args[6]);
//         int maxTransferTime = Integer.parseInt(args[7]);
//         String outputFileName = args[8];
//         
//         RaptorMatrixCalculator r = new RaptorMatrixCalculator(gtfsFileName, pointsFileName, regionsFileName, serviceId);
//         r.calculateStopMatrix(startTime, endTime, maxTrips, maxTransferTime);
//         r.calculateRegionMatrixToFile(outputFileName);
	}
	
	public void start(String[] args) {
	    CmdLineParser parser = new CmdLineParser(this);
	    
	    if (args.length == 0) {
	        System.err.println("No options specified.");
	        parser.printUsage(System.err);
	        System.err.println();
	        System.err.println("Example: java -jar NexusTTTCalc -g gtfs_file.zip -p points.csv -r regions.csv -id SEP09-Multi-Weekday-01 -s 25200 -e 32400 -w 900 -b 2 -o results.csv");
	        return;
	    }
	    
	    try {
	        parser.parseArgument(args);
	    } catch (CmdLineException e) {
	        System.err.println(e.getMessage());
	        parser.printUsage(System.err);
	        System.err.println();
	        System.err.println();
	        System.err.println("Example: java -jar NexusTTTCalc -g gtfs_file.zip -p points.csv -r regions.csv -id SEP09-Multi-Weekday-01 -s 25200 -e 32400 -w 900 -b 2 -o results.csv");
	        return;
	    }
	    	    
	    gtfsData = new GTFSData(gtfsFileName, pointsFileName, regionsFileName, serviceId);
        calculateStopMatrix(startTime, endTime, maxTrips, maxTransferTime, maxThreads);
	    calculateRegionMatrixToFile(outputFileName, 1);
	    
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
	
	public void calculateRegionMatrixToFile(String fileName, int maxThreads) {
		System.out.println("Calculating region travel time matrix...");
		BufferedWriter writer = null;
		long startCalc;
		long totalCalcTime;
		int currentPercent = 0;
		int lastPercent = 0;
		ExecutorService es = Executors.newFixedThreadPool(maxThreads);
		JobCounter jobCounter = new JobCounter(gtfsData.getRegions().size());
		
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
			Future f = es.submit(new RegionRowCalculator(gtfsData, region, stopMatrix, regionMatrix, writer, jobCounter));		
		}
		es.shutdown();
	    
	    while (!es.isTerminated()) {
	        try {
	            Thread.sleep(10000);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        currentPercent = jobCounter.getPercentOfMax();
		    if (currentPercent >= (lastPercent + 10)) {
		    	lastPercent = currentPercent;
		    	System.out.print("  " + currentPercent + "%");
		    	System.out.flush();
		    }
	    }
	    totalCalcTime = System.currentTimeMillis() - startCalc;
	    System.out.println();
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
	    JobCounter jobCounter = new JobCounter(regions.size());
	    ExecutorService es = Executors.newFixedThreadPool(1);
	    
	    for (ODRegion region : regions) {
			RegionRowCalculator rrc = new RegionRowCalculator(gtfsData, region, stopMatrix, regionMatrix, writer, jobCounter);
			rrc.run();
		}
	}
	
	public void calculateStopMatrix(int startTime, int endTime, int maxTrips, int maxTransferTime, int maxThreads) {
		System.out.println("Calculating stop travel time matrix...");
		int numStops = 100;
		int completedStops = 0;
		long totalCalcTime = 0;
		long startCalc;
		Random rng = new Random();
		ExecutorService es = Executors.newFixedThreadPool(maxThreads);
		JobCounter jobCounter = new JobCounter(gtfsData.getStops().size());
		int currentPercent = 0;
		int lastPercent = 0;
		
		
		startCalc = System.currentTimeMillis();
		for (GTFSStop stop : gtfsData.getStops()) {
		    Future f = es.submit(new RaptorRowCalculator(gtfsData, stop, startTime, endTime, maxTrips, maxTransferTime, stopMatrix, jobCounter));
		}
		es.shutdown();
		
		while (!es.isTerminated()) {
		    try {
		        Thread.sleep(10000);
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		    
		    currentPercent = jobCounter.getPercentOfMax();
		    if (currentPercent >= (lastPercent + 10)) {
		    	lastPercent = currentPercent;
		    	System.out.print("  " + currentPercent + "%");
		    	System.out.flush();
		    }
		}
        totalCalcTime = System.currentTimeMillis() - startCalc;
        System.out.println();
        System.out.println("  Total calculation time for " + gtfsData.getStops().size() + " stops: " + (totalCalcTime / 1000.0) + " seconds");
	}
}