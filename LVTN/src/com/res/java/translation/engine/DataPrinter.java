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
import com.res.java.lib.EditedVar;
import com.res.java.translation.symbol.SymbolProperties;
import com.res.java.translation.symbol.SymbolProperties.CobolDataDescription;
import com.res.java.util.JavaCodePrinter;
import com.res.java.util.NameUtil;

public class DataPrinter {
	private Queue<SymbolProperties> group01ToCreate = new LinkedList<SymbolProperties>();
	private Queue<SymbolProperties> innerGroupToCreate = new LinkedList<SymbolProperties>();

	private static String[] javaType = new String[] { "byte", "char", "short", "int",
            "long", "float", "double", "BigDecimal", "String" };
	
	private final boolean useJava = (RESConfig.getInstance().getOptimizeAlgorithm() == 1);
	
	private int getLength(SymbolProperties props) {
	    return useJava ? props.getAdjustedLength() : props.getLength();
	}
	
	private int getGlobalOffset(SymbolProperties props) {
	    return useJava ? props.getGlobalAdjustedOffset() : props.getGlobalOffset();
	}
	
	private int getRelativeOffset(SymbolProperties props) {
	    return useJava ? props.getAdjustedOffset() : props.getOffset();
	}
	
	public void printDataForProgram(SymbolProperties props,
			JavaCodePrinter printer) throws IOException {
		if (!props.isProgram())
			return;

		if (getLength(props) > 0) {
			printer.println("public " + props.getJavaName2() + "() {");
			printer.increaseIndent();
			printer.println(String.format("super(%d);", getLength(props)));
			printer.decreaseIndent();
			printer.println("}");
			printer.println();
		}
		
		printDataChildren(props, printer);

		while (group01ToCreate.size() > 0) {
			createGroup01File(group01ToCreate.poll());
		}
	}

	private void createGroup01File(SymbolProperties props) throws IOException {
        // create new file
        String fileName = RESConfig.getInstance().getDataPackage()
                + File.separatorChar + NameUtil.getFileName(props);
        System.out.println("Create file " + fileName);

        JavaCodePrinter printer = new JavaCodePrinter(new FileOutputStream(
                fileName));

        // print package
        printer.println("package " + RESConfig.getInstance().getDataPackage()
                + ";");
        printer.println();

        // print import
        printer.printImport(BaseClass.class);
        printer.printImport(EditedVar.class);
        printer.printImport(BigDecimal.class);
        printer.println();

        // print class definition
        printer.println("public class " + props.getJavaName2() + " extends "
                + BaseClass.class.getSimpleName() + " {");

        printer.increaseIndent();

        if (getLength(props) > 0) {
            overrideConstructor(props.getJavaName2(), printer);
        }

        // print all children of this group
        printDataChildren(props, printer);

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
            JavaCodePrinter printer) {
        // print class definition
        printer.println("public class " + props.getJavaName2() + " extends "
                + BaseClass.class.getSimpleName() + " {");

        printer.increaseIndent();

        if (getLength(props) > 0) {
            overrideConstructor(props.getJavaName2(), printer);
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
		if (!props.hasChildren())
			return;
		for (SymbolProperties p : props.getChildren()) {
			if (!p.isData() || p.isFromRESLibrary() ) {
				continue;
			}
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
		// renames field always use byte array
		byte typeInJava = props.getCobolDesc().getTypeInJava();
		String typeStr = null;
		if (typeInJava == Constants.GROUP) {
			typeStr = "String";
		} else {
			typeStr = javaType[typeInJava];
		}

		//getter
		printer.beginMethod("public", typeStr, "get" + props.getJavaName2(), null, null);
		printer.println("return " + getValueMethodName(props, getOffsetWithoutIndex(props)) + ";");
		printer.endMethod();
		printer.println();

		//setter
		String argName = "input";
		printer.beginMethod("public", "void", "set" + props.getJavaName2(), new String[]{typeStr + " " + argName}, null);
		printer.println(setValueMethodName(props, argName, getOffsetWithoutIndex(props)) + ";");
		printer.endMethod();
		printer.println();
	}
	
	private void printLv88Data(SymbolProperties props, JavaCodePrinter printer) {
        printer.println("//Create getter, setter for condition-name"
                + props.getDataName());
        printer.println();
    }

	private void printGroupData(SymbolProperties props, JavaCodePrinter printer) {
		if (props.getLevelNumber() == 1) {
			group01ToCreate.add(props);
		} else {
			innerGroupToCreate.add(props);
		}

		boolean genJava = useJava && (getLength(props) == 0);
		
		String className = props.getJavaName2();
		String fieldName = props.getJavaName1();
		
		String argName = "input";
		
		if (props.isOccurs()) { // has occurs
		    String indexName = "i";

			// create array field
			printer.println(String.format(
                    "private %1$s %2$s[] = new %1$s[%3$d];", className,
                    fieldName, props.getMaxOccursInt()));
			
			// loop to initialize each group
            printer.beginBlock();
            
            printer.println(String.format("for (int i = 0; i < %d; i++) {", props.getMaxOccursInt()));
            printer.increaseIndent();
            
            if (genJava) {
                printer.println(String.format("%s[i] = new %s()", fieldName, className));
            } else {
                printer.println(String.format("%s[i] = new %s(this.getBytes(), %s, %s);",
                        fieldName,
                        className,
                        getOffsetWithIndex(props, "i"),
                        getLength(props)));
            }
            
            // end loop
            printer.endBlock();
            // end initialize block
            printer.endBlock();
            
            printer.println();
            
            // getter
            printer.beginMethod("public", className, "get" + className, new String[]{"int " + indexName}, null);
            printer.println(String.format("return this.%s[%s];", fieldName, indexName));
            printer.endMethod();
            printer.println();
            
            // setter
            printer.beginMethod("public", "void", "set" + className, new String[]{"int " + indexName, "String " + argName}, null);
            if (genJava) {
                // TODO: java types
                
            } else {
                printer.println(setValueMethodName(props, argName, getOffsetWithIndex(props, indexName)) + ";");
            }
            printer.endMethod();
            printer.println();
            
		} else { // no occurs
		    
		    if (genJava) { // java
		        printer.println(String.format(
                        "private %1$s %2$s = new %1$s();", className, fieldName));
		    } else {
		        // use byte array
		        printer.println(String.format("private %1$s %2$s = new %1$s(this.getBytes(), %3$d, %4$d);",
		                className,
		                fieldName,
		                getGlobalOffset(props),
		                getLength(props)));
		    }
		    printer.println();
		    
		    // getter
		    printer.beginMethod("public", className, "get" + className, null, null);
		    printer.println(String.format("return this.%s;", fieldName));
		    printer.endMethod();
		    printer.println();

		    // setter
		    printer.beginMethod("public", "void", "set" + className, new String[]{"String " + argName}, null);
		    if (genJava) {
		        //TODO: java setter
		    } else {
		        printer.println(setValueMethodName(props, argName, getOffsetWithoutIndex(props)) + ";");
		    }
		    printer.endMethod();
		    printer.println();
		}
	}

	private void printElementData(SymbolProperties props,
			JavaCodePrinter printer) {
		
	    CobolDataDescription desc = props.getCobolDesc();
        String type = javaType[desc.getTypeInJava()];
        
        String className = props.getJavaName2();
        String fieldName = props.getJavaName1();
        
        String argName = "input";
		
		boolean doEdit = false;
		if (desc.getDataCategory() == Constants.ALPHANUMERIC_EDITED
                || desc.getDataCategory() == Constants.NUMERIC_EDITED) {
            doEdit = true;
            printer.println(
                    String.format("private EditedVar %s = new EditedVar(\"%s\", (byte) %s, %s, %s);",
                                    getEditorName(props),
                                    desc.getPic(),
                                    desc.getDataCategory(),
                                    desc.isJustifiedRight(),
                                    desc.isBlankWhenZero()));
        }
		
		boolean genJava = useJava && (getLength(props) == 0);
		
		if (props.isOccurs()) { // has occurs
		    String indexName = "i";
		    
		    if (genJava) {
		        // create Java field
		        printer.println(String.format(
		                "private %1$s %2$s[] = new %1$s[%3$d];", type,
		                fieldName, props.getMaxOccursInt()));
		    } 

		    // getter
		    printer.beginMethod("public", type, "get" + className, new String[]{"int " + indexName}, null);
		    if (genJava) {
		        printer.println(String.format("return this.%s[%s];", fieldName, indexName));
		    } else {
		        printer.println("return " + getValueMethodName(props, getOffsetWithIndex(props, indexName)) + ";");
		    }
		    printer.endMethod();
		    printer.println();

		    // setter
		    printer.beginMethod("public", "void", "set" + className, new String[]{"int " + indexName, type + " " + argName}, null);
		    if (doEdit) {
		        printer.println(String.format("%1$s = %2$s.doEdit(%1$s);", argName, getEditorName(props)));
		    }
		    if (genJava) {
		        printer.println(String.format("this.%s[%s] = %s;", fieldName,
                        indexName, getAdjustValueMethodName(props, argName)));
		    } else {
		        printer.println(setValueMethodName(props, argName, getOffsetWithIndex(props, indexName)) + ";");
		    }
		    printer.endMethod();
		    printer.println();
			
		} else {
		    if (genJava) {
		        // create Java field
		        printer.println(String.format("private %s %s;", type, fieldName));
		        printer.println();
		    }
		    
		    //getter
		    printer.beginMethod("public", type, "get" + className, null, null);
		    if (genJava) {
		        printer.println(String.format("return this.%s;", fieldName));
		    } else {
		        printer.println("return " + getValueMethodName(props, getOffsetWithoutIndex(props)) + ";");
		    }
		    printer.endMethod();
		    printer.println();

		    //setter
		    printer.beginMethod("public", "void", "set" + className, new String[]{type + " " + argName}, null);
		    if (doEdit) {
		        printer.println(String.format("%1$s = %2$s.doEdit(%1$s);", argName, getEditorName(props)));
		    }
		    if (genJava) {
		        printer.println(String.format("this.%s = %s;", fieldName, getAdjustValueMethodName(props, argName)));
		    } else {
		        printer.println(setValueMethodName(props, argName, getOffsetWithoutIndex(props)) + ";");
		    }
		    printer.endMethod();
		    printer.println();
		}
	}
	
	private void overrideConstructor(String className, JavaCodePrinter printer) {

        // printer.println("public " + className + "() {");
        // printer.increaseIndent(); printer.println("super(0);");
        // printer.decreaseIndent(); printer.println("}"); printer.println();
        //          
        // printer.println("public " + className + "(int size) {");
        // printer.increaseIndent(); printer.println("super(size);");
        // printer.decreaseIndent(); printer.println("}"); printer.println();

        printer.println("public " + className
                + "(byte[] data, int offset, int length) {");
        printer.increaseIndent();
        printer.println("super(data, offset, length);");
        printer.decreaseIndent();
        printer.println("}");
        printer.println();
    }
	
	private String getEditorName(SymbolProperties props) {
	    return "_" + props.getJavaName1() + "_editor";
	}
	
	private String getOffsetWithIndex(SymbolProperties props, String indexArg) {
	    if (!props.isAParentInOccurs()) { // outer most entry has occurs
	        if (getGlobalOffset(props) == 0) {
	            // also first entry of storage area
	            return String.format("%s * %s", indexArg, getLength(props));
	        } else {
	            return String.format("%s + %s * %s", getGlobalOffset(props), indexArg, getLength(props));
	        }
	    } else {
	        // entry is inside an entry that has occurs
	        if (getRelativeOffset(props) == 0) {
	            return String.format("this.offset + %s * %s", indexArg, getLength(props));
	        } else {
	            return String.format("this.offset + %s + %s * %s", getRelativeOffset(props), indexArg, getLength(props));
	        }
	    }
	}
	
	private String getOffsetWithoutIndex(SymbolProperties props) {
	    if (props.isAParentInOccurs()) {
	        if (getRelativeOffset(props) == 0) {
	            return "this.offset";
	        } else {
	            return "this.offset + " + getRelativeOffset(props);
	        }
	    } else {
	        return getGlobalOffset(props) + "";
	    }
	}
	
	private String getAdjustValueMethodName(SymbolProperties props, String argName) {
	    CobolDataDescription desc = props.getCobolDesc();
	    byte type = desc.getTypeInJava();

	    if (type == Constants.SHORT || type == Constants.INTEGER) {
	        String typeStr = javaType[type];
	        return String.format("(%s) getAlgebraicValue(%s, %s, %s, %s)",
                    typeStr, argName, desc.getMaxIntLength(), desc.isSigned(),
                    desc.getMaxScalingLength());
	    } else if (type == Constants.LONG) {
	        return String.format("getAlgebraicValue(%s, %s, %s, %s)", argName,
                    desc.getMaxIntLength(), desc.isSigned(),
                    desc.getMaxScalingLength());
	    } else if (type == Constants.STRING) {
	        return String.format("getStringValue(%s, %s, %s)", argName,
	                desc.getMaxStringLength(), desc.isJustifiedRight());
	    } else if (type == Constants.BIGDECIMAL) {
	        return String.format("getAlgebraicValue(%s, %s, %s, %s, %s)",
                    argName, desc.getMaxIntLength(),
                    desc.getMaxFractionLength(),
                    desc.getMaxScalingLength(), desc.isSigned());
	    }
	    
	    return "";
	}
	
	private String getValueMethodName(SymbolProperties props, String offset) {
		CobolDataDescription desc = props.getCobolDesc();
		byte type = desc.getTypeInJava();
		byte usage = desc.getUsage();
								
		if (type == Constants.SHORT || type == Constants.INTEGER) {
			String typeStr = javaType[type];
			if (usage == Constants.DISPLAY) {
				return String.format("(%s) getLongDisplay(%s, %s, %s, %s, %s, %s)",
										typeStr,
										offset,
										getLength(props),
										desc.isSigned(),
										desc.isSignLeading(),
										desc.isSignSeparate(),
										desc.getMaxScalingLength());
			} else if (usage == Constants.BINARY) {
				return String.format("(%s) getLongBytes(%s, %s, %s, %s, %s)",
										typeStr,
										offset,
										getLength(props),
										desc.isSigned(),
										desc.getMaxIntLength(),
										desc.getMaxScalingLength());
			} else if (usage == Constants.PACKED_DECIMAL) {
				return String.format("(%s) getLongBCD(%s, %s, %s, %s, %s)",
										typeStr,
										offset,
										getLength(props),
										desc.isSigned(),
										desc.getMaxIntLength(),
										desc.getMaxScalingLength());
			}
		} else if (type == Constants.LONG) {
			if (usage == Constants.DISPLAY) {
				return String.format("getLongDisplay(%s, %s, %s, %s, %s, %s)",
										offset,
										getLength(props),
										desc.isSigned(),
										desc.isSignLeading(),
										desc.isSignSeparate(),
										desc.getMaxScalingLength());
			} else if (usage == Constants.BINARY) {
				return String.format("getLongBytes(%s, %s, %s, %s, %s)",
										offset,
										getLength(props),
										desc.isSigned(),
										desc.getMaxIntLength(),
										desc.getMaxScalingLength());
			} else if (usage == Constants.PACKED_DECIMAL) {
				return String.format("getLongBCD(%s, %s, %s, %s, %s)",
										offset,
										getLength(props),
										desc.isSigned(),
										desc.getMaxIntLength(),
										desc.getMaxScalingLength());
			}
		}else if (type == Constants.STRING || type == Constants.GROUP) {
			return String.format("getStringDisplay(%s, %s)", offset,
                    getLength(props));
		} else if (type == Constants.BIGDECIMAL) {
			if (usage == Constants.DISPLAY) {
				return String.format("getBigDecimalDisplay(%s, %s, %s, %s, %s, %s)",
										offset,
										getLength(props),
										desc.isSigned(),
										desc.isSignLeading(),
										desc.isSignSeparate(),
										desc.getMaxFractionLength() + desc.getMaxScalingLength());
			} else if (usage == Constants.BINARY) {
				return String.format("getBigDecimalBytes(%s, %s, %s, %s, %s, %s)",
										offset,
										getLength(props),
										desc.isSigned(),
										desc.getMaxIntLength(),
										desc.getMaxFractionLength(),
										desc.getMaxScalingLength());
			} else if (usage == Constants.PACKED_DECIMAL) {
				return String.format("getBigDecimalBCD(%s, %s, %s, %s, %s, %s)",
										offset,
										getLength(props),
										desc.isSigned(),
										desc.getMaxIntLength(),
										desc.getMaxFractionLength(),
										desc.getMaxScalingLength());
			}
		}
		return "";
	}
	
	private String setValueMethodName(SymbolProperties props, String argName, String offset) {
		CobolDataDescription desc = props.getCobolDesc();
		byte type = desc.getTypeInJava();
		byte usage = desc.getUsage();
		
		if (type == Constants.SHORT || type == Constants.INTEGER || type == Constants.LONG) {
			if (usage == Constants.DISPLAY) {
				return String.format("setLongDisplay(%s, %s, %s, %s, %s, %s, %s, %s)",
										argName,
										offset,
										getLength(props),
										desc.isSigned(),
										desc.isSignLeading(),
										desc.isSignSeparate(),
										desc.getMaxIntLength(),
										desc.getMaxScalingLength());
			} else if (usage == Constants.BINARY) {
				return String.format("setLongBytes(%s, %s, %s, %s, %s, %s)",
										argName,
										offset,
										getLength(props),
										desc.isSigned(),
										desc.getMaxIntLength(),
										desc.getMaxScalingLength());
			} else if (usage == Constants.PACKED_DECIMAL) {
				return String.format("setLongBCD(%s, %s, %s, %s, %s, %s)",
										argName,
										offset,
										getLength(props),
										desc.isSigned(),
										desc.getMaxIntLength(),
										desc.getMaxScalingLength());
			}
		} else if (type == Constants.STRING || type == Constants.GROUP) {
			return String.format("setStringDisplay(%s, %s, %s, %s)",
									argName,
									offset,
									getLength(props),
									desc.isJustifiedRight());
		} else if (type == Constants.BIGDECIMAL) {
			if (usage == Constants.DISPLAY) {
				return String.format("setBigDecimalDisplay(%s, %s, %s, %s, %s, %s, %s, %s, %s)",
										argName,
										offset,
										getLength(props),
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
										getLength(props),
										desc.isSigned(),
										desc.getMaxIntLength(),
										desc.getMaxFractionLength(),
										desc.getMaxScalingLength());
			} else if (usage == Constants.PACKED_DECIMAL) {
				return String.format("setBigDecimalBCD(%s, %s, %s, %s, %s, %s, %s)",
										argName,
										offset,
										getLength(props),
										desc.isSigned(),
										desc.getMaxIntLength(),
										desc.getMaxFractionLength(),
										desc.getMaxScalingLength());
			}
		}
		return "";
	}
}
