package com.res.java.lib;

import java.math.BigDecimal;

public class MyMath {
	public BigDecimal add(BigDecimal op1, BigDecimal op2) {
		return op1.add(op2);
	}
	public byte[] add(byte[] op1, byte[] op2) {
		return new byte[1];
	}
	public BigDecimal subtract(BigDecimal op1, BigDecimal op2) {
		return op1.subtract(op2);
	}
	public byte[] subtract(byte[] op1, byte[] op2) {
		return new byte[1];
	}
	public BigDecimal multiply(BigDecimal op1, BigDecimal op2) {
		return op1.multiply(op2);
	}
	//convert Display form to Int
	public int convertDisplayToInt(byte[] input) {
		
		return 0;
	}
	public BigDecimal convertBCDToBigDec(byte[] input, int scale) {
		boolean sign = false;
		BigDecimal result = new BigDecimal(0);
		for (int index =0; index < input.length; index++) {
			byte eachByte = input[index];
			int firstDigit = (eachByte >> 4) & 0x0f;
			int secondDigit = eachByte & 0x0f;
			if (index == (input.length - 1)) {
				if (secondDigit == 0xd) {
					sign = true;
				} else if (secondDigit == 0xc){
					sign = false;
				} else {
					return new BigDecimal(-1);
				}
				result = result.multiply(new BigDecimal(10)).add(new BigDecimal(firstDigit));
			} else {
				result = result.multiply(new BigDecimal(100)).add(new BigDecimal(firstDigit*10 + secondDigit));
			}
		}
		if (sign) {
			result =  result.negate();
		}
		if (scale > 0) {
			result = result.scaleByPowerOfTen(-scale);
		}
		return result;
	}
	
	//convert Packed Decimal to Int
	public int convertBCDToInt(byte[] input) {
		boolean sign = false;
		int result = 0;
		if (input.length >= 6) {
			throw new ArithmeticException();
		}
		for (int index =0; index < input.length; index++) {
			byte eachByte = input[index];
			int firstDigit = (eachByte >> 4) & 0x0f;
			int secondDigit = eachByte & 0x0f;
			if (index == (input.length - 1)) {
				if (secondDigit == 0xd) {
					sign = true;
				} else if (secondDigit == 0xc){
					sign = false;
				} else {
					return -1;
				}
				result = result*10 + firstDigit;
			} else {
				result = (firstDigit*10 + secondDigit) + result*100;
			}
		}
		if (sign) {
			return -result;
		}
		return result;
	}
	public long convertBCDToLong(byte[] input) {
		boolean sign = false;
		long result = 0;
		for (int index =0; index < input.length; index++) {
			byte eachByte = input[index];
			int firstDigit = (eachByte >> 4) & 0x0f;
			int secondDigit = eachByte & 0x0f;
			if (index == (input.length - 1)) {
				if (secondDigit == 0xd) {
					sign = true;
				} else if (secondDigit == 0xc){
					sign = false;
				} else {
					return -1;
				}
				result = result*10 + firstDigit;
			} else {
				result = (firstDigit*10 + secondDigit) + result*100;
			}
		}
		if (sign) {
			result = -result;
		}
		return result;
	}
	public static void main(String[] args) {
		MyMath test = new MyMath();
		byte[] testInput = {0x45,0x12,0x3d};
		System.out.println(test.convertBCDToBigDec(testInput, 1).toString());
	}
	
};


