import java.io.*;
import java.util.*;

public class TTTMatrixCalculator {
    private GTFSData gtfsData;
    private int startTime;
    private int endTime;
    private Set<String> selectedRouteIds;
    private Map<String, Map<String, Integer>> resultMatrix;
    private static final double walkSpeed = 5000.0; // m/h
    private static final double circuityAdjustment = 1.2;
    private static final int maxTransferTime = 900;
    
    public static void writeMatrixToFile(Map<String, Map<String, Integer>> matrix, String fileName) {
        System.out.println("Writing results to file...");
        Set<String> pointIds = new TreeSet<String>(matrix.keySet());
        BufferedWriter writer;
        double mins;
             
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write("otaz,dtaz,mins");
            writer.newLine();
            
            for (String opointId : pointIds) {
                for (String dpointId : pointIds) {
                    mins = (double)matrix.get(opointId).get(dpointId) / 60;
                    writer.write(opointId + "," + dpointId + "," + mins);
                    writer.newLine();
                }
            }
            writer.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void main (String[] args) {
        // Usage: TTTMatrixCalculator google_transit.zip points.csv MAY12-Multi-Weekday-01 25200 32400 "2,3,53,114" results.csv
        String gtfsFileName = args[0];
        String pointsFileName = args[1];
        String serviceId = args[2];
        int startTime = Integer.parseInt(args[3]);
        int endTime = Integer.parseInt(args[4]);
        Set<String> selectedRoutes = new HashSet<String>(Arrays.asList(args[5].split(",")));
        String outputFileName = args[6];
        
        TTTMatrixCalculator c = new TTTMatrixCalculator(gtfsFileName, pointsFileName, serviceId, startTime, endTime, selectedRoutes);
        Map<String, Map<String, Integer>> matrix = c.calculateMatrix();
        TTTMatrixCalculator.writeMatrixToFile(matrix, outputFileName);
    }
    
    public TTTMatrixCalculator(String gtfsFileName, String pointsFileName, String serviceId, int inputStartTime, int inputEndTime, Set<String> inputSelectedRouteIds) {
        gtfsData = new GTFSData(gtfsFileName, pointsFileName, serviceId);
        startTime = inputStartTime;
        endTime = inputEndTime;
        if (inputSelectedRouteIds.isEmpty()) {
            selectedRouteIds = gtfsData.getRouteIds();
        } else {
            selectedRouteIds = inputSelectedRouteIds;
        }
    }
    
    private Map<String, Map<String, Integer>> baseMatrix() {
        System.out.println("Calculating base walking matrix...");
        Collection<ODPoint> points = gtfsData.getPoints();
        Map<String, Map<String, Integer>> matrix = new HashMap<String, Map<String, Integer>>();
        Map<String, Integer> rowMap;
        for (ODPoint opoint : points) {
            rowMap = new HashMap<String, Integer>();
            for (ODPoint dpoint : points) {
                rowMap.put(dpoint.id, (int)Math.round((Haversine.distanceBetween(opoint.location, dpoint.location) * circuityAdjustment) / walkSpeed * 3600));
            }
            matrix.put(opoint.id, rowMap);
        }
        return matrix;
    }
    
    public Map<String, Map<String, Integer>> calculateMatrix() {
        resultMatrix = baseMatrix();
        
        for (GTFSStop ostop : gtfsData.getStops()) {
            SortedMap<Integer, Set<GTFSTrip>> ostopTripTimes = ostop.tripsBetween(startTime, endTime);
            
            for (int departureTime1 : ostopTripTimes.keySet()) {

                for (GTFSTrip trip1 : ostopTripTimes.get(departureTime1)) {
                    if (selectedRouteIds.contains(gtfsData.routeWithId(trip1.routeId).name)) {
                        //System.out.println("Stop " +ostop.id+ ",  Route " + trip1.routeId + ", trip " + trip1.id);
                        SortedMap<Integer, Set<GTFSStop>> destinationTimes1 = trip1.stopTimesAfter(departureTime1);
                        
                        for (int arrivalTime1 : destinationTimes1.keySet()) {
                            
                            for (GTFSStop stop1 : destinationTimes1.get(arrivalTime1)) {
                                connectPointsFromStops(ostop, stop1, arrivalTime1 - departureTime1);
                            
                                for (GTFSStop tstop : stop1.transferStops) {
                                    int transferAccessTime = (int)Math.round(Haversine.secondsBetween(tstop.location, stop1.location, walkSpeed) * circuityAdjustment);
                                    System.out.println("Transfer time to stop " +tstop.id+ ": " + transferAccessTime);
                                    SortedMap<Integer, Set<GTFSTrip>> tstopTripTimes = tstop.tripsBetween(arrivalTime1 + transferAccessTime, arrivalTime1 + maxTransferTime);
                                    
                                    for (int departureTime2 : tstopTripTimes.keySet()) {
                                        
                                        for (GTFSTrip trip2 : tstopTripTimes.get(departureTime2)) {
                                            SortedMap<Integer, Set<GTFSStop>> destinationTimes2 = trip2.stopTimesAfter(departureTime2);
                                            
                                            for (int arrivalTime2 : destinationTimes2.keySet()) {
                                            
                                                for (GTFSStop stop2 : destinationTimes2.get(arrivalTime2)) {
                                                    connectPointsFromStops(ostop, stop2, arrivalTime2 - departureTime1);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return resultMatrix;
    }
    
    private void connectPointsFromStops(GTFSStop ostop, GTFSStop dstop, int elapsedTime) {
        System.out.println("*** Reached stop " + dstop.id + " from stop " + ostop.id + " in " + elapsedTime + " seconds");
        for (String opointId : ostop.getPointIds()) {
            for (String dpointId : dstop.getPointIds()) {
                int originalTime = resultMatrix.get(opointId).get(dpointId);
                int newTime = elapsedTime + ostop.pointAccessTimes.get(opointId) + dstop.pointAccessTimes.get(dpointId);
                if (newTime < originalTime) {
                    resultMatrix.get(opointId).put(dpointId, newTime);
                }
            }
        }
    }

}