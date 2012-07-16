import java.io.*;
import java.util.*;

public class GTFSTrip {
    public String id;
    public String routeId;
    public List<GTFSStopTime> stopTimes;
    
    public GTFSTrip(String inputId, String inputRouteId) {
        id = inputId;
        routeId = inputRouteId;
        stopTimes = new ArrayList<GTFSStopTime>();
    }
    
    public List<GTFSStopTime> stopsTimesAfterTime(int time) {
        GTFSStopTime lastStopTime;
        // check the last stoptime to see if we can ignore the whole trip
        lastStopTime = stopTimes.get(stopTimes.size() - 1);
        if (lastStopTime.time < time) {
            return Collections.<GTFSStopTime>emptyList();
        }
    
        for (GTFSStopTime curStopTime : stopTimes) {
            if (curStopTime.time >= time) {
                return stopTimes.subList(stopTimes.indexOf(curStopTime), stopTimes.size());
            }
        }
        
        return Collections.<GTFSStopTime>emptyList();
    }
    
    public int firstStopTime() {
        
    }
    
    public int lastStopTime() {
    }
    
}