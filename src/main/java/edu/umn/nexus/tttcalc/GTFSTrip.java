package edu.umn.nexus.tttcalc;

import java.io.*;
import java.util.*;

public class GTFSTrip {
    private String id;
    private GTFSRoute route;
    private SortedSet<GTFSStopTime> stopTimes;
    private Map<GTFSStop, GTFSStopTime> timeStops;
    private GTFSStopTime queryStopTime1;
    private GTFSStopTime queryStopTime2;
    
    public GTFSTrip(String id, GTFSRoute route) {
        this.id = id;
        this.route = route;
        //System.out.println("Trip " +id + " belongs to route " + routeId);
        stopTimes = new TreeSet<GTFSStopTime>();
        timeStops = new HashMap<GTFSStop, GTFSStopTime>();
        queryStopTime1 = new GTFSStopTime(null, null, 0);
        queryStopTime2 = new GTFSStopTime(null, null, 0);
    }
    
    public SortedSet<GTFSStopTime> stopTimesAtOrAfter(int time) {
    	queryStopTime1.setTime(time);
        return stopTimes.tailSet(queryStopTime1);
    }
    
    public SortedSet<GTFSStopTime> stopTimesAfter(int time) {
    	return stopTimesAtOrAfter(time+1);
    }
    
    public GTFSStopTime firstStopTime() {
        return stopTimes.first();
    }
      
    public GTFSStopTime lastStopTime() {
		return stopTimes.last();
    }
    
    public void addStopTime(GTFSStopTime stopTime) {
		// add stopTime to list
		stopTimes.add(stopTime);
		
		// add stopTime to stop index
		timeStops.put(stopTime.getStop(), stopTime);
		
    }
    
    public boolean visitsStop(GTFSStop stop) {
    	return timeStops.containsKey(stop);
    }
    
    public GTFSStopTime stopTimeAtStop(GTFSStop stop) {
    	return timeStops.get(stop);
    }
    
    public GTFSRoute getRoute() { return route; }
    
    public String getId() { return id; }
    
}