/**
 * 
 */
package com.res.java.lib;

import java.math.BigDecimal;

import com.res.java.lib.exceptions.InvalidCobolFormatException;

/**
 * @author Jupiter
 * 
 */
public class EditedVar {
	private String normalizedPic;
	String beforeDecimal = "";
	String afterDecimal = "";
	private int definedLength = 0;
	private boolean rightJustified = false;
	private byte picType = 0;
	private char decimalChar = '.';
	private char commaChar = ',';
	private boolean vAppearance = false;

	public EditedVar(String picString, byte picType)
			throws InvalidCobolFormatException {
		if ((picType != Constants.ALPHANUMERIC_EDITED)
				&& (picType != Constants.NUMERIC_EDITED)) {
			// TODO: ThrowError
		} else {
			this.picType = picType;
			this.normalizedPic = normalizePicString(picString);
		}
	}

	public EditedVar(String picString, byte picType, boolean rightJustified)
			throws InvalidCobolFormatException {
		if ((picType != Constants.ALPHANUMERIC_EDITED)
				&& (picType != Constants.NUMERIC_EDITED)) {
			// TODO: ThrowError
			System.out.println("ERROR 1");
		} else {

			if (picType != Constants.ALPHANUMERIC_EDITED && rightJustified) {
				System.out.println("ERROR 2");
			}
			this.picType = picType;
			this.rightJustified = rightJustified;
			this.normalizedPic = normalizePicString(picString);
		}
	}

	public String doEdit(String input) {
		if (picType == Constants.ALPHANUMERIC_EDITED) {
			return simpleInsert(input);
		} else {
			try {
				// TODO: COMMA SWAP PERIOD
				BigDecimal value = new BigDecimal(input.trim());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "";
		}
	}

	public String doEdit(long input) {
		if (picType == Constants.ALPHANUMERIC_EDITED) {
			return simpleInsert(String.valueOf(input));
		} else {
			return "";
		}
	}

	public String doEdit(BigDecimal input) {
		if (picType == Constants.ALPHANUMERIC_EDITED) {
			return simpleInsert(input.toPlainString());
		} else {
			return simpleInsert(doNumericEdit(input));
		}
	}

	private String normalizePicString(String picString)
			throws InvalidCobolFormatException {
		StringBuilder normalizedString;
		if (picType == Constants.NUMERIC_EDITED) {
			//picString.replace('V', '.');
			int lastPosition = 0;
			normalizedString = new StringBuilder(picString.toUpperCase());
			do {
				int start = normalizedString.indexOf("(", lastPosition);
				if (start != -1) {
					lastPosition = normalizedString.indexOf(")", start);
					if (lastPosition != -1) {
						int occurence = Integer.parseInt(normalizedString
								.substring(start + 1, lastPosition));
						char symbol = normalizedString.charAt(start - 1);
						normalizedString.delete(start, lastPosition + 1);
						for (int i = 0; i < occurence - 1; i++) {
							normalizedString.insert(start, symbol);
						}
					} else {
						throw new InvalidCobolFormatException(
								"Format of PicString is not right PIC " + picString);
					}
				} else {
					break;
				}

			} while (true);
			// if (RunConfig.getInstance().isDecimalPointAsComma()) {
			// picString.replace(',', '.');
			// }

			// TODO: Generate Decimal Information.
			int decimalPosition = normalizedString.indexOf(String.valueOf(decimalChar));
			if (decimalPosition != -1) {
				int vPosition = normalizedString.indexOf("V");
				if (vPosition != -1) {
					throw new InvalidCobolFormatException(
							"Format of PicString is not right PIC " + picString);
				} 
				beforeDecimal = normalizedString.substring(0,
						decimalPosition);
				afterDecimal = normalizedString.substring(decimalPosition + 1,
						normalizedString.length());
			} else {
				int vPosition = normalizedString.indexOf("V");
				if (vPosition != -1) {
					beforeDecimal = normalizedString.substring(0,
							vPosition);
					afterDecimal = normalizedString.substring(vPosition + 1,
							normalizedString.length());
					vAppearance = true;
				} else {
					beforeDecimal = normalizedString.toString();
					afterDecimal = "";
				}
				
			}
			return normalizedString.toString();
		} else if (picType == Constants.ALPHANUMERIC_EDITED) {
			int lastPosition = 0;
			normalizedString = new StringBuilder(picString.toUpperCase());
			do {
				int start = normalizedString.indexOf("(", lastPosition);
				if (start != -1) {
					lastPosition = normalizedString.indexOf(")", start);
					if (lastPosition != -1) {
						int occurence = Integer.parseInt(normalizedString
								.substring(start + 1, lastPosition));
						char symbol = normalizedString.charAt(start - 1);
						normalizedString.delete(start, lastPosition + 1);
						for (int i = 0; i < occurence - 1; i++) {
							normalizedString.insert(start, symbol);
						}
					} else {
						throw new InvalidCobolFormatException(
								"Format of PicString is not right " + picString);
					}
				} else {
					break;
				}
			} while (true);
			String temp = normalizedString.toString();
			// Remove all simple insertion symbol to calculate actualLength
			this.definedLength = temp.replaceAll("([B0/,.&])+", "").length();
			return normalizedString.toString();
		} else {
			System.out.println("ERROR " + picType);
		}
		return "";
	}

	private String simpleInsert(String input) {
		// Apply for Numeric-edited and Alphanumeric-edited
		// AlphaNum first rightJustified???
		System.out.println("BEFORE SIMPLE " + input);
		int definedLength = this.definedLength;
		int inputLength = input.length();
		if (picType == Constants.ALPHANUMERIC_EDITED) {
			if (inputLength > definedLength) {
				if (rightJustified) {
					input = input.substring(inputLength - definedLength);
				} else {
					input = input.substring(0, definedLength);
				}
			} else {
				if (rightJustified) {
					for (int i = 0; i < (definedLength - inputLength); i++) {
						input = " " + input;
					}
				} else {
					for (int i = 0; i < (definedLength - inputLength); i++) {
						input = input + " ";
					}
				}
			}
		}
		
		StringBuilder inputBuilder = new StringBuilder(input);
		char[] charArray = this.normalizedPic.toCharArray();
		
		for (int i = 0; i < charArray.length; i++) {
			char currentChar = charArray[i];
			if (this.picType == Constants.ALPHANUMERIC_EDITED) {
				switch (currentChar) {
				case 'B':
					inputBuilder.insert(i, ' ');
					break;
				case ',':
				case '.':
				case '-':
				case '/':
				case '&':
				case '0':
					inputBuilder.insert(i, currentChar);
					break;
				}
			} else if (this.picType == Constants.NUMERIC_EDITED) {
				// COMMA PERIOD Swapping???
				switch (currentChar) {
				case 'B':
					inputBuilder.insert(i, ' ');
					break;
				case ',':
				case '/':
				case '0':
					inputBuilder.insert(i, currentChar);
					break;
				case '9':
					char curChar = inputBuilder.charAt(i);
					if ((curChar < '0' && curChar > '9')) {
						inputBuilder.setCharAt(i, '0');
					}
					break;
				}
			}
		}
		return inputBuilder.toString();
	}

	private String fixedInsert(String input, boolean isNegative) {
		String retVal = "";
		if (isNegative) {
			retVal = input.replaceAll("\\+", "-");
		} else {
			retVal = input.replaceAll("\\-", " ").replaceAll("CR","  ").replaceAll("DB", "  ");
		}
		return retVal;
	}

	private String doNumericEdit(BigDecimal input) {
		String replaceZ = normalizedPic.replaceAll("([Z" + decimalChar +  "])+", " ");
		int replaceAsterix = normalizedPic.replaceAll("([*" + decimalChar +  "])+", "").length();
		if (replaceZ.length() == normalizedPic.length()) { // ZERO
			if (input.compareTo(BigDecimal.ZERO) == 0) {
				return replaceZ;
			}
		} else if (replaceAsterix == 0) {
			if (input.compareTo(BigDecimal.ZERO) == 0) {
				return normalizedPic;
			}
		}
		
		boolean isNegative = false;
		if (input.compareTo(BigDecimal.ZERO) == -1) {
			isNegative = true;
		}
		
		input = input.abs();
		long intPart = input.longValue();
		int scale = input.scale();
		input = input.scaleByPowerOfTen(input.scale());
		long fractionPart = input.longValue() - (long) (intPart*Math.pow(10, scale));
		StringBuilder intString;
		StringBuilder fractionString;
		
		char[] intArray = beforeDecimal.toCharArray();
		if (intArray.length > 0) {
			intString = new StringBuilder(String.valueOf(intPart));
			intString = intString.reverse();
			intString = intString.delete(intArray.length, intString.length());
		} else {
			intString = new StringBuilder("");
		}
		
		boolean doneFloatingInsert = false;
		System.out.println(intPart + "/" + fractionPart);
		System.out.println(beforeDecimal);
		for (int i = 0; i < intArray.length; i++) {
			System.out.println(i + "/" + intString);
			char currentChar = intArray[intArray.length - i - 1];
			switch (currentChar) {
			case 'Z':
				if (i >= intString.length()) {
					intString.append(' ');
				} else {
					char c = intString.charAt(i);
					if (c == '0') {
						intString.setCharAt(i, ' ');
					}
				}
				break;
			case '*':
				if (i >= intString.length()) {
					intString.append(currentChar);
				} else {
					char c = intString.charAt(i);
					if (c == '0') {
						intString.setCharAt(i, currentChar);
					}
				}
				break;
			case '9':
				if (i > intString.length()) {
					intString.append('0');
				} 
				break;
			case '+':
			case '-':
			case '$':
				if (doneFloatingInsert) {
					if (i >= intString.length()) {
						intString.append(' ');
					} else {
						intString.setCharAt(i, ' ');
					}
					break;
				} else {
					if (i >= intString.length()) {
						intString.append(currentChar);
						doneFloatingInsert = true;
					} else {
						if ((intArray.length - i - 2) < 0) {
							intString.setCharAt(i, currentChar);
							doneFloatingInsert = true;
						} else {
							char prevChar = intArray[intArray.length - i - 2];
							char inputChar = intString.charAt(i);
							if (prevChar == '+' || prevChar == '-' || prevChar == '$') {
								if (inputChar >= '0' && inputChar <= '9') {
								} else {
									intString.setCharAt(i, '0');
								}
							} else {
								intString.setCharAt(i, currentChar);
							}
						}
					}
					break;
				}
			case 'B':
			case ',':
			case '/':
			case '0':
				break;
			}
		}
		System.out.println("AFTER EDIT INT STRING " + intString.toString());
		char[] fractionArray = afterDecimal.toCharArray();
		if (fractionArray.length > 0) {
			fractionString = new StringBuilder(
					String.valueOf(fractionPart));
		} else {
			fractionString = new StringBuilder("");
		}
		System.out.println("FRACTION STRING " + afterDecimal);
		if (!fractionString.equals("")) {
			for (int i = 0; i < fractionArray.length; i++) {
				char currentChar = fractionArray[i];
				switch (currentChar) {
				case 'Z':
					if (i >= fractionString.length()) {
						fractionString.append('0');
					}
					break;
				case '*':
					if (i >= fractionString.length()) {
						fractionString.append('0');
					}
					break;
				case '9':
					if (i >= fractionString.length()) {
						fractionString.append('0');
					}
					break;
				case 'B':
				case '+':
				case '-':
				case 'C':
				case 'D':
				case 'R':
				case '/':
					if (i >= fractionString.length()) {
						fractionString.append(currentChar);
					} else {
						fractionString.setCharAt(i, currentChar);
					}
					break;
				default:
					// TODO: Handle currencySign here
					break;
				}
			}
		}
		System.out.println("AFTER EDIT Fraction STRING " + fractionString.toString());
		String retVal = "";
		if (intArray.length > 0) {
			if (fractionArray.length > 0) {
				if (!this.vAppearance) {
					fractionString.insert(0, this.decimalChar);
				} 
				
				retVal = intString.reverse().append(fractionString).toString();
			} else {
				retVal = intString.reverse().toString();
			}
		} else {
			if (fractionArray.length > 0) {
				if (!this.vAppearance) {
					fractionString.insert(0, this.decimalChar);
				} 
				retVal = fractionString.toString();
			} else {
				//TODO: throw ERROR
				System.out.println("ERROR NUMER FORMAT");
			}
		}
		
		return fixedInsert(retVal, isNegative);
	}

	public static void main(String[] args) {
		EditedVar a;
		try {
			a = new EditedVar("+$$$.99CR", Constants.NUMERIC_EDITED,
					false);
			BigDecimal test = new BigDecimal("12345.2");
			System.out.println(a.doEdit(test) + "|");
		} catch (InvalidCobolFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
