package com.res.java.util;

/*****************************************************************************
 Copyright 2009 Venkat Krishnamurthy
 This file is part of RES.

 RES is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 RES is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with RES.  If not, see <http://www.gnu.org/licenses/>.

 @author VenkatK mailto: open.cobol.to.java at gmail.com
 ******************************************************************************/

import java.util.regex.Pattern;

import com.res.java.lib.Constants;
import com.res.java.translation.symbol.SymbolProperties;
import com.res.java.translation.symbol.SymbolProperties.CobolDataDescription;

public class FieldAttributes {

	// Update further
	public static int calculateBytesLength(SymbolProperties props) {
		int len = 0;
		boolean isNumber = false;
		CobolDataDescription desc = props.getCobolDesc();

		switch (desc.getTypeInJava()) {
		case Constants.FLOAT:
		case Constants.DOUBLE:
		case Constants.BIGDECIMAL:
			len = desc.getMaxIntLength() + desc.getMaxFractionLength();
			isNumber = true;
			break;
		case Constants.SHORT:
		case Constants.INTEGER:
		case Constants.LONG:
			len = desc.getMaxIntLength();
			isNumber = true;
			break;
		case Constants.CHAR:
		case Constants.STRING:
			len = desc.getMaxStringLength();
			break;
		}

		/*if (props.getDataCategory() == Constants.NUMERIC_EDITED) {
			desc.setTypeInJava(Constants.STRING);
			if (desc.getMaxFractionLength() > 0)
				len++;
			desc.setMaxIntLength((short) 0);
			desc.setMaxFractionLength((short) 0);
			desc.setMaxStringLength(len);
			isNumber = false;
		}*/

		if (isNumber) {
			switch (desc.getUsage()) {
			case Constants.BINARY:
//				props.setAdjustedLength(len);
				if (len >= 1 && len <= 4) {
					len = 2;
				} else if (len >= 5 && len <= 9) {
					len = 4;
				} else if (len >= 10) {
					len = 8;
					//					if (desc.getTypeInJava() == Constants.INTEGER)
					//						desc.setTypeInJava(Constants.LONG);
				} else
					len = 0;
				break;
			case Constants.PACKED_DECIMAL:
//				props.setAdjustedLength(len);
				len = (len + 2) / 2;
				/*int len2 = len;
				len = len2 / 2;
				if (len2 % 2 > 0)
					len++;
				if (len >= 1 && len <= 4) {
				} else if (len >= 5 && len <= 10) {
					if (desc.getTypeInJava() == Constants.INTEGER)
						desc.setTypeInJava(Constants.LONG);
				} else
					len = 0;*/
				break;
			case Constants.DISPLAY:
//				props.setAdjustedLength(len);
				if (desc.isSigned() && desc.isSignSeparate())
					len++;
				//				if (desc.isCurrency())
				//					len++;
				/*if (len >= 1 && len <= 4) {
				} else if (len >= 5 && len <= 9) {
				} else if (len >= 10) {
					if (desc.getTypeInJava() == Constants.INTEGER)
						desc.setTypeInJava(Constants.LONG);
				} else
					len = 0;*/
				break;
			default:
//				props.setAdjustedLength(len);
			}
		}/* else if (desc.getMaxStringLength() == 1)
						desc.setTypeInJava(Constants.CHAR);*/
		return len;
	}

	public static void processPicture(SymbolProperties props) {
		CobolDataDescription desc = props.new CobolDataDescription();
	
		props.setCobolDesc(desc);
	
		String pic = props.getPictureString().toUpperCase();
		desc.setPic(normalizePicture(pic));
	
		if (alphabetic.matcher(pic).matches()) {
			desc.setDataCategory(Constants.ALPHABETIC);
			if (pic.length() == 1)
				desc.setTypeInJava(Constants.CHAR);
			else
				desc.setTypeInJava(Constants.STRING);
			desc.setMaxStringLength(desc.getPic().length());
		} else if (alphanumeric.matcher(pic).matches()) {
			desc.setDataCategory(Constants.ALPHANUMERIC);
			desc.setTypeInJava(Constants.STRING);
			desc.setMaxStringLength(desc.getPic().length());
		} else if (numericLeftPScaling.matcher(pic).matches()
				|| numericRightPScaling.matcher(pic).matches()) {
			desc.setDataCategory(Constants.NUMERIC);
			processPScaling(desc);
		} else if (numericInteger.matcher(pic).matches()) {
			desc.setDataCategory(Constants.NUMERIC);
			processNumericInteger(desc);
		} else if (numericDecimal.matcher(pic).matches()) {
			desc.setDataCategory(Constants.NUMERIC);
			desc.setTypeInJava(Constants.BIGDECIMAL);
			processNumericDecimal(desc);
		} else if (alphanumericEdited.matcher(pic).matches()) {
			desc.setDataCategory(Constants.ALPHANUMERIC_EDITED);
			desc.setTypeInJava(Constants.STRING);
			desc.setMaxStringLength(desc.getPic().length());
		} else if (numericEdited.matcher(pic).matches()) {
			desc.setDataCategory(Constants.NUMERIC_EDITED);
			desc.setTypeInJava(Constants.STRING);
			if (desc.getPic().indexOf("V") > 0) {
				desc.setMaxStringLength(desc.getPic().length() - 1);
			} else {
				desc.setMaxStringLength(desc.getPic().length());
			}
		} else {
//			SymbolUtil.getInstance().reportError(
//					"Data name:" + props.getDataName()
//							+ " has invalid picture string: " + pic);
		    //TODO: throws exception
			System.exit(0);
		}
	}

	private static Pattern alphabetic = Pattern
			.compile("(A(\\(0*[1-9][0-9]*\\))?)+");

	private static Pattern alphanumeric = Pattern
			.compile("([AX9](\\(0*[1-9][0-9]*\\))?)*X(\\(0*[1-9][0-9]*\\))?([AX9](\\([1-9][0-9]*\\))?)*");

	// numeric, pscaling left, decimal value
	private static Pattern numericLeftPScaling = Pattern
			.compile("S?V?(P(\\(0*[1-9][0-9]*\\))?)+(9(\\(0*[1-9][0-9]*\\))?)+");

	// numeric, pscaling right, int value
	private static Pattern numericRightPScaling = Pattern
			.compile("S?(9(\\(0*[1-9][0-9]*\\))?)+(P(\\(0*[1-9][0-9]*\\))?)+V?");

	private static Pattern numericInteger = Pattern
			.compile("S?(9(\\(0*[1-9][0-9]*\\))?)+V?");

	private static Pattern numericDecimal = Pattern
			.compile("S?(9(\\(0*[1-9][0-9]*\\))?)*V(9(\\(0*[1-9][0-9]*\\))?)+");

	private static Pattern alphanumericEdited = Pattern
			.compile("(([AX9B0/](\\(0*[1-9][0-9]*\\))?)*[AX](\\(0*[1-9][0-9]*\\))?([AX9B0/](\\(0*[1-9][0-9]*\\))?)*[B0/](\\(0*[1-9][0-9]*\\))?(\\(0*[1-9][0-9]*\\))?([AX9B0/](\\(0*[1-9][0-9]*\\))?)*"
					+ "|([AX9B0/](\\(0*[1-9][0-9]*\\))?)*[B0/](\\(0*[1-9][0-9]*\\))?(\\(0*[1-9][0-9]*\\))?([AX9B0/](\\(0*[1-9][0-9]*\\))?)*[AX](\\(0*[1-9][0-9]*\\))?([AX9B0/](\\(0*[1-9][0-9]*\\))?)*)");

	private static Pattern numericEdited = Pattern
			.compile("(S{0,1}([$PZ9B0/\\,\\<\\>\\+\\-\\*](\\([0-9]+\\))?)*([V\\.]"
					+ "([$PZ9B0/\\,\\<\\>\\+\\-\\*](\\([0-9]+\\))?)*)?((CR)|(DB)){0,1})");

	private static void processNumericDecimal(CobolDataDescription desc) {
		StringBuilder sb = new StringBuilder(desc.getPic());

		if (sb.charAt(0) == 'S') {
			desc.setSigned(true);
			sb.deleteCharAt(0);
		}

		int i = sb.indexOf("V");
		desc.setMaxIntLength((short) i);
		desc.setMaxFractionLength((short) (sb.length() - i - 1));
	}

	private static void processNumericInteger(CobolDataDescription desc) {
		StringBuilder sb = new StringBuilder(desc.getPic());
		if (sb.charAt(0) == 'S') {
			desc.setSigned(true);
			sb.deleteCharAt(0);
		}
		if (sb.charAt(sb.length() - 1) == 'V') {
			sb.setLength(sb.length() - 1);
		}
		desc.setMaxIntLength((short) sb.length());
		processIntegralJavaType(desc);
	}

	private static String normalizePicture(String picture) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while (i < picture.length()) {
			if (picture.charAt(i) == '(') {
				char c = picture.charAt(i - 1);
				int j = i + 1;
				for (; picture.charAt(j) != ')'; j++)
					;
				int size = Integer.parseInt(picture.substring(i + 1, j));
				i = j;
				for (int k = 0; k < size - 1; k++)
					// one char already appended
					sb.append(c);
			} else {
				sb.append(picture.charAt(i));
			}
			i++;
		}
		return sb.toString();
	}

	private static void processPScaling(CobolDataDescription desc) {
		StringBuilder sb = new StringBuilder(desc.getPic());

		if (sb.charAt(0) == 'S') {
			desc.setSigned(true);
			sb.deleteCharAt(0);
		}

		// remove redundant V
		int i = 0;
		if ((i = sb.indexOf("V")) > 0) {
			sb.deleteCharAt(i);
		}

		if (sb.charAt(0) == 'P') {
			// Ps on leftmost
			i = sb.lastIndexOf("P");
			desc.setMaxScalingLength((short) (i + 1));
			desc.setMaxFractionLength((short) (sb.length() - i - 1));
			desc.setTypeInJava(Constants.BIGDECIMAL);
		} else {
			// Ps on rightmost
			i = sb.indexOf("P");
			desc.setMaxIntLength((short) i);
			desc.setMaxScalingLength((short) (sb.length() - i));
			processIntegralJavaType(desc);
		}
	}

	private static void processIntegralJavaType(CobolDataDescription desc) {
		short size = desc.getMaxIntLength();
		if (size < 1) {
			// error
		} else if (size < 5) {
			desc.setTypeInJava(Constants.SHORT);
		} else if (size < 10) {
			desc.setTypeInJava(Constants.INTEGER);
		} else if (size < 19) {
			desc.setTypeInJava(Constants.LONG);
		}
	}

	/*public static void main(String[] args) {
		SymbolProperties sym = new SymbolProperties();
		sym.setPictureString("999V");
		sym.setDataUsage(Constants.DISPLAY);
		System.out.println(normalizePicture(sym.getPictureString()));
		processPicture(sym);
		CobolDataDescription desc = sym.getCobolDesc();
		System.out.println(desc.getTypeInJava());
		System.out.println(desc.getMaxIntLength()+"."+desc.getMaxFractionLength()+":"+desc.getMaxScalingLength());
		System.out.println(desc.getMaxStringLength());
	}*/
}
