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
													         0x06, 0x07, 0x08, 0x09, 0x10, 0x11,
													         0x12, 0x13, 0x14, 0x15, 0x16,
													         0x17, 0x18, 0x19, 0x20, 0x21,
													         0x22, 0x23, 0x24, 0x25, 0x26,
													         0x27, 0x28, 0x29, 0x30, 0x31,
													         0x32, 0x33, 0x34, 0x35, 0x36,
													         0x37, 0x38, 0x39, 0x40, 0x41,
													         0x42, 0x43, 0x44, 0x45, 0x46,
													         0x47, 0x48, 0x49, 0x50, 0x51,
													         0x52, 0x53, 0x54, 0x55, 0x56,
													         0x57, 0x58, 0x59, 0x60, 0x61,
													         0x62, 0x63, 0x64, 0x65, 0x66,
													         0x67, 0x68, 0x69, 0x70, 0x71,
													         0x72, 0x73, 0x74, 0x75, 0x76,
													         0x77, 0x78, 0x79, (byte) 0x80, (byte) 0x81,
													         (byte) 0x82, (byte) 0x83, (byte) 0x84, (byte) 0x85, (byte) 0x86,
													         (byte) 0x87, (byte) 0x88, (byte) 0x89, (byte) 0x90, (byte) 0x91,
													         (byte) 0x92, (byte) 0x93, (byte) 0x94, (byte) 0x95, (byte) 0x96,
													         (byte) 0x97, (byte) 0x98, (byte) 0x99 };
	
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
		int length = String.valueOf(Math.abs(input)).length() + 1;
		if (length > 10) {
			throw new OverflowException("Length of int is too long. > 9");
		}
		return ((length / 2) + (length % 2));
	}
	
	/**
	 * Convert int to BCD format
	 * @param input
	 * @param signed
	 * @return
	 */
	public byte[] convertIntToBCD(int input, boolean signed) {
		ByteBuffer buffer;
		int byteLength = calculateByteLengthBCD(input);
		byte[] result = new byte[byteLength];
		buffer = ByteBuffer.wrap(result);
		byte signByte = 0x0F;
		if (signed) {
			if  (input < 0) {
				signByte = 0x0D;
			} else {
				signByte = 0x0C;
			}
		}
		input = Math.abs(input);
		int lastDigit = input % 10;
		signByte = (byte) ((signByte | (lastDigit << 4)) & 0xFF);
		buffer.position(byteLength - 1);
		buffer.put(signByte);
		input = input / 10;
		for (int index = 1; index < byteLength; index++ , input /= 100) {
			buffer.position(byteLength - index - 1);
			buffer.put(PACKED_DECIMALS[input % 100]);
		}
		
		return buffer.array();
	}
	
	private int calculateByteLengthBCD(long input) {
		int length = String.valueOf(Math.abs(input)).length() + 1;
		if (length > 19) {
			throw new OverflowException("Length of long is too long. > 18");
		}
		return ((length / 2) + (length % 2));
	}
	
	/**
	 * Convert long to BCD format
	 * @param input
	 * @param signed
	 * @return
	 */
	public byte[] convertLongToBCD(long input, boolean signed) {
		ByteBuffer buffer;
		int byteLength = calculateByteLengthBCD(input);
		byte[] result = new byte[byteLength];
		buffer = ByteBuffer.wrap(result);
		byte signByte = 0x0F;
		if (signed) {
			if  (input < 0) {
				signByte = 0x0D;
			} else {
				signByte = 0x0C;
			}
		}
		input = Math.abs(input);
		long lastDigit = input % 10;
		signByte = (byte) ((signByte | (lastDigit << 4)) & 0xFF);
		buffer.position(byteLength - 1);
		buffer.put(signByte);
		input = input / 10;
		for (int index = 1; index < byteLength; index++ , input /= 100) {
			buffer.position(byteLength - index - 1);
			buffer.put(PACKED_DECIMALS[(int) (input % 100)]);
		}
		
		return buffer.array();
	}
	/**
	 * Convert BigDecimal to BCD, remember to normalize BigDecimal before set
	 * @param input
	 * @param signed
	 * @return
	 */
	public byte[] convertBigDecimalToBCD(BigDecimal input, boolean signed) {
		input = input.scaleByPowerOfTen(input.scale());
		long longVal = input.longValue();
		return convertLongToBCD(longVal, signed);
		
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
			throw new OverflowException("Length of bytes array is too Long");
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
	public BigDecimal convertDisplayToBigDecimal(byte[] input, int scale,  boolean signed) {
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
		byte[] testInput = {0x01,0x45,0x12,0x3D};
		BigDecimal testBig = test.convertBCDToBigDec(testInput, 2);
		System.out.println(test.convertBCDToBigDec(testInput, 2).toString());
//		System.out.println(test.convertBCDToInt(testInput));
//		byte[] testInput = {(byte) 0x0f,(byte) 0xff, (byte) 0xfb, 0x2e};
//		System.out.println(test.convertBytesToInt(testInput, false));
//		byte[] testInput2 = {(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte) 0xD4};
//		System.out.println(test.convertDisplayToLong(testInput2, true));
		long testInt = 123456789123456789L;
		System.out.println(test.printByteArray(test.convertLongToBCD(testInt, true)));
		//BigDecimal testBig = new BigDecimal(123123123.1);
		//testBig.setScale(1);
		System.out.println(test.printByteArray(test.convertBigDecimalToBCD(testBig, true)));
		//System.out.println(Integer.toHexString(-123));
	}
	
};


