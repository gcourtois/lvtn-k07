package com.res.java.lib;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

import com.res.java.lib.exceptions.OverflowException;

public class BaseClass {
	public byte[] data;
	
	
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
	public byte[] convertIntToBCD(int input, byte[] dest, int offset, boolean signed) {
		ByteBuffer buffer;
		int byteLength = calculateByteLengthBCD(input);
		buffer = ByteBuffer.wrap(dest);
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
		buffer.position(byteLength + offset - 1);
		buffer.put(signByte);
		input = input / 10;
		for (int index = 1; index < byteLength; index++ , input /= 100) {
			buffer.position(byteLength + offset - index - 1);
			buffer.put(TranslateConstants.PACKED_DECIMALS[input % 100]);
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
	public void convertLongToBCD(long input, byte[] dest, int offset, boolean signed) {
		ByteBuffer buffer;
		int byteLength = calculateByteLengthBCD(input);
		buffer = ByteBuffer.wrap(dest);
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
		buffer.position(byteLength + offset - 1);
		buffer.put(signByte);
		input = input / 10;
		for (int index = 1 ; index < byteLength ; index++ , input /= 100) {
			buffer.position(byteLength + offset - index - 1);
			buffer.put(TranslateConstants.PACKED_DECIMALS[(int) (input % 100)]);
		}
		
	}
	/**
	 * Convert BigDecimal to BCD, remember to normalize BigDecimal before set
	 * @param input
	 * @param signed
	 * @return
	 */
	public void convertBigDecimalToBCD(BigDecimal input, byte[] dest, int offset, boolean signed) {
		input = input.scaleByPowerOfTen(input.scale());
		long longVal = input.longValue();
		convertLongToBCD(longVal, dest, offset, signed);
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
	 * Convert Bytes to Int usage Display
	 * TODO:Handle EBCDIC
	 * @param input
	 * @param signed
	 * @return
	 */
	public int convertDisplayToInt(byte[] input, boolean signed) {
		int result = 0;
		if (input.length > 9) {
			throw new OverflowException("Length of bytes array is too Long > 9");
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
			throw new OverflowException("Length of Bytes array is too Long > 18"); 
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
	
	/**
	 * Convert Int to bytes array (usage display)
	 * TODO: Handle EBCDIC
	 * @param input
	 * @param signed
	 * @return
	 */
	public void convertIntToDisplay(int input, byte[] dest, int offset, boolean signed) {
		String inputStr = String.valueOf(Math.abs(input));
		int byteLength = inputStr.length();
		if (byteLength > 9) {
			throw new ArithmeticException("Length of int value is too long > 9"); 
		}
		ByteBuffer buffer;
		buffer = ByteBuffer.wrap(dest);
		byte signByte = (byte) 0xF0;
		if (signed) {
			if  (input < 0) {
				signByte = (byte) 0xD0;
			} else {
				signByte = (byte) 0xC0;
			}
		}
		input = Math.abs(input);
		int lastDigit = input % 10;
		signByte = (byte) ((signByte | (lastDigit)) & 0xFF);
		buffer.position(byteLength + offset - 1);
		buffer.put(signByte);
		buffer.position(offset);
		buffer.put(inputStr.substring(0, byteLength-1).getBytes());
	}
	
	/**
	 * Convert Long to bytes array (usage display)
	 * TODO: Handle EBCDIC
	 * @param input
	 * @param signed
	 * @return
	 */
	public void convertLongToDisplay(long input, byte[] dest, int offset, boolean signed) {
		String inputStr = String.valueOf(Math.abs(input));
		int byteLength = inputStr.length();
		if (byteLength > 18) {
			throw new ArithmeticException("Length of long value is too long > 18"); 
		}
		ByteBuffer buffer;
		buffer = ByteBuffer.wrap(dest);
		byte signByte = (byte) 0xF0;
		if (signed) {
			if  (input < 0) {
				signByte = (byte) 0xD0;
			} else {
				signByte = (byte) 0xC0;
			}
		}
		input = Math.abs(input);
		int lastDigit = (int) (input % 10);
		signByte = (byte) ((signByte | (lastDigit)) & 0xFF);
		buffer.position(byteLength + offset - 1);
		buffer.put(signByte);
		buffer.position(offset);
		buffer.put(inputStr.substring(0, byteLength-1).getBytes());
	}
	/**
	 * Convert Long to bytes array (usage display)
	 * TODO: Handle EBCDIC
	 * @param input
	 * @param signed
	 * @return
	 */
	public void convertBigDecimalToDisplay(BigDecimal input, byte[] dest, int offset, boolean signed) {
		input = input.scaleByPowerOfTen(input.scale());
		long longVal = input.longValue();
		convertLongToDisplay(longVal, dest, offset, signed);
	}
	
	
	public String printByteArray(byte[] input) {
		String result = "";
		for (byte b : input) {
			 result +=
		          Integer.toString( ( b & 0xff ) + 0x100, 16).substring( 1 );

		}
		return result;
	}

}
