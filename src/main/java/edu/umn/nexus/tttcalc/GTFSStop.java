package edu.umn.nexus.tttcalc;

import java.io.*;
import java.util.*;

public class GTFSStop {
    private String id;
    private GeoPoint location;
    private Map<GTFSStop, Integer> transferStopAccessTimes;
    private Set<GTFSRoute> routes;
    private SortedSet<GTFSStopTime> stopTimes;
    private Map<ODPoint, Integer> pointAccessTimes;
    private GTFSStopTime queryStopTime1;
    private GTFSStopTime queryStopTime2;
    
    public GTFSStop(String id, GeoPoint location) {
        this.id = id;
        this.location = location;
        this.transferStopAccessTimes = new HashMap<GTFSStop, Integer>();
        this.routes = new HashSet<GTFSRoute>();
        this.stopTimes =  new TreeSet<GTFSStopTime>();
        this.pointAccessTimes = new HashMap<ODPoint, Integer>();
        this.queryStopTime1 = new GTFSStopTime(null, null, 0);
        this.queryStopTime2 = new GTFSStopTime(null, null, 0);
    }
    
    public void addStopTime(GTFSStopTime stopTime) {
		stopTimes.add(stopTime);
		addRoute(stopTime.getTrip().getRoute());
    }
    
    public void addRoute(GTFSRoute route) {
        routes.add(route);
    }
    
    public void addTransferStop(GTFSStop stop, int accessTime) {
        transferStopAccessTimes.put(stop, accessTime);
    }
    
    public Integer getAccessTimeForTransferStop(GTFSStop stop) {
    	return transferStopAccessTimes.get(stop);
    }
    
    public void addPoint(ODPoint point, int accessTime) {
        pointAccessTimes.put(point, accessTime);
        //System.out.println("  Stop " + id + ": added point " + point.getId());
    }
    
    public void removePoint(ODPoint point) {
        pointAccessTimes.remove(point);
    }
    
    public Set<ODPoint> getPoints() {
        return pointAccessTimes.keySet();
    }
    
    public int getAccessTimeForPoint(ODPoint point) {
        return pointAccessTimes.get(point);
    }
    
    public Set<GTFSRoute> getRoutes() {
        return routes;
    }
    
    public Set<GTFSStop> getTransferStops() {
        return transferStopAccessTimes.keySet();
    }
    
    public SortedSet<GTFSStopTime> stopTimesBetween(int startTime, int endTime) {
        queryStopTime1.setTime(startTime);
        queryStopTime2.setTime(endTime);
        return stopTimes.subSet(queryStopTime1, queryStopTime2);
    }
    
    public GeoPoint getLocation() {
    	return location;
    }
    
    public String getId() { return id; }
}