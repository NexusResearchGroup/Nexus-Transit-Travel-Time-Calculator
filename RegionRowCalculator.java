import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.text.DecimalFormat;

public class RegionRowCalculator implements Runnable {
    private final GTFSData gtfsData;
    private ODRegion originRegion;
    private final RegionResultMatrix resultMatrix;
    private final RaptorResultMatrix stopMatrix;
    private Map<ODRegion, Integer> row;
    private DecimalFormat formatter;
    private Collection<ODRegion> dRegions;
    private Collection<ODPoint> oPoints;
    private BufferedWriter writer;

    public RegionRowCalculator(GTFSData gtfsData, ODRegion originRegion, RaptorResultMatrix stopMatrix, RegionResultMatrix resultMatrix, BufferedWriter writer) {
        this.gtfsData = gtfsData;
        this.originRegion = originRegion;
        this.oPoints = originRegion.getPoints();
        this.row = new HashMap<ODRegion, Integer>();
        this.resultMatrix = resultMatrix;
        this.stopMatrix = stopMatrix;
        this.writer = writer;
        this.formatter = new DecimalFormat();
        formatter.setMaximumFractionDigits(2);
        formatter.setGroupingUsed(false);
        this.dRegions = gtfsData.getRegions();
    }
    
    public void run() {
        calculateResultsToFile();
        //resultMatrix.putRow(originRegion, row);
    }
    
    public void calculateResultsToFile() {
		String formattedValue;
    	//System.out.println("Calculating results for region " + originRegion.getId());
        Collection<ODPoint> dPoints;
        long totalTime;
        long numPairs;
        int averageTime;
                
        //System.out.println("I see " + dRegions.size() + " destination regions");
        for (ODRegion dRegion : dRegions) {
            //System.out.println("  Calculating time to region " + dRegion.getId());
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
                //System.out.println( "    Average time is " + averageTime + " seconds");
            }
			
			formattedValue = formatter.format(averageTime / 60.0);
			synchronized (writer) {
				try {
					writer.write("\"" + originRegion.getId() + "\"" + "," + "\"" + dRegion.getId() + "\"" + "," + formattedValue);
					writer.newLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
        }
        
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
        
        calculateWalkingTimes();
        
        //System.out.println("I see " + dRegions.size() + " destination regions");
        for (ODRegion dRegion : dRegions) {
            //System.out.println("  Calculating time to region " + dRegion.getId());
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
                //System.out.println( "    Average time is " + averageTime + " seconds");
            }

			// does this improve on the walking time?
			if (averageTime < getResult(dRegion)) {
				putResult(dRegion, averageTime);
			}
        }
    }
    
    private void calculateWalkingTimes() {
        for (ODRegion dRegion : gtfsData.getRegions()) {
            int walkTime = walkingTimeBetweenRegions(originRegion, dRegion);
            putResult(dRegion, walkTime);
        }
    }
    
    private int minimumTimeBetweenPoints(ODPoint oPoint, ODPoint dPoint) {
    	int minimumTime = Haversine.secondsBetween(oPoint.getLocation(), dPoint.getLocation(), GTFSData.walkSpeed) * GTFSData.circuityAdjustment;
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
                transitTime = stopMatrix.getResult(oStop, dStop).activeTime;
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
    
    private int walkingTimeBetweenRegions(ODRegion oRegion, ODRegion dRegion) {
        double distance = Haversine.distanceBetween(oRegion.getLocation(), dRegion.getLocation()) * GTFSData.circuityAdjustment;
        return (int) Math.round(distance / GTFSData.walkSpeed * 3600);
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