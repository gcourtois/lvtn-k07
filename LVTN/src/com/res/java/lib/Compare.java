package com.res.java.lib;

import java.math.BigDecimal;

public class Compare {
    public static final boolean equal(long op1, long op2) {
        return op1 == op2;
    }
    
    public static final boolean equal(long op1, BigDecimal op2) {
        return op1 == op2.longValue();
    }
    
    public static final boolean equal(long op1, String op2) {
        try {
            Long tmp2 = Long.valueOf(op2);
            return op1 == tmp2;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static final boolean equal(BigDecimal op1, long op2) {
        return op1.longValue() == op2;
    }
    
    public static final boolean equal(BigDecimal op1, BigDecimal op2) {
        return (op1.compareTo(op2) == 0);
    }
    
    public static final boolean equal(String op1, long op2) {
        return equal(op1, String.valueOf(op2));
    }
    
    public static final boolean equal(String op1, String op2) {
        if (op1.length() < op2.length()) {
            StringBuilder sb = new StringBuilder(op1);
            while (sb.length() < op2.length()) {
                sb.append(' ');
            }
            op1 = sb.toString();
        } else if (op1.length() > op2.length()) {
            StringBuilder sb = new StringBuilder(op2);
            while (sb.length() < op1.length()) {
                sb.append(' ');
            }
            op2 = sb.toString();
        }
        return op1.equals(op2);
    }
}


