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
	private boolean blankWhenZero = false;
	
	public EditedVar(String picString, byte picType) {
		if ((picType != Constants.ALPHANUMERIC_EDITED)
				&& (picType != Constants.NUMERIC_EDITED)) {
			// TODO: ThrowError
		} else {
			this.picType = picType;
			this.normalizedPic = normalizePicString(picString);
		}
	}

	public EditedVar(String picString, byte picType, boolean rightJustified) {
		if ((picType != Constants.ALPHANUMERIC_EDITED)
				&& (picType != Constants.NUMERIC_EDITED)) {
			// TODO: ThrowError
			// System.out.println("ERROR 1");
		} else {
			this.picType = picType;
			this.normalizedPic = normalizePicString(picString);
			if (picType == Constants.ALPHANUMERIC_EDITED) {
				this.rightJustified = rightJustified;
			} else if (picType == Constants.NUMERIC_EDITED) {
				this.blankWhenZero = rightJustified;
				if (this.normalizedPic.contains("*") || this.normalizedPic.contains("S")) {
					this.blankWhenZero = false;
				}
			}
			
			
		}
	}
	
	public String doEdit(String input) {
		if (picType == Constants.ALPHANUMERIC_EDITED) {
			return simpleInsert(input);
		} else {
			try {
				// TODO: COMMA SWAP PERIOD
				return doNumericEdit(input.trim());
			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}

		}
	}

	private String normalizePicString(String picString) {
		StringBuilder normalizedString = new StringBuilder(picString.toUpperCase());
		if (picType == Constants.NUMERIC_EDITED) {
			// Generate Decimal Information.
			int decimalPosition = normalizedString.indexOf(String
					.valueOf(decimalChar));
			if (decimalPosition != -1) {
				int vPosition = normalizedString.indexOf("V");
				if (vPosition != -1) {
					throw new InvalidCobolFormatException(
							"Format of PicString is not right PIC " + picString);
				}
				beforeDecimal = normalizedString.substring(0, decimalPosition);
				afterDecimal = normalizedString.substring(decimalPosition + 1,
						normalizedString.length());
			} else {
				int vPosition = normalizedString.indexOf("V");
				if (vPosition != -1) {
					beforeDecimal = normalizedString.substring(0, vPosition);
					afterDecimal = normalizedString.substring(vPosition + 1,
							normalizedString.length());
					vAppearance = true;
					normalizedString.deleteCharAt(vPosition);
				} else {
					beforeDecimal = normalizedString.toString();
					afterDecimal = "";
				}

			}
			return normalizedString.toString();
		} else if (picType == Constants.ALPHANUMERIC_EDITED) {
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
		StringBuilder picBuilder = new StringBuilder(normalizedPic);
		for (int i = 0; i < picBuilder.length(); i++) {
			char currentChar = charArray[i];
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
		}
		normalizedPic = picBuilder.toString();
		return inputBuilder.toString();
	}

	private String fixedInsert(String input, boolean isNegative) {
		String retVal = "";
		if (isNegative) {
			retVal = input.replaceAll("\\+", "-");
		} else {
			retVal = input.replaceAll("\\-", " ").replaceAll("CR", "  ")
					.replaceAll("DB", "  ");
		}
		return retVal;
	}

	private String doNumericEdit(String stringInput) {
		BigDecimal input = new BigDecimal(stringInput);
		if (this.blankWhenZero && (input.compareTo(BigDecimal.ZERO) == 0)) {
			return normalizedPic.replaceAll("(.)", " ");
		}
		int replaceZ = normalizedPic.replaceAll(
				"([^Z" + decimalChar + commaChar + "B0/])", "").length();
		int replaceAsterix = normalizedPic.replaceAll(
				"([\\*" + decimalChar + "])", "").length();
		if (replaceZ == normalizedPic.length()) { // ZERO
			if (input.compareTo(BigDecimal.ZERO) == 0) {
				return normalizedPic.replaceAll("(.)", " ");
			}
		} else if (replaceAsterix == 0) {
			if (input.compareTo(BigDecimal.ZERO) == 0) {
				return normalizedPic;
			}
		} else {
			char[] floatingSymbols = { '+', '-', '$' };
			for (char symbol : floatingSymbols) {
				int normalizedLength = normalizedPic.replaceAll(
						"([^\\" + symbol + decimalChar + commaChar + "B0/])",
						"").length();
				if (input.compareTo(BigDecimal.ZERO) == 0) {
					if (normalizedLength == normalizedPic.length()) {
						return normalizedPic.replaceAll("(.)", " ");
					}
				}

			}
		}
		StringBuilder intString = new StringBuilder("");
		StringBuilder fractionString = new StringBuilder("");
		char[] intArray = beforeDecimal.toCharArray();
		char[] fractionArray = afterDecimal.toCharArray();

		boolean isNegative = false;
		int compareWithZero = input.compareTo(BigDecimal.ZERO);
		if (compareWithZero == 0) {
			String[] temp = stringInput.split("(\\" + decimalChar + ")");
			System.out.println(temp[0]);
			intString.append(temp[0]);
			if (temp.length == 2) {
				fractionString.append(temp[1]);
			}
		} else {
			if (compareWithZero == -1) {
				isNegative = true;
			}
			input = input.abs();
			long intPart = input.longValue();
			int scale = input.scale();
			input = input.scaleByPowerOfTen(input.scale());
			long fractionPart = input.longValue()
					- (long) (intPart * Math.pow(10, scale));
			if (intArray.length > 0) {
				intString = new StringBuilder(String.valueOf(intPart));
				intString = intString.reverse();
			}
			if (fractionArray.length > 0) {
				fractionString = new StringBuilder(String.valueOf(fractionPart));
			}
		}

		boolean doneFloatingInsert = false;
		//System.out.println("PIC STRING " + normalizedPic);
		StringBuilder picBuilder = new StringBuilder(normalizedPic);
		char currentEditingSymbol = ' ';
		for (int i = 0; i < intArray.length; i++) {

			int currentPos = intArray.length - i - 1;
			char currentChar = intArray[currentPos];

			int prevPos = currentPos - 1;
			char prevChar = '@';
			if (currentPos != 0) {
				prevChar = intArray[prevPos];
			}
			System.out.println(i + "%" + currentChar + " |" + intString + "|");
			if (currentChar == 'Z' || currentChar == '*') {
				if (currentPos == 0) {
					if (currentEditingSymbol == ' '
							|| (currentEditingSymbol != ' ' && currentEditingSymbol == currentChar)) {
						if (i >= intString.length()) {
							intString.append(currentChar);
						} else {
							if (intString.charAt(i) == '0') {
								intString.setCharAt(i, currentChar);
							}
						}
					} else {
						// Z can't be the first of +++$$$
						throw new InvalidCobolFormatException("Wrong Format "
								+ normalizedPic);
					}
				} else {
					if (currentEditingSymbol == ' ') {
						currentEditingSymbol = currentChar;
					} else if (currentEditingSymbol != currentChar) {
						// +-$ is editing symbol now.
						throw new InvalidCobolFormatException("Wrong Format "
								+ normalizedPic);
					}
					if (i >= intString.length()) {
						intString.append(currentChar);
					} else {
						if (intString.charAt(i) == '0') {
							intString.setCharAt(i, currentChar);
						}
					}

				}
			} else if (currentChar == '+' || currentChar == '-'
					|| currentChar == '$') {
				if (currentPos == 0) {
					// End of Pic String
					char addChar = currentChar;
					if (currentEditingSymbol != currentChar) {
					} else {
						// DONE ? --> space else --> set currentChar
						if (doneFloatingInsert) {
							addChar = ' ';
						}
					}
					if (i >= intString.length()) {
						intString.append(addChar);
					} else {
						intString.setCharAt(i, addChar);
					}
				} else if (currentPos == 1) {
					boolean max = false;
					if (currentEditingSymbol == ' ') {
						currentEditingSymbol = currentChar;
						if (prevChar != currentChar) {
							max = true;
						}
					} else if (currentEditingSymbol == currentChar) {
						if (prevChar != currentChar) {
							max = true;
						}
					} else {
						throw new InvalidCobolFormatException("Wrong Format "
								+ normalizedPic);
					}

					char addChar = currentChar;
					if (doneFloatingInsert) {
						addChar = ' ';
					}
					if (i >= intString.length()) {
						intString.append(addChar);
						doneFloatingInsert = true;
					} else {
						char numChar = intString.charAt(i);
						if (doneFloatingInsert) {
							intString.setCharAt(i, addChar);
						} else {
							if (max == false) {
								if (numChar >= '0' && numChar <= '9') {
									addChar = numChar;
								} else {
									addChar = '0';
								}
							} else {
								addChar = currentChar;
								doneFloatingInsert = true;
							}
							intString.setCharAt(i, addChar);
						}
					}
				} else {
					if (currentEditingSymbol == ' ') {
						int symbolIndex = beforeDecimal.indexOf(currentChar);
						if (symbolIndex < currentPos) {
							currentEditingSymbol = currentChar;
						} else {
							intString.setCharAt(i, currentChar);
							continue;
						}
					} else if (currentEditingSymbol != currentChar) {
						// Z is editing symbol now.
						throw new InvalidCobolFormatException("Wrong Format "
								+ normalizedPic);
					} else {
						int symbolIndex = beforeDecimal
								.indexOf(currentEditingSymbol);
						if (symbolIndex == -1) {
							throw new InvalidCobolFormatException(
									"Wrong Format " + normalizedPic);
						} else {
							if (symbolIndex >= currentPos) {
								if (!doneFloatingInsert) {
									intString.setCharAt(i, currentChar);
									doneFloatingInsert = true;
									continue;
								}

							}
						}
					}
					char addChar = currentChar;
					if (doneFloatingInsert) {
						addChar = ' ';
					}
					if (i >= intString.length()) {
						intString.append(addChar);
						doneFloatingInsert = true;
					} else {
						char numChar = intString.charAt(i);
						if (doneFloatingInsert) {
							intString.setCharAt(i, addChar);
						} else {
							if (numChar >= '0' && numChar <= '9') {
							} else {
								intString.setCharAt(i, '0');
							}
						}
					}
				}
			} else {
				if (currentChar == '9') {
					if (i >= intString.length()) {
						intString.append('0');
					}
				} else if (currentChar == 'B' || currentChar == '0'
						|| currentChar == '/' || currentChar == commaChar) {
					if (currentPos == 0) {
						// End of String --> do nothing
						if (i >= intString.length()) {
							intString.append(currentChar);
						} else {
							intString.insert(i, currentChar);
						}
					} else {
						if (currentEditingSymbol != ' ') {
							if (prevChar == currentEditingSymbol) {
								// Immediate right of editingSymbol
								char addChar = currentEditingSymbol;
								if (doneFloatingInsert) {
									if (currentEditingSymbol != '*') {
										addChar = ' ';
									} else {
										addChar = '*';
									}

								}
								if (i >= intString.length()) {
									intString.append(addChar);
									if (!doneFloatingInsert) {
										doneFloatingInsert = true;
									}
								} else {
									intString.insert(i, currentChar);
								}
							} else {
								if (currentPos == 0) {
									// Do Nothing
									// B0/, is the first
									if (i >= intString.length()) {
										intString.append(currentChar);
									} else {
										intString.insert(i, currentChar);
									}
								} else {
									int symbolIndex = beforeDecimal
											.indexOf(currentEditingSymbol);
									if (symbolIndex == -1) {
										// Do Nothing, consider to be simple
										// insert
										if (i >= intString.length()) {
											intString.append(currentChar);
										} else {
											intString.insert(i, currentChar);
										}
									} else {
										// Inside 2 currentEditingSymbol
										if (symbolIndex < currentPos) {
											char addChar = currentEditingSymbol;
											if (doneFloatingInsert) {
												if (currentEditingSymbol != '*') {
													addChar = ' ';
												} else {
													addChar = '*';
												}
											}
											if (i >= intString.length()) {
												// intString.append(addChar);
												if (!doneFloatingInsert) {
													doneFloatingInsert = true;
												}
											} else {
												intString
														.insert(i, currentChar);
											}
										} else {
											// Not inside 2 currentEditting
											// Symbol
											// Do nothing
											if (i >= intString.length()) {
												intString.append(currentChar);
											} else {
												intString
														.insert(i, currentChar);
											}
										}
									}
								}
							}
						} else {
							char[] editingSymbol = { '$', '+', '-', 'Z', '*' };
							for (char symbol : editingSymbol) {
								int symbolIndex = beforeDecimal.indexOf(symbol);
								if (symbolIndex > -1
										&& symbolIndex < currentPos) {
									int symbolIndex2 = beforeDecimal.indexOf(
											symbol, symbolIndex + 1);
									if (symbolIndex2 != -1) {
										currentEditingSymbol = symbol;
										break;
									} else {
										// One Symbol detect
										if (symbolIndex == currentPos - 1) {
											// Immediate right
											currentEditingSymbol = symbol;
											doneFloatingInsert = true;
											break;
										}
									}
								}
							}
							if (currentEditingSymbol == ' ') {
								if (i >= intString.length()) {
									intString.append(currentChar);
								} else {
									intString.insert(i, currentChar);
								}
							} else {
								if (doneFloatingInsert == true) {
									if (i >= intString.length()) {
										intString.append(currentEditingSymbol);
										doneFloatingInsert = true;
									} else {
										intString.setCharAt(i,
												currentEditingSymbol);
									}
								} else {
									if (i >= intString.length()) {
										intString.append(currentChar);
										doneFloatingInsert = true;
									} else {
										intString.insert(i, currentChar);
									}
								}

							}

						}
					}

				}
			}

		}
		if (intString.length() > intArray.length) {
			intString = intString.delete(intArray.length, intString.length());
		}
//		System.out.println("AFTER EDIT INT STRING " + intString.toString()
//				+ "|");
//
//		System.out.println("FRACTION STRING " + afterDecimal);
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
				case 'P':
					if (i >= fractionString.length()) {
					} else {
						fractionString.deleteCharAt(i);
					}
					break;
				case 'B':
				case '0':
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
		if (fractionString.length() > fractionArray.length) {
			fractionString = fractionString.delete(fractionArray.length,
					fractionString.length());
		}
//		System.out.println("AFTER EDIT Fraction STRING "
//				+ fractionString.toString());
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
				// TODO: throw ERROR
				System.out.println("ERROR NUMER FORMAT");
			}
		}
		normalizedPic = picBuilder.toString();
//		System.out.println("END: " + retVal + "|");
//		System.out.println("NORMALIZED PIC " + normalizedPic);
		retVal = retVal.replaceAll("[ZB]", " ").replaceAll("D\\ ", "DB");
		return fixedInsert(retVal, isNegative);
	}
	
	public String getNormalizedPic() {
		return this.normalizedPic;
	}
	public String getBeforeDecimal() {
		return this.beforeDecimal;
	}
	
	public static void main(String[] args) {
		EditedVar a = new EditedVar("99.", Constants.NUMERIC_EDITED);
		System.out.println(a.doEdit("1234"));
	}
}
