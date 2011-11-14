package com.res.java.util;

import java.io.OutputStream;
import java.io.PrintStream;

public class JavaCodePrinter extends CodePrinter {
	public JavaCodePrinter(PrintStream ps) {
		super(ps);
	}
	
	public JavaCodePrinter(OutputStream os) {
		super(os);
	}
	
	public void printPackage(String packageName) {
		println("package " + packageName + ";");
	}
	
	@SuppressWarnings("unchecked")
	public void printImport(Class c) {
		println("import " + c.getCanonicalName() + ";");
	}
	
	public void printImport(String packageName) {
	    println("import " + packageName + ";");
	}
	
	public void beginMethod(String accessSpecifier, String returnType, String name, String[] params, String[] exceptions) {
	    StringBuilder sb = new StringBuilder();
	    sb.append(accessSpecifier + " ");
	    sb.append(returnType + " ");
	    sb.append(name + "(");
	    if (params != null && params.length > 0) {
	        boolean exist = false;
	        for (int i = 0; i < params.length; i++) {
	            if (params[i] != null && params[i] != "") {
	                if (exist) {
	                    sb.append(", " + params[i]);
	                } else {
	                    sb.append(params[i]);
	                    exist = true;
	                }
	            }
	        }
	    }
	    sb.append(")");
	    if (exceptions != null && exceptions.length > 0) {
	        boolean exist = false;
	        for (int i = 0; i < exceptions.length; i++) {
	            if (exceptions[i] != null && exceptions[i] != "") {
	                if (exist) {
	                    sb.append(", " + exceptions[i]);
	                } else {
	                    sb.append(" throws " + exceptions[i]);
	                    exist = true;
	                }
	            }
	        }
	    }
	    sb.append(" {");
	    println(sb.toString());
	    increaseIndent();
	}
	
	public void endMethod() {
	    decreaseIndent();
	    println("}");
	}
	
	public void beginBlock() {
	    println("{");
	    increaseIndent();
	}
	
	public void endBlock() {
	    decreaseIndent();
	    println("}");
	}
}
