package com.res.java.translation.engine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.Queue;

import com.res.common.RESConfig;
import com.res.java.lib.BaseClass;
import com.res.java.lib.Constants;
import com.res.java.translation.symbol.SymbolConstants;
import com.res.java.translation.symbol.SymbolProperties;
import com.res.java.translation.symbol.SymbolProperties.CobolDataDescription;
import com.res.java.util.JavaCodePrinter;
import com.res.java.util.NameUtil;
import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

public class DataPrinter {
	private Queue<SymbolProperties> group01ToCreate = new LinkedList<SymbolProperties>();
	private Queue<SymbolProperties> innerGroupToCreate = new LinkedList<SymbolProperties>();

	private String[] javaType = new String[]{
			"byte", "char", "short", "int", "long", "float", "double", "BigDecimal", "String"
	};
	
	public void printDataForProgram(SymbolProperties props,
			JavaCodePrinter printer) throws IOException {
		if (!props.isProgram())
			return;


		printer.increaseIndent();
		
		if (props.getLength() > 0) {
//			printer.println("// byte size: " + props.getLength());
			printer.println("public " + props.getJavaName2() + "() {");
			printer.increaseIndent();
			printer.println(String.format("super(%d);", props.getLength()));
			printer.decreaseIndent();
			printer.println("}");
			printer.println();
		}
		
		printDataChildren(props, printer);

		printer.decreaseIndent();

		while (group01ToCreate.size() > 0) {
			createGroup01File(group01ToCreate.poll());
		}
	}

	private void createGroup01File(SymbolProperties props) throws IOException {
		// create new file
		String fileName = RESConfig.getInstance().getDataPackage()
				+ File.separatorChar + NameUtil.getFileName(props);
		System.out.println("Create file " + fileName);

//		JavaCodePrinter printer = new JavaCodePrinter(System.out);
		JavaCodePrinter printer = new JavaCodePrinter(new FileOutputStream(
				fileName));

		// print package
		printer.println("package " + RESConfig.getInstance().getDataPackage()
				+ ";");
		printer.println();

		// print import
		printer.printImport(BaseClass.class);
		printer.printImport(BigDecimal.class);
		printer.println();

		// print class definition
		printer.println("public class " + props.getJavaName2() + " extends "
				+ BaseClass.class.getSimpleName() + " {");

		printer.increaseIndent();

		overrideConstructor(props.getJavaName2(), printer);
		
		// if use bytes
		if (props.getLength() > 0) {
			printer.println("//byte size: " + props.getLength());
		}

		// print all children of this group
		printDataChildren(props, printer);

		printer.decreaseIndent();

		// end class
		printer.println("}");

		// print other inner groups
		while (innerGroupToCreate.size() > 0) {
			createInnerGroupClass(innerGroupToCreate.poll(), printer);
		}

		printer.close();
	}

	private void createInnerGroupClass(SymbolProperties props,
			JavaCodePrinter printer) {
		// print class definition
		printer.println("class " + props.getJavaName2() + " extends "
				+ BaseClass.class.getSimpleName() + " {");

		printer.increaseIndent();

		overrideConstructor(props.getJavaName2(), printer);
		
		// if use bytes
		if (props.getLength() > 0) {
			printer.println("//byte size: " + props.getLength());
		}

		// print all children of this group
		printDataChildren(props, printer);

		printer.decreaseIndent();

		// end class
		printer.println("}");
		printer.println();
	}

	private void printDataChildren(SymbolProperties props,
			JavaCodePrinter printer) {
		if (props.getChildren() == null || props.getChildren().size() == 0)
			return;
		for (SymbolProperties p : props.getChildren()) {
			if (p.getType() != SymbolConstants.DATA || p.isFromRESLibrary()) {
				continue;
			}
//			printEachChild(p, printer);
			short lvNumber = p.getLevelNumber();
			if (lvNumber == 66) {
				printLv66Data(p, printer);
			} else if (lvNumber == 88) {
				printLv88Data(p, printer);
			} else {
				if (p.getPictureString() == null) {
					// group
					printGroupData(p, printer);
				} else {
					// element
					printElementData(p, printer);
				}
			}
		}
	}


	private void printLv66Data(SymbolProperties props, JavaCodePrinter printer) {
		// renames field
		printer.println("//Create getter, setter for renames field "
				+ props.getDataName());
		printer.println(String.format("//Offset = %d, length = %d", props
				.getUnAdjustedOffset(), props.getLength()));
		printer.println();
	}
	
	private void printLv88Data(SymbolProperties props, JavaCodePrinter printer) {
		printer.println("//Create getter, setter for condition-name" + props.getDataName());
		printer.println();
	}

	private void printGroupData(SymbolProperties props, JavaCodePrinter printer) {
		if (props.getLevelNumber() == 1) {
			// create new file for 01 groups
			group01ToCreate.add(props);
		} else {
			innerGroupToCreate.add(props);
		}

		if (props.isOccurs()) {
			// create array field
			printer.println(String.format(
					"private %1$s %2$s[] = new %1$s[%3$d];", props
							.getJavaName2(), props.getJavaName1(),
					props.getMaxOccursInt()));
			if (props.getLength() > 0) { // use byte
				printer.println("{");
				printer.increaseIndent();
				
				// loop to initialize each group
				printer.println(String.format("for (int i = 0; i < %d; i++) {", props.getMaxOccursInt()));
				printer.increaseIndent();
				printer.println(String.format("%1$s[i] = new %2$s(this.getBytes(), %3$d + i*%4$d, %4$d);",
									props.getJavaName1(),
									props.getJavaName2(),
									props.getOffset(),
									props.getLength()));
				printer.decreaseIndent();
				printer.println("}");
				printer.decreaseIndent();
				printer.println("}");
				
			} else { // use java types
				printer.println("{");
				printer.increaseIndent();
				printer.println(String.format("for (%1$s e_ : %2$s) {",
						props.getJavaName2(), props.getJavaName1()));
				printer.increaseIndent();
				printer.println(String.format("e_ = new %1$s();", props
						.getJavaName2()));
				printer.decreaseIndent();
				printer.println("}");
				printer.decreaseIndent();
				printer.println("}");
			}
			
			// create getter, setter with index
		} else {
			if (props.getLength() > 0) { // use byte
				/*printer.println("//create field for group "
						+ props.getDataName());
				printer.println("//Offset = "
						+ props.getOffset() + " length = "
						+ props.getLength());*/
				printer.println(String.format("private final %1$s %2$s = new %1$s(this.getBytes(), %3$d, %4$d);",
										props.getJavaName2(),
										props.getJavaName1(),
										props.getOffset(),
										props.getLength()));
				
				printer.println();
				// create getter
				printer.println(String.format("public %1$s get%1$s() {",
						props.getJavaName2()));
				printer.increaseIndent();
				printer.println(String.format("return this.%s;", props
						.getJavaName1()));
				printer.decreaseIndent();
				printer.println("}");
				printer.println();
				
				// create setter
				String argName = "input";
				/*printer.println(String.format(
						"public void set%1$s(%1$s %2$s){", props
						.getJavaName2(), props.getJavaName1()));
				printer.increaseIndent();
				printer.println(String.format("this.%1$s = %1$s;", props
						.getJavaName1()));
				printer.decreaseIndent();
				printer.println("}");
				printer.println();*/
				printer.println(String.format(
						"public void set%s(String %s) {", props.getJavaName2(), argName));
				printer.increaseIndent();
				printer.println(setValueMethodName(props, argName) + ";");
				printer.decreaseIndent();
				printer.println("}");
				printer.println();
				
			} else { // use java types
				printer.println(String.format(
						"private %1$s %2$s = new %1$s();", props
								.getJavaName2(), props.getJavaName1()));
			}
		}
	}

	private void printElementData(SymbolProperties props,
			JavaCodePrinter printer) {
		String type = javaType[props.getCobolDesc().getTypeInJava()];
		if (props.isOccurs()) {
			if (props.getLength() > 0) {
				// create getter setter with index
				printer
						.println("//Create getter, setter with index for "
								+ props.getDataName());
				printer.println("//Each element length: "
						+ props.getLength());
			} else {
				// use java type, so create new array field
				printer.println(String.format(
						"private %1$s %2$s[] = new %1$s[%3$d];", type,
						props.getJavaName2(), props.getMaxOccursInt()));

				// create getter, setter with index
				printer.println("//Create getter, setter for "
						+ props.getDataName());
			}
		} else {

			if (props.getLength() > 0) {// use byte
				// create getter setter for elements
				printer.println("//Create getter, setter for "
						+ props.getDataName() + " pic = " + props.getPictureString());
				printer.println("//Offset = "
						+ props.getOffset() + " length = "
						+ props.getLength());
				
				//getter
				printer.println(String.format("public %s get%s() {", type, props.getJavaName2()));
				printer.increaseIndent();
				printer.println("return " + getValueMethodName(props) + ";");
				printer.decreaseIndent();
				printer.println("}");
				printer.println();
				
				//setter
				String argName = "input";
				printer.println(String.format("public void set%s(%s %s) {", props.getJavaName2(), type, argName));
				printer.increaseIndent();
				printer.println(setValueMethodName(props, argName) + ";");
				printer.decreaseIndent();
				printer.println("}");
				printer.println();
			} else {// use java types
				// create field first
				printer.println("//Create field for "
						+ props.getDataName());
				// create getter, setter
				printer.println("//Create getter, setter for"
						+ props.getDataName());
				printer.println("//Use Java type");
			}
		}

		printer.println();
	}
	
	private void overrideConstructor(String className, JavaCodePrinter printer) {
		printer.println("public " + className + "() {");
		printer.increaseIndent();
		printer.println("super(0);");
		printer.decreaseIndent();
		printer.println("}");
		printer.println();
		
		/*printer.println("public " + className + "(int size) {");
		printer.increaseIndent();
		printer.println("super(size);");
		printer.decreaseIndent();
		printer.println("}");
		printer.println();
		
		printer.println("public " + className + "(byte[] data) {");
		printer.increaseIndent();
		printer.println("super(data);");
		printer.decreaseIndent();
		printer.println("}");
		printer.println();*/
		
		printer.println("public " + className + "(byte[] data, int offset, int length) {");
		printer.increaseIndent();
		printer.println("super(data, offset, length);");
		printer.decreaseIndent();
		printer.println("}");
		printer.println();
	}
	
	private String getValueMethodName(SymbolProperties props) {
		CobolDataDescription desc = props.getCobolDesc();
		byte type = desc.getTypeInJava();
		byte usage = desc.getUsage();
		
		if (type == Constants.SHORT || type == Constants.INTEGER) {
			String typeStr = javaType[type];
			if (usage == Constants.DISPLAY) {
				return String.format("(%s) getLongDisplay(%d, %d, %b, %b, %b, %d)",
										typeStr,
										props.getOffset(),
										props.getLength(),
										desc.isSigned(),
										desc.isSignLeading(),
										desc.isSignSeparate(),
										desc.getMaxScalingLength());
			} else if (usage == Constants.BINARY) {
				return String.format("(%s) getLongBytes(%d, %d, %b, %d, %d)",
										typeStr,
										props.getOffset(),
										props.getLength(),
										desc.isSigned(),
										desc.getMaxIntLength(),
										desc.getMaxScalingLength());
			} else if (usage == Constants.PACKED_DECIMAL) {
				return String.format("(%s) getLongBCD(%d, %d, %b, %d, %d)",
										typeStr,
										props.getOffset(),
										props.getLength(),
										desc.isSigned(),
										desc.getMaxIntLength(),
										desc.getMaxScalingLength());
			}
		} else if (type == Constants.LONG) {
			if (usage == Constants.DISPLAY) {
				return String.format("getLongDisplay(%d, %d, %b, %b, %b, %d)",
										props.getOffset(),
										props.getLength(),
										desc.isSigned(),
										desc.isSignLeading(),
										desc.isSignSeparate(),
										desc.getMaxScalingLength());
			} else if (usage == Constants.BINARY) {
				return String.format("getLongBytes(%d, %d, %b, %d, %d)",
										props.getOffset(),
										props.getLength(),
										desc.isSigned(),
										desc.getMaxIntLength(),
										desc.getMaxScalingLength());
			} else if (usage == Constants.PACKED_DECIMAL) {
				return String.format("getLongBCD(%d, %d, %b, %d, %d)",
										props.getOffset(),
										props.getLength(),
										desc.isSigned(),
										desc.getMaxIntLength(),
										desc.getMaxScalingLength());
			}
		}else if (type == Constants.STRING || type == Constants.GROUP) {
			return String.format("getStringDisplay(%d, %d)",
									props.getOffset(),
									props.getLength());
		} else if (type == Constants.BIGDECIMAL) {
			if (usage == Constants.DISPLAY) {
				return String.format("getBigDecimalDisplay(%d, %d, %b, %b, %b, %d)",
										props.getOffset(),
										props.getLength(),
										desc.isSigned(),
										desc.isSignLeading(),
										desc.isSignSeparate(),
										desc.getMaxFractionLength() + desc.getMaxScalingLength());
			} else if (usage == Constants.BINARY) {
				return String.format("getBigDecimalBytes(%d, %d, %b, %d, %d, %d)",
										props.getOffset(),
										props.getLength(),
										desc.isSigned(),
										desc.getMaxIntLength(),
										desc.getMaxFractionLength(),
										desc.getMaxScalingLength());
			} else if (usage == Constants.PACKED_DECIMAL) {
				return String.format("getBigDecimalBCD(%d, %d, %b, %d, %d, %d)",
										props.getOffset(),
										props.getLength(),
										desc.isSigned(),
										desc.getMaxIntLength(),
										desc.getMaxFractionLength(),
										desc.getMaxScalingLength());
			}
		}
		return "";
	}
	
	private String setValueMethodName(SymbolProperties props, String argName) {
		CobolDataDescription desc = props.getCobolDesc();
		byte type = desc.getTypeInJava();
		byte usage = desc.getUsage();
		
		if (type == Constants.SHORT || type == Constants.INTEGER || type == Constants.LONG) {
			if (usage == Constants.DISPLAY) {
				return String.format("setLongDisplay(%s, %d, %d, %b, %b, %b, %d, %d)",
										argName,
										props.getOffset(),
										props.getLength(),
										desc.isSigned(),
										desc.isSignLeading(),
										desc.isSignSeparate(),
										desc.getMaxIntLength(),
										desc.getMaxScalingLength());
			} else if (usage == Constants.BINARY) {
				return String.format("setLongBytes(%s, %d, %d, %b, %d, %d)",
										argName,
										props.getOffset(),
										props.getLength(),
										desc.isSigned(),
										desc.getMaxIntLength(),
										desc.getMaxScalingLength());
			} else if (usage == Constants.PACKED_DECIMAL) {
				return String.format("setLongBCD(%s, %d, %d, %b, %d, %d)",
										argName,
										props.getOffset(),
										props.getLength(),
										desc.isSigned(),
										desc.getMaxIntLength(),
										desc.getMaxScalingLength());
			}
		} else if (type == Constants.STRING || type == Constants.GROUP) {
			return String.format("setStringDisplay(%s, %d, %d, %b)",
									argName,
									props.getOffset(),
									props.getLength(),
									desc.isJustifiedRight());
		} else if (type == Constants.BIGDECIMAL) {
			if (usage == Constants.DISPLAY) {
				return String.format("setBigDecimalDisplay(%s, %d, %d, %b, %b, %b, %d, %d, %d)",
										argName,
										props.getOffset(),
										props.getLength(),
										desc.isSigned(),
										desc.isSignLeading(),
										desc.isSignSeparate(),
										desc.getMaxIntLength(),
										desc.getMaxFractionLength(),
										desc.getMaxScalingLength());
			} else if (usage == Constants.BINARY) {
				return String.format("setBigDecimalBytes(%s, %d, %d, %b, %d, %d, %d)", 
										argName,
										props.getOffset(),
										props.getLength(),
										desc.isSigned(),
										desc.getMaxIntLength(),
										desc.getMaxFractionLength(),
										desc.getMaxScalingLength());
			} else if (usage == Constants.PACKED_DECIMAL) {
				return String.format("setBigDecimalBCD(%s, %d, %d, %b, %d, %d, %d)",
										argName,
										props.getOffset(),
										props.getLength(),
										desc.isSigned(),
										desc.getMaxIntLength(),
										desc.getMaxFractionLength(),
										desc.getMaxScalingLength());
			}
		}
		return "";
	}
}