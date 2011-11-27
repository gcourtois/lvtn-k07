package com.res.java.translation.engine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import com.res.cobol.visitor.PrintSourceAsComment;
import com.res.common.RESConfig;
import com.res.demo.util.GenDetails;
import com.res.java.lib.BaseClass;
import com.res.java.lib.Constants;
import com.res.java.translation.symbol.SymbolProperties;
import com.res.java.translation.symbol.SymbolProperties.CobolDataDescription;
import com.res.java.translation.symbol.SymbolProperties.CoupleValue;
import com.res.java.util.FileUtil;
import com.res.java.util.JavaCodePrinter;

public class DataPrinter {
	private Queue<SymbolProperties> group01ToCreate = new LinkedList<SymbolProperties>();
	private Queue<SymbolProperties> innerGroupToCreate = new LinkedList<SymbolProperties>();

	private static String[] javaTypeStr = new String[] { "byte", "char", "short", "int",
            "long", "float", "double", "BigDecimal", "String" };
	
	private static String setGroupMethodName = "_setValue";
	private static String getFromBytesMethodName = "_getFromBytes";
	private static String setToBytesMethodName = "_setToBytes";
	private static String initMethodName = "_init";
	
	private boolean useJava = (RESConfig.getInstance().getOptimizeAlgorithm() == 1);
	
	private PrintSourceAsComment dumper;
	private String currentOutputFile;
	private GenDetails genDetails = GenDetails.getInstance();
	
	private String className(SymbolProperties props) {
        return props.getJavaName2();
    }

    private String fieldName(SymbolProperties props) {
        return props.getJavaName1();
    }

    private String editorName(SymbolProperties props) {
        return "_" + props.getJavaName1() + "_editor";
    }

    public final static String getMethodName(String name) {
        return "get" + name;
    }
    
    private String getMethodName(SymbolProperties props) {
        return getMethodName(className(props));
    }
    
    public final static String setMethodName(String name) {
        return "set" + name;
    }
    
    private String setMethodName(SymbolProperties props) {
        return setMethodName(className(props));
    }
    
    public final static String getAsStringName(String name) {
        return getMethodName(name) + "String";
    }
    
    private String getAsStringName(SymbolProperties props) {
        return getAsStringName(className(props));
    }
    
    private String getAsDoubleName(SymbolProperties props) {
        return getMethodName(className(props)) + "Double";
    }
    
    private boolean genJava(SymbolProperties props) {
        return (useJava && (props.getAdjustedLength() != props.getLength()));
    }
    
    public void printDataForProgram(SymbolProperties props,
			JavaCodePrinter printer) throws Exception {
		if (!props.isProgram())
			return;

		currentOutputFile = FileUtil.getProgramFilePath(FileUtil.getJavaFileName(props));
		
		printer.println("public " + className(props) + "() {");
		printer.increaseIndent();
		if (props.getLength() > 0) {
		    printer.println(String.format("super(%d);", props.getLength()));
		}
		printer.println(initMethodName + "();");
		printer.decreaseIndent();
		printer.println("}");
		printer.println();
		
		printInitMethod(props, printer);
		
		dumper = new PrintSourceAsComment(printer);
		dumper.printSpecials(false);
		
		printDataChildren(props, printer);

		while (group01ToCreate.size() > 0) {
			createGroup01File(group01ToCreate.poll());
		}
	}

    private void printEntryComment(SymbolProperties props, JavaCodePrinter printer) throws Exception {
//        printer.print("// " + props.getDataDescriptionEntry().line + " ");
        printer.print("// ");
        dumper.startAtNextToken();
        dumper.visit(props.getDataDescriptionEntry());
        printer.println();
    }
    
	private void createGroup01File(SymbolProperties props) throws Exception {
        // create new file
        String fileName = FileUtil.getJavaFileName(props);
        currentOutputFile = FileUtil.getDataFilePath(fileName);
        
        JavaCodePrinter printer = new JavaCodePrinter(FileUtil.newDataFile(fileName));
        dumper = new PrintSourceAsComment(printer);
        dumper.printSpecials(false);
        
        // print package
        printer.println("package " + RESConfig.getInstance().getDataPackage()
                + ";");
        printer.println();

        // print import
//        printer.printImport(BaseClass.class);
//        printer.printImport(EditedVar.class);
        printer.printImport("com.res.java.lib.*");
        printer.printImport(BigDecimal.class);
        printer.println();

        // print class definition
        printer.println("public class " + className(props) + " extends "
                + BaseClass.class.getSimpleName() + " {");

        printer.increaseIndent();

        overrideConstructor(props, printer);

        printInitMethod(props, printer);
        
        printSetValueForGroup(props, printer);
        
        // print all children of this group
        printDataChildren(props, printer);
        
        if (genJava(props)) {
            printSetToBytesMethod(props, printer);
            printGetFromBytesMethod(props, printer);
            overrideToStringForGroup(props, printer);
        }

        // print other inner groups
        while (innerGroupToCreate.size() > 0) {
            createInnerGroupClass(innerGroupToCreate.poll(), printer);
        }

        printer.decreaseIndent();

        // end class
        printer.println("}");

        printer.close();
    }

	private void createInnerGroupClass(SymbolProperties props,
            JavaCodePrinter printer) throws Exception {
        // print class definition
        printer.println("public class " + className(props) + " extends "
                + BaseClass.class.getSimpleName() + " {");

        printer.increaseIndent();

        overrideConstructor(props, printer);

        printInitMethod(props, printer);
        
        printSetValueForGroup(props, printer);
        
        // print all children of this group
        printDataChildren(props, printer);
        
        if (genJava(props)) {
            printSetToBytesMethod(props, printer);
            printGetFromBytesMethod(props, printer);
            overrideToStringForGroup(props, printer);
        }
        
        printer.decreaseIndent();

        // end class
        printer.println("}");
        printer.println();
    }

	private void printDataChildren(SymbolProperties props,
			JavaCodePrinter printer) throws Exception {
		if (!props.hasChildren())
			return;
		for (SymbolProperties p : props.getChildren()) {
			if (!p.isData() || p.isFromRESLibrary() ) {
				continue;
			}
			
			int beginLine = printer.getCurrentLine();
			
			printEntryComment(p, printer);
			
			short lvNumber = p.getLevelNumber();
			if (lvNumber == 66) {
				printLv66Data(p, printer);
			} else if (lvNumber == 88) {
				printLv88Data(p, printer);
			} else {
				if (p.isGroupData()) {
					// group
					printGroupData(p, printer);
				} else {
					// element
					printElementData(p, printer);
				}
			}
			
			int endLine = printer.getCurrentLine() - 1;
			genDetails.add(p.getDataDescriptionEntry().line + 1, genDetails.new OutputInfo(currentOutputFile, beginLine, endLine));
		}
	}


	private void printLv66Data(SymbolProperties props, JavaCodePrinter printer) throws Exception {
//	    printEntryComment(props, printer);
	    
		// renames field always use byte array
		byte typeInJava = props.getCobolDesc().getTypeInJava();
		
		if (typeInJava == Constants.STRING || typeInJava == Constants.GROUP) {
		    printStringGetter(props, printer);
		    printStringSetter(props, printer);
		} else {
		    printNumericGetter(props, printer);
		    printNumericSetter(props, printer);
		}
	}
	
	private void printLv88Data(SymbolProperties props, JavaCodePrinter printer) throws Exception {
//	    printEntryComment(props, printer);
	    
	    SymbolProperties parent = props.getParent();
	    String indexName = parent.isOccurs() ? "i" : "";
	    String valName = "val";
	    String valType = javaTypeStr[parent.getCobolDesc().getTypeInJava()];
//	    printer.println("//Create getter, setter for condition-name"
//                + props.getDataName());
        printer.beginMethod("public", "boolean", getMethodName(props), indexName == "" ? null : new String[]{"int " + indexName}, null);
        printer.println(String.format("%s %s = %s(%s);", valType, valName, getMethodName(parent), indexName));

        StringBuilder expr = new StringBuilder();
        ArrayList<CoupleValue> values = props.getValues();
        LiteralString firstVal = values.get(0).value1;
        firstVal.convertToPrint();
        expr.append(String.format("Compare.equal(%s, %s)", valName, firstVal.toString()));
        
        for (int i = 1; i < values.size(); i++) {
            LiteralString v = values.get(i).value1;
            v.convertToPrint();
            expr.append(String.format(" || Compare.equal(%s, %s)", valName, v.toString()));
        }
        
        printer.println("return " + expr.toString() + ";");
        printer.endMethod();
        printer.println();
        
        printer.beginMethod("public", "void", setMethodName(props), indexName == "" ? null : new String[]{"int " + indexName}, null);
        printer.println(String.format("%s(%s%s);", setMethodName(parent), indexName == "" ? "" : indexName + ", ", firstVal.toString()));
        printer.endMethod();
        printer.println();
    }

	private void printGroupData(SymbolProperties props, JavaCodePrinter printer) throws Exception {
//		printEntryComment(props, printer);
	    if (props.is01Group()) {
	        group01ToCreate.add(props);
	    } else {
	        innerGroupToCreate.add(props);
	    }
		
		String argName = "input";
		String arraySpecifier = "";
		String indexName = null;
		
		if (props.isOccurs()) {
		    indexName = "i";
		    arraySpecifier = "[" + indexName + "]";
		    
		    // create array field
		    printer.println(String.format(
		            "private %1$s %2$s[] = new %1$s[%3$d];", className(props),
		            fieldName(props), props.getMaxOccursInt()));

		    printer.beginBlock(); // initialize
		    printer.println(String.format("for (int i = 0; i < %d; i++) {", props.getMaxOccursInt()));
		    printer.increaseIndent();
		    printer.println(String.format("%s[i] = new %s(this.getBytes(), %s, %s);",
		            fieldName(props),
		            className(props),
		            getOffsetWithIndex(props, "i"),
		            props.getLength()));
		    printer.endBlock(); // end loop
		    printer.endBlock(); // end initialize block
		    
		} else {
		    
		    // create field for group
		    printer.println(String.format("private %1$s %2$s = new %1$s(this.getBytes(), %3$d, %4$d);",
                    className(props),
                    fieldName(props),
                    props.getGlobalOffset(),
                    props.getLength()));
		}
		printer.println();
		
		// getter
        printer.beginMethod("public", className(props), getMethodName(props), new String[]{indexName == null ? null : "int " + indexName}, null);
        printer.println(String.format("return this.%s%s;", fieldName(props), arraySpecifier));
        printer.endMethod();
        printer.println();
        
        // set String
        printer.beginMethod("public", "void", setMethodName(props), new String[]{indexName == null ? null : "int " + indexName, "String " + argName}, null);
        printer.println(String.format("this.%s%s.%s(%s);", fieldName(props), arraySpecifier, setGroupMethodName, argName));
        printer.endMethod();
        printer.println();
        
        // set long
        printer.beginMethod("public", "void", setMethodName(props), new String[]{indexName == null ? null : "int " + indexName, "long " + argName}, null);
        printer.println(String.format("this.%s%s.%s(%s(%s));", fieldName(props), arraySpecifier, setGroupMethodName, "unsignedValue", argName));
        printer.endMethod();
        printer.println();
	}

	private void printElementData(SymbolProperties props,
			JavaCodePrinter printer) throws Exception {
//	    printEntryComment(props, printer);
	    
		// create field if use java
		if (genJava(props)) {
		    if (props.isOccurs()) { // create array
		        printer.println(String.format(
		                "private %1$s %2$s[] = new %1$s[%3$d];", javaClassName(props),
		                fieldName(props), props.getMaxOccursInt()));

		        printer.beginBlock(); // initialize
		        printer.println(String.format("for (int i = 0; i < %d; i++) {", props.getMaxOccursInt()));
		        printer.increaseIndent();
		        printer.println(String.format("%s[i] = new %s;", fieldName(props), constructorForJavaField(props, getOffsetWithIndex(props, "i"))));
		        printer.endBlock(); // end loop
		        printer.endBlock(); // end initialize block
		    } else { // no occurs, just create field
		        printer.println(String.format("private %s %s = new %s;",
		                javaClassName(props), fieldName(props),
		                constructorForJavaField(props, getOffsetWithoutIndex(props))));
		    }
		}
		
		// create getter, setter
		if (props.getCobolDesc().getTypeInJava() == Constants.STRING) {
		    printStringGetter(props, printer);
		    printStringSetter(props, printer);
		} else {
		    printNumericGetter(props, printer);
		    printNumericSetter(props, printer);
		}
		
		// if has child, then must be lv88-entry
		printDataChildren(props, printer);
	}
	
	private void printInitMethod(SymbolProperties props, JavaCodePrinter printer) {
	    printer.beginMethod("private", "void", initMethodName, null, null);
	    if (props.hasChildren()) {
	        for (SymbolProperties child : props.getChildren()) {
	            if (!child.isData())
	                continue;

	            if (child.getValues() != null && child.getValues().size() > 0) {
	                LiteralString val = child.getValues().get(0).value1;
	                val.convertToPrint();
	                String input = val.literal.toString();

	                if (val.javaType == Constants.STRING) {
	                    if (child.isOccurs()) {
	                        printer.println(String.format("for(int i = 0; i < %s; i++) {", child.getMaxOccursInt()));
	                        printer.increaseIndent();
	                        if (genJava(child)) {
	                            printer.println(setValueDirectly(child, input, "i") + ";");
	                        } else {
	                            printer.println(setStringMethod(child, input, getOffsetWithIndex(child, "i")) + ";");
	                        }
	                        printer.endBlock();
	                    } else {
	                        if (genJava(child)) {
	                            printer.println(setValueDirectly(child, input, null) + ";");
	                        } else {
	                            printer.println(setStringMethod(child, input, getOffsetWithoutIndex(child)) + ";");
	                        }
	                    }
	                } else {
	                    /*if (val.javaType == Constants.BIGDECIMAL) {
	                    input = String.format("new BigDecimal(\"%s\")", input);
	                }*/
	                    if (child.isOccurs()) {
	                        printer.println(String.format("for(int i = 0; i < %s; i++) {", child.getMaxOccursInt()));
	                        printer.increaseIndent();
	                        printer.println(String.format("%s(i, %s);", setMethodName(child), input));
	                        printer.endBlock();
	                    } else {
	                        printer.println(String.format("%s(%s);", setMethodName(child), input));
	                    }
	                }
	            }
	            
	        }
	    }
	    printer.endMethod();
	    printer.println();
	}
	
	private void printNumericGetter(SymbolProperties props, JavaCodePrinter printer) {
	    byte typeInJava = props.getCobolDesc().getTypeInJava();
	    String type = javaTypeStr[typeInJava];
	    String offset = "";
	    String[] params = null;
	    
	    String indexName = "";
	    if (props.isOccurs()) {
	        indexName = "i";
	        params = new String[]{"int " + indexName};
	        offset = getOffsetWithIndex(props, indexName);
	    } else {
	        offset = getOffsetWithoutIndex(props);
	    }
	    
	    printer.beginMethod("public", type, getMethodName(props), params, null);
	    if (genJava(props)) {
	        printer.println("return " + getValueJavaField(props, typeInJava, indexName) + ";");
	    } else {
	        printer.println("return " + getValueMethodName(props, offset) + ";");
	    }
	    printer.endMethod();
	    printer.println();
	    
	    if (typeInJava >= Constants.SHORT && typeInJava <= Constants.LONG) {
	        // get double
	        printer.beginMethod("public", "double", getAsDoubleName(props), params, null);
	        printer.println(String.format("return %s(%s);", getMethodName(props), indexName));
	        printer.endMethod();
	        printer.println();
	    }
	    
	    // string getter
	    printer.beginMethod("public", "String", getAsStringName(props), params, null);
	    if (genJava(props)) {
	        printer.println("return " + getValueJavaField(props, Constants.STRING, indexName) + ";");
	    } else {
	        printer.println(String.format("return String.format(\"%s\", %s);", outputFormatSpecifier(props), getValueMethodName(props, offset)));
	    }
	    printer.endMethod();
	    printer.println();
	}
	
	private String outputFormatSpecifier(SymbolProperties props) {
	    CobolDataDescription desc = props.getCobolDesc();
	    byte typeInJava = desc.getTypeInJava();
	    int len = 0;
	    if (typeInJava >= Constants.SHORT && typeInJava <= Constants.LONG) {
	        len = desc.getMaxIntLength() + desc.getMaxScalingLength();
            if (desc.isSigned())
                len++;
            return String.format("%%%s0%sd", desc.isSigned() ? "+" : "", len);
	    } else if (typeInJava == Constants.BIGDECIMAL) {
	        len = desc.getMaxIntLength() + desc.getMaxFractionLength() + desc.getMaxScalingLength() + 1;
            int preciseLen = desc.getMaxFractionLength() + desc.getMaxScalingLength();
	        if (desc.isSigned())
                len++;
            return String.format("%%%s0%s.%sf", desc.isSigned() ? "+" : "", len, preciseLen);
	    }
	    return "";
	}
	
	private void printNumericSetter(SymbolProperties props, JavaCodePrinter printer) {
	    
	    String indexName = null;
	    String argName = "input";
	    String offset = "";
	    String methodName = setMethodName(props);
	    
	    if (props.isOccurs()) {
	        indexName = "i";
	        offset = getOffsetWithIndex(props, indexName);
	    } else {
	        offset = getOffsetWithoutIndex(props);
	    }
	    
        // set by correct type
	    if (props.getCobolDesc().getTypeInJava() == Constants.SHORT
	            || props.getCobolDesc().getTypeInJava() == Constants.INTEGER) {
	        String argType = javaTypeStr[props.getCobolDesc().getTypeInJava()] + " ";
	        printer.beginMethod("public", "void", methodName, new String[]{indexName == null ? null : "int " + indexName, argType + argName}, null);
	        if (genJava(props)) {
	            printer.println(setValueJavaField(props, argName, indexName) + ";");
	        } else {
	            printer.println(setValueMethodName(props, argName, offset) + ";");
	        }
	        printer.endMethod();
	        printer.println();
	        
	        // set long
            printer.beginMethod("public", "void", methodName, new String[]{indexName == null ? null : "int " + indexName, "long " + argName}, null);
            if (genJava(props)) {
                printer.println(setValueJavaField(props, argName, indexName) + ";");
            } else {
                printer.println(setLongMethod(props, argName, offset) + ";");
            }
            printer.endMethod();
            printer.println();
	    } else {
	        // set long
	        printer.beginMethod("public", "void", methodName, new String[]{indexName == null ? null : "int " + indexName, "long " + argName}, null);
	        if (genJava(props)) {
	            printer.println(setValueJavaField(props, argName, indexName) + ";");
	        } else {
	            printer.println(setValueMethodName(props, argName, offset) + ";");
	        }
	        printer.endMethod();
	        printer.println();
	    }
	    
	    // set double
	    printer.beginMethod("public", "void", methodName, new String[]{indexName == null ? null : "int " + indexName, "double " + argName}, null);
	    printer.println(String.format("%s(%sBigDecimal.valueOf(%s).longValue());", methodName, indexName == null ? "" : indexName + ", ", argName));
	    printer.endMethod();
	    printer.println();
	    
	    // set BigDecimal
	    printer.beginMethod("public", "void", methodName, new String[]{indexName == null ? null : "int " + indexName, "BigDecimal " + argName }, null);
	    if (genJava(props)) {
	        printer.println(setValueJavaField(props, argName, indexName) + ";");
	    } else {
	        printer.println(setValueMethodName(props, argName, offset) + ";");
	    }
	    printer.endMethod();
	    printer.println();
	    
	    // set String
	    printer.beginMethod("public", "void", methodName, new String[]{indexName == null ? null : "int " + indexName, "String " + argName }, null);
	    if (genJava(props)) {
	        printer.println(setValueJavaField(props, argName, indexName) + ";");
	    } else {
	        printer.println(setValueMethodName(props, argName, offset) + ";");
	    }
	    printer.endMethod();
	    printer.println();
	}
	
	private void printStringGetter(SymbolProperties props, JavaCodePrinter printer) {
	    String offset = "";
	    String[] params = null;
	    String indexName = null;
	    
	    if (props.isOccurs()) {
	        indexName = "i";
	        params = new String[]{"int " + indexName};
	        offset = getOffsetWithIndex(props, indexName);
	    } else {
	        offset = getOffsetWithoutIndex(props);
	    }
	    
	    printer.beginMethod("public", "String", getMethodName(props), params, null);
	    if (genJava(props)) {
	        printer.println("return " + getValueJavaField(props, Constants.STRING, indexName) + ";");
	    } else {
	        printer.println("return " + getValueMethodName(props, offset) + ";");
	    }
	    printer.endMethod();
	    printer.println();
	}
	
	private void printStringSetter(SymbolProperties props, JavaCodePrinter printer) {
	    CobolDataDescription desc = props.getCobolDesc();
	    boolean doEdit = false;
	    if (desc.getDataCategory() == Constants.ALPHANUMERIC_EDITED
                || desc.getDataCategory() == Constants.NUMERIC_EDITED) {
            doEdit = true;
            printer.println(
                    String.format("private EditedVar %s = new EditedVar(\"%s\", (byte) %s, %s, %s);",
                                    editorName(props),
                                    desc.getPic(),
                                    desc.getDataCategory(),
                                    desc.isJustifiedRight(),
                                    desc.isBlankWhenZero()));
        }

	    String argName = "input";
	    String editedInput = String.format("%s.doEdit(%s)", editorName(props), argName);
	    
	    String offset = "";
	    String indexName = null;
	    
	    if (props.isOccurs()) {
	        indexName = "i";
	        offset = getOffsetWithIndex(props, indexName);
	    } else {
	        offset = getOffsetWithoutIndex(props);
	    }
	    
	    // set String
	    printer.beginMethod("public", "void", setMethodName(props), new String[]{indexName == null ? null : "int " + indexName, "String " + argName}, null);
        if (genJava(props)) {
            if (doEdit) {
                printer.println(setValueJavaField(props, editedInput, indexName) + ";");
            } else {
                printer.println(setValueJavaField(props, argName, indexName) + ";");
            }
        } else {
            if (doEdit) {
                printer.println(setValueMethodName(props, editedInput, offset) + ";");
            } else {
                printer.println(setValueMethodName(props, argName, offset) + ";");
            }
        }
        printer.endMethod();
        printer.println();
        
        // set long
        printer.beginMethod("public", "void", setMethodName(props), new String[]{indexName == null ? null : "int " + indexName, "long " + argName}, null);
        if (desc.getDataCategory() == Constants.ALPHABETIC
                || desc.getDataCategory() == Constants.ALPHANUMERIC) {
            argName = String.format("unsignedValue(%s)", argName);
        }
        if (genJava(props)) {
            if (doEdit) {
                printer.println(setValueJavaField(props, editedInput, indexName) + ";");
            } else {
                printer.println(setValueJavaField(props, argName, indexName) + ";");
            }
        } else {
            if (doEdit) {
                printer.println(setValueMethodName(props, editedInput, offset) + ";");
            } else {
                printer.println(setValueMethodName(props, argName, offset) + ";");
            }
        }
        printer.endMethod();
        printer.println();
        
        if (desc.getDataCategory() == Constants.NUMERIC_EDITED) {
            // set BigDec
            printer.beginMethod("public", "void", setMethodName(props), new String[]{indexName == null ? null : "int " + indexName, "BigDecimal " + argName}, null);
            if (genJava(props)) {
                printer.println(setValueJavaField(props, editedInput, indexName) + ";");
            } else {
                printer.println(setValueMethodName(props, editedInput, offset) + ";");
            }
            printer.endMethod();
            printer.println();
        }
	}
	
	private void overrideConstructor(SymbolProperties props, JavaCodePrinter printer) {
        // printer.println("public " + className + "() {");
        // printer.increaseIndent(); printer.println("super(0);");
        // printer.decreaseIndent(); printer.println("}"); printer.println();
        //          
        // printer.println("public " + className + "(int size) {");
        // printer.increaseIndent(); printer.println("super(size);");
        // printer.decreaseIndent(); printer.println("}"); printer.println();

	    printer.beginMethod("public", "", className(props), new String[]{"byte[] data", "int offset", "int length"}, null);
        printer.println("super(data, offset, length);");
        printer.println(initMethodName + "();");
        printer.endMethod();
        printer.println();
    }
	
	private void printSetValueForGroup(SymbolProperties props, JavaCodePrinter printer) {
	    String argName = "input";
	    // set string
	    printer.beginMethod("public", "void", setGroupMethodName, new String[]{"String " + argName}, null);
	    printer.println(setValueMethodName(props, argName, getOffsetWithoutIndex(props)) + ";");
	    // if gen java, synchronize byte[] <-> field
	    if (genJava(props)) {
	        printer.println(getFromBytesMethodName + "();");
	    }
	    printer.endMethod();
	    printer.println();
	}
	
	// call for group, java types
	private void overrideToStringForGroup(SymbolProperties props, JavaCodePrinter printer) {
	    if (!genJava(props))
	        return;
	    printer.beginMethod("public", "String", "toString", null, null);
	    printer.println(setToBytesMethodName + "();");
	    printer.println("return " + getValueMethodName(props, getOffsetWithoutIndex(props)) + ";");
	    printer.endMethod();
	}
	
	private void printGetFromBytesMethod(SymbolProperties props, JavaCodePrinter printer) {
	    printer.beginMethod("public", "void", getFromBytesMethodName, null, null);
	    for (SymbolProperties child : props.getChildren()) {
	        if (genJava(child)) {
	            if (child.isOccurs()) {
	                printer.println(String.format("for (int i = 0; i < %s; i++) {", child.getMaxOccursInt()));
	                printer.increaseIndent();
	                if (child.isGroupData()) {
	                    printer.println(String.format("%s[i].%s();", fieldName(child), getFromBytesMethodName));
	                } else {
	                    printer.println(String.format("%s[i].%s();", fieldName(child), "getCurrentValueFromBytes"));
	                }
	                printer.endBlock();
	            } else {
	                if (child.isGroupData()) {
	                    printer.println(String.format("%s.%s();", fieldName(child), getFromBytesMethodName));
	                } else {
	                    printer.println(String.format("%s.%s();", fieldName(child), "getCurrentValueFromBytes"));
	                }
	            }
	        }
	    }
	    printer.endMethod();
	    printer.println();
	}
	
	private void printSetToBytesMethod(SymbolProperties props, JavaCodePrinter printer) {
	    printer.beginMethod("public", "void", setToBytesMethodName, null, null);
        for (SymbolProperties child : props.getChildren()) {
            if (genJava(child)) {
                if (child.isOccurs()) {
                    printer.println(String.format("for (int i = 0; i < %s; i++) {", child.getMaxOccursInt()));
                    printer.increaseIndent();
                    if (child.isGroupData()) {
                        printer.println(String.format("%s[i].%s();", fieldName(child), setToBytesMethodName));
                    } else {
                        printer.println(String.format("%s[i].%s();", fieldName(child), "setCurrentValueToBytes"));
                    }
                    printer.endBlock();
                } else {
                    if (child.isGroupData()) {
                        printer.println(String.format("%s.%s();", fieldName(child), setToBytesMethodName));
                    } else {
                        printer.println(String.format("%s.%s();", fieldName(child), "setCurrentValueToBytes"));
                    }
                }
            }
        }
        printer.endMethod();
        printer.println();
	}
	
	private String getOffsetWithIndex(SymbolProperties props, String indexArg) { // props have occurs
	    if (!props.isAParentInOccurs()) { // parent doesn't have occurs, mean props is outer-most item
	        if (props.getGlobalOffset() == 0) {
	            // also first entry of storage area
	            return String.format("%s * %s", indexArg, props.getLength());
	        } else {
	            return String.format("%s + %s * %s", props.getGlobalOffset(), indexArg, props.getLength());
	        }
	    } else {
	        // entry is inside an entry that has occurs
	        if (props.getOffset() == 0) {
	            return String.format("this.offset + %s * %s", indexArg, props.getLength());
	        } else {
	            return String.format("this.offset + %s + %s * %s", props.getOffset(), indexArg, props.getLength());
	        }
	    }
	}
	
	private String getOffsetWithoutIndex(SymbolProperties props) { // props don't have occurs
	    if (props.isAParentInOccurs()) {
	        if (props.getOffset() == 0) {
	            return "this.offset";
	        } else {
	            return "this.offset + " + props.getOffset();
	        }
	    } else {
	        return props.getGlobalOffset() + "";
	    }
	}
	
	private String getValueMethodName(SymbolProperties props, String offset) {
		byte type = props.getCobolDesc().getTypeInJava();
		
		if (type == Constants.SHORT) {
		    return "(short) " + getIntMethod(props, offset);
		} else if (type == Constants.INTEGER) {
		    return getIntMethod(props, offset);
		} else if (type == Constants.LONG) {
		    return getLongMethod(props, offset);
		} else if (type == Constants.BIGDECIMAL) {
		    return getBigDecMethod(props, offset);
		} else if (type == Constants.GROUP || type == Constants.STRING) {
		    return getStringMethod(props, offset);
		}
		
		return "";
	}
	
	private String setValueMethodName(SymbolProperties props, String argName, String offset) {
		byte type = props.getCobolDesc().getTypeInJava();
		
		if (type == Constants.SHORT || type == Constants.INTEGER) {
		    return setIntMethod(props, argName, offset);
		} else if (type == Constants.LONG) {
		    return setLongMethod(props, argName, offset);
		} else if (type == Constants.BIGDECIMAL) {
		    return setBigDecMethod(props, argName, offset);
		} else if (type == Constants.STRING || type == Constants.GROUP) {
		    return setStringMethod(props, argName, offset);
		}
		
		return "";
	}

	private String getIntMethod(SymbolProperties props, String offset) {
	    CobolDataDescription desc = props.getCobolDesc();
	    byte usage = desc.getUsage();
	    if (usage == Constants.DISPLAY) {
            return String.format("getIntDisplay(%s, %s, %s, %s, %s, %s)",
                                    offset,
                                    props.getLength(),
                                    desc.isSigned(),
                                    desc.isSignLeading(),
                                    desc.isSignSeparate(),
                                    desc.getMaxScalingLength());
        } else if (usage == Constants.BINARY) {
            return String.format("getIntBytes(%s, %s, %s, %s, %s)",
                                    offset,
                                    props.getLength(),
                                    desc.isSigned(),
                                    desc.getMaxIntLength(),
                                    desc.getMaxScalingLength());
        } else if (usage == Constants.PACKED_DECIMAL) {
            return String.format("getIntBCD(%s, %s, %s, %s, %s)",
                                    offset,
                                    props.getLength(),
                                    desc.isSigned(),
                                    desc.getMaxIntLength(),
                                    desc.getMaxScalingLength());
        }
	    return "";
	}
	
	private String getLongMethod(SymbolProperties props, String offset) {
	    CobolDataDescription desc = props.getCobolDesc();
	    byte usage = desc.getUsage();
	    if (usage == Constants.DISPLAY) {
            return String.format("getLongDisplay(%s, %s, %s, %s, %s, %s)",
                                    offset,
                                    props.getLength(),
                                    desc.isSigned(),
                                    desc.isSignLeading(),
                                    desc.isSignSeparate(),
                                    desc.getMaxScalingLength());
        } else if (usage == Constants.BINARY) {
            return String.format("getLongBytes(%s, %s, %s, %s, %s)",
                                    offset,
                                    props.getLength(),
                                    desc.isSigned(),
                                    desc.getMaxIntLength(),
                                    desc.getMaxScalingLength());
        } else if (usage == Constants.PACKED_DECIMAL) {
            return String.format("getLongBCD(%s, %s, %s, %s, %s)",
                                    offset,
                                    props.getLength(),
                                    desc.isSigned(),
                                    desc.getMaxIntLength(),
                                    desc.getMaxScalingLength());
        }
	    return "";
	}
	
	private String getBigDecMethod(SymbolProperties props, String offset) {
	    CobolDataDescription desc = props.getCobolDesc();
	    byte usage = desc.getUsage();
	    if (usage == Constants.DISPLAY) {
            return String.format("getBigDecimalDisplay(%s, %s, %s, %s, %s, %s)",
                                    offset,
                                    props.getLength(),
                                    desc.isSigned(),
                                    desc.isSignLeading(),
                                    desc.isSignSeparate(),
                                    desc.getMaxFractionLength() + desc.getMaxScalingLength());
        } else if (usage == Constants.BINARY) {
            return String.format("getBigDecimalBytes(%s, %s, %s, %s, %s, %s)",
                                    offset,
                                    props.getLength(),
                                    desc.isSigned(),
                                    desc.getMaxIntLength(),
                                    desc.getMaxFractionLength(),
                                    desc.getMaxScalingLength());
        } else if (usage == Constants.PACKED_DECIMAL) {
            return String.format("getBigDecimalBCD(%s, %s, %s, %s, %s, %s)",
                                    offset,
                                    props.getLength(),
                                    desc.isSigned(),
                                    desc.getMaxIntLength(),
                                    desc.getMaxFractionLength(),
                                    desc.getMaxScalingLength());
        }
	    return "";
	}
	
	private String getStringMethod(SymbolProperties props, String offset) {
	    return String.format("getStringDisplay(%s, %s)", offset, props.getLength());
	}
	
	private String setIntMethod(SymbolProperties props, String argName, String offset) {
	    CobolDataDescription desc = props.getCobolDesc();
	    byte usage = desc.getUsage();
	    if (usage == Constants.DISPLAY) {
            return String.format("setIntDisplay(%s, %s, %s, %s, %s, %s, %s, %s)",
                                    argName,
                                    offset,
                                    props.getLength(),
                                    desc.isSigned(),
                                    desc.isSignLeading(),
                                    desc.isSignSeparate(),
                                    desc.getMaxIntLength(),
                                    desc.getMaxScalingLength());
        } else if (usage == Constants.BINARY) {
            return String.format("setIntBytes(%s, %s, %s, %s, %s, %s)",
                                    argName,
                                    offset,
                                    props.getLength(),
                                    desc.isSigned(),
                                    desc.getMaxIntLength(),
                                    desc.getMaxScalingLength());
        } else if (usage == Constants.PACKED_DECIMAL) {
            return String.format("setIntBCD(%s, %s, %s, %s, %s, %s)",
                                    argName,
                                    offset,
                                    props.getLength(),
                                    desc.isSigned(),
                                    desc.getMaxIntLength(),
                                    desc.getMaxScalingLength());
        }
	    return "";
	}
	
	private String setLongMethod(SymbolProperties props, String argName, String offset) {
	    CobolDataDescription desc = props.getCobolDesc();
	    byte usage = desc.getUsage();
	    if (usage == Constants.DISPLAY) {
            return String.format("setLongDisplay(%s, %s, %s, %s, %s, %s, %s, %s)",
                                    argName,
                                    offset,
                                    props.getLength(),
                                    desc.isSigned(),
                                    desc.isSignLeading(),
                                    desc.isSignSeparate(),
                                    desc.getMaxIntLength(),
                                    desc.getMaxScalingLength());
        } else if (usage == Constants.BINARY) {
            return String.format("setLongBytes(%s, %s, %s, %s, %s, %s)",
                                    argName,
                                    offset,
                                    props.getLength(),
                                    desc.isSigned(),
                                    desc.getMaxIntLength(),
                                    desc.getMaxScalingLength());
        } else if (usage == Constants.PACKED_DECIMAL) {
            return String.format("setLongBCD(%s, %s, %s, %s, %s, %s)",
                                    argName,
                                    offset,
                                    props.getLength(),
                                    desc.isSigned(),
                                    desc.getMaxIntLength(),
                                    desc.getMaxScalingLength());
        }
	    return "";
	}
	
	private String setBigDecMethod(SymbolProperties props, String argName, String offset) {
	    CobolDataDescription desc = props.getCobolDesc();
	    byte usage = desc.getUsage();
	    if (usage == Constants.DISPLAY) {
            return String.format("setBigDecimalDisplay(%s, %s, %s, %s, %s, %s, %s, %s, %s)",
                                    argName,
                                    offset,
                                    props.getLength(),
                                    desc.isSigned(),
                                    desc.isSignLeading(),
                                    desc.isSignSeparate(),
                                    desc.getMaxIntLength(),
                                    desc.getMaxFractionLength(),
                                    desc.getMaxScalingLength());
        } else if (usage == Constants.BINARY) {
            return String.format("setBigDecimalBytes(%s, %s, %s, %s, %s, %s, %s)", 
                                    argName,
                                    offset,
                                    props.getLength(),
                                    desc.isSigned(),
                                    desc.getMaxIntLength(),
                                    desc.getMaxFractionLength(),
                                    desc.getMaxScalingLength());
        } else if (usage == Constants.PACKED_DECIMAL) {
            return String.format("setBigDecimalBCD(%s, %s, %s, %s, %s, %s, %s)",
                                    argName,
                                    offset,
                                    props.getLength(),
                                    desc.isSigned(),
                                    desc.getMaxIntLength(),
                                    desc.getMaxFractionLength(),
                                    desc.getMaxScalingLength());
        }
	    return "";
	}
	
	private String setStringMethod(SymbolProperties props, String argName, String offset) {
	    return String.format("setStringDisplay(%s, %s, %s, %s)",
                argName,
                offset,
                props.getLength(),
                props.getCobolDesc().isJustifiedRight());
	}
	
	private String javaClassName(SymbolProperties props) {
	    byte typeInJava = props.getCobolDesc().getTypeInJava();
	    if (typeInJava >= Constants.SHORT && typeInJava <= Constants.LONG) {
	        return "LongField";
	    } else if (typeInJava == Constants.BIGDECIMAL) {
	        return "BigDecimalField";
	    } else if (typeInJava == Constants.STRING) {
	        return "StringField";
	    }
	    return "";
	}
	
	private String constructorForJavaField(SymbolProperties props, String offset) {
	    CobolDataDescription desc = props.getCobolDesc();
	    byte typeInJava = desc.getTypeInJava();
	    if (typeInJava >= Constants.SHORT && typeInJava <= Constants.LONG) {
	        return String.format("LongField(%s, %s, %s, %s, %s, %s, %s, %s)",
	                offset, props.getLength(),
	                desc.getUsage(), desc.getMaxIntLength(), desc.getMaxScalingLength(),
	                desc.isSigned(), desc.isSignLeading(), desc.isSignSeparate()
	                );
	    } else if (typeInJava == Constants.BIGDECIMAL) {
	        return String.format("BigDecimalField(%s, %s, %s, %s, %s, %s, %s, %s, %s)",
	                offset, props.getLength(), desc.getUsage(),
	                desc.getMaxIntLength(), desc.getMaxFractionLength(), desc.getMaxScalingLength(),
	                desc.isSigned(), desc.isSignLeading(), desc.isSignSeparate()
	        );
	    } else if (typeInJava == Constants.STRING) {
	        return String.format("StringField(%s, %s, %s)", offset, props.getLength(), desc.isJustifiedRight());
	    }
	    return "";
	}
	
	private String getValueJavaField(SymbolProperties props, int returnType, String indexArg) {
	    StringBuilder sb = new StringBuilder();
	    if (returnType == Constants.SHORT || returnType == Constants.INTEGER) {
	        sb.append("(" + javaTypeStr[returnType] + ") ");
	    }
	    sb.append(fieldName(props));
	    if (indexArg != null && indexArg != "") {
	        sb.append("[" + indexArg + "]");
	    }
	    sb.append(".");
	    if (props.getCobolDesc().getTypeInJava() != Constants.STRING && returnType == Constants.STRING) {
	        sb.append("getStringValue()");
	    } else {
	        sb.append("getValue()");
	    }
	    return sb.toString();
	}
	
	private String setValueJavaField(SymbolProperties props, String argName, String indexArg) {
	    StringBuilder sb = new StringBuilder();
	    sb.append(fieldName(props));
	    if (indexArg != null && indexArg != "") {
	        sb.append("[" + indexArg + "]");
	    }
	    sb.append(".setValue(" + argName + ")");
	    return sb.toString();
	}

	private String setValueDirectly(SymbolProperties props, String argName, String indexArg) {
	    StringBuilder sb = new StringBuilder();
        sb.append(fieldName(props));
        if (indexArg != null && indexArg != "") {
            sb.append("[" + indexArg + "]");
        }
        sb.append(".setDirectly(" + argName + ")");
        return sb.toString();
	}
}
