import java.io.*;
import java.util.*;

public class RaptorMatrixCalculator {
	private GTFSData gtfsData;
	private RaptorResultMatrix resultMatrix;

	public RaptorMatrixCalculator(String gtfsFileName, String pointsFileName, String serviceId) {
		this.gtfsData = new GTFSData(gtfsFileName, pointsFileName, serviceId);
		resultMatrix = new RaptorResultMatrix();
	}

	public static void main (String[] args) {
		// Usage: GTFSData google_transit.zip points.csv MAR12-Multi-Weekday-01 25200 32400 2 900 results.csv
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
		//for (GTFSStop stop : gtfsData.getStops()) {
		//	calculateResultsForStop(stop, startTime, endTime, maxTrips, maxTransferTime);
		//}
		
		Set<String> stopIds = gtfsData.getStopIds();
		int randomStopId = Random.nextInt(60000);
		
		calculateResultsForStop(gtfsData.getStopForId("1318"), startTime, endTime, maxTrips, maxTransferTime);
		
	}
	
	private void processTripFromStop(GTFSTrip trip, GTFSStop currentStop, GTFSStop originStop, Set<GTFSStop> markedStops) {
		//System.out.println(">> Route " + trip.getRoute().getId());

		GTFSStop futureStop;
		String currentStopId = currentStop.getId();
		String originStopId = originStop.getId();
		RaptorResult currentBest = resultMatrix.getResult(originStopId, currentStopId);
		RaptorResult futureBest;
		int futureArrivalTime;
		int futureActiveTime;
		int departureTime = trip.stopTimeAtStop(currentStop).getTime();
		
		for (GTFSStopTime futureStopTime : trip.stopTimesAfter(departureTime)) {
			futureStop = futureStopTime.getStop();
			futureBest = resultMatrix.getResult(originStopId, futureStop.getId());
			futureArrivalTime = futureStopTime.getTime();
			futureActiveTime = currentBest.activeTime + (futureArrivalTime - departureTime);
			
			if (futureActiveTime < futureBest.activeTime) {
				//System.out.println(">>> Stop " + futureStop.getId() + ": improved time from " + futureBest.activeTime + " to " + futureActiveTime + " by trip " + trip.getId() + " from stop " + currentStop.getId());
				futureBest.arrivalTime = futureArrivalTime;
				futureBest.activeTime = futureActiveTime;
				markedStops.add(futureStop);
			}
		}
	}
	
	private void processTransfersFromStop(GTFSStop currentStop, GTFSStop originStop, Set<GTFSStop> markedStops) {
		String currentStopId = currentStop.getId();
		String originStopId = originStop.getId();
		RaptorResult currentBest = resultMatrix.getResult(originStopId, currentStopId);
		RaptorResult transferBest;
		int transferArrivalTime;
		int transferActiveTime;
		int transferAccessTime;
		
		for (GTFSStop transferStop : currentStop.getTransferStops()) {
			transferBest = resultMatrix.getResult(originStopId, transferStop.getId());
			transferAccessTime = currentStop.getAccessTimeForTransferStop(transferStop);
			transferArrivalTime = currentBest.arrivalTime + transferAccessTime;
			transferActiveTime = currentBest.activeTime + transferAccessTime;
			
			if (transferActiveTime < transferBest.activeTime) {
				//System.out.println(">>> Stop " + transferStop.getId() + ": improved time from " + transferBest.activeTime + " to " + transferActiveTime + " by walking from stop " + currentStop.getId());
				transferBest.arrivalTime = transferArrivalTime;
				transferBest.activeTime = transferActiveTime;
				
				markedStops.add(transferStop);
			}
		}
	}
	
	private void calculateResultsForStop(GTFSStop originStop, int startTime, int endTime, int maxTrips, int maxTransferTime) {
		//System.out.println("Stop " + originStop.getId());
		HashSet<GTFSStop> nextRoundMarkedStops = new HashSet<GTFSStop>();
		nextRoundMarkedStops.add(originStop);
		HashSet<GTFSStop> thisRoundMarkedStops;
		String originStopId = originStop.getId();
		resultMatrix.updateResult(originStopId, originStopId, startTime, 0);
		RaptorResult currentBest;
		int lastAllowedBoardingTime;
		
		// for the origin stop, there is no time limit on which trips can be taken
		for (int k=1; k<=maxTrips; k++) {
			//System.out.println("> Trip number " + k);
			thisRoundMarkedStops = new HashSet<GTFSStop>(nextRoundMarkedStops);
			nextRoundMarkedStops = new HashSet<GTFSStop>();
			
			for (GTFSStop currentStop : thisRoundMarkedStops) {
				
				currentBest = resultMatrix.getResult(originStopId, currentStop.getId());
				
				if (k == 1) {
					lastAllowedBoardingTime = endTime;
				} else {
					lastAllowedBoardingTime = currentBest.arrivalTime + maxTransferTime;
				}
				
				// process trips available from the current stop
				for (GTFSStopTime nextStopTime : currentStop.stopTimesBetween(currentBest.arrivalTime, lastAllowedBoardingTime)) {
					processTripFromStop(nextStopTime.getTrip(), currentStop, originStop, nextRoundMarkedStops);
				}	
				
				// process transfers available from the current stop
				HashSet<GTFSStop> possibleTransferStops = new HashSet<GTFSStop>(nextRoundMarkedStops);
				for (GTFSStop possibleTransferStop : possibleTransferStops) {
					processTransfersFromStop(possibleTransferStop, originStop, nextRoundMarkedStops);
				}
			}
		}
	}
}