package com.res.java.translation.engine;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.Queue;

import com.res.cobol.syntaxtree.CobolWord;
import com.res.cobol.syntaxtree.DataDivision;
import com.res.cobol.syntaxtree.DisplayStatement;
import com.res.cobol.syntaxtree.Identifier;
import com.res.cobol.syntaxtree.Literal;
import com.res.cobol.syntaxtree.NodeToken;
import com.res.cobol.syntaxtree.NonNumericConstant;
import com.res.cobol.syntaxtree.Paragraph;
import com.res.cobol.syntaxtree.ParagraphName;
import com.res.cobol.syntaxtree.ProcedureBody;
import com.res.cobol.syntaxtree.ProcedureSection;
import com.res.cobol.syntaxtree.ProgramIdParagraph;
import com.res.cobol.syntaxtree.ProgramUnit;
import com.res.cobol.syntaxtree.Sentence;
import com.res.cobol.visitor.GJDepthFirst;
import com.res.common.RESConfig;
import com.res.java.lib.EditedVar;
import com.res.java.lib.Program;
import com.res.java.translation.symbol.SymbolConstants;
import com.res.java.translation.symbol.SymbolProperties;
import com.res.java.translation.symbol.SymbolTable;
import com.res.java.util.JavaCodePrinter;
import com.res.java.util.NameUtil;

public class Cobol2Java extends GJDepthFirst<Object, Object> {
    private String runMethodName = "_run";
    
	@Override
	public Object visit(ProgramUnit n, Object argu) throws Exception {
	    File f = new File(RESConfig.getInstance().getProgramPackage());
        if (!f.exists()) {
            f.mkdir();
        }
        f = new File(RESConfig.getInstance().getDataPackage());
        if (!f.exists()) {
            f.mkdir();
        }

        String programName = n.identificationDivision.programIdParagraph.programName.cobolWord.nodeToken.tokenImage;

        SymbolProperties props = SymbolTable.getInstance().lookup(programName);
        // create new file
        String fileName = RESConfig.getInstance().getProgramPackage()
                + File.separatorChar + NameUtil.getFileName(props);
        System.out.println("Create file " + fileName);

        JavaCodePrinter printer;
        // printer = new JavaCodePrinter(System.out);
        printer = new JavaCodePrinter(new FileOutputStream(fileName));

        // print package
        printer.println("package "
                + RESConfig.getInstance().getProgramPackage() + ";");
        printer.println();

        // print import
        printer.printImport(Program.class);
        printer.printImport(EditedVar.class);
        printer.printImport(BigDecimal.class);
        printer.println("import " + RESConfig.getInstance().getDataPackage()
                + ".*;");
        printer.println();

        // print class definition
        printer.println("public class " + props.getJavaName2()
                + " extends Program {");
        printer.increaseIndent();

        createListParagraphs(props);
        super.visit(n, printer);

        printer.println("public static void main(String[] args) {");
        printer.increaseIndent();
        printer.println(String.format("new %s().%s();", programName,
                runMethodName));
        printer.decreaseIndent();
        printer.println("}");
        printer.decreaseIndent();

        // end class
        printer.println("}");

        printer.close();
        return null;
	}

	private Queue<SymbolProperties> listParagraphs = new LinkedList<SymbolProperties>();
	
	private void createListParagraphs(SymbolProperties props) {
        for (SymbolProperties p : props.getChildren()) {
            if (p.getType() == SymbolConstants.PARAGRAPH)
                listParagraphs.add(p);
            else if (p.getType() == SymbolConstants.SECTION) {
                listParagraphs.addAll(p.getParagraphList());
            }
        }
    }

    @Override
	public Object visit(ProgramIdParagraph n, Object o) throws Exception {
		String programName = n.programName.cobolWord.nodeToken.tokenImage;
		SymbolProperties props = SymbolTable.getInstance().lookup(programName);

		JavaCodePrinter printer = (JavaCodePrinter) o;

		// print all data field
		new DataPrinter().printDataForProgram(props, printer);

		return null;
	}
	
	@Override
	public Object visit(DataDivision n, Object o) throws Exception {
        return null;
    }
	
	private String getQualifiedJavaName(SymbolProperties props) {
	    return props.getQualifiedName().replaceAll("\\.", "_");
	}
	
	@Override
	public Object visit(ProcedureBody n, Object o) throws Exception {
	    JavaCodePrinter printer = (JavaCodePrinter) o;
	    printer.println(String.format("public void %s() {", runMethodName));
	    printer.increaseIndent();
	    n.paragraphs.nodeListOptional.accept(this, o);
	    if (listParagraphs.size() > 0) {
	        printer.println(getQualifiedJavaName(listParagraphs.peek()) + "(false);");
	    }
	    printer.decreaseIndent();
	    printer.println("}");
	    printer.println();
	    n.paragraphs.nodeListOptional1.accept(this, o);
	    n.nodeListOptional.accept(this, o);
	    return null;
	}
	
	@Override
	public Object visit(ProcedureSection n, Object o) throws Exception {
	    String sectionName = n.sectionHeader.sectionName.nodeChoice.choice.accept(this, null).toString();

	    SymbolProperties props = SymbolTable.getInstance().lookup(sectionName, SymbolConstants.SECTION);
	    
	    JavaCodePrinter printer = (JavaCodePrinter) o;
	    
	    printer.println(String.format("public final void %s() {", getQualifiedJavaName(props)));
	    printer.increaseIndent();
	    
	    n.paragraphs.nodeListOptional.accept(this, o);
	    
	    for (SymbolProperties par : props.getParagraphList()) {
	        printer.println(String.format("%s(true);", getQualifiedJavaName(par)));
	    }

	    printer.decreaseIndent();
	    printer.println("}");
	    printer.println();
	    
	    n.paragraphs.nodeListOptional1.accept(this, o);
	    
	    return null;
	}
	
	@Override
	public Object visit(Paragraph n, Object o) throws Exception {
	    String parName = n.nodeChoice.choice.accept(this, o).toString();
	    
	    if (listParagraphs.peek().getDataName().equalsIgnoreCase(parName)) {
	        SymbolProperties props = listParagraphs.poll();

	        JavaCodePrinter printer = (JavaCodePrinter) o;

	        printer.println(String.format("public final void %s(boolean returned) {", getQualifiedJavaName(props)));
	        printer.increaseIndent();
	        n.nodeChoice1.accept(this, o);
	        printer.println("if (!returned) {");
	        printer.increaseIndent();
	        if (listParagraphs.peek() != null) {
	            printer.println(String.format("%s(false);", getQualifiedJavaName(listParagraphs.peek())));
	        } else {
	            printer.println("System.exit(0);");
	        }
	        printer.decreaseIndent();
	        printer.println("}");
	        printer.decreaseIndent();
	        printer.println("}");
	        printer.println();
	    }
	    
	    return null;
	}
	
	@Override
	public Object visit(NodeToken n, Object o) throws Exception {
	    return n.tokenImage;
	}
	
	@Override
	public Object visit(CobolWord n, Object o) throws Exception {
	    return n.nodeToken.tokenImage;
	}
	
	@Override
	public Object visit(ParagraphName n, Object o) throws Exception {
	    return n.nodeChoice.choice.accept(this, o);
	}
	
	@Override
	public Object visit(Sentence n, Object o) throws Exception {
	    n.nodeList.accept(this, o);
	    return null;
	}
	
	@Override
	public Object visit(DisplayStatement n, Object o) throws Exception {
	    return super.visit(n, o);
	}
	
	public Object visit(Literal n, Object o) throws Exception {
	    return n.nodeChoice.choice.accept(this, o);
	}
	
	public Object visit(NonNumericConstant n, Object o) throws Exception {
	    return n.nodeChoice.choice.accept(this, o);
	}
	
	public Object visit(Identifier n, Object o) throws Exception {
	    return null;
	}
}