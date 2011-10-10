package com.res.java.lib;

/**
 * TODO: Unsigned Integer/Long
 * TODO: Convert EBCDIC --> ASCII or EBCDIC --> number
 * -odir "/home/jupiter/workspaceLVTN/MyRES/src/"  -dp "com.res.generated" -pp "com.res.generated" -dp1 -opt0 "/home/jupiter/workspaceLVTN/MyRES/src/com/res/betatest/Test1.cbl"
 */

import java.math.BigDecimal;
import java.nio.ByteBuffer;

import com.res.java.lib.exceptions.InvalidDataFormatException;
import com.res.java.lib.exceptions.OverflowException;

public class BaseClass {
	public byte[] data;

	protected int offset;

	protected int length;
	private ByteBuffer buffer;
	//private static byte[] temp = new byte[100];
	public BaseClass() {
	}
	
	public BaseClass(int size) {
		data = new byte[size];
		buffer = ByteBuffer.wrap(data);
		offset = 0;
		length = size;
	}

	public BaseClass(byte[] data, int offset, int length) {
		this.data = data;
		buffer = ByteBuffer.wrap(data);
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
			boolean signed, int intLength, int fractionLength, int pscale) throws InvalidDataFormatException {
		long longValue = convertBCDToLong(offset, length, signed);
//		System.out.println(longValue);
		longValue = adjustIntegralValue(longValue, intLength + fractionLength, signed, pscale);
		BigDecimal returnValue = new BigDecimal(longValue);
		return doPscaling(returnValue, fractionLength + pscale);
	}
	
	protected int getIntBCD(int offset, int length, boolean signed,
			int intLength, int pscale) throws InvalidDataFormatException {
		if (length > 5) {
			throw new ArithmeticException(
					"Bytes array is too long for Int type");
		}
		int tempValue = convertBCDToInt(offset, length, signed);
		tempValue = adjustIntegralValue(tempValue, intLength, signed, pscale);
		return tempValue;
	}
	
	protected long getLongBCD(int offset, int length, boolean signed,
			int intLength, int pscale) throws InvalidDataFormatException {
		if (length > 10) {
			throw new ArithmeticException(
					"Bytes array is too long for Long type");
		}
		long tempValue = convertBCDToLong(offset, length, signed);
		tempValue = adjustIntegralValue(tempValue, intLength, signed, pscale);
		return tempValue;
	}
	
	protected void setIntBCD(int input, int offset, int length,
			boolean signed, int intLength, int pscale) {
		int tempValue = adjustIntegralValue(input, intLength, signed, pscale);
		convertIntToBCD(tempValue, offset, length, signed);
	}
	
	protected void setIntBCD(BigDecimal input, int offset, int length,
            boolean signed, int intLength, int pscale) {
	    setIntBCD(input.intValue(), offset, length, signed, intLength, pscale);
	}
	
	protected void setIntBCD(String input, int offset, int length,
            boolean signed, int intLength, int pscale) {
	    try {
	        setIntBCD(literalToInt(input), offset, length, signed, intLength, pscale);
	    } catch (NumberFormatException e) {
	        setStringDisplay(input, offset, length, false);
	    }
	}
	
	protected void setLongBCD(long input, int offset, int length,
			boolean signed, int intLength, int pscale) {
		long tempValue = adjustIntegralValue(input, intLength, signed, pscale);
		convertLongToBCD(tempValue, offset, length, signed);

	}

	protected void setLongBCD(BigDecimal input, int offset, int length,
            boolean signed, int intLength, int pscale) {
	    setLongBCD(input.longValue(), offset, length, signed, intLength, pscale);
	}
	
	protected void setLongBCD(String input, int offset, int length,
            boolean signed, int intLength, int pscale) {
	    try {
	        setLongBCD(literalToLong(input), offset, length, signed, intLength, pscale);
	    } catch (NumberFormatException e) {
	        setStringDisplay(input, offset, length, false);
	    }
	}
	
	protected void setBigDecimalBCD(BigDecimal input, int offset, int length,
			boolean signed, int intLength, int fractionLength, int pscale) {
		long longVal = adjustDecimalValue(input, intLength, fractionLength,
				pscale, signed);
		convertLongToBCD(longVal, offset, length, signed);
	}
	
	protected void setBigDecimalBCD(long input, int offset, int length,
            boolean signed, int intLength, int fractionLength, int pscale) {
	    setBigDecimalBCD(BigDecimal.valueOf(input), offset, length, signed, intLength, fractionLength, pscale);
	}

	protected void setBigDecimalBCD(String input, int offset, int length,
	        boolean signed, int intLength, int fractionLength, int pscale) {
	    try {
	        setBigDecimalBCD(literalToBigDecimal(input), offset, length, signed, intLength, fractionLength, pscale);
	    } catch (NumberFormatException e) {
	        setStringDisplay(input, offset, length, false);
	    }
	}
	
	
	
	protected int getIntDisplay(int offset, int length, boolean signed,
			boolean signLeading, boolean signSeparate, int pscale) throws InvalidDataFormatException {
		int tempValue = convertDisplayToInt(offset, length, signed, signLeading, signSeparate);
		if (pscale > 0) {
			return doPscaling(tempValue, pscale);
		}
		return tempValue;
	}
	
	protected long getLongDisplay(int offset, int length, boolean signed,
			boolean signLeading, boolean signSeparate, int pscale) throws InvalidDataFormatException {
		long tempValue = convertDisplayToLong(offset, length, signed,
				signLeading, signSeparate);
		if (pscale > 0) {
			return doPscaling(tempValue, pscale);
		}
		return tempValue;
	}

	protected BigDecimal getBigDecimalDisplay(int offset, int length,
			boolean signed, boolean signLeading, boolean signSeparate, int scale) throws InvalidDataFormatException {
		long tempValue = convertDisplayToLong(offset, length, signed,
				signLeading, signSeparate);
		BigDecimal returnValue = new BigDecimal(tempValue);
		if (scale > 0) {
			return doPscaling(returnValue, scale);
		}
		return returnValue;
	}
	
	protected void setIntDisplay(int input, int offset, int length,
			boolean signed, boolean signLeading, boolean signSeparate,
			int intLength, int pscale) {
		int tempValue = (int) adjustIntegralValue(input, intLength, signed, pscale);
		convertIntToDisplay(tempValue, offset, length, signed, signLeading,
				signSeparate);
	}
	
	protected void setIntDisplay(BigDecimal input, int offset, int length,
            boolean signed, boolean signLeading, boolean signSeparate,
            int intLength, int pscale) {
	    setIntDisplay(input.intValue(), offset, length, signed, signLeading, signSeparate, intLength, pscale);
	}
	
	protected void setIntDisplay(String input, int offset, int length,
            boolean signed, boolean signLeading, boolean signSeparate,
            int intLength, int pscale) {
	    try {
	        setIntDisplay(literalToInt(input), offset, length, signed, signLeading, signSeparate, intLength, pscale);
	    } catch (NumberFormatException e) {
	        setStringDisplay(input, offset, length, false);
	    }
	}
	
	protected void setLongDisplay(long input, int offset, int length,
			boolean signed, boolean signLeading, boolean signSeparate,
			int intLength, int pscale) {
		long tempValue = adjustIntegralValue(input, intLength, signed, pscale);
		convertLongToDisplay(tempValue, offset, length, signed, signLeading,
				signSeparate);
	}

	protected void setLongDisplay(BigDecimal input, int offset, int length,
            boolean signed, boolean signLeading, boolean signSeparate,
            int intLength, int pscale) {
	    setLongDisplay(input.longValue(), offset, length, signed, signLeading, signSeparate, intLength, pscale);
	}
	
	protected void setLongDisplay(String input, int offset, int length,
            boolean signed, boolean signLeading, boolean signSeparate,
            int intLength, int pscale) {
	    try {
	        setLongDisplay(literalToLong(input), offset, length, signed, signLeading, signSeparate, intLength, pscale);
	    } catch (NumberFormatException e) {
	        setStringDisplay(input, offset, length, false);
	    }
	}
	
	protected void setBigDecimalDisplay(BigDecimal input, int offset,
			int length, boolean signed, boolean signLeading,
			boolean signSeparate, int intLength, int fractionLength, int pscale) {
//		long tempValue = adjustDecimalValue(input, intLength, fractionLength,
//				pscale, signed);
//		convertLongToDisplay(tempValue, offset, length, signed, signLeading,
//				signSeparate);

	}
	
	protected void setBigDecimalDisplay(long input, int offset,
            int length, boolean signed, boolean signLeading,
            boolean signSeparate, int intLength, int fractionLength, int pscale) {
	    setBigDecimalDisplay(BigDecimal.valueOf(input), offset, length, signed, signLeading, signSeparate, intLength, fractionLength, pscale);
	}
	
	protected void setBigDecimalDisplay(String input, int offset,
	        int length, boolean signed, boolean signLeading,
	        boolean signSeparate, int intLength, int fractionLength, int pscale) {
	    try {
	        setBigDecimalDisplay(literalToBigDecimal(input), offset, length, signed, signLeading, signSeparate, intLength, fractionLength, pscale);
	    } catch (NumberFormatException e) {
	        setStringDisplay(input, offset, length, false);
	    }
	}
	
	protected String getStringDisplay(int offset, int length) {
    	return convertDisplayToString(offset, length);
    }

    protected void setStringDisplay(String input, int offset, int length,
			boolean rightJustified) {
		convertStringToDisplay(input, offset, length, rightJustified);
	}
    
    protected String unsignedValue(long input) {
        if (input < 0) {
            input = -input;
        }
        return String.valueOf(input);
    }
    
    protected int getIntBytes(int offset, int length, boolean signed,
            int intLength, int pscale) throws InvalidDataFormatException {
        int tempValue = convertBytesToInt(offset, length, signed);
        tempValue = adjustIntegralValue(tempValue, intLength, signed, pscale);
        if (pscale > 0) {
            return doPscaling(tempValue, pscale);
        }
        return tempValue;
    }    
    
    protected long getLongBytes(int offset, int length, boolean signed,
			int intLength, int pscale) throws InvalidDataFormatException {
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
			boolean signed, int intLength, int fractionLength, int pscale) throws InvalidDataFormatException {
		if (length > 8) {
			throw new ArithmeticException(
					"Bytes array is too long for BigDec type");
		}
		long longValue = convertBytesToLong(offset, length, signed);
		longValue = adjustIntegralValue(longValue, intLength + fractionLength, signed, pscale);
		BigDecimal returnValue = new BigDecimal(longValue);
		return doPscaling(returnValue, fractionLength + pscale);
	}

	protected void setIntBytes(int input, int offset, int length,
	        boolean signed, int intLength, int pscale) {
	    int tempValue = adjustIntegralValue(input, intLength, signed, pscale);
	    convertIntToBytes(tempValue, offset, length, signed);
	}
	
	protected void setIntBytes(BigDecimal input, int offset, int length,
            boolean signed, int intLength, int pscale) {
	    setIntBytes(input.intValue(), offset, length, signed, intLength, pscale);
	}
	
	protected void setIntBytes(String input, int offset, int length,
            boolean signed, int intLength, int pscale) {
	    try {
	        setIntBytes(literalToInt(input), offset, length, signed, intLength, pscale);
	    } catch (NumberFormatException e) {
	        setStringDisplay(input, offset, length, false);
	    }
	}
	
	protected void setLongBytes(long input, int offset, int length,
			boolean signed, int intLength, int pscale) {
		long tempValue = adjustIntegralValue(input, intLength, signed, pscale);
		convertLongToBytes(tempValue, offset, length, signed);
	}
	
	protected void setLongBytes(BigDecimal input, int offset, int length,
            boolean signed, int intLength, int pscale) {
	    setLongBytes(input.longValue(), offset, length, signed, intLength, pscale);
	}

	protected void setLongBytes(String input, int offset, int length,
	        boolean signed, int intLength, int pscale) {
	    try {
	        setLongBytes(literalToLong(input), offset, length, signed, intLength, pscale);
	    } catch (NumberFormatException e) {
	        setStringDisplay(input, offset, length, false);
	    }
	}
	
	protected void setBigDecimalBytes(BigDecimal input, int offset, int length,
			boolean signed, int intLength, int fractionLength, int pscale) {
		long tempValue = adjustDecimalValue(input, intLength, fractionLength,
				pscale, signed);
		convertLongToBytes(tempValue, offset, length, signed);
	}

	protected void setBigDecimalBytes(long input, int offset, int length,
            boolean signed, int intLength, int fractionLength, int pscale) {
	    setBigDecimalBytes(BigDecimal.valueOf(input), offset, length, signed, intLength, fractionLength, pscale);
	}
	
	protected void setBigDecimalBytes(String input, int offset, int length,
            boolean signed, int intLength, int fractionLength, int pscale) {
	    try {
	        setBigDecimalBytes(literalToBigDecimal(input), offset, length, signed, intLength, fractionLength, pscale);
	    } catch (NumberFormatException e) {
	        setStringDisplay(input, offset, length, false);
	    }
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
	
	protected int adjustIntegralValue(int input, int intLength,
			boolean signed, int pscale) {
		if (intLength > 8) {
			throw new ArithmeticException("Size is larger than 18");
		}
		// pscale must >= 0
		if (pscale == 0) {
			input %= powerBase10[intLength];
		} else {
			input %= powerBase10[intLength + pscale];
			input = input / (int) powerBase10[pscale];
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

	private long literalToLong(String input) {
	    long tmp = Long.valueOf(input);
	    return tmp < 0 ? -tmp : tmp;
	}
	
	private int literalToInt(String input) {
	    int tmp = Integer.valueOf(input);
	    return tmp < 0 ? -tmp : tmp;
	}
	
	private BigDecimal literalToBigDecimal(String input) {
	    return new BigDecimal(input).abs();
	}
	
	private long doPscaling(long input, int pscale) {
		return input * powerBase10[pscale];
	}
	
	private int doPscaling(int input, int pscale) {
		return (int) (input * powerBase10[pscale]);
	}

	private BigDecimal doPscaling(BigDecimal input, int scale) {
		return input.scaleByPowerOfTen(-scale);
	}

	
	private int convertBCDToInt(int offset, int length, boolean signed) {
//		System.out.println("PREV " + this.printByteArray(data));
		boolean negate = false;
		int result = 0;
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
	
	private void convertIntToBCD(int input, int offset, int length,
			boolean signed) {
		int byteLength = length;
		byte[] temp = new byte[length];
		for (int i = 0; i < length; i++) {
			temp[i] = '0';
		}
		byte signByte = 0x0F;
		if (signed) {
			if (input < 0) {
				signByte = 0x0D;
				input = Math.abs(input);
			} else {
				signByte = 0x0F;
			}
		} 
		int lastDigit = input % 10;
		signByte = (byte) ((signByte | (lastDigit << 4)) & 0xFF);
		temp[byteLength + offset - 1] = signByte;
		input = input / 10;
		for (int index = 1; index < byteLength; index++, input /= 100) {
			temp[byteLength + offset - index - 1] = TranslateConstants.PACKED_DECIMALS[input % 100];
		}
		buffer.position(offset);
		buffer.put(temp, 0, length);
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
		int byteLength = length;
		byte[] temp = new byte[length];
		for (int i = 0; i < length; i++) {
			temp[i] = '0';
		}
		byte signByte = 0x0F;
		if (signed) {
			if (input < 0) {
				signByte = 0x0D;
				input = Math.abs(input);
			} else {
				signByte = 0x0F;
			}
		} 
		long lastDigit = input % 10;
		signByte = (byte) ((signByte | (lastDigit << 4)) & 0xFF);
		temp[byteLength + offset - 1] = signByte;
		input = input / 10;
		for (int index = 1; index < byteLength; index++, input /= 100) {
			temp[byteLength + offset - index - 1] = TranslateConstants.PACKED_DECIMALS[(int) (input % 100)];
		}
		buffer.position(offset);
		buffer.put(temp, 0, length);

	}

	private void convertLongToBytes(long input, int offset, int length,
			boolean signed) {
		fillWithZero(data, offset, length, false);
		buffer.position(offset);
		if (length == 8) {
		    buffer.putLong(input);
		} else if (length == 4) {
		    buffer.putInt((int) input);
		} else if (length == 2) {
		    buffer.putShort((short) input);
		}
	}

	private void convertIntToBytes(int input, int offset, int length, boolean signed) {
	    fillWithZero(data, offset, length, false);
	    buffer.position(offset);
	    if (length == 4) {
	        buffer.putInt(input);
	    } else if (length == 2) {
	        buffer.putShort((short) input);
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
		buffer.position(offset);
		if (length > 8) {
			throw new ArithmeticException("Length for Long conversion is too large (>8 bytes)");
		}
//		System.out.println("Bytes " + this.printByteArray(temp.array()));
		long result = 0;
		if (length == 8) {
		    result = buffer.getLong();
		} else if (length == 4) {
		    result = buffer.getInt();
		} else if (length == 2) {
		    result = buffer.getShort();
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

	private int convertBytesToInt(int offset, int length, boolean signed) {
	    buffer.position(offset);
	    if (length > 8) {
	        throw new ArithmeticException("Length for Long conversion is too large (>8 bytes)");
	    }
	    int result = 0;
	    if (length == 4) {
	        result = buffer.getInt();
	    } else if (length == 2) {
	        result = buffer.getShort();
	    }

	    /*if (signed) {
	        if (result >= powerBase10[18]) {
	            throw new OverflowException("Overflow Long Conversion");
	        }
	    } else {
	        if (result >= powerBase10[18] || result < 0) {
	            throw new OverflowException("UnSigned Long is not correct");
	        }
	    }*/
	    
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

	private int convertDisplayToInt(int offset, int length, boolean signed,
			boolean signLeading, boolean signSeparate) {
		int result = 0;
		byte[] tempArray = new byte[length];
		buffer.position(offset);
		buffer.get(tempArray, 0, length);
		if (length > 8) {
			throw new OverflowException(
					"Length of Bytes array is too Long > 8");
		}
		if (RunConfig.getInstance().isEbcdicMachine()) {
		}
		boolean negate = false;
		int start = 0;
		int end = 0;
		if (signed) {
			if (signLeading) {
				end = length - 1;
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
					start = 1;
				} else {
					// TODO: just ASCII for sign.
					int signIndex = 0;
					for (int i = 0; i < length; i++) {
						if (tempArray[i] != 0x30) {
							signIndex = i;
							break;
						}
					}
					int signValue = (tempArray[signIndex] >> 4) & 0x0f;
					if (signValue == 0x7) {
						negate = true;
					} else if (signValue == 0x3) {
						negate = false;
					} else {
						throw new ArithmeticException(
								"Sign byte character is not right");
					}
					byte lastByte = tempArray[signIndex];
					lastByte = (byte) (lastByte & 0x0f);
					lastByte = (byte) (lastByte + 0x30);
					tempArray[signIndex] = lastByte;
					start = signIndex;
				}
				
			} else {
				start = 0;
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
					end = length - 2;
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
					end = length - 1;
				}
				
			}
		} else {
			start = 0;
			end = length - 1;
		}
		boolean passNonZero = false;
		for (int i = start; i <= end; i++) {
			char c = (char) tempArray[i];
			if (c >= '0' && c <= '9') {
				if (passNonZero) {
					result = result*10 + (c - '0'); 
				} else {
					if (c != '0') {
						passNonZero = true;
						result = result*10 + (c - '0'); 
					}
				}
			} else {
				throw new ArithmeticException(
						"Convert Bytes (Display) to Long failed ");
			}
		}
		if (negate && signed) {
			result = -result;
		}
		return result;
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
		buffer.position(offset);
		buffer.get(tempArray, 0, length);
		if (length > 8) {
			throw new OverflowException(
					"Length of Bytes array is too Long > 8");
		}
		if (RunConfig.getInstance().isEbcdicMachine()) {
		}
		boolean negate = false;
		int start = 0;
		int end = 0;
		if (signed) {
			if (signLeading) {
				end = length - 1;
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
					start = 1;
				} else {
					// TODO: just ASCII for sign.
					int signIndex = 0;
					for (int i = 0; i < length; i++) {
						if (tempArray[i] != 0x30) {
							signIndex = i;
							break;
						}
					}
					int signValue = (tempArray[signIndex] >> 4) & 0x0f;
					if (signValue == 0x7) {
						negate = true;
					} else if (signValue == 0x3) {
						negate = false;
					} else {
						throw new ArithmeticException(
								"Sign byte character is not right");
					}
					byte lastByte = tempArray[signIndex];
					lastByte = (byte) (lastByte & 0x0f);
					lastByte = (byte) (lastByte + 0x30);
					tempArray[signIndex] = lastByte;
					start = signIndex;
				}
				
			} else {
				start = 0;
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
					end = length - 2;
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
					end = length - 1;
				}
				
			}
		} else {
			start = 0;
			end = length - 1;
		}
		boolean passNonZero = false;
		for (int i = start; i <= end; i++) {
			char c = (char) tempArray[i];
			if (c >= '0' && c <= '9') {
				if (passNonZero) {
					result = result*10 + (c - '0'); 
				} else {
					if (c != '0') {
						passNonZero = true;
						result = result*10 + (c - '0'); 
					}
				}
			} else {
				throw new ArithmeticException(
						"Convert Bytes (Display) to Long failed ");
			}
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
		//Charset ascii = Charset.forName("US-ASCII");
		byte[] tempArray = new byte[length];
		buffer.position(offset);
		buffer.get(tempArray, 0, length);
		String result = "";
		StringBuilder a = new StringBuilder(result);
		for (int i = 0; i < length; i++) {
			char c = (char) tempArray[i];
			if (tempArray[i] == 0x00) {
				a.append(" ");
			} else {
				a.append(c);
			}
		}
		return a.toString();
	}

	/**
	 * Convert Int to BytesArray in Display Usage.
	 * @param input
	 * @param offset
	 * @param length
	 * @param signed
	 * @param signLeading
	 * @param signSeparate
	 */
	private void convertIntToDisplay(int input, int offset, int length,
			boolean signed, boolean signLeading, boolean signSeparate) {
		byte[] temp = new byte[length];
		if (signed) {
			if (signLeading) {
				int i = length-1;
				int firstDigitIndex = -1;
				int data = Math.abs(input);
				while (i >= 0) {
					byte digit = (byte) ('0' + (data % 10));
					temp[i] = digit;
					data = data /10;
					if ((data == 0) && firstDigitIndex == -1) {
						firstDigitIndex = i;
					}
					i--;
				}
				
				if (signSeparate) {
					int signIndex = 0;
					if (input < 0) {
						temp[signIndex] = TranslateConstants.asciiNegative;
					} else {
						temp[signIndex] = TranslateConstants.asciiPositive;
					}
				} else {
					int signIndex = firstDigitIndex;
					if (input < 0) {
						temp[signIndex] = (byte) ((byte)(temp[signIndex] & 0x0F) | 0x70);
					} else {
						temp[signIndex] = (byte) ((byte)(temp[signIndex] & 0x0F)  | 0x30);
					}
				}
			} else {
				int data = Math.abs(input);
				int i = length-1;
				if (signSeparate) {
					if (input < 0) {
						temp[length - 1] = TranslateConstants.asciiNegative;
					} else {
						temp[length - 1] = TranslateConstants.asciiPositive;
					}
					i = length - 2;
				} 
				while (i >= 0) {
					byte digit = (byte) ('0' + (data % 10));
					temp[i] = digit;
					data = data /10;
					i--;
				}
				if (!signSeparate) {
					if (input < 0) {
						temp[length - 1] = (byte) ((byte) (temp[length - 1] & 0x0F) | 0x70);
					} else {
						temp[length - 1] = (byte) ((byte) (temp[length - 1] & 0x0F) | 0x30);
					}
					 
				}
				
			}
		} else {
			int i = length-1;
			int data = Math.abs(input);
			while (i >= 0) {
				byte digit = (byte) ('0' + (data % 10));
				temp[i] = digit;
				data = data /10;
				i--;
			}
		}
		buffer.position(offset);
		buffer.put(temp, 0, length);

	}
	
	
	/**
	 * Convert Long to bytes array in Display Usage.
	 * @param input
	 * @param offset
	 * @param length
	 * @param signed
	 * @param signLeading
	 * @param signSeparate
	 */
	private void convertLongToDisplay(long input, int offset, int length,
			boolean signed, boolean signLeading, boolean signSeparate) {
		byte[] temp = new byte[length];
		if (signed) {
			if (signLeading) {
				int i = length-1;
				int firstDigitIndex = -1;
				long data = Math.abs(input);
				while (i >= 0) {
					byte digit = (byte) ('0' + (data % 10));
					temp[i] = digit;
					data = data /10;
					if ((data == 0) && firstDigitIndex == -1) {
						firstDigitIndex = i;
					}
					i--;
				}
				
				if (signSeparate) {
					int signIndex = 0;
					if (input < 0) {
						temp[signIndex] = TranslateConstants.asciiNegative;
					} else {
						temp[signIndex] = TranslateConstants.asciiPositive;
					}
				} else {
					int signIndex = firstDigitIndex;
					if (input < 0) {
						temp[signIndex] = (byte) ((byte)(temp[signIndex] & 0x0F) | 0xD0);
					} else {
						temp[signIndex] = (byte) ((byte)(temp[signIndex] & 0x0F)  | 0xC0);
					}
				}
			} else {
				long data = Math.abs(input);
				int i = length-1;
				if (signSeparate) {
					if (input < 0) {
						temp[length - 1] = TranslateConstants.asciiNegative;
					} else {
						temp[length - 1] = TranslateConstants.asciiPositive;
					}
					i = length - 2;
				} 
				while (i >= 0) {
					byte digit = (byte) ('0' + (data % 10));
					temp[i] = digit;
					data = data /10;
					i--;
				}
				if (!signSeparate) {
					if (input < 0) {
						temp[length - 1] = (byte) ((byte) (temp[length - 1] & 0x0F) | 0xD0);
					} else {
						temp[length - 1] = (byte) ((byte) (temp[length - 1] & 0x0F) | 0xC0);
					}
					 
				}
				
			}
		} else {
			int i = length-1;
			long data = Math.abs(input);
			while (i >= 0) {
				byte digit = (byte) ('0' + (data % 10));
				temp[i] = digit;
				data = data /10;
				i--;
			}
		}
		buffer.position(offset);
		buffer.put(temp, 0, length);

	}
	/**
	 * Convert String to bytes array display usage.
	 * @param input
	 * @param offset
	 * @param length
	 * @param rightJustified
	 */
	private void convertStringToDisplay(String input, int offset, int length, boolean rightJustified) {
		byte[] temp = new byte[length];
		fillWithSpace(temp, 0, length);
		int inputLength = input.length();
		int start = 0;
		int end = 0;
		if (rightJustified) {
			start = length - 1;
			if (inputLength > length) {
				end = 0;
			} else {
				end = length - inputLength;
			}
			for (int i = start; i >= end; i--) {
				char c = input.charAt(i - length + inputLength);
				temp[i] = (byte) c;
			}
		} else {
			start = 0;
			if (inputLength > length) {
				end = length;
			} else {
				end = inputLength;
			}
			for (int i = start; i < end; i++) {
				char c = input.charAt(i);
				temp[i] = (byte) c;
			}
		}
		buffer.position(offset);
		buffer.put(temp, 0, length);
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
		for (int i = 0; i < length; i++) {
			input[i] = TranslateConstants.asciiSpace;
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
	
	public BigDecimal getDecimalValue(int offset, int length, EditedVar numericEditedVar) {
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

	protected class BigDecimalField {
	    protected int offset;
	    protected int length;
	    protected int usage;
	    protected int intLength;
	    protected int fractionLength;
	    protected int scaleLength;
	    protected boolean isSigned;
	    protected boolean isSignLeading;
	    protected boolean isSignSeparate;
	    protected boolean isNum = false;
	    protected String outputFormat;
	    
	    public BigDecimalField(int offset, int length, int usage, int intLength, int fractionLength, int scaleLength, boolean isSigned, boolean isSignLeading, boolean isSignSeparate) {
	        this.offset = offset;
	        this.length = length;
	        this.usage = usage;
	        this.intLength = intLength;
	        this.fractionLength = fractionLength;
	        this.scaleLength = scaleLength;
	        this.isSigned = isSigned;
	        this.isSignLeading = isSignLeading;
	        this.isSignSeparate = isSignSeparate;
	        
	        // plus 1 for decimal point character '.'
	        int len = intLength + fractionLength + scaleLength + 1;
	        if (isSigned)
	            len++;
	        outputFormat = String.format("%%%s0%s.%sf", isSigned ? "+" : "", len, fractionLength + scaleLength);
	    }
	    
	    private BigDecimal value = BigDecimal.ZERO;
	    
	    public void setValue(long input) {
	        setValue(BigDecimal.valueOf(input));
	    }
	    
	    public void setValue(BigDecimal input) {
	        this.value = getAlgebraicValue(input, intLength, fractionLength, scaleLength, isSigned);
	        isNum = true;
	    }
	    
	    public void setValue(String input) {
	        try {
	            BigDecimal tmp = new BigDecimal(input);
	            setValue(tmp.abs());
	        } catch (NumberFormatException e) {
	            setStringDisplay(input, offset, length, false);
	            isNum = false;
	        }
	    }
	    
	    public void setCurrentValueToBytes() {
	        if (isNum) {
	            switch (usage) {
	            case Constants.DISPLAY:
	                setBigDecimalDisplay(this.value, offset, length, isSigned, isSignLeading, isSignSeparate, intLength, fractionLength, scaleLength);
	                break;
	            case Constants.BINARY:
	                setBigDecimalBytes(this.value, offset, length, isSigned, intLength, fractionLength, scaleLength);
	                break;
	            case Constants.PACKED_DECIMAL:
	                setBigDecimalBCD(this.value, offset, length, isSigned, intLength, fractionLength, scaleLength);
	                break;
	            }
	        }
	    }
	    
	    public void getCurrentValueFromBytes() {
	        try {
	            switch (usage) {
	            case Constants.DISPLAY:
	                this.value = getBigDecimalDisplay(offset, length, isSigned, isSignLeading, isSignSeparate, scaleLength);
	                break;
	            case Constants.BINARY:
	                this.value = getBigDecimalBytes(offset, length, isSigned, intLength, fractionLength, scaleLength);
	                break;
	            case Constants.PACKED_DECIMAL:
	                this.value = getBigDecimalBCD(offset, length, isSigned, intLength, fractionLength, scaleLength);
	                break;
	            }
	            isNum = true;
	        } catch (InvalidDataFormatException e) {
	            isNum = false;
	        }
	    }
	    
	    public BigDecimal getValue() {
            if (isNum)
                return this.value;
            else
                throw new InvalidDataFormatException("Cannot get nonnumeric value as numeric.");
	    }
	    
	    public String getStringValue() {
	        if (isNum) {
                return String.format(outputFormat, this.value);
            } else {
                return getStringDisplay(offset, length);
            }
	    }
	}

	protected class LongField {
	    protected int offset;
        protected int length;
        protected int usage;
        protected int intLength;
        protected int scaleLength;
        protected boolean isSigned;
        protected boolean isSignLeading;
        protected boolean isSignSeparate;
        protected boolean isNum = false;
        protected String outputFormat;
        
        public LongField(int offset, int length, int usage, int intLength,
                int scaleLength, boolean isSigned, boolean isSignLeading,
                boolean isSignSeparate) {
            this.offset = offset;
            this.length = length;
            this.usage = usage;
            this.intLength = intLength;
            this.scaleLength = scaleLength;
            this.isSigned = isSigned;
            this.isSignLeading = isSignLeading;
            this.isSignSeparate = isSignSeparate;
            
            int len = intLength + scaleLength;
            if (isSigned)
                len++;
            this.outputFormat = String.format("%%%s0%sd", isSigned ? "+" : "", len);
        }
	    
        private long value = 0;

        public long getValue() {
            if (isNum)
                return this.value;
            else
                throw new InvalidDataFormatException("Cannot get nonnumeric value as numeric.");
        }
        
        public String getStringValue() {
            if (isNum) {
                return String.format(outputFormat, this.value);
            } else {
                return getStringDisplay(offset, length);
            }
        }

        public void setCurrentValueToBytes() {
            if (isNum) {
                switch (usage) {
                case Constants.DISPLAY:
                    setLongDisplay(this.value, offset, length, isSigned, isSignLeading, isSignSeparate, intLength, scaleLength);
                    break;
                case Constants.BINARY:
                    setLongBytes(this.value, offset, length, isSigned, intLength, scaleLength);
                    break;
                case Constants.PACKED_DECIMAL:
                    setLongBCD(this.value, offset, length, isSigned, intLength, scaleLength);
                    break;
                }
            }
        }
        
        public void getCurrentValueFromBytes() {
            try {
                switch (usage) {
                case Constants.DISPLAY:
                    this.value = getLongDisplay(offset, length, isSigned, isSignLeading, isSignSeparate, scaleLength);
                    break;
                case Constants.BINARY:
                    this.value = getLongBytes(offset, length, isSigned, intLength, scaleLength);
                    break;
                case Constants.PACKED_DECIMAL:
                    this.value = getLongBCD(offset, length, isSigned, intLength, scaleLength);
                    break;
                }
                isNum = true;
            } catch (InvalidDataFormatException e) {
                isNum = false;
            }
        }

        public void setValue(BigDecimal input) {
            setValue(input.longValue());
        }

        public void setValue(long input) {
            this.value = getAlgebraicValue(input, intLength, isSigned, scaleLength);
            isNum = true;
        }

        public void setValue(String input) {
            try {
                long tmp = Long.parseLong(input);
                if (tmp < 0) {
                    tmp = -tmp;
                }
                setValue(tmp);
            } catch (NumberFormatException e) {
                setStringDisplay(input, offset, length, false);
                isNum = false;
            }
        }
	}

	protected class StringField {
	    private int offset;
        private int length;
        private boolean rightJustified;

        public StringField(int offset, int length, boolean rightJustified) {
	        this.offset = offset;
	        this.length = length;
	        this.rightJustified = rightJustified;
	        StringBuilder sb = new StringBuilder();
	        for (int i = 0; i < length; i++) {
	            sb.append(" ");
	        }
	        this.value = sb.toString();
	    }
        
        private String value;
        
        public void setValue(String input) {
            this.value = getStringValue(input, length, rightJustified);
        }
        
        public String getValue() {
            return this.value;
        }
        
        public void setCurrentValueToBytes() {
            setStringDisplay(this.value, offset, length, rightJustified);
        }
        
        public void getCurrentValueFromBytes() {
            this.value = getStringDisplay(offset, length);
        }
	}
	
	public static void main(String[] args) {
		BaseClass a = new BaseClass(6);
		long start = System.currentTimeMillis();
//    	for (long i = 1; i < 100000000; i++) {
//    		a.convertLongToDisplay(i % 10000, 0, 6, false, false, false);
//    	}
//		for (int i = 1; i < 100000000; i++) {
//			a.convertStringToDisplay("ABC" + i, 0, 5, false);	
//		}
//		a.convertLongToDisplay(12, 0, 6, false, false, false);
//		System.out.println(a.convertDisplayToInt(0, 6, true, false, true));
//		for (int i = 0; i < 100000000; i++) {
//			a.convertDisplayToInt(0, 6, false, false, false);
//		}
//		a.convertStringToDisplay("ABCD", 0, 6, true);
//		System.out.println(a.convertDisplayToString(0, 6));
//		for (int i = 0; i < 100000000; i++) {
//			a.convertDisplayToString(0, 6);
//		}
//		a.convertIntToBCD(12, 0, 4, true);
//		System.out.println(a.convertBCDToInt(0, 4, true));
		for (int i = 0; i < 100000000; i++) {
			a.convertIntToBCD((i % 10000), 0,4, false);
		}
    	long end = System.currentTimeMillis();
    	System.out.println("TIME " + (end-start));
    	System.out.println("ARRAY " + a.printByteArray(a.data));
	}
}