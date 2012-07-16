import java.io.*;
import java.util.*;

public class TTTMatrixCalculator {
    private GTFSData gtfsData;
    
    private int startTime;
    private int endTime;

    public static void main (String[] args) {
    }
    
    public TTTMatrixCalculator(GTFSData inputGTFSData, String serviceID, int inputStartTime, int inputEndTime) {
        gtfsData = inputGTFSData;
        startTime = inputStartTime;
        endTime = inputEndTime;
    }

}