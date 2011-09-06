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
