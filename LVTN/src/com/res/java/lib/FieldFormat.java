package com.res.java.lib;

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

public class FieldFormat {
	private static byte dataCategoryTemp = -1;

	private static Pattern numericInteger = Pattern
			.compile("(S{0,1}([9P](\\([0-9]+\\))?)*([9P](\\([0-9]+\\))?)*)");
	private static Pattern numericDecimal = Pattern
			.compile("(S{0,1}([9P](\\([0-9]+\\))?)*[V]([9P](\\([0-9]+\\))?)*)");
	private static Pattern numericEdited = Pattern
			.compile("(S{0,1}([$PZ9B0/\\,\\<\\>\\+\\-\\*](\\([0-9]+\\))?)*([V\\.]"
					+ "([$PZ9B0/\\,\\<\\>\\+\\-\\*](\\([0-9]+\\))?)*)?((CR)|(DB)){0,1})");
	private static Pattern alphaNumericEdited = Pattern
			.compile("([AX9B0/\\.\\,](\\([0-9]+\\))?)*");
	private static Pattern national = Pattern.compile("([N])*");
	private static Pattern nationalEdited = Pattern.compile("([WGNB09/])*");
	private static Pattern externalFloatingPoint = Pattern
			.compile("[+-]([9.V](\\([0-9]+\\))?)*[E][+-]([9])*");
	private static Pattern plainString = Pattern
			.compile("([AX](\\([0-9]+\\))?)*");

	public static byte getDataCategory(String pic) {
		verifyCobolPicture(pic);
		return dataCategoryTemp;
	}
	
	public static byte verifyCobolPicture(String pic) {
		if (pic == null) {
			dataCategoryTemp = Constants.ALPHANUMERIC;
			return Constants.GROUP;
		}
		String myFormat = pic.toUpperCase();
		if (numericInteger.matcher(myFormat).matches()) {
			dataCategoryTemp = Constants.NUMERIC;
			return Constants.INTEGER;
		} else if (numericDecimal.matcher(myFormat).matches()) {
			dataCategoryTemp = Constants.NUMERIC;
			return Constants.BIGDECIMAL;
		} else if (numericEdited.matcher(myFormat).matches()) {
			dataCategoryTemp = Constants.NUMERIC_EDITED;
			return Constants.BIGDECIMAL;
		} else if (alphaNumericEdited.matcher(myFormat).matches()) {
			dataCategoryTemp = Constants.ALPHANUMERIC_EDITED;
			return Constants.STRING;
		} else if (national.matcher(myFormat).matches()) {
			dataCategoryTemp = Constants.NATIONAL;
			return Constants.STRING;
		} else if (nationalEdited.matcher(myFormat).matches()) {
			dataCategoryTemp = Constants.NATIONAL_EDITED;
			return Constants.STRING;
		} else if (externalFloatingPoint.matcher(myFormat).matches()) {
			dataCategoryTemp = Constants.EXTERNAL_FLOATING_POINT;
			return Constants.BIGDECIMAL;
		} else {
			return -1;
		}
	}

	public static boolean isPlainString(String picture) {
		if (picture != null
				&& plainString.matcher(picture.toUpperCase()).matches())
			return true;
		return false;
	}

	public static boolean isString(String picture) {
		if (verifyCobolPicture(picture) == Constants.STRING)
			return true;
		return false;
	}
}
