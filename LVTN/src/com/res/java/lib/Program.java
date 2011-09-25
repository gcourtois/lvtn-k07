package com.res.java.lib;

import java.math.BigDecimal;

public class Program extends BaseClass {
    public Program() {}
    
	public Program(int size) {
		super(size);
	}
	
	private String formatString(String alphanumericInput) {
		BigDecimal numeric = new BigDecimal(alphanumericInput);
		numeric = numeric.abs();
		return numeric.toString();
	}
	
	protected final void display(boolean withNoAdvancing, Object ... args) {
	    StringBuilder sb = new StringBuilder();
	    for (Object o : args) {
	        sb.append(o.toString());
	    }
	    if (withNoAdvancing) {
	        System.out.print(sb.toString());
	    } else {
	        System.out.println(sb.toString());
	    }
	}
}