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
        resultMatrix.putRow(originStopId, row);
    }
    
    private void calculateResults() {
        Collection<ODPoint> oPoints = originRegion.getPoints();
        Collection<ODPoint> dPoints;
        int totalTime;
        int numPairs;
        int averageTime;
        
        for (ODRegion dRegion : gtfsData.getRegions()) {
            dPoints = dRegion.getPoints();
            totalTime = 0;
            numPairs = 0;
            
            for (ODPoint oPoint : oPoints) {
                for (ODPoint dPoint : dPoints) {
                    numPairs++;
                    totalTime += minimumTimeBetweenPoints(oPoint, dPoint);
                }
            }
            
            averageTime = (int)Math.round(totalTime / numPairs);
            currentTime = row.get(dRegion);
            if (averageTime < currentTime) {
                row.put(dRegion, avergeTime);
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
    
    private int minimumTimeBetweenPoints(ODPoint oPoint, ODPoint dPoint, RaptorResltsMatrix stopMatrix) {
        int minimumTime = Integer.MAX_VALUE;
        int accessTime;
        int egressTime;
        int totalTime;
        Collection<GTFSStop> oStops = oPoint.getStops();
        Collection<GTFSStop> dStops = dPoint.getStops();
        
        for (GTFSStop oStop : oStops) {
            entryTime = oStop.getAccessTimeForPoint(oPoint);
            
            for (GTFSStop dStop : dStops) {
                egressTime = dStop.getAccessTimeForPoint(dPoint);
                totalTime = entryTime + egressTime + stopMatrix.getResult(oStop.getId(), dStop.get()).activeTime();
                if (totalTime < minimumTime) {
                    minimumTime = totalTime;
                }
            }
        }
    }
    
}