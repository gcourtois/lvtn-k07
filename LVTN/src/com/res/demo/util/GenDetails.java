package com.res.demo.util;

import java.util.HashMap;

public class GenDetails {
    private HashMap<Integer, OutputInfo> table = new HashMap<Integer, OutputInfo>();
    private static GenDetails instance = null;
    
    private GenDetails() {
    }
    
    public static GenDetails getInstance() {
        if (instance == null) {
            instance = new GenDetails();
        }
        return instance;
    }
    
    public void add(Integer i, OutputInfo info) {
        table.put(i, info);
    }
    
    public OutputInfo get(int i) {
        return table.get(i);
    }
    
    public void clear() {
        table.clear();
    }
    
    public class OutputInfo {
        public String fileName;
        public int beginLine;
        public int endLine;
        
        public OutputInfo(String fileName, int beginLine, int endLine) {
            this.fileName = fileName;
            this.beginLine = beginLine;
            this.endLine = endLine;
        }
    }
}
