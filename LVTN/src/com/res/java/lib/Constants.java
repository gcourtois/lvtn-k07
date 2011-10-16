package com.res.java.lib;

/*******************************************************************************
 * Copyright 2009 Venkat Krishnamurthy This file is part of RES.
 * 
 * RES is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * RES is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * RES. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author VenkatK mailto: open.cobol.to.java at gmail.com
 ******************************************************************************/

public class Constants {

	// Java types
	public static final byte BYTE = 0;
	public static final byte CHAR = 1;
	public static final byte SHORT = 2;
	public static final byte INTEGER = 3;
	public static final byte LONG = 4;
	public static final byte FLOAT = 5;
	public static final byte DOUBLE = 6;
	public static final byte BIGDECIMAL = 7;
	public static final byte STRING = 8;
	public static final byte GROUP = 9;
	public static final byte OBJECT = 10;
	public static final byte MAX_TYPES = 11;
	public static final byte UNKNOWN = MAX_TYPES;

	// Basic Cobol Usage Types
	public static final byte BINARY = 0;
	public static final byte PACKED_DECIMAL = 1;
	public static final byte DISPLAY = 2;
	public static final byte COMPUTATIONAL1 = 3;
	public static final byte COMPUTATIONAL2 = 4;
	public static final byte FLOATING_POINT = 5;
	public static final byte COMPUTATIONAL5 = 6;
	public static final byte DISPLAY_1 = 7;
	public static final byte INDEX = 8;
	public static final byte NATIONAL_U = 9;
	public static final byte POINTER = 10;
	public static final byte PROCEDURE_POINT = 11;
	public static final byte FUNCTION_POINTER = 12;

	// Basic Cobol Data Categories
	public static final byte ALPHABETIC = 0;
	public static final byte NUMERIC = 1;
	public static final byte NUMERIC_EDITED = 2;
	public static final byte ALPHANUMERIC = 3;
	public static final byte ALPHANUMERIC_EDITED = 4;
	public static final byte EXTERNAL_FLOATING_POINT = 5;
	public static final byte NATIONAL = 6;
	public static final byte NATIONAL_EDITED = 7;

	// figurative constants type
	public static final byte SPACE = 8;
	public static final byte QUOTE = 9;
	public static final byte LOW_VALUE = 10;
	public static final byte HIGH_VALUE = 11;
	public static final byte ZERO = 12;
	public static final byte NULL = 13;
	
	// File Organization Codes
	public static final byte SEQUENTIAL = 0;
	public static final byte INDEXED = 1;
	public static final byte RELATIVE = 2;
	public static final byte LINE_SEQUENTIAL = 3;

	// Access Mode Codes
	public static final byte SEQUENTIAL_ACCESS = 0;
	public static final byte RANDOM_ACCESS = 1;
	public static final byte DYNAMIC_ACCESS = 2;

}
