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
	
	 private static final byte[] PACKED_DECIMALS = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x5,
													         0x06, 0x07, 0x08, 0x09, 0x10, 11,
													         12, 13, 14, 15, 16,
													         17, 18, 19, 20, 21,
													         22, 23, 24, 25, 26,
													         27, 28, 29, 30, 31,
													         32, 33, 34, 35, 36,
													         37, 38, 39, 40, 41,
													         42, 43, 44, 45, 46,
													         47, 48, 49, 50, 51,
													         52, 53, 54, 55, 56,
													         57, 58, 59, 60, 61,
													         62, 63, 64, 65, 66,
													         67, 68, 69, 70, 71,
													         72, 73, 74, 75, 76,
													         77, 78, 79, 80, 81,
													         82, 83, 84, 85, 86,
													         87, 88, 89, 90, 91,
													         92, 93, 94, 95, 96,
													         97, 98, 99 };
	
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
	
	private int calculateByteLengthBCD(int input) {
		int length = String.valueOf(input).length() + 1;
		return ((length / 2) + (length % 2));
	}
	
	public byte[] convertIntToBCD(int input, boolean signed) {
		ByteBuffer buffer;
		int byteLength = calculateByteLengthBCD(input);
		byte[] result = new byte[byteLength];
		System.out.println("LENGTH " + calculateByteLengthBCD(input));
		buffer = ByteBuffer.wrap(result);
		String inputStr = String.valueOf(Math.abs(input));
		byte signByte = 0x0F;
		if (signed) {
			if  (input < 0) {
				signByte = 0x0D;
			} else {
				signByte = 0x0C;
			}
		}
		int lastDigit = input % 10;
		signByte = (byte) ((signByte | (lastDigit << 4)) & 0xFF);
		buffer.position(byteLength - 1);
		buffer.put(signByte);
		input = input / 10;
		buffer.position(0);
		for (int index = 1; index < inputStr.length(); index++, input /= 100) {
			buffer.put(PACKED_DECIMALS[input % 100]);
		}
		
		return buffer.array();
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
	/**
	 * 
	 * @param input
	 * @return
	 */
	private byte[] convertEBCDICtoAscii(byte[] input) {
		
		return null;
	}
	
	/**
	 * Convert  Bytes to Int usage Display
	 * TODO:Handle EBCDIC
	 * @param input
	 * @param signed
	 * @return
	 */
	public int convertDisplayToInt(byte[] input, boolean signed) {
		int result = 0;
		if (input.length > 9) {
			throw new OverflowException("Convert Bytes(Display) to Int failed " + input);
		}
		if (signed) {
			boolean negate = false;
			int signValue = (input[input.length - 1] >> 4) & 0x0f;
			if (signValue == 0xD) {
				negate = true;
			} else if (signValue == 0xC) {
				negate = false;
			} else if (signValue == 0xF) {
				negate = false;
			} 
			//if Ascii --> change to 3
			byte[] standardArray = input.clone();
			byte lastByte = standardArray[standardArray.length - 1];
			lastByte = (byte) (lastByte & 0x0f);
			lastByte = (byte) (lastByte + 0x30);
			standardArray[standardArray.length - 1] = lastByte;
			try {
				result = Integer.valueOf(new String(standardArray));
			} catch(Exception e) {
				throw new ArithmeticException("Convert Bytes (Display) to Int failed " + input);
			}
			if (negate) {
				result = -result;
			}
		} else {
			try {
				result = Integer.valueOf(new String(input));
			} catch(Exception e) {
				throw new ArithmeticException("Convert Bytes (Display) to Int failed " + input);
			}
		}
		return result;
		
	}
	
	/**
	 * Convert  Bytes to Long usage Display
	 * TODO:Handle EBCDIC
	 * @param input
	 * @param signed
	 * @return
	 */
	public long convertDisplayToLong(byte[] input, boolean signed) {
		long result = 0;
		if (input.length > 18) {
			throw new OverflowException("Convert Bytes(Display) to Long failed " + input); 
		}
		if (signed) {
			boolean negate = false;
			int signValue = (input[input.length - 1] >> 4) & 0x0f;
			if (signValue == 0xD) {
				negate = true;
			} else if (signValue == 0xC) {
				negate = false;
			} else if (signValue == 0xF) {
				negate = false;
			} 
			//if Ascii --> change to 3
			byte[] standardArray = input.clone();
			byte lastByte = standardArray[standardArray.length - 1];
			lastByte = (byte) (lastByte & 0x0f);
			lastByte = (byte) (lastByte + 0x30);
			standardArray[standardArray.length - 1] = lastByte;
			try {
				result = Long.valueOf(new String(standardArray));
			} catch(Exception e) {
				throw new ArithmeticException("Convert Bytes (Display) to Long failed " + e);
			}
			if (negate) {
				result = -result;
			}
		} else {
			try {
				result = Long.valueOf(new String(input));
			} catch(Exception e) {
				throw new ArithmeticException("Convert Bytes (Display) to Long failed " + input);
			}
		}
		return result;
	}
	
	/**
	 * Convert  Bytes to BigDecimal usage Display
	 * TODO:Handle EBCDIC
	 * @param input
	 * @param signed
	 * @return
	 */
	public BigDecimal convertDisplayToLong(byte[] input, int scale,  boolean signed) {
		BigDecimal result;
		if (input.length > 18) {
			throw new OverflowException("Convert Bytes(Display) to BigDecimal failed " + input); 
		}
		if (signed) {
			boolean negate = false;
			int signValue = (input[input.length - 1] >> 4) & 0x0f;
			if (signValue == 0xD) {
				negate = true;
			} else if (signValue == 0xC) {
				negate = false;
			} else if (signValue == 0xF) {
				negate = false;
			} 
			//if Ascii --> change to 3
			byte[] standardArray = input.clone();
			byte lastByte = standardArray[standardArray.length - 1];
			lastByte = (byte) (lastByte & 0x0f);
			lastByte = (byte) (lastByte + 0x30);
			standardArray[standardArray.length - 1] = lastByte;
			try {
				result = BigDecimal.valueOf(Long.valueOf(new String(standardArray)));
			} catch(Exception e) {
				throw new ArithmeticException("Convert Bytes (Display) to Long failed " + e);
			}
			if (negate) {
				result = result.negate();
			}
			if (scale > 0) {
				result.scaleByPowerOfTen(scale);
			}
		} else {
			try {
				result = BigDecimal.valueOf(Long.valueOf(new String(input)));
			} catch(Exception e) {
				throw new ArithmeticException("Convert Bytes (Display) to Long failed " + input);
			}
			if (scale > 0) {
				result.scaleByPowerOfTen(scale);
			}
		}
		return result;
	}
	
	public String printByteArray(byte[] input) {
		String result = "";
		for (byte b : input) {
			 result +=
		          Integer.toString( ( b & 0xff ) + 0x100, 16).substring( 1 );

		}
		return result;
	}
	
	
	public static void main(String[] args) {
		MyMath test = new MyMath();
//		byte[] testInput = {0x45,0x12,0x3D};
//		System.out.println(test.convertBCDToBigDec(testInput, 1).toString());
//		System.out.println(test.convertBCDToInt(testInput));
//		byte[] testInput = {(byte) 0x0f,(byte) 0xff, (byte) 0xfb, 0x2e};
//		System.out.println(test.convertBytesToInt(testInput, false));
//		byte[] testInput2 = {(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte) 0xD4};
//		System.out.println(test.convertDisplayToLong(testInput2, true));
		int testInt = 151;
		System.out.println(test.printByteArray(test.convertIntToBCD(testInt, false)));
		//System.out.println(Integer.toHexString(-123));
	}
	
};


