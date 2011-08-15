package com.res.java.lib;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.res.java.lib.exceptions.OverflowException;

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
				} else if (secondDigit == 0xc || secondDigit == 0xf){
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
				} else if (secondDigit == 0xc || secondDigit == 0xf){
					sign = false;
				} else {
					//return -1;
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
				} else if (secondDigit == 0xc || secondDigit == 0xf){
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
	/*
	 * Need more refinements
	 */
	public int convertBytesToInt(byte[] input, boolean signed) {
		ByteBuffer temp = ByteBuffer.wrap(input);
		if (input.length > 4) {
			throw new ArithmeticException("Length for Integer conversion > 4");
		}
		if (signed) {
			int result =  temp.getInt();
			if (result > 999999999) {
				throw new OverflowException("Overflow Integer Conversion");
			} else {
				return result;
			}
		} else {
			int result = temp.getInt();
			if (result < 0 || result > 999999999) {
				throw new OverflowException("Signed Number is not correct");
			} else {
				return result;
			}
		}
	}
	
	/*
	 * Need more refinements
	 */
	public long convertBytesToLong(byte[] input, boolean signed) {
		ByteBuffer temp = ByteBuffer.wrap(input);
		if (input.length > 8) {
			throw new ArithmeticException("Length for Long conversion is wrong");
		}
		if (signed) {
			long result =  temp.getLong();
			return result;
		} else {
			long result = temp.getLong();
			return result;
		}
	}
	
	public int convertDisplayToInt(byte[] input, boolean signed) {
		int result = 0;
		if (signed) {
			boolean negate = false;
			int signValue = input[input.length - 1] >> 4;
			if (signValue == 0xD) {
				negate = false;
			} else if (signValue == 0xC) {
				negate = true;
			} else if (signValue == 0xF) {
				negate = false;
			} 
			for (int index = 0; index < input.length; index++) {
				byte eachByte = input[index];
				int value = eachByte & 0x0f;
				if (value < 10 && value >= 0) {
					result = result*10 + value;
				} else {
					throw new ArithmeticException("Display format is not right ");
				}
			}
			if (negate) {
				result = -result;
			}
		} else {
			for (int index = 0; index < input.length; index++) {
				byte eachByte = input[index];
				int value = eachByte & 0x0f;
				if (value < 10 && value >= 0) {
					result = result*10 + value;
				} else {
					throw new ArithmeticException("Display format is not right ");
				}
			}
		}
		return result;
		
	}
	
	public static void main(String[] args) {
		MyMath test = new MyMath();
//		byte[] testInput = {0x45,0x12,0x3d};
//		System.out.println(test.convertBCDToBigDec(testInput, 1).toString());
		byte[] testInput = {(byte) 0x0f,(byte) 0xff, (byte) 0xfb, 0x2e};
		System.out.println(test.convertBytesToInt(testInput, false));
		byte[] testInput2 = {(byte)0xf1,(byte) 0xf2, (byte) 0xc4};
		System.out.println(test.convertDisplayToInt(testInput2, true));
		//System.out.println(Integer.toHexString(-123));
	}
	
};


