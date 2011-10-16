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

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

@SuppressWarnings("unchecked")
public class NameUtil {

	public static String convertCobolNameToJava(String cobolName,
			boolean firstUpper) {

		if (cobolName == null)
			return "";

		int i = 0;
		StringBuilder javaName = new StringBuilder();
		while (i < cobolName.length()) {
			char c = cobolName.charAt(i);
			if (i == 0) {
				// first character
				if (Character.isLetter(c)) {
					if (firstUpper) {
						javaName.append(Character.toUpperCase(c));
					} else {
						javaName.append(Character.toLowerCase(c));
					}
				} else {
					javaName.append('_').append(c);
				}
			} else {
				// other characters
				if (c == '-')
					javaName.append('_');
				if (Character.isLetterOrDigit(c))
					javaName.append(c);
			}
			i ++;
		}
		
		if (JAVA_RESERVED_WORDS.contains(javaName.toString().toLowerCase())) {
			javaName.append('_');
		}
		
		return javaName.toString().trim();
	}

	private static final TreeSet<String> JAVA_RESERVED_WORDS = new TreeSet<String>();

	static {
		((Collection) JAVA_RESERVED_WORDS).addAll(Arrays.asList(new String[] {
				"abstract", "continue", "for", "new", "switch", "assert",
				"default", "goto", "package", "synchronized", "boolean", "do",
				"if", "private", "this", "break", "double", "implements",
				"protected", "throw", "byte", "else", "import", "public",
				"throws", "case", "enum", "instanceof", "return", "transient",
				"catch", "extends", "int", "short", "try", "char", "final",
				"interface", "static", "void", "class", "finally", "long",
				"strictfp", "volatile", "const", "float", "native", "super",
				"while" }));

	}
}
