package com.res.java.translation.engine;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.res.cobol.syntaxtree.AcceptStatement;
import com.res.cobol.syntaxtree.AddStatement;
import com.res.cobol.syntaxtree.AlterStatement;
import com.res.cobol.syntaxtree.CallStatement;
import com.res.cobol.syntaxtree.CancelStatement;
import com.res.cobol.syntaxtree.CloseStatement;
import com.res.cobol.syntaxtree.CobolWord;
import com.res.cobol.syntaxtree.CommitStatement;
import com.res.cobol.syntaxtree.ComputeStatement;
import com.res.cobol.syntaxtree.ContinueStatement;
import com.res.cobol.syntaxtree.DataDivision;
import com.res.cobol.syntaxtree.DataName;
import com.res.cobol.syntaxtree.DeclareCursorStatement;
import com.res.cobol.syntaxtree.DeleteStatement;
import com.res.cobol.syntaxtree.DisableStatement;
import com.res.cobol.syntaxtree.DisplayStatement;
import com.res.cobol.syntaxtree.DivideStatement;
import com.res.cobol.syntaxtree.EnableStatement;
import com.res.cobol.syntaxtree.EndProgramStatement;
import com.res.cobol.syntaxtree.EntryStatement;
import com.res.cobol.syntaxtree.EvaluateStatement;
import com.res.cobol.syntaxtree.ExecSqlStatement;
import com.res.cobol.syntaxtree.ExitProgramStatement;
import com.res.cobol.syntaxtree.ExitStatement;
import com.res.cobol.syntaxtree.FetchStatement;
import com.res.cobol.syntaxtree.FigurativeConstant;
import com.res.cobol.syntaxtree.GobackStatement;
import com.res.cobol.syntaxtree.GotoStatement;
import com.res.cobol.syntaxtree.Identifier;
import com.res.cobol.syntaxtree.IfStatement;
import com.res.cobol.syntaxtree.InitializeStatement;
import com.res.cobol.syntaxtree.InsertStatement;
import com.res.cobol.syntaxtree.InspectStatement;
import com.res.cobol.syntaxtree.IntegerConstant;
import com.res.cobol.syntaxtree.Literal;
import com.res.cobol.syntaxtree.LockTableStatement;
import com.res.cobol.syntaxtree.MergeStatement;
import com.res.cobol.syntaxtree.MoveStatement;
import com.res.cobol.syntaxtree.MultiplyStatement;
import com.res.cobol.syntaxtree.Node;
import com.res.cobol.syntaxtree.NodeChoice;
import com.res.cobol.syntaxtree.NodeListOptional;
import com.res.cobol.syntaxtree.NodeOptional;
import com.res.cobol.syntaxtree.NodeSequence;
import com.res.cobol.syntaxtree.NodeToken;
import com.res.cobol.syntaxtree.NonNumericConstant;
import com.res.cobol.syntaxtree.NumericConstant;
import com.res.cobol.syntaxtree.OpenStatement;
import com.res.cobol.syntaxtree.Paragraph;
import com.res.cobol.syntaxtree.ParagraphName;
import com.res.cobol.syntaxtree.PerformStatement;
import com.res.cobol.syntaxtree.ProcedureBody;
import com.res.cobol.syntaxtree.ProcedureSection;
import com.res.cobol.syntaxtree.ProgramIdParagraph;
import com.res.cobol.syntaxtree.ProgramUnit;
import com.res.cobol.syntaxtree.QualifiedDataName;
import com.res.cobol.syntaxtree.QueryStatement;
import com.res.cobol.syntaxtree.ReceiveStatement;
import com.res.cobol.syntaxtree.ReleaseStatement;
import com.res.cobol.syntaxtree.ReturnStatement;
import com.res.cobol.syntaxtree.RewriteStatement;
import com.res.cobol.syntaxtree.RollbackStatement;
import com.res.cobol.syntaxtree.SavepointStatement;
import com.res.cobol.syntaxtree.SearchStatement;
import com.res.cobol.syntaxtree.SelectStatement;
import com.res.cobol.syntaxtree.SendStatement;
import com.res.cobol.syntaxtree.Sentence;
import com.res.cobol.syntaxtree.SetStatement;
import com.res.cobol.syntaxtree.SortStatement;
import com.res.cobol.syntaxtree.StartStatement;
import com.res.cobol.syntaxtree.Statement;
import com.res.cobol.syntaxtree.StopStatement;
import com.res.cobol.syntaxtree.StringStatement;
import com.res.cobol.syntaxtree.Subscript;
import com.res.cobol.syntaxtree.SubtractStatement;
import com.res.cobol.syntaxtree.UnstringStatement;
import com.res.cobol.syntaxtree.UpdateStatement;
import com.res.cobol.syntaxtree.UseStatement;
import com.res.cobol.syntaxtree.WriteStatement;
import com.res.cobol.visitor.GJDepthFirst;
import com.res.common.RESConfig;
import com.res.common.exceptions.ErrorInCobolSourceException;
import com.res.java.lib.Constants;
import com.res.java.lib.EditedVar;
import com.res.java.lib.Program;
import com.res.java.translation.symbol.SymbolConstants;
import com.res.java.translation.symbol.SymbolProperties;
import com.res.java.translation.symbol.SymbolTable;
import com.res.java.util.JavaCodePrinter;
import com.res.java.util.NameUtil;

public class Cobol2Java extends GJDepthFirst<Object, Object> {
    private String runMethodName = "_run";
    
	private Queue<SymbolProperties> listParagraphs = new LinkedList<SymbolProperties>();

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

        printer.beginMethod("public static", "void", "main", new String[]{"String[] args"}, null);
        printer.println(String.format("new %s().%s();", programName,
                runMethodName));
        printer.endMethod();

        // end class
        printer.endBlock();

        printer.close();
        return null;
	}

	private void createListParagraphs(SymbolProperties props) {
        for (SymbolProperties p : props.getChildren()) {
            if (p.getType() == SymbolConstants.PARAGRAPH)
                listParagraphs.add(p);
            else if (p.getType() == SymbolConstants.SECTION) {
                if (p.getParagraphList() != null && p.getParagraphList().size() > 0)
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
	    printer.beginMethod("public", "void", runMethodName, null, null);
	    n.paragraphs.nodeListOptional.accept(this, o);
	    if (listParagraphs.size() > 0) {
	        printer.println(getQualifiedJavaName(listParagraphs.peek()) + "(false);");
	    }
	    printer.endMethod();
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
	    
	    printer.beginMethod("public final", "void", getQualifiedJavaName(props), null, null);
	    
	    n.paragraphs.nodeListOptional.accept(this, o);
	    
	    if (props.getParagraphList() != null && props.getParagraphList().size() > 0) {
	        for (SymbolProperties par : props.getParagraphList()) {
	            printer.println(String.format("%s(true);", getQualifiedJavaName(par)));
	        }
	    }

	    printer.endMethod();
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

	        printer.beginMethod("public final", "void", getQualifiedJavaName(props), new String[]{"boolean returned"}, null);
	        n.nodeChoice1.accept(this, o);
	        
	        printer.println("if (!returned) {");
	        printer.increaseIndent();
	        if (listParagraphs.peek() != null) {
	            printer.println(String.format("%s(false);", getQualifiedJavaName(listParagraphs.peek())));
	        } else {
	            printer.println("System.exit(0);");
	        }
	        printer.endBlock(); // end if
	        
	        printer.endMethod();
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
	public Object visit(Statement n, Object o) throws Exception {
	    n.nodeChoice.choice.accept(this, o);
	    return null;
	}
	
	@Override
	public Object visit(AcceptStatement n, Object o) {
	    return null;
	}
	
	@Override
	public Object visit(AddStatement n, Object o) {
	    return null;
	}
	
	@Override
    public Object visit(AlterStatement n, Object argu) throws Exception {
	    return null;
	}

    @Override
    public Object visit(CallStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(CancelStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(CloseStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(CommitStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(ComputeStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(ContinueStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(DeclareCursorStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(DeleteStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(DisableStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(DivideStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(EnableStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(EndProgramStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(EntryStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(EvaluateStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(ExecSqlStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(ExitProgramStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(ExitStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(FetchStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(GobackStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(GotoStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(IfStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(InitializeStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(InsertStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(InspectStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(LockTableStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(MergeStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(MoveStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(MultiplyStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(OpenStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(PerformStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(QueryStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(ReceiveStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(ReleaseStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(ReturnStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(RewriteStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(RollbackStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(SavepointStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(SearchStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(SelectStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(SendStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(SetStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(SortStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(StartStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(StopStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(StringStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(SubtractStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(UnstringStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(UpdateStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(UseStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
    public Object visit(WriteStatement n, Object argu) throws Exception {
        return null;
    }

    @Override
	public Object visit(DisplayStatement n, Object o) throws Exception {
	    JavaCodePrinter printer = (JavaCodePrinter) o;
	    
	    List<Object> args = new ArrayList<Object>();
	    for (Node node : n.nodeList.nodes) {
	        Object tmp = ((NodeChoice)((NodeSequence)node).nodes.get(0)).choice.accept(this, true);
	        if (tmp != null) {
	            args.add(tmp);
	        }
	    }
//	    n.nodeList.accept(this, null);
	    StringBuilder sb = new StringBuilder();
	    sb.append("display(");
	    sb.append(n.nodeOptional1.present());
	    for (Object arg : args) {
	        sb.append(", ");
	        if (arg instanceof IdentifierInfo) {
	            IdentifierInfo info = (IdentifierInfo) arg;
	            sb.append(callMethodPath(info.getQualifiedName(), info.getListSubscripts(), true, true, null));
	        } else if (arg instanceof Long) {
	            sb.append(arg);
	            if ((Long)arg > Integer.MAX_VALUE) {
	                sb.append("L");
	            }
	        } else {
	            sb.append(arg.toString());
	        }
	    }
	    sb.append(");");
	    printer.println(sb.toString());
	    return null;
	}
	
	public Object visit(Literal n, Object o) throws Exception {
	    if (n.nodeChoice.which == 2) {
	        // figurative constant
	        return null;
	    } else {
	        return n.nodeChoice.choice.accept(this, o);
	    }
	}
	
	@Override
	public Object visit(NonNumericConstant n, Object o) throws Exception {
	    return n.nodeChoice.choice.accept(this, o);
	}
	
	@Override
	public Object visit(NumericConstant n, Object o) throws Exception {
	    boolean getString = (Boolean) o;
	    
	    String sign = null;
	    if (n.nodeOptional.present()) {
	        sign = ((NodeToken)((NodeChoice)n.nodeOptional.node).choice).tokenImage;
	    }
	    
	    StringBuilder sb = new StringBuilder();
	    if (sign != null)
	        sb.append(sign);
	    
	    NodeSequence sequence = null;
	    
	    switch (n.nodeChoice.which) {
	    case 0: // <integer>.<integer>
	        sequence = (NodeSequence) n.nodeChoice.choice;
	        sb.append(((IntegerConstant)sequence.nodes.get(0)).nodeChoice.choice.accept(this, null));
	        sb.append(((NodeToken)sequence.nodes.get(1)).tokenImage);
	        NodeOptional opt = (NodeOptional) sequence.nodes.get(2);
	        if (opt.present()) {
	            sb.append(((IntegerConstant)opt.node).nodeChoice.choice.accept(this, null));
	        }
	        break;
	    case 1: // .<integer>
	        sequence = (NodeSequence) n.nodeChoice.choice;
	        sb.append(((NodeToken)sequence.nodes.get(0)).tokenImage);
	        sb.append(((IntegerConstant)sequence.nodes.get(1)).nodeChoice.choice.accept(this, null));
	        break;
	    case 2: // integer
	        sb.append(((IntegerConstant)n.nodeChoice.choice).nodeChoice.choice.accept(this, null));
	        break;
	    }
	    
	    if (getString) {
	        return "\"" + sb.toString() + "\"";
	    } else {
	        if (sb.indexOf(".") > 0) {
	            return new BigDecimal(sb.toString());
	        } else {
	            return Long.valueOf(sb.toString());
	        }
	    }
	}
	
	public Object visit(FigurativeConstant n, Object o) throws Exception {
	    return null;
	}
	
	public final class IdentifierInfo {
	    private SymbolProperties qualifiedName;
	    private List<Object> listSubscripts;
        public SymbolProperties getQualifiedName() {
            return qualifiedName;
        }
        public void setQualifiedName(SymbolProperties qualifiedName) {
            this.qualifiedName = qualifiedName;
        }
        public List<Object> getListSubscripts() {
            return listSubscripts;
        }
        public void setListSubscripts(List<Object> listSubscripts) {
            this.listSubscripts = listSubscripts;
        }
	}
	
	public Object visit(Identifier n, Object o) throws Exception {
	    if (n.nodeChoice.which == 0) {
	        NodeSequence sequence = (NodeSequence) n.nodeChoice.choice;

            IdentifierInfo info = new IdentifierInfo();
	        // QualifiedDataName
	        SymbolProperties props = (SymbolProperties) sequence.nodes.get(0).accept(this, null);
	        info.setQualifiedName(props);
	        
	        // Subscript
	        NodeListOptional subscript = (NodeListOptional) sequence.nodes.get(1);
	        ArrayList<SymbolProperties> occursParents = props.getOccursParents();
	        if (subscript.present()) {
	            if (occursParents == null || occursParents.size() == 0) {
	                throw new ErrorInCobolSourceException(subscript, props.getDataName() + " cannot be subscript");
	            }
	            if (subscript.size() > 1) {
	                throw new ErrorInCobolSourceException(subscript, "Invalid subscript");
	            } else {
	                NodeSequence subscriptSequence = (NodeSequence) subscript.nodes.get(0);
	                List<Object> listSubscript = new ArrayList<Object>();
	                // subscript
	                listSubscript.add(subscriptSequence.nodes.get(1).accept(this, null));
	                // nodelistoptional
	                NodeListOptional listOptional = (NodeListOptional) subscriptSequence.nodes.get(2);
	                if (listOptional.present()) {
	                    for (Node s : listOptional.nodes) {
	                        listSubscript.add(((NodeSequence)s).nodes.get(1).accept(this, null));
	                    }
	                }
	                
	                // process list subscript
	                if (occursParents.size() != listSubscript.size()) {
	                    throw new ErrorInCobolSourceException(n, String.format("%s need %s suscript", props.getDataName(), occursParents.size()));
	                } else {
	                    for (int i = 0; i < listSubscript.size(); i++) {
	                        if (listSubscript.get(i) instanceof Long) {
	                            Long s = (Long) listSubscript.get(i);
	                            SymbolProperties occurProps = occursParents.get(i);
	                            boolean error = false;
	                            if (occurProps.getMinOccurs() == occurProps.getMaxOccursInt()) {
	                                if (s > occurProps.getMinOccurs())
	                                    error = true;
	                            } else {
	                                if (s < occursParents.get(i).getMinOccurs() || s > occursParents.get(i).getMaxOccursInt()) {
	                                    error = true;
	                                }
	                            }
	                            if (error) {
	                                throw new ErrorInCobolSourceException(n, "Subscript out of range for " + occursParents.get(i).getDataName());
	                            }
	                        }
	                    }
	                }
	                info.setListSubscripts(listSubscript);
	            }
	        } else {
	            if (occursParents != null && occursParents.size() > 0) {
	                throw new ErrorInCobolSourceException(n, String.format("%s need %s suscript", props.getDataName(), occursParents.size()));	                
	            }
	        }
	        
	        // Reference modification
	        NodeOptional refModification = (NodeOptional) sequence.nodes.get(2);
	        if (refModification.present()) {
	            // TODO: implement later
	        }
	        
	        return info;
	    }
	    return null;
	}
	
	/*
	 * check whether the symbol has same qualification as expected
	 */
	private boolean checkQualifiers(SymbolProperties props,
            List<String> parentNames, int pos) {
	    SymbolProperties parent = props;
	    while ((parent = parent.getParent()) != null) {
	        if (parent.getDataName().equalsIgnoreCase(parentNames.get(pos))) {
	            if (pos == parentNames.size() - 1) {
	                return true;
	            } else {
	                return checkQualifiers(parent, parentNames, pos + 1);
	            }
	        }
	    }
	    return false;
    }
	
	@Override
	public Object visit(Subscript n, Object o) throws Exception {
	    NodeSequence sequence = (NodeSequence) n.nodeChoice.choice;
	    switch(n.nodeChoice.which) {
	    case 0: // +/- integer
	        NodeOptional sign = (NodeOptional) sequence.nodes.get(0);
	        String signStr = null;
	        if (sign.present()) {
	            signStr = ((NodeToken)((NodeChoice)((NodeChoice)sign.node).choice).choice).tokenImage;
	        }
	        if (signStr != null && signStr.equalsIgnoreCase("-")) {
	            throw new ErrorInCobolSourceException(n, "Subscript must be positive");
	        } 
	        IntegerConstant val = (IntegerConstant) sequence.nodes.get(1);
	        Long i = (Long) val.accept(this, null);
	        if (i == 0) {
	            throw new ErrorInCobolSourceException(n, "Subscript must be positive");
	        }
	        return i;
	    case 1: // id + integer
	        StringBuilder sb = new StringBuilder();
	        SymbolProperties props = (SymbolProperties) sequence.nodes.get(0).accept(this, null);
	        if (props.getCobolDesc().getTypeInJava() == Constants.LONG) {
	            sb.append("(int)");
	        }
	        if (props.getCobolDesc().getDataCategory() != Constants.NUMERIC || props.getCobolDesc().getTypeInJava() == Constants.BIGDECIMAL) {
	            throw new ErrorInCobolSourceException(n, "Subscript must be integer: " + props.getDataName());
	        }
	        sb.append(callMethodPath(props, null, true, false, null));
	        NodeOptional optional = (NodeOptional) sequence.nodes.get(1);
	        if (optional.present()) {
	            sb.append(((NodeToken)((NodeChoice)((NodeSequence)optional.node).nodes.get(0)).choice).tokenImage);
	            sb.append(((NodeSequence)optional.node).nodes.get(1).accept(this, null).toString());
	        }
	        return sb.toString();
	    case 2: // later
	        break;
	    }
	    return null;
	}
	
	/*
	 * full path to get/set the identifier
	 */
	private String callMethodPath(SymbolProperties props, List<Object> listSubscript, boolean isGetMethod, boolean getAsString, String inputToSet) {
	    String[] path = props.getQualifiedName().split("\\.");
	    StringBuilder rs = new StringBuilder();

	    if (listSubscript == null || listSubscript.size() == 0) {
	        int i = 0;
	        for (; i < path.length - 1; i++) {
	            rs.append(DataPrinter.getMethodName(path[i]) + "()");
	            rs.append(".");
	        }
	        if (isGetMethod) {
	            if (getAsString) {
	                if (props.isGroupData()) {
	                    rs.append(DataPrinter.getMethodName(path[i]) + "().toString()");
	                } else {
	                    rs.append(DataPrinter.getAsStringName(path[i]) + "()");
	                }
	            } else {
	                rs.append(DataPrinter.getMethodName(path[i]) + "()");
	            }
	        } else {
	            rs.append(String.format("%s(%s)", DataPrinter.setMethodName(path[i]), inputToSet));
	        }
	    } else {
	        Queue<Object> subscripts = new LinkedList<Object>(listSubscript);
	        Queue<SymbolProperties> occurParents = new LinkedList<SymbolProperties>(props.getOccursParents());
	        for (int i = 0; i < path.length; i++) {
	            String s = path[i];
	            if (i == path.length - 1) {
	                if (isGetMethod) {
	                    String methodName = (getAsString && !props.isGroupData())
	                                        ? DataPrinter.getAsStringName(s)
	                                        : DataPrinter.getMethodName(s);
	                    if (s.equalsIgnoreCase(occurParents.peek().getJavaName2())) {
	                        occurParents.poll();
	                        rs.append(String.format("%s(%s)", methodName, adjustSubscript(subscripts.poll())));
	                    } else {
	                        rs.append(methodName + "()");
	                    }
	                    if (getAsString && props.isGroupData()) {
	                        rs.append("toString()");
	                    }
	                } else {
	                    // set method
	                    if (s.equalsIgnoreCase(occurParents.peek().getJavaName2())) {
	                        occurParents.poll();
	                        rs.append(String.format("%s(%s, %s)",
	                                DataPrinter.setMethodName(s),
	                                adjustSubscript(subscripts.poll()), inputToSet));
	                    } else {
	                        rs.append(String.format("%s(%s)", DataPrinter.setMethodName(s), inputToSet));
	                    }
	                }
	            } else {
	                if (s.equalsIgnoreCase(occurParents.peek().getJavaName2())) {
	                    occurParents.poll();
	                    rs.append(String.format("%s(%s)", DataPrinter.getMethodName(s), adjustSubscript(subscripts.poll())));
	                } else {
	                    rs.append(DataPrinter.getMethodName(s) + "()");
	                }
	                rs.append(".");
	            }
	        }
	    }
	    
	    return rs.toString();
	}
	
	private Object adjustSubscript(Object subscript) {
	    if (subscript instanceof Long) {
	        return ((Long) subscript - 1);
	    } else {
	        return subscript.toString() + " - 1";
	    }
	}
	
	@Override
	public Object visit(IntegerConstant n, Object o) throws Exception {
	    return Long.valueOf(n.nodeChoice.choice.accept(this, null).toString());
	}
	
	@Override
	public Object visit(QualifiedDataName n, Object o) throws Exception {
	    String dataName = (String) n.nodeSequence.nodes.get(0).accept(this, null);
	    SymbolTable symTable = SymbolTable.getInstance();
	    NodeListOptional qualifiers = (NodeListOptional)n.nodeSequence.nodes.get(1);
	    if (qualifiers.present()) {
	        List<String> listQualifiers = new ArrayList<String>();
	        for (Node q : qualifiers.nodes) {
	            listQualifiers.add((String)((NodeSequence)q).nodes.get(1).accept(this, null));
	        }

	        List<SymbolProperties> listChild = symTable.findAll(dataName);
	        for (int i = listChild.size() - 1; i >= 0; i--) {
	            if (!checkQualifiers(listChild.get(i), listQualifiers, 0)) {
	                listChild.remove(i);
	            }
            }
	        
	        StringBuilder sb = new StringBuilder();
	        for (String s : listQualifiers) {
	            sb.append(" IN " + s);
	        }
	        
	        if (listChild.size() > 1) {
	            throw new ErrorInCobolSourceException(n, "Ambiguous identifier: " + dataName + sb.toString());
	        } else if (listChild.size() < 1) {
	            throw new ErrorInCobolSourceException(n, "Unknown identifier: " + dataName + sb.toString());
	        } else {
	            return listChild.get(0);
	        }
	        
	    } else {
	        List<SymbolProperties> l = symTable.findAll(dataName);
	        if (l.size() > 1) {
	            throw new ErrorInCobolSourceException(n, "Ambiguous identifier " + dataName);
	        } else if (l.size() == 0) {
	            throw new ErrorInCobolSourceException(n, "Unknown identifier " + dataName);
	        }
	        return l.get(0);
	    }
	}
	
	@Override
	public Object visit(DataName n, Object o) {
	    return n.cobolWord.nodeToken.tokenImage;
	}
}