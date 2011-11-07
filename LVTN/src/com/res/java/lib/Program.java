package com.res.java.lib;



public class Program extends BaseClass {
    public Program() {}
    
	public Program(int size) {
		super(size);
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