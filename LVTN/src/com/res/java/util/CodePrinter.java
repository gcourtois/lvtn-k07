package com.res.java.util;

import java.io.OutputStream;
import java.io.PrintStream;


public class CodePrinter {
    private int tabSize = 4;
    private StringBuilder indent = new StringBuilder();
    private int indentLength = 0;
    private PrintStream ps;
    
    private int currentLine = 1;
    
    public CodePrinter(PrintStream ps) {
        this.ps = ps;
    }
    
    public CodePrinter(OutputStream os) {
        this.ps = new PrintStream(os);
    }
    
    public PrintStream getStream() {
        return this.ps;
    }
    
    public void close() {
        ps.close();
    }
    
    public int getTabSize() {
        return tabSize;
    }

    public void setTabSize(int tabSize) {
        this.tabSize = tabSize;
    }

    public void increaseIndent() {
        indentLength += tabSize;
        for (int i = 0; i < tabSize; i ++) {
            indent.append(' ');
        }
    }
    
    public void decreaseIndent() {
        indentLength -= tabSize;
        indent.setLength(indentLength);
    }
    
    public void println() {
        ps.println();
        currentLine++;
    }
    
    public void println(String s) {
        ps.println(indent.toString() + s);
        increaseLineNumber(s);
        currentLine++;
    }
    
    public void print(String s) {
        ps.print(indent.toString() + s);
        increaseLineNumber(s);
    }
    
    public void print(char c) {
        ps.print(c);
        if (c == '\n') {
            currentLine++;
        }
    }
    
    public int getCurrentLine() {
        return this.currentLine;
    }
    
    private void increaseLineNumber(String s) {
        int startIndex = 0;
        int foundIndex = -1;
        while ((foundIndex = s.indexOf('\n', startIndex)) > -1) {
            currentLine++;
            startIndex = foundIndex + 1;
        }
    }
}