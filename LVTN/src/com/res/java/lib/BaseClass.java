package com.res.java.lib;

/**
 * TODO: Handle overflow when putting to bytes array
 * TODO: Alignment of data
 * TODO: Sign Trailing/ Sign Leading
 * TODO: Unsigned Integer/Long
 * TODO: Convert EBCDIC --> ASCII or EBCDIC --> number
 */

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.res.java.lib.exceptions.OverflowException;

public class BaseClass {
	private byte[] data;

	private int offset;

	private int length;

	public BaseClass(int size) {
		data = new byte[size];
		offset = 0;
		length = size;
	}

	public BaseClass(byte[] data) {
		this.data = data;
		offset = 0;
		length = data.length;
	}

	public BaseClass(byte[] data, int offset, int length) {
		this.data = data;
		this.offset = offset;
		this.length = length;
	}

	public byte[] getBytes() {
		return this.data;
	}
	
	/**
	 * Get BCD Value
	 * Remember to calculate length of element int and long.
	 * Cast is not right if value > Int.MAX_VALUE 
	 * @param offset
	 * @param length
	 * @param scale
	 * @param signed
	 * @return
	 */
	protected BigDecimal getBigDecimalBCD(int offset, int length, int scale, boolean signed) {
		long longValue = convertBCDToLong(offset, length, signed);
		BigDecimal returnValue = new BigDecimal(longValue);
		if (scale > 0) {
			return doPscaling(returnValue, scale);
		}
		return returnValue;
	}
	
	protected long getLongBCD(int offset, int length, int pscale, boolean signed) {
		if (length > 10) {
			throw new ArithmeticException("Bytes array is too long for Long type");
		}
		long tempValue = convertBCDToLong(offset, length, signed);
		if (pscale > 0) {
			return doPScaling(tempValue, pscale);
		}
		return tempValue;
	}
	
	protected int getIntBCD(int offset, int length, int pscale, boolean signed) {
		if (length > 6) {
			throw new ArithmeticException("Bytes array is too long for Integer type");
		}
		long tempValue = convertBCDToLong(offset, length, signed);
		if (pscale > 0) {
			tempValue = doPScaling(tempValue, pscale);
			if (tempValue >= powerBase10[10]) {
				throw new ArithmeticException("Use Long method instead of Integer");
			}
		}
		return (int) tempValue;
	}
	
	protected void setBCDInteger(int input,  int offset, int actualSize, boolean signed, int pscale) {
//		int tempValue = (int) getAlgebraicValue((long) input, actualSize, signed, pscale);
//		convertIntToBCD(tempValue, offset, signed);
	}
	
	protected void setLongBCD(long input, int offset, int actualSize, boolean signed, int pscale) {
		long tempValue = adjustIntegralValue(input, actualSize, signed, pscale);
		convertLongToBCD(tempValue, offset, signed);
		
	}
	
	protected void setBigDecimalBCD(BigDecimal input, int offset, int intLength, int fractionLength, boolean signed, int pscale) {
		long longVal = adjustDecimalValue(input, intLength, fractionLength, pscale, signed);
		convertLongToBCD(longVal, offset, signed);
	}
	

	protected int getIntDisplay(int offset, int length, boolean signed, boolean signLeading, boolean signSeparate, int pscale) {
		return 1;
	}
	
	protected long getLongDisplay(int offset, int length, boolean signed, boolean signLeading, boolean signSeparate,int pscale) {
		return 1;
	}
	
	protected BigDecimal getBigDecimalDisplay(int offset, int length, boolean signed, boolean signLeading, boolean signSeparate,int pscale) {
		return new BigDecimal(1);
	}
	
	protected String getStringDisplay(int offset, int length) {
		return "";
	}
	
	protected void setIntDisplay(int input, int offset, int actualSize, boolean signed, boolean signLeading, boolean signSeparate,int pscale) {
		
	}
	
	protected void setLongDisplay(long input, int offset, int actualSize, boolean signed, boolean signLeading, boolean signSeparate,int pscale) {
		
	}
	
	protected void setBigDecimalDisplay(BigDecimal input, int offset, int intLength, int fractionLength, boolean signed, boolean signLeading, boolean signSeparate,int pscale) {
		
	}
	
	protected void setStringDisplay(String input, int offset, int length, boolean rightJustified) {
		
	}
	
	protected int getIntBytes(int offset, int length, boolean signed, int pscale) {
		return 1;
	}
	
	protected long getLongBytes(int offset, int length, boolean signed, int pscale) {
		return 1;
	}
	
	protected BigDecimal getBigDecimalBytes(int offset, int length, boolean signed, int pscale) {
		return new BigDecimal(1);
	}
	
	protected void setIntBytes(int input, int offset, int actualSize, boolean signed, int pscale) {
		
	}
	
	protected void setLongBytes(long input, int offset, int actualSize, boolean signed, int pscale) {
		
	}
	
	protected void setBigDecimalBytes(BigDecimal input, int offset, int intLength, int fractionLength, boolean signed, int pscale) {
		
	}
	
	
	
	
	private long[] powerBase10 = new long[] { 1, 10, 100, 1000, 10000, 100000,
			1000000, 10000000, 100000000, 1000000000, 10000000000L,
			100000000000L, 1000000000000L, 10000000000000L, 100000000000000L,
			1000000000000000L, 10000000000000000L, 100000000000000000L,
			1000000000000000000L };

	protected long adjustIntegralValue(long input, int size, boolean signed,
			int pscale) {
		if (size > 18) {
			throw new ArithmeticException("Size is larger than 18");
		}

		// pscale must >= 0
		if (pscale == 0) {
			input %= powerBase10[size];
		} else {
			input %= powerBase10[size + pscale];
			input = input / powerBase10[pscale];
		}
		if (input < 0 && !signed)
			input = Math.abs(input);
		return input;
	}

	protected long adjustDecimalValue(BigDecimal input, int intLength,
			int fractionLength, int pscale, boolean signed) {
		// fraction length must > 0

		input = input.remainder(BigDecimal.valueOf(powerBase10[intLength]));

		if (pscale > 0) {
			input = input.movePointRight(pscale).remainder(BigDecimal.ONE);
		}

		input = input.scaleByPowerOfTen(fractionLength);

		if (input.signum() < 0 && !signed)
			input = input.abs();

		return input.longValue();
	}

	protected long getAlgebraicValue(long input, int size, boolean signed,
			int pscale) {
		if (pscale == 0) {
			input %= powerBase10[size];
		} else {
			input %= powerBase10[size + pscale];
			input = input / powerBase10[pscale] * powerBase10[pscale];
		}
		if (input < 0 && !signed)
			input = Math.abs(input);
		return input;
	}

	protected BigDecimal getAlgebraicValue(BigDecimal input, int intLength,
			int fractionLength, int pscale, boolean signed) {
		input = input.remainder(BigDecimal.valueOf(powerBase10[intLength]));

		int s = 0;
		if (pscale > 0) {
			input = input.movePointRight(pscale).remainder(BigDecimal.ONE)
					.movePointLeft(pscale);
			// input = input.scaleByPowerOfTen(pscale + fractionLength);
			s = pscale + fractionLength;
		} else {
			// input = input.scaleByPowerOfTen(fractionLength);
			s = fractionLength;
		}

		input = BigDecimal.valueOf(input.scaleByPowerOfTen(s).longValue())
				.scaleByPowerOfTen(-s);

		if (input.signum() < 0 && !signed) {
			input = input.abs();
		}

		return input;
	}
	private int doPScaling(int input, int pscale) {
		//What if pscale too big
		return input*= powerBase10[pscale];
	}
	private long doPScaling(long input, int pscale) {
		return input *= powerBase10[pscale];
	}

	private BigDecimal doPscaling(BigDecimal input, int scale) {
		return input.scaleByPowerOfTen(-scale);
	}

	/* START HERE */
	
	
	
	

	private long convertBCDToLong(int offset, int length, boolean signed) {
		boolean negate = false;
		long result = 0;
		for (int index = offset; index < offset + length; index++) {
			byte eachByte = data[index];
			int firstDigit = (eachByte >> 4) & 0x0f;
			int secondDigit = eachByte & 0x0f;
			if (index == (data.length - 1)) {
				if (signed) {
					if (secondDigit == 0xd) {
						negate = true;
					} else if (secondDigit == 0xc || secondDigit == 0xf) {
						negate = false;
					}
				}
				result = result * 10 + firstDigit;
			} else {
				result = (firstDigit * 10 + secondDigit) + result * 100;
			}
		}
		if (negate && signed) {
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
	 * 
	 * @param input
	 * @param signed
	 * @return
	 */
	private void convertIntToBCD(int input, int offset,
			boolean signed) {
		ByteBuffer buffer;
		int byteLength = calculateByteLengthBCD(input);
		buffer = ByteBuffer.wrap(data);
		byte signByte = 0x0F;
		if (signed) {
			if (input < 0) {
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
		for (int index = 1; index < byteLength; index++, input /= 100) {
			buffer.position(byteLength + offset - index - 1);
			buffer.put(TranslateConstants.PACKED_DECIMALS[input % 100]);
		}
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
	 * 
	 * @param input
	 * @param signed
	 * @return
	 */
	private void convertLongToBCD(long input, int offset,
			boolean signed) {
		ByteBuffer buffer;
		int byteLength = calculateByteLengthBCD(input);
		buffer = ByteBuffer.wrap(data);
		byte signByte = 0x0F;
		if (signed) {
			if (input < 0) {
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
		for (int index = 1; index < byteLength; index++, input /= 100) {
			buffer.position(byteLength + offset - index - 1);
			buffer.put(TranslateConstants.PACKED_DECIMALS[(int) (input % 100)]);
		}

	}
	
	

	/**
	 * Convert bytes array to Integer TODO: unsigned number :(
	 * 
	 * @param input
	 * @param offset
	 * @param length
	 * @param signed
	 * @return
	 */
	/*
	 * public int convertBytesToInt(byte[] input, int offset, int length,
	 * boolean signed) { ByteBuffer temp = ByteBuffer.wrap(input, offset,
	 * length); if (input.length > 4) { throw new
	 * ArithmeticException("Length for Integer conversion > 4"); } if (signed) {
	 * int result = temp.getInt(); if (result > 999999999) { throw new
	 * OverflowException("Overflow Integer Conversion"); } else { return result;
	 * } } else { int result = temp.getInt(); if (result < 0 || result >
	 * 999999999) { throw new
	 * OverflowException("UnSigned Integer is not correct"); } else { return
	 * result; } } }
	 */

	/**
	 * Convert bytes array to Long TODO: Unsigned Long
	 * 
	 * @param offset
	 * @param length
	 * @param signed
	 * @return
	 */
	private long convertBytesToLong(int offset, int length, int pscale,
			boolean signed) {
		ByteBuffer temp = ByteBuffer.wrap(data, offset, length);
		if (data.length > 8) {
			throw new ArithmeticException("Length for Long conversion is wrong");
		}
		long result = temp.getLong();

		if (pscale > 0)
			result = doPScaling(result, pscale);

		if (signed) {
			if (result > powerBase10[18]) {
				throw new OverflowException("Overflow Long Conversion");
			}
			// return result;
		} else {
			// long result = temp.getLong();
			if (result > powerBase10[18] || result < 0) {
				throw new OverflowException("UnSigned Long is not correct");
			}
		}
		return result;
	}

	/**
	 * Convert bytes array to BigDecimal
	 * 
	 * @param input
	 * @param signed
	 * @param scale
	 * @return
	 */

	private BigDecimal convertBytesToBigDecimal(int offset, int length,
			int scale, boolean signed) {
		long longVal = convertBytesToLong(offset, length, 0, signed);
		BigDecimal result = new BigDecimal(longVal);
		if (scale> 0) {
			result = result.scaleByPowerOfTen(-scale);
		}
		return result;
	}

	/**
	 * Convert EBCDIC to ASCII
	 * 
	 * @param input
	 * @return
	 */
	private byte[] convertEBCDICtoAscii(byte[] input, int offset) {

		return null;
	}
	
	
	

	/**
	 * Convert Bytes to Int usage Display TODO:Handle EBCDIC
	 * 
	 * @param input
	 * @param signed
	 * @return
	 */
	/*
	 * public int convertDisplayToInt(byte[] input, int offset, int length,
	 * boolean signed) { int result = 0; if (length > 9) { throw new
	 * OverflowException("Length of bytes array is too Long > 9"); } if (signed)
	 * { boolean negate = false; int signValue = (input[offset + length - 1] >>
	 * 4) & 0x0f; if (signValue == 0xD) { negate = true; } else if (signValue ==
	 * 0xC) { negate = false; } else if (signValue == 0xF) { negate = false; }
	 * //if Ascii --> change to 3 //if EBCDIC --> change to F byte[]
	 * standardArray = new byte[length]; System.arraycopy(input, offset,
	 * standardArray, 0, length); byte lastByte =
	 * standardArray[standardArray.length - 1]; lastByte = (byte) (lastByte &
	 * 0x0f); lastByte = (byte) (lastByte + 0x30);
	 * standardArray[standardArray.length - 1] = lastByte; try { result =
	 * Integer.valueOf(new String(standardArray)); } catch(Exception e) { throw
	 * new ArithmeticException("Convert Bytes (Display) to Int failed " +
	 * input); } if (negate) { result = -result; } } else { try { ByteBuffer
	 * buffer = ByteBuffer.wrap(input, offset, length); result =
	 * Integer.valueOf(new String(buffer.array())); } catch(Exception e) { throw
	 * new ArithmeticException("Convert Bytes (Display) to Int failed " +
	 * input); } } return result;
	 * 
	 * }
	 */

	/**
	 * Convert Bytes to Long usage Display TODO:Handle EBCDIC
	 * 
	 * @param signed
	 * @return
	 */
	private long convertDisplayToLong(int offset, int length, int scale,
			boolean signed, boolean signLeading, boolean signSeparate) {
		long result = 0;
		if (length > 18) {
			throw new OverflowException(
					"Length of Bytes array is too Long > 18");
		}
		boolean negate = false;
		if (signed) {
			int signValue = (data[offset + length - 1] >> 4) & 0x0f;
			if (signValue == 0xD) {
				negate = true;
			} else if (signValue == 0xC) {
				negate = false;
			} else if (signValue == 0xF) {
				negate = false;
			}
		}
		// Ascii --> change to 3
		byte[] standardArray = new byte[length];
		System.arraycopy(data, offset, standardArray, 0, length);
		byte lastByte = standardArray[standardArray.length - 1];
		lastByte = (byte) (lastByte & 0x0f);
		lastByte = (byte) (lastByte + 0x30);
		standardArray[standardArray.length - 1] = lastByte;
		try {
			result = Long.valueOf(new String(standardArray));
		} catch (Exception e) {
			throw new ArithmeticException(
					"Convert Bytes (Display) to Long failed " + e);
		}

		if (scale > 0) {
			result = doPScaling(result, scale);
		}

		if (negate && signed) {
			result = -result;
		}
		return result;
	}

	/**
	 * Convert Bytes to BigDecimal usage Display TODO:Handle EBCDIC
	 * 
	 * @param data
	 * @param signed
	 * @return
	 */
	private BigDecimal convertDisplayToBigDecimal(int offset, int length,
			int scale, boolean signed, boolean signLeading, boolean signSeparate) {
		BigDecimal result;
		if (length > 18) {
			throw new OverflowException(
					"Convert Bytes(Display) to BigDecimal failed " + data);
		}
		boolean negate = false;
		if (signed) {
			int signValue = (data[offset + length - 1] >> 4) & 0x0f;
			if (signValue == 0xD) {
				negate = true;
			} else if (signValue == 0xC) {
				negate = false;
			} else if (signValue == 0xF) {
				negate = false;
			}
		}
		// Ascii --> change to 3
		byte[] standardArray = new byte[length];
		System.arraycopy(data, offset, standardArray, 0, length);
		byte lastByte = standardArray[standardArray.length - 1];
		lastByte = (byte) (lastByte & 0x0f);
		lastByte = (byte) (lastByte + 0x30);
		standardArray[standardArray.length - 1] = lastByte;
		try {
			result = BigDecimal
					.valueOf(Long.valueOf(new String(standardArray)));
		} catch (Exception e) {
			throw new ArithmeticException(
					"Convert Bytes (Display) to Long failed " + e);
		}
		if (negate) {
			result = result.negate();
		}
		if (scale> 0) {
			result = result.scaleByPowerOfTen(-scale);
		}
		return result;
	}

	/**
	 * Convert DISPLAY bytes to String. TODO: Handle EBCDIC. Is it so easy ???
	 * 
	 * @param offset
	 * @param len
	 * @return
	 */
	private String convertDisplayToString(int offset, int length) {
		return new String(data, offset, length);
	}

	/**
	 * Convert Int to bytes array (usage display) TODO: Handle EBCDIC
	 * 
	 * @param input
	 * @param signed
	 * @return
	 */
	private void convertIntToDisplay(int input, byte[] dest, int offset,
			boolean signed, boolean signLeading, boolean signSeparate) {
		String inputStr = String.valueOf(Math.abs(input));
		int byteLength = inputStr.length();
		if (byteLength > 9) {
			throw new ArithmeticException("Length of int value is too long > 9");
		}
		ByteBuffer buffer;
		buffer = ByteBuffer.wrap(dest);
		byte signByte = (byte) 0xF0;
		if (signed) {
			if (input < 0) {
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
		buffer.put(inputStr.substring(0, byteLength - 1).getBytes());
	}

	/**
	 * Convert Long to bytes array (usage display) TODO: Handle EBCDIC
	 * 
	 * @param input
	 * @param signed
	 * @return
	 */
	private void convertLongToDisplay(long input, byte[] dest, int offset,
			boolean signed, boolean signLeading, boolean signSeparate) {
		String inputStr = String.valueOf(Math.abs(input));
		int byteLength = inputStr.length();
		if (byteLength > 18) {
			throw new ArithmeticException(
					"Length of long value is too long > 18");
		}
		ByteBuffer buffer;
		buffer = ByteBuffer.wrap(dest);
		byte signByte = (byte) 0xF0;
		if (signed) {
			if (input < 0) {
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
		buffer.put(inputStr.substring(0, byteLength - 1).getBytes());

	}

	/**
	 * Convert Long to bytes array (usage display) TODO: Handle EBCDIC
	 * 
	 * @param input
	 * @param signed
	 * @return
	 */
	private void convertBigDecimalToDisplay(BigDecimal input, byte[] dest,
			int offset, boolean signed, boolean signLeading, boolean signSeparate) {
		input = input.scaleByPowerOfTen(input.scale());
		long longVal = input.longValue();
		convertLongToDisplay(longVal, dest, offset, signed, signLeading, signSeparate);
	}

	private void convertStringToDisplay(String input, byte[] dest, int offset) {
		Charset ascii = Charset.forName("US-ASCII");
		byte[] asciiArray = input.getBytes(ascii);
		ByteBuffer buffer = ByteBuffer.wrap(dest, offset, asciiArray.length);
		if (offset + asciiArray.length >= dest.length) {
			buffer.put(asciiArray, 0, dest.length - 1 - offset);
		} else {
			buffer.put(asciiArray);
		}

		System.out.println(printByteArray(asciiArray));

	}

	private String printByteArray(byte[] input) {
		String result = "";
		for (byte b : input) {
			result += Integer.toString((b & 0xff) + 0x100, 16).substring(1);

		}
		return result;
	}

	public static void main(String[] args) {
		BaseClass test = new BaseClass(1);
		long testLong = test.adjustIntegralValue(888000, 3, true, 3);
		System.out.println(testLong);
//		long i = new BaseClass(1).getAlgebraicValue(123456789, 3, false, 3);
		 long i = new BaseClass(1).adjustDecimalValue(new
		 BigDecimal("12345.67891"), 0, 3, 3, false);
//		 BigDecimal i = new BaseClass(1).getAlgebraicValue(new
//		 BigDecimal("12345.6789123456"), 0, 3, 2, false);

		System.out.println(i);
	}
}
