package com.res.java.lib;

import java.math.BigDecimal;

public class Program extends BaseClass {
	public Program(int size) {
		super(size);
	}
	private String formatString(String alphanumericInput) {
		BigDecimal numeric = new BigDecimal(alphanumericInput);
		numeric = numeric.abs();
		return numeric.toString();
	}
}