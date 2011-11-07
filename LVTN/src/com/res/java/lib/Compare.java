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
        return op1.equals(String.valueOf(op2));
    }
    
    public static final boolean equal(String op1, String op2) {
        return op1.equals(op2);
    }
}
