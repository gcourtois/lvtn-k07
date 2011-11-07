package com.res.java.translation.engine;

import com.res.java.lib.Constants;


public class LiteralString {
	public StringBuilder literal;
	public boolean isAll = false;
	public byte javaType;
	public byte category;
	
	public LiteralString() {
	    literal = new StringBuilder();
	}

	public LiteralString(String s) {
	    literal = new StringBuilder(s);
	}
	
	public String toString() {
	    return this.literal.toString();
	}
	
	/**
	 * Apply to nonnumeric literals.<br/>
	 * Remove enclosed character (" or ').<br/>
	 * Replace double quotes ("") or double apostrophe('')
	 */
	public void convertToJavaLiteral() {
	    if (category == Constants.ALPHANUMERIC) {
	        char firstChar = literal.charAt(0);
	        if (firstChar != '"' && firstChar != '\'')
	            return;
	        literal.deleteCharAt(0);
	        literal.deleteCharAt(literal.length() - 1);
	        String tmp = literal.toString();
	        if (firstChar == '"') {
	            tmp = tmp.replace("\"\"", "\"");
	        } else {
	            tmp = tmp.replace("''", "'");
	        }
	        literal = new StringBuilder(tmp);
	    }
	}
	
	public void convertToPrint() {
	    if (category == Constants.NUMERIC) {
	        if (javaType == Constants.LONG) {
	            if (Long.valueOf(literal.toString()) > Integer.MAX_VALUE) {
	                literal.append("L");
	            }
	        } else if (javaType == Constants.BIGDECIMAL) {
	            String tmp = String.format("new BigDecimal(\"%s\")", literal.toString());
	            literal = new StringBuilder(tmp);
	        }
	    } else if (category == Constants.ALPHANUMERIC
	            || (category >= Constants.SPACE && category <= Constants.HIGH_VALUE)) {
	        String tmp = literal.toString();
	        tmp = tmp.replace("\\", "\\\\");
	        tmp = tmp.replace("\"", "\\\"");
	        literal = new StringBuilder();
	        literal.append("\"");
	        literal.append(tmp);
	        literal.append("\"");
	    }
	}
	
	public void fillToSize(int size) {
	    String tmp = literal.toString();
	    while (literal.length() < size) {
	        literal.append(tmp);
	    }
	}
}
