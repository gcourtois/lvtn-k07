package com.res.java.lib;

/**
 * TODO: Unsigned Integer/Long
 * TODO: Convert EBCDIC --> ASCII or EBCDIC --> number
 * -odir "/home/jupiter/workspaceLVTN/MyRES/src/"  -dp "com.res.generated" -pp "com.res.generated" -dp1 -opt0 "/home/jupiter/workspaceLVTN/MyRES/src/com/res/betatest/Test1.cbl"
 */

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.res.java.lib.exceptions.OverflowException;

public class BaseClass {
	public byte[] data;

	protected int offset;

	protected int length;

	public BaseClass() {
	}
	
	public BaseClass(int size) {
		data = new byte[size];
		offset = 0;
		length = size;
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
	 * Get BCD Value Remember to calculate length of element int and long. Cast
	 * is not right if value > Int.MAX_VALUE
	 * 
	 * @param offset
	 * @param length
	 * @param scale
	 * @param signed
	 * @return
	 */
	protected BigDecimal getBigDecimalBCD(int offset, int length,
			boolean signed, int intLength, int fractionLength, int pscale) {
		long longValue = convertBCDToLong(offset, length, signed);
		System.out.println(longValue);
		longValue = adjustIntegralValue(longValue, intLength + fractionLength, signed, pscale);
		BigDecimal returnValue = new BigDecimal(longValue);
		return doPscaling(returnValue, fractionLength + pscale);
	}

	protected long getLongBCD(int offset, int length, boolean signed,
			int intLength, int pscale) {
		if (length > 10) {
			throw new ArithmeticException(
					"Bytes array is too long for Long type");
		}
		long tempValue = convertBCDToLong(offset, length, signed);
		tempValue = adjustIntegralValue(tempValue, intLength, signed, pscale);
		return tempValue;
	}

	protected void setLongBCD(long input, int offset, int length,
			boolean signed, int intLength, int pscale) {
		long tempValue = adjustIntegralValue(input, intLength, signed, pscale);
		convertLongToBCD(tempValue, offset, length, signed);

	}

	protected void setBigDecimalBCD(BigDecimal input, int offset, int length,
			boolean signed, int intLength, int fractionLength, int pscale) {
		long longVal = adjustDecimalValue(input, intLength, fractionLength,
				pscale, signed);
		convertLongToBCD(longVal, offset, length, signed);
	}

	protected long getLongDisplay(int offset, int length, boolean signed,
			boolean signLeading, boolean signSeparate, int pscale) {
		long tempValue = convertDisplayToLong(offset, length, signed,
				signLeading, signSeparate);
		if (pscale > 0) {
			return doPscaling(tempValue, pscale);
		}
		return tempValue;
	}

	protected BigDecimal getBigDecimalDisplay(int offset, int length,
			boolean signed, boolean signLeading, boolean signSeparate, int scale) {
		long tempValue = convertDisplayToLong(offset, length, signed,
				signLeading, signSeparate);
		BigDecimal returnValue = new BigDecimal(tempValue);
		if (scale > 0) {
			return doPscaling(returnValue, scale);
		}
		return returnValue;
	}

	protected String getStringDisplay(int offset, int length) {
		return convertDisplayToString(offset, length);
	}

	protected void setLongDisplay(long input, int offset, int length,
			boolean signed, boolean signLeading, boolean signSeparate,
			int intLength, int pscale) {
		long tempValue = adjustIntegralValue(input, intLength, signed, pscale);
		convertLongToDisplay(tempValue, offset, length, signed, signLeading,
				signSeparate);
	}

	protected void setBigDecimalDisplay(BigDecimal input, int offset,
			int length, boolean signed, boolean signLeading,
			boolean signSeparate, int intLength, int fractionLength, int pscale) {
		long tempValue = adjustDecimalValue(input, intLength, fractionLength,
				pscale, signed);
		convertLongToDisplay(tempValue, offset, length, signed, signLeading,
				signSeparate);

	}
	
	protected String getDisplayString(int offset, int length) {
		return convertDisplayToString(offset, length); 
	}
	
	protected void setStringDisplay(String input, int offset, int length,
			boolean rightJustified) {
		convertStringToDisplay(input, offset, length, rightJustified);
	}

	protected long getLongBytes(int offset, int length, boolean signed,
			int intLength, int pscale) {
		if (length > 8) {
			throw new ArithmeticException(
					"Bytes array is too long for Long type");
		}
		long tempValue = convertBytesToLong(offset, length, signed);
		tempValue = adjustIntegralValue(tempValue, intLength, signed, pscale);
		if (pscale > 0) {
			return doPscaling(tempValue, pscale);
		}
		return tempValue;
	}

	protected BigDecimal getBigDecimalBytes(int offset, int length,
			boolean signed, int intLength, int fractionLength, int pscale) {
		if (length > 8) {
			throw new ArithmeticException(
					"Bytes array is too long for BigDec type");
		}
		long longValue = convertBytesToLong(offset, length, signed);
		longValue = adjustIntegralValue(longValue, intLength + fractionLength, signed, pscale);
		BigDecimal returnValue = new BigDecimal(longValue);
		return doPscaling(returnValue, fractionLength + pscale);
	}

	protected void setLongBytes(long input, int offset, int length,
			boolean signed, int intLength, int pscale) {
		long tempValue = adjustIntegralValue(input, intLength, signed, pscale);
		convertLongToBytes(tempValue, offset, length, signed);
	}

	protected void setBigDecimalBytes(BigDecimal input, int offset, int length,
			boolean signed, int intLength, int fractionLength, int pscale) {
		long tempValue = adjustDecimalValue(input, intLength, fractionLength,
				pscale, signed);
		convertLongToBytes(tempValue, offset, length, signed);
	}

	private static long[] powerBase10 = new long[] { 1, 10, 100, 1000, 10000,
			100000, 1000000, 10000000, 100000000, 1000000000, 10000000000L,
			100000000000L, 1000000000000L, 10000000000000L, 100000000000000L,
			1000000000000000L, 10000000000000000L, 100000000000000000L,
			1000000000000000000L };

	protected long adjustIntegralValue(long input, int intLength,
			boolean signed, int pscale) {
		if (intLength > 18) {
			throw new ArithmeticException("Size is larger than 18");
		}
		// pscale must >= 0
		if (pscale == 0) {
			input %= powerBase10[intLength];
		} else {
			input %= powerBase10[intLength + pscale];
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

	protected long getAlgebraicValue(long input, int intLength, boolean signed,
			int pscale) {
		if (pscale == 0) {
			input %= powerBase10[intLength];
		} else {
			input %= powerBase10[intLength + pscale];
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

	protected String getStringValue(String input, int length, boolean rightJustified) {
		if (input.length() == length) {
			return input;
		} else if (input.length() > length) {
			if (rightJustified) {
				return input.substring(input.length() - length, input.length());
			} else {
				return input.substring(0, length);
			}
		} else {
			StringBuilder sb = new StringBuilder();
			if (rightJustified) {
				int spaces = length - input.length();
				while(sb.length() < spaces) {
					sb.append(' ');
				}
				sb.append(input);
			} else {
				sb.append(input);
				while (sb.length() < length) {
					sb.append(' ');
				}
			}
			return sb.toString();
		}
	}

	private long doPscaling(long input, int pscale) {
		return input * powerBase10[pscale];
	}

	private BigDecimal doPscaling(BigDecimal input, int scale) {
		return input.scaleByPowerOfTen(-scale);
	}


	private long convertBCDToLong(int offset, int length, boolean signed) {
//		System.out.println("PREV " + this.printByteArray(data));
		boolean negate = false;
		long result = 0;
		for (int index = offset; index < offset + length; index++) {
			byte eachByte = data[index];
			int firstDigit = (eachByte >> 4) & 0x0f;
			int secondDigit = eachByte & 0x0f;
			if (index == (length - 1)) {
				if (signed) {
					if (secondDigit == 0xd) {
						negate = true;
					} else if (secondDigit == 0xf) {
						negate = false;
					} else {
						//TODO: Don't Know
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

	/**
	 * Convert long to BCD format
	 * 
	 * @param input
	 * @param signed
	 * @return
	 */
	private void convertLongToBCD(long input, int offset, int length,
			boolean signed) {
		ByteBuffer buffer;
		int byteLength = length;
		fillWithZero(data, offset, length, false);
		buffer = ByteBuffer.wrap(data);
		
		byte signByte = 0x0F;
		if (signed) {
			if (input < 0) {
				signByte = 0x0D;
			} else {
				signByte = 0x0F;
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

	private void convertLongToBytes(long input, int offset, int length,
			boolean signed) {
		fillWithZero(data, offset, length, false);
		ByteBuffer buffer = ByteBuffer.wrap(data);

		if (!signed && input < 0) {
			input = Math.abs(input);
		}
		buffer.position(offset);
		if (length <=2 ) {
			buffer.putShort(0, (short) input);
		} else if (length >= 4) {
			buffer.putLong(0, input);
		} else {
			buffer.putInt(0, (int) input);
		}
		
	}

	/**
	 * Convert bytes array to Long TODO: Unsigned Long
	 * 
	 * @param offset
	 * @param length
	 * @param signed
	 * @return
	 */
	private long convertBytesToLong(int offset, int length, boolean signed) {
		ByteBuffer temp = ByteBuffer.wrap(data, offset, length);
		if (length > 8) {
			throw new ArithmeticException("Length for Long conversion is too large (>8 bytes)");
		}
		System.out.println("Bytes " + this.printByteArray(temp.array()));
		long result = 0;
		if (length <=2 ) {
			result = temp.getShort();
		} else if (length >= 4) {
			result = temp.getLong();
		} else {
			result = temp.getInt();
		}

		if (signed) {
			if (result >= powerBase10[18]) {
				throw new OverflowException("Overflow Long Conversion");
			}
		} else {
			if (result >= powerBase10[18] || result < 0) {
				throw new OverflowException("UnSigned Long is not correct");
			}
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
	 * Convert Bytes to Long usage Display TODO:Handle EBCDIC
	 * 
	 * @param signed
	 * @return
	 */
	private long convertDisplayToLong(int offset, int length, boolean signed,
			boolean signLeading, boolean signSeparate) {
		long result = 0;
		byte[] tempArray = new byte[length];
		String displayString = "";
		System.arraycopy(data, offset, tempArray, 0, length);
//		System.out.println("PREV " + this.printByteArray(tempArray));
		if (length > 18) {
			throw new OverflowException(
					"Length of Bytes array is too Long > 18");
		}
		if (RunConfig.getInstance().isEbcdicMachine()) {
		}
		boolean negate = false;
		if (signed) {
			if (signLeading) {
				if (signSeparate) {
					if (tempArray[0] == TranslateConstants.asciiNegative) {
						negate = true;
					} else if (tempArray[0] == TranslateConstants.asciiPositive) {
						negate = false;
					} else {
						// TODO: Don't know what to do.
						throw new ArithmeticException(
								"Sign Separate character is not right");
					}
					displayString = new String(tempArray, 1, length - 1);
				} else {
					// TODO: just ASCII for sign.
					int signValue = (tempArray[0] >> 4) & 0x0f;
					if (signValue == 0x7) {
						negate = true;
					} else if (signValue == 0x3) {
						negate = false;
					} else {
						throw new ArithmeticException(
								"Sign byte character is not right");
					}
					byte lastByte = tempArray[0];
					lastByte = (byte) (lastByte & 0x0f);
					lastByte = (byte) (lastByte + 0x30);
					tempArray[0] = lastByte;
					displayString = new String(tempArray);
				}

			} else {
				if (signSeparate) {
					if (tempArray[length - 1] == TranslateConstants.asciiNegative) {
						negate = true;
					} else if (tempArray[length - 1] == TranslateConstants.asciiPositive) {
						negate = false;
					} else {
						// TODO: Don't know what to do.
						throw new ArithmeticException(
								"Sign Separate character is not right");
					}
					tempArray[length - 1] = 0x00;
					displayString = new String(tempArray, 0, length - 1);
				} else {
					int signValue = (tempArray[length - 1] >> 4) & 0x0f;
					if (signValue == 0x7) {
						negate = true;
					} else if (signValue == 0x3) {
						negate = false;
					} else {
						throw new ArithmeticException(
								"Sign byte character is not right");
					}
					byte lastByte = tempArray[length - 1];
					lastByte = (byte) (lastByte & 0x0f);
					lastByte = (byte) (lastByte + 0x30);
					tempArray[length - 1] = lastByte;
					displayString = new String(tempArray);
				}

			}
		} else {
			displayString = new String(tempArray);
		}

		try {
//			System.out.println("After " + this.printByteArray(tempArray));
			displayString = displayString.trim();
			if (displayString.length() == 0) {
				result = 0;
			} else {
				result = Long.valueOf(displayString);
			}
			
		} catch (Exception e) {
			throw new ArithmeticException(
					"Convert Bytes (Display) to Long failed " + e);
		}
		if (negate && signed) {
			result = -result;
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
		Charset ascii = Charset.forName("US-ASCII");
		byte[] tempArray = new byte[length]; 
		System.arraycopy(data, offset, tempArray, 0, length);
		for (int i = 0; i < length; i++) {
			if (tempArray[i] == 0x00) {
				tempArray[i] = 0x20;
			} 
		}
		return new String(tempArray, ascii);
	}

	/**
	 * Convert Long to bytes array (usage display) TODO: Handle EBCDIC
	 * 
	 * @param input
	 * @param signed
	 * @return
	 */
	private void convertLongToDisplay(long input, int offset, int length,
			boolean signed, boolean signLeading, boolean signSeparate) {
		String inputStr = String.valueOf(Math.abs(input));
		int byteLength = inputStr.length();
		if (byteLength > 18) {
			throw new ArithmeticException(
					"Length of long value is too long > 18");
		}
		fillWithZero(data, offset, length, true);
		ByteBuffer buffer;
		buffer = ByteBuffer.wrap(data);
		// TODO: if EBCDIC
		if (input == 0) {
			//TODO: if blank when zero --> fill with space
			//fillWithSpace(data, offset, length);
		}
		byte signByte = (byte) 0x30;
		if (signed) {
			if (signSeparate) {
				if (input < 0) {
					signByte = TranslateConstants.asciiNegative;
				} else {
					signByte = TranslateConstants.asciiPositive;
				}
				if (signLeading) {
					buffer.position(offset);
					buffer.put(signByte);
					buffer.position(offset + length - byteLength);
				} else {
					buffer.position(offset + length - 1);
					buffer.put(signByte);
					buffer.position(offset + length - 1 - byteLength);
				}
				if (input != 0) {
					buffer.put(inputStr.getBytes());
				}
			} else {
				if (input < 0) {
					signByte = (byte) 0x70;
				} else {
					signByte = (byte) 0x30;
				}
				input = Math.abs(input);
				if (signLeading) {
					buffer.position(offset + length - byteLength);
					if (input != 0) {
						buffer.put(inputStr.getBytes());
					}
					int firstDigit = 0;
					if (byteLength == length) {
						firstDigit = (int) (input / powerBase10[byteLength - 1]);
					}
					signByte = (byte) ((signByte | (firstDigit)) & 0xFF);
					buffer.position(offset);
					buffer.put(signByte);

				} else {
					int lastDigit = (int) (input % 10);
					signByte = (byte) ((signByte | (lastDigit)) & 0xFF);
					buffer.position(offset + length - 1);
					buffer.put(signByte);
					buffer.position(offset + length - byteLength);
					if (input != 0) {
						buffer.put(inputStr.substring(0, byteLength - 1).getBytes());
					}
					
				}
			}
		} else {
			buffer.position(offset + length - byteLength);
			buffer.put(inputStr.getBytes());
		}

	}


	private void convertStringToDisplay(String input, int offset, int length, boolean rightJustified) {
		Charset ascii = Charset.forName("US-ASCII");
		int inputLength = input.length();
		fillWithSpace(data, offset, length);
		ByteBuffer buffer = ByteBuffer.wrap(data, offset, length);
		if (rightJustified) {
			if (inputLength > length) {
				byte[] asciiArray = input.substring(inputLength - length).getBytes(ascii);
				buffer.put(asciiArray);
			} else {
				buffer.position(offset + length - inputLength);
				byte[] asciiArray = input.getBytes(ascii);
				buffer.put(asciiArray);
			}
		} else {
			if (inputLength > length) {
				byte[] asciiArray = input.substring(0, length - 1).getBytes(ascii);
				buffer.put(asciiArray);
			} else {
				byte[] asciiArray = input.getBytes(ascii);
				buffer.put(asciiArray);
			}
		}
	}

	/**
	 * TODO: EBCDIC
	 * 
	 * @param input
	 * @param offset
	 * @param length
	 */
	private void fillWithZero(byte[] input, int offset, int length, boolean display) {
		ByteBuffer buffer = ByteBuffer.wrap(input, offset, length);
		for (int i = 0; i < length; i++) {
			if (display) {
				buffer.put(TranslateConstants.asciiZero);
			} else {
				buffer.put((byte) 0);
			}
		}
	}

	/**
	 * TODO: EBCDIC ??
	 * 
	 * @param input
	 * @param offset
	 * @param length
	 */
	private void fillWithSpace(byte[] input, int offset, int length) {
		ByteBuffer buffer = ByteBuffer.wrap(input, offset, length);
		for (int i = 0; i < length; i++) {
			buffer.put(TranslateConstants.asciiSpace);
		}
	}

	public String printByteArray(byte[] input) {
		String result = "";
		for (byte b : input) {
			result += Integer.toString((b & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}
	
	public String toString() {
		return this.convertDisplayToString(this.offset, this.length);
	}
	
	private BigDecimal getDecimalValue(int offset, int length, EditedVar numericEditedVar) {
		StringBuilder stringValue = new StringBuilder(convertDisplayToString(offset, length));
		String picString = numericEditedVar.getNormalizedPic();
		String beforeDecimal = numericEditedVar.getBeforeDecimal();
		if (beforeDecimal.length() < picString.length()) {
			int decimalOffset = picString.length() - beforeDecimal.length();
			if (picString.indexOf('V') != -1) {
				stringValue.insert(decimalOffset, '.');
			}
		}
		BigDecimal returnVal = BigDecimal.ZERO;
		try {
			returnVal = new BigDecimal(stringValue.toString().replaceAll("[$\\,/]", ""));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} 
		return returnVal;
	}

}