package edu.umn.nexus.tttcalc;

import java.io.*;
import java.util.*;

public class GTFSStopTime implements Comparable<GTFSStopTime> {
	private GTFSTrip trip;
	private GTFSStop stop;
    private int time; // seconds past midnight
    
    public GTFSStopTime(GTFSTrip trip, GTFSStop stop, int time) {
        this.trip = trip;
        this.stop = stop;
        this.time = time;
    }
    
    public GTFSStopTime(GTFSTrip trip, GTFSStop stop, String time) {
        this.trip = trip;
        this.stop = stop;
        this.time = GTFSData.gtfsTimeToSeconds(time);
    }
    
    public int compareTo(GTFSStopTime other) {
    	if (this.time < other.time) return -1;
    	if (this.time > other.time) return 1;
    	return 0;
    }
    
    public int compareTo(int otherTime) {
    	if (this.time < otherTime) return -1;
    	if (this.time > otherTime) return 1;
    	return 0;
    }
    
    public boolean equals(GTFSStopTime other) {
    	if (this.time == other.time) return true;
    	return false;
    }

    public boolean equals(int otherTime) {
    	if (this.time == otherTime) return true;
    	return false;
    }
    
    public GTFSTrip getTrip() {
    	return trip;
    }
    
    public GTFSStop getStop() {
    	return stop;
    }
    
    public int getTime() {
    	return time;
    }
    
    public void setTime(int time) {
    	this.time = time;
    }
}