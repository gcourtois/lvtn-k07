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
	
	public void beginMethod(String accessSpecifier, String returnType, String name, String[] params, String[] exceptions) {
	    StringBuilder sb = new StringBuilder();
	    sb.append(accessSpecifier + " ");
	    sb.append(returnType + " ");
	    sb.append(name + "(");
	    if (params != null && params.length > 0 && params[0] != null) {
	        sb.append(params[0]);
	        for (int i = 1; i < params.length; i++) {
	            if (params[i] != null && params[i] != "")
	                sb.append(", " + params[i]);
	        }
	    }
	    sb.append(")");
	    if (exceptions != null && exceptions.length > 0) {
	        if (exceptions[0] != null && exceptions[0] != "") {
	            sb.append(" throws " + exceptions[0]);
	            for (int i = 1; i < exceptions.length; i++) {
	                if (exceptions[i] != null && exceptions[i] != "") {
	                    sb.append(", " + exceptions[i]);
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

class CodePrinter {
	private int tabSize = 4;
	private StringBuilder indent = new StringBuilder();
	private int indentLength = 0;
	private PrintStream ps;
	
	public CodePrinter(PrintStream ps) {
		this.ps = ps;
	}
	
	public CodePrinter(OutputStream os) {
		this.ps = new PrintStream(os);
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
	}
	
	public void println(String s) {
		ps.println(indent.toString() + s);
	}
	
	public void print(String s) {
		ps.print(indent.toString() + s);
	}
}
