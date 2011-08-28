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

import com.res.common.RESConfig;
import com.res.java.lib.RunTimeUtil;
import com.res.java.translation.symbol.SymbolProperties;
import com.res.java.translation.symbol.SymbolTable;

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

	public static String getFileName(SymbolProperties props) {
		return getJavaName2(props) + ".java";
	}

	public static String getBeanInfoFileName(SymbolProperties props2,
			boolean isData) {
		String s = getJavaName2(props2) + "BeanInfo.java";
		return s;
	}

	public static String getPackageName(SymbolProperties props2,
			boolean programPackage) {

		RESConfig config = RESConfig.getInstance();

		if (programPackage)
			return config.getProgramPackage().replace('\\', '.');
		else {

			SymbolProperties props = props2;

			props = SymbolTable.getInstance().getFirstProgram();

			if (props != null && !props.isProgram())
				props = null;

			return config.getDataPackage().replace('\\', '.')
					+ ((RESConfig.getInstance().isLongDataPackageName() && props != null) ? ('.' + props
							.getJavaName1().toLowerCase()) : "");

		}
	}

	public static String getPathName(String fileName, String suffix,
			boolean isData) {
		String p = null;
		if (isData)
			p = RESConfig.getInstance().getDataPackage();
		else
			p = RESConfig.getInstance().getProgramPackage();
		p = p.toLowerCase().replace('.', '\\') + '\\';
		if (suffix != null && suffix.trim().length() > 0)
			p += suffix.toLowerCase().replace('.', '\\');
		p += '\\' + NameUtil.getClassName(fileName, suffix, isData);
		p += ".java";

		return p;
	}


	public static String getClassName(String fileName, String suffix,
			boolean isData) {
		String c = convertCobolNameToJava(fileName, true);
		if (suffix != null && suffix.trim().length() > 0) {
			c += convertCobolNameToJava(suffix, true);
		}
		return c;
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

	public static String getJavaName1(SymbolProperties props) {
		if (props.getJavaName1() != null)
			return props.getJavaName1();
		RunTimeUtil.getInstance().reportError(
				"Error: Java name for " + props.getDataName()
						+ " is null. Contact RES support.", true);
		return "nullName";

	}

	public static String getJavaName2(SymbolProperties props) {
		if (props.getJavaName2() != null)
			return props.getJavaName2();
		RunTimeUtil.getInstance().reportError(
				"Error: Java name for " + props.getDataName()
						+ " is null. Contact RES support.", true);
		return "NullName";
	}
}
