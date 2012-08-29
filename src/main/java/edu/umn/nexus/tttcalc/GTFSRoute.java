package edu.umn.nexus.tttcalc;

import java.io.*;
import java.util.*;

public class GTFSRoute {
    private String id;
    private String name;
    private Set<GTFSTrip> trips;
    private Set<GTFSStop> stops;
    
    public GTFSRoute(String inputId, String inputName) {
        this.id = inputId;
        this.name = inputName;
        this.trips = new HashSet<GTFSTrip>();
        this.stops = new HashSet<GTFSStop>();
    }
    
    public void addStop(GTFSStop stop) {
        stops.add(stop);
    }
    
    public void addTrip(GTFSTrip trip) {
        trips.add(trip);
    }
    
    public String getId() {
    	return id;
    }
    
    public String getName() {
    	return name;
    }
    
    public GTFSTrip nextFutureTripForStop(GTFSStop stop, int time, int maxTime) {
    	GTFSTrip nextTrip = null;
    	int bestTime = Integer.MAX_VALUE;
    	for (GTFSTrip trip : trips) {
    		GTFSStopTime stopTime = trip.stopTimeAtStop(stop);
    		if (stopTime != null) {
    			int thisTime = stopTime.getTime();
    			if (thisTime < bestTime && thisTime < maxTime) {
    				bestTime = thisTime;
    				nextTrip = trip;
    			}
    		}
    	}
    	return nextTrip;
    }
}