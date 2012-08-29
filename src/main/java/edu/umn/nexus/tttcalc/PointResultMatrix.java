package edu.umn.nexus.tttcalc;

import java.io.*;
import java.util.*;

public class PointResultMatrix {
    private Map<ODPoint, Map<ODPoint, Integer>> matrix;
    
    public PointResultMatrix() {
        this.matrix = new HashMap<ODPoint, Map<ODPoint, Integer>>();
    }
}