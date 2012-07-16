import java.io.*;
import java.util.*;

public class GTFSStopTime {
    public String tripId;
    public String stopId;
    public int time; // seconds past midnight
    
    public GTFSStopTime(String inputTripId, String inputStopId, int inputTime) {
        tripId = inputTripId;
        stopId = inputStopId;
        time = inputTime;
    }
    
    public GTFSStopTime(String inputTripId, String inputStopId, String inputTimeString) {
        tripId = inputTripId;
        stopId = inputStopId;
        time = GTFSData.gtfsTimeToSeconds(inputTimeString);
    }
}