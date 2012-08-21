import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class RegionRowCalculator implements Runnable {
    private final GTFSData gtfsData;
    private final ODRegion originRegion;
    private final RegionResultMatrix resultMatrix;
    private final RaptorResultMatrix stopMatrix;
    private Map<ODRegion, Integer> row;

    public RegionRowCalculator(GTFSData gtfsData, ODRegion originRegion, RaptorResultMatrix stopMatrix, RegionResultMatrix resultMatrix) {
        this.gtfsData = gtfsData;
        this.originRegion = originRegion;
        this.resultMatrix = resultMatrix;
        this.stopMatrix = stopMatrix;
        this.row = new HashMap<ODRegion, Integer>();
    }
    
    public void run() {
        calculateResults();
        System.out.println("Back from calculating results");
        resultMatrix.putRow(originRegion, row);
    }
    
    private void calculateResults() {
        System.out.println("Calculating results for region " + originRegion.getId());
        Collection<ODRegion> dRegions = gtfsData.getRegions();
        Collection<ODPoint> oPoints = originRegion.getPoints();
        Collection<ODPoint> dPoints;
        long totalTime;
        long numPairs;
        int averageTime;
        int currentTime;
        
        System.out.println("I see " + dRegions.size() + " destination regions");
        for (ODRegion dRegion : dRegions) {
            System.out.println("  Calculating time to region " + dRegion.getId());
            dPoints = dRegion.getPoints();
            totalTime = 0;
            numPairs = 0;
            
            for (ODPoint oPoint : oPoints) {
                //System.out.println("    Using origin point " + oPoint.getId());
                for (ODPoint dPoint : dPoints) {
                    //System.out.println("      and destination point " + dPoint.getId());
                    numPairs++;
                    totalTime += minimumTimeBetweenPoints(oPoint, dPoint);
                }
            }
            
            if (numPairs == 0) {
                System.out.println("    No point pairs connect to region " + dRegion.getId());
                averageTime = Integer.MAX_VALUE;
            } else {
                averageTime = (int)Math.round(totalTime / numPairs);
                System.out.println( "    Average time is " + averageTime + " seconds");
            }

            currentTime = getResult(dRegion);

            if (averageTime < currentTime) {
                putResult(dRegion, averageTime);
            }
        }
    }
    
    private void calculateWalkingTimes() {
        for (ODRegion dRegion : gtfsData.getRegions()) {
            double distance = Haversine.distanceBetween(originRegion.getLocation(), dRegion.getLocation()) * GTFSData.circuityAdjustment;
            int walkTime = (int)Math.round(distance / GTFSData.walkSpeed * 3600);
            row.put(dRegion, walkTime);
        }
    }
    
    private int minimumTimeBetweenPoints(ODPoint oPoint, ODPoint dPoint) {
        int minimumTime = Integer.MAX_VALUE;
        int accessTime;
        int egressTime;
        int totalTime;
        int transitTime;
        Collection<GTFSStop> oStops = oPoint.getStops();
        Collection<GTFSStop> dStops = dPoint.getStops();
        
        for (GTFSStop oStop : oStops) {
            //System.out.println("        using origin stop " + oStop.getId());
            accessTime = oStop.getAccessTimeForPoint(oPoint);
            
            for (GTFSStop dStop : dStops) {
                //System.out.println("          and destination stop " + dStop.getId());
                egressTime = dStop.getAccessTimeForPoint(dPoint);
                transitTime = stopMatrix.getResult(oStop.getId(), dStop.getId()).activeTime;
                if (transitTime == Integer.MAX_VALUE) {
                    totalTime = transitTime;
                } else {
                    totalTime = accessTime + egressTime + transitTime;
                }
                if (totalTime < minimumTime) {
                    minimumTime = totalTime;
                }
            }
        }
        //System.out.println("      Best time is " + minimumTime + " seconds");
        return minimumTime;
    }
    
    private int getResult(ODRegion region) {
        if (!row.containsKey(region)) {
            row.put(region, Integer.MAX_VALUE);
        }
        return row.get(region);
    }
    
    private void putResult(ODRegion region, int result) {
        row.put(region, result);
    }
    
}