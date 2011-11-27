package com.res.java.translation.engine;

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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;

import com.res.cobol.RESNode;
import com.res.cobol.parser.CobolParserConstants;
import com.res.cobol.syntaxtree.*;
import com.res.cobol.visitor.DepthFirstVisitor;
import com.res.common.RESConfig;
import com.res.common.RESContext;
import com.res.common.exceptions.ErrorInCobolSourceException;
import com.res.java.lib.Constants;
import com.res.java.lib.FieldFormat;
import com.res.java.lib.RunTimeUtil;
import com.res.java.translation.engine.Cobol2Java.IdentifierInfo;
import com.res.java.translation.symbol.SymbolConstants;
import com.res.java.translation.symbol.SymbolProperties;
import com.res.java.translation.symbol.SymbolTable;
import com.res.java.translation.symbol.SymbolProperties.CobolDataDescription;
import com.res.java.util.NameUtil;

public class CobolFillTable extends DepthFirstVisitor {
    private Cobol2Java cobol2Java = new Cobol2Java();

    private Stack<String> qualified = new Stack<String>();
    private Stack<SymbolProperties> dataStack = null;
    private SymbolProperties props = null;
    private SymbolProperties parent = null;
    private SymbolProperties paragraphProps = null;
    private SymbolProperties sectionProps = null;
    private String paragraphName = null;
    private String sectionName = null;
    private LiteralString literalString = new LiteralString();
    private String pictureString = null;
    private String dataName = null;
    private String levelNumber = null;
    private String value1 = null;
    private String value2 = null;
    private String lastTokenString = null;
    private int fillerMark = 0;
    private byte usage = Constants.DISPLAY;
    private boolean doLiteral;
    private boolean firstParagraph = true;
    private boolean doQualified = false;
    private boolean doingProgramName = false;
    private boolean doingValue = false;
    private boolean doingLevelNumber = false;
    private boolean doingDataName = false;
    private boolean doingPicture = false;
    public boolean isStatementInError;
    public String errorMessage;

    @Override
    public void visit(CompilationUnit n) throws Exception {
        SymbolTable.getInstance().setCloneOnLookup(false);
        super.visit(n);
    }

    @Override
    public void visit(NestedProgramUnit n) throws Exception {
        boolean saveFirstParagraph = firstParagraph;
        firstParagraph = true;
        n.nestedIdentificationDivision.accept(this);
        n.nodeOptional1.accept(this);
        n.nodeOptional.accept(this);
        postProcess(n);
        n.nodeOptional2.accept(this);
        n.nodeListOptional.accept(this);
        SymbolTable.getInstance().endProgram();
        firstParagraph = saveFirstParagraph;
    }

    private void postProcess(Node n) throws Exception {
        for (SymbolProperties file : filesToPostProcess) {
            if (file.getOtherData2() != null) {
                //file.setOtherData2(SymbolTable.getScope().lookup((String)file.getOtherData2(),SymbolConstants.DATA));
                if (file.getOtherData2() == null) {
                    throw new ErrorInCobolSourceException((RESNode)n, "Invalid Primary key in file " + file.getDataName() + ".");
                } else {
                    setRef(((SymbolProperties) file.getOtherData2()));
                }
            }
            if (file.getOtherData() != null && file.getOtherData().size() > 0) {
                ArrayList<Object> a = new ArrayList<Object>();
                for (Object o : file.getOtherData()) {
                    //o=SymbolTable.getScope().lookup((String)o,SymbolConstants.DATA);
                    if (o == null) {
                        throw new ErrorInCobolSourceException((RESNode)n, "Invalid Alternate key in file " + file.getDataName() + ".");
                    } else {
                        setRef(((SymbolProperties) o));
                        a.add(o);
                    }
                }
                file.setOtherData(a);
            }
        }
        if (dependingOnToPostProcess != null) {
            for (SymbolProperties dep : dependingOnToPostProcess) {
                if (dep.getOtherData2() != null) {
                    props = null;
                    ((Node) dep.getOtherData2()).accept(this);
                    if (props == null) {
                        throw new ErrorInCobolSourceException((RESNode) n, "Unknown symbol in DEPENDING ON clause of " + dep.getDataName() + ".");
                    } else {
                        dep.setDependingOnOccurs(props);
                        setRef(props);
                    }
                }
            }
        }
    }

    @Override
    public void visit(ProgramUnit n) throws Exception {
        firstParagraph = true;
        n.identificationDivision.accept(this);
        n.nodeOptional1.accept(this);
//        createSystemSymbols();
        n.nodeOptional.accept(this);
        postProcess(n);
        n.nodeOptional2.accept(this);
        SymbolTable.getInstance().endProgram();
        //super.visit(n);
    }

    private void createSystemSymbols() {

        props = createIndexSymbol("RETURN-CODE", SymbolTable.getInstance().getCurrentProgram(), true);
        props.setFromRESLibrary(true);
        //props.setJavaType(new CobolSymbol());
        adjustSetJavaName(props);

        props = createIndexSymbol("FILE-STATUS", SymbolTable.getInstance().getCurrentProgram(), true);
        props.setFromRESLibrary(true);
        //props.setJavaType(new CobolSymbol());
        adjustSetJavaName(props);

        props = createIndexSymbol("DEBUG-LINE", SymbolTable.getInstance().getCurrentProgram(), true);
        props.setPictureString("9(6)");
        props.setDataUsage(Constants.DISPLAY);
        //props.setJavaType(new CobolSymbol());
        props.setFromRESLibrary(true);
        adjustSetJavaName(props);

        props = createIndexSymbol("DEBUG-CONTENTS", SymbolTable.getInstance().getCurrentProgram(), true);
        props.setPictureString("X(100)");
        props.setDataUsage(Constants.DISPLAY);
        props.getCobolDesc().setTypeInJava(Constants.STRING);
        props.setFromRESLibrary(true);
        adjustSetJavaName(props);

        props = createIndexSymbol("DEBUG-NAME", SymbolTable.getInstance().getCurrentProgram(), true);
        props.setPictureString("X(100)");
        props.setDataUsage(Constants.DISPLAY);
        props.getCobolDesc().setTypeInJava(Constants.STRING);
        props.setFromRESLibrary(true);
        adjustSetJavaName(props);

        props = createIndexSymbol("DEBUG-SUB-1", SymbolTable.getInstance().getCurrentProgram(), true);
        props.setPictureString("X(10)");
        props.setDataUsage(Constants.DISPLAY);
        props.getCobolDesc().setTypeInJava(Constants.STRING);
        props.setFromRESLibrary(true);
        adjustSetJavaName(props);

        props = createIndexSymbol("DEBUG-SUB-2", SymbolTable.getInstance().getCurrentProgram(), true);
        props.setPictureString("X(10)");
        props.setDataUsage(Constants.DISPLAY);
        props.getCobolDesc().setTypeInJava(Constants.STRING);
        props.setFromRESLibrary(true);
        adjustSetJavaName(props);

        props = createIndexSymbol("DEBUG-SUB-3", SymbolTable.getInstance().getCurrentProgram(), true);
        props.setPictureString("X(10)");
        props.setDataUsage(Constants.DISPLAY);
        props.getCobolDesc().setTypeInJava(Constants.STRING);
        props.setFromRESLibrary(true);
        adjustSetJavaName(props);
    }

    @Override
    public void visit(SectionName n) throws Exception {
        super.visit(n);
        sectionName = lastTokenString;
        sectionProps = SymbolTable.getInstance().lookup(sectionName, SymbolConstants.SECTION);
    }

    @Override
    public void visit(ParagraphName n) throws Exception {
        super.visit(n);
        paragraphName = lastTokenString;
        paragraphProps = SymbolTable.getInstance().lookup(paragraphName, SymbolConstants.PARAGRAPH);
    }

    @Override
    public void visit(ProcedureName n) throws Exception {
        sectionName = paragraphName = null;
        n.nodeChoice.choice.accept(this);
        if (paragraphName != null && sectionName != null) {
            paragraphProps = SymbolTable.getInstance().lookup(paragraphName, sectionName);
        }
    }

    @Override
    public void visit(ProcedureSection n) throws Exception {

        parent = null;
        props = null;

        popToProgram();

        n.sectionHeader.accept(this);

        if ((props = createSection()) == null) {
            throw new ErrorInCobolSourceException(n, "Duplicate Section: " + sectionName);
        }

        n.paragraphs.accept(this);

    }

    private void popToProgram() {
        while (dataStack != null && dataStack.size() > 0) {
            parent = (SymbolProperties) dataStack.peek();
            if (parent.getType() == SymbolConstants.PROGRAM) {
                break;
            }
            if (props == null && parent.getType() == SymbolConstants.SECTION) {
                props = (SymbolProperties) dataStack.pop();
            } else {
                dataStack.pop();
            }
        }
    }

    private SymbolProperties createSection() {
        sectionProps = new SymbolProperties();

        if (firstParagraph) {
            firstParagraph = false;
            //parent.setFallThruPara(sectionProps);
        } else {
            //props.setFallThruPara(sectionProps);
        }

        props = sectionProps;
        props.setDataName(sectionName);
        props.setLevelNumber((short) 0);
        props.setType(SymbolConstants.SECTION);
        props.setParent(parent);
        props.setParagraphMark(new Integer(++SymbolTable.paragraphMark));
        adjustSetJavaName(props);
        addChild(props, parent);
        if (parent.getParagraphList() == null) {
            parent.setParagraphList(new ArrayList<SymbolProperties>());
        }
        parent.getParagraphList().add(props);

        //Lookup and Insert
        if (SymbolTable.getInstance().lookup(sectionName) != null) {
            return null;
        } else {
            SymbolTable.getInstance().insert(sectionName, props);
            dataStack.push(props);
            return props;
        }
    }

    @Override
    public void visit(Paragraph n) throws Exception {
        n.nodeChoice.choice.accept(this);
        if (n.nodeChoice.which == 0) {
            parent = null;
            props = null;
            while (dataStack != null && dataStack.size() > 0) {
                parent = (SymbolProperties) dataStack.peek();
                if (parent.getType() == SymbolConstants.PROGRAM
                        || parent.getType() == SymbolConstants.SECTION)  {
                    break;
                }
                if (props == null) {
                    props = (SymbolProperties) dataStack.pop();
                } else {
                    dataStack.pop();
                }
            }

            if ((parent == null && firstParagraph) || dataStack == null || dataStack.size() <= 0) {
                throw new ErrorInCobolSourceException(n, "Fatal Error " + ((parent == null) ? 2 : (dataStack == null) ? 3 : 4)
                        + " Encountered while processing Paragraph: " + paragraphName);
            }

            paragraphProps = new SymbolProperties();
            props = paragraphProps;
            props.setDataName(paragraphName);
            props.setLevelNumber((short) 0);
            props.setType(SymbolConstants.PARAGRAPH);
            props.setParent(parent);
            adjustSetJavaName(props);
        } else {
            while (dataStack != null && dataStack.size() > 0) {
                parent = (SymbolProperties) dataStack.peek();
                if (parent.getType() == SymbolConstants.PROGRAM
                        || parent.getType() == SymbolConstants.SECTION) {
                    break;
                }
                dataStack.pop();
            }
            props.setParent(parent);
            return;
        }

        //	props.setParagraphMark(new Integer(++SymbolTable.paragraphMark));
        addChild(props, parent);
        if (parent.getParagraphList() == null) {
            parent.setParagraphList(new ArrayList<SymbolProperties>());
        }
        parent.getParagraphList().add(props);
        if (SymbolTable.getInstance().lookup(paragraphName, parent.getDataName()) != null) {
            throw new ErrorInCobolSourceException(n, "Duplicate Paragraph: " + paragraphName);
        } else {
            SymbolTable.getInstance().insert(paragraphName, props);
            ((RESNode) n).props = props;
            dataStack.push(props);
        }
        n.nodeChoice1.accept(this);
    }

    @Override
    public void visit(EntryStatement n) throws Exception {
        n.literal.accept(this);
        createProgram(paragraphName = RunTimeUtil.getInstance().stripQuotes(literalString.toString(), true), true);
    }

    private SymbolProperties find01Level(SymbolProperties par) {
        while (!(par == null || par.isProgram() || par.isFile() || par.getLevelNumber() == 01 || par.getLevelNumber() == 77)) {
            par = par.getParent();
        }
        return par;
    }
    private boolean doUseStatementsInDeclaratives = false;

    @Override
    public void visit(ProcedureDivision n) throws Exception {
        doUseStatementsInDeclaratives = false;
        n.nodeOptional1.accept(this);
        if (n.procedureBody.paragraphs.nodeListOptional.present()) {
            ++SymbolTable.paragraphMark;
            n.procedureBody.paragraphs.nodeListOptional.accept(this);
        }
        if (n.procedureBody.paragraphs.nodeListOptional1.present()) {
            n.procedureBody.paragraphs.nodeListOptional1.accept(this);
        }
        if (n.procedureBody.nodeListOptional.present()) {
            n.procedureBody.nodeListOptional.accept(this);
        }
        n.nodeOptional.accept(this);
        doUseStatementsInDeclaratives = true;
        n.nodeOptional1.accept(this);
        /*if (!RESConfig.getInstance().isInError()) {
            SymbolTable.visit(SymbolTable.getInstance().getCurrentProgram(), new CalculateSymbolLength());
        }*/
    }

    @Override
    public void visit(UsingArgs n) throws Exception {
        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {
            props = null;
            e.nextElement().accept(this);
            if (props != null) {
                if (!props.isTopLevelData()) {
                    throw new ErrorInCobolSourceException(n, "Using clause arguments must be 01 or 77 levels only.");
                }
                if (!props.isLinkageSection()) {
                    throw new ErrorInCobolSourceException(n, "Using clause arguments must be defined in Linkage Section only.");
                }
                setRef(props);
                setMod(props);
            }
        }

    }

    @Override
    public void visit(NodeToken n) throws Exception {
        if (n.tokenImage.trim().length() <= 0) {
            return;
        }
        lastTokenString = n.tokenImage;
        if (firstTokenInStatement == null) {
            firstTokenInStatement = n;
        }
        super.visit(n);
        if (doQualified) {
            if (qualified == null) {
                qualified = new Stack<String>();
            }
            if (n.tokenImage != null) {
                if (n.tokenImage.equalsIgnoreCase("IN")
                        || n.tokenImage.equalsIgnoreCase("OF")); else {
                    qualified.push(n.tokenImage);
                }
            }
        } else if (doLiteral) {
            literalString.literal.append(n.tokenImage);
        } else if (doingDataName) {
            if (dataName == null) {
                dataName = "";
            }
            dataName += n.tokenImage;
        } else if (doingLevelNumber) {
            if (levelNumber == null) {
                levelNumber = "";
            }
            levelNumber += n.tokenImage;
        } else if (doingValue) {
            if (value1 == null) {
                value1 = "";
                value1 += n.tokenImage;
            } else if (n.tokenImage.equalsIgnoreCase("THROUGH")
                    || n.tokenImage.equalsIgnoreCase("THRU")) {
                if (value1 == null) {
                    value1 = "";
                }
            } else {
                value2 = "";
                value2 += n.tokenImage;
            }
        } else if (doingPicture) {
            pictureString += n.tokenImage;
        } 
    }

    @Override
    public void visit(Literal n) throws Exception {
        doLiteral = true;
        literalString = new LiteralString();
        if (n.nodeChoice.which == 0 || n.nodeChoice.which == 1 || n.nodeChoice.which == 2) {
            literalString = (LiteralString) n.nodeChoice.accept(cobol2Java, false);
        } else {
            n.nodeChoice.accept(this);
        }
        doLiteral = false;
    }

    @Override
    public void visit(FigurativeConstant n) throws Exception {
        String lit = null;
        byte cat = 0;
        switch (n.nodeChoice.which) {
            case 0:
            case 1:
            case 2:
                lit = "0";
                cat = Constants.ZERO;
                break;
            case 3:
            case 4:
                lit = " ";
                cat = Constants.SPACE;
                break;
            case 5:
            case 6:
                lit = "HIGH-VALUE";
                cat = Constants.HIGH_VALUE;
                break;
            case 7:
            case 8:
                lit = "LOW-VALUE";
                cat = Constants.LOW_VALUE;
                break;
            case 9:
            case 10:
                lit = "\"";
                cat = Constants.QUOTE;
                break;
            case 11:
            case 12:
            default:
                lit = "0";
                break;
        }
        if (doLiteral) {
            literalString.literal.append(lit);
        } else {
            literalString = new LiteralString(lit);
        }
        literalString.category = cat;
    }
    private ArrayList<SymbolProperties> filesToPostProcess = new ArrayList<SymbolProperties>();

    @Override
    public void visit(FileAndSortDescriptionEntry n) throws Exception {
        while (!dataStack.peek().isProgram()) {
            dataStack.pop();
        }
        if (context.getTraceLevel() >= 2) {
            System.out.println("Doing CobolFillTable FD/SD " + n.line);
        }
        isStatementInError = false;
        usage = Constants.DISPLAY;
        n.fileName.accept(this);
        while (dataStack.size() > 0 && (parent = (SymbolProperties) dataStack.peek()) != null && parent.getType() != SymbolConstants.PROGRAM) {
            dataStack.pop();
        }
        if (lastTokenString != null && lastTokenString.trim().length() > 0
                && parent != null && parent.getType() == SymbolConstants.PROGRAM
                && (SymbolTable.getInstance().lookup(lastTokenString, parent.getDataName())) == null) {
            props = new SymbolProperties();
            props.setType(SymbolConstants.FILE);
            props.setDataName(lastTokenString);
            props.setLength(0);
            props.setAdjustedLength(0);
            props.setParent(parent);
            n.nodeListOptional.accept(this);
            if (doExternal(SymbolConstants.FILE)) {
                return;
            }
            addChild(props, parent);
            adjustSetJavaName(props);
            SymbolTable.getInstance().insert(props.getDataName(), props);
            dataStack.push(props);
            filesToPostProcess.add(dataStack.peek());
            props = null;
            if (RESConfig.getInstance().isPrintCobolStatementsAsComments()
                    || RESConfig.getInstance().isRetainCobolComments()) {
                //props.setDataDescriptionEntry(n);
            }
        }

    }

    @Override
    public void visit(DataBlankWhenZeroClause n) throws Exception {
        props.setBlankWhenZero(true);
    }

    @Override
    public void visit(ExternalClause n) throws Exception {
        props.setExternal(true);
    }

    @Override
    public void visit(SameAreaClause n) throws Exception {
        redefinesName = null;
        SymbolProperties par = null,prev=null;
        ArrayList<SymbolProperties> a = new ArrayList<SymbolProperties>();
        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {
            e.nextElement().accept(this);
            if (props != null) {
                a.add(props);
                if(prev==null)
                    prev=props;
                else
                    prev=((prev.getInternalMark()<=props.getInternalMark())?prev:props);
            }
        }
        if(prev==null) return;
        redefinesName=prev.getChildren().get(0).getDataName();par=prev;
        for(SymbolProperties props2:a) {
            if (prev.getDataName().equalsIgnoreCase(props2.getDataName())) {
                continue;
            }
            props=props2.getChildren().get(0);
            
            try {
                doRedefines(par);
            } catch (ErrorInCobolSourceException e) {
                throw new ErrorInCobolSourceException(n, e.getMessage());
            }
        }
    }
    private boolean doingLinkageSection = false;

    @Override
    public void visit(LinkageSection n) throws Exception {
        doingLinkageSection = true;
        //Pop the file name as not parents until we reach the program
        while (dataStack.size() > 0 && ((SymbolProperties) dataStack.peek()).getType() != SymbolConstants.PROGRAM) {
            dataStack.pop();
        }
        super.visit(n);
        doingLinkageSection = false;
    }

    @Override
    public void visit(DataDivision n) throws Exception {
        super.visit(n);
        SymbolProperties currentProgram = SymbolTable.getInstance().getCurrentProgram();
        SymbolTable.visit(currentProgram, new SymbolValidation());
        SymbolTable.visit(currentProgram, new CalculateSymbolLength());
    }
    
    @Override
    public void visit(WorkingStorageSection n) throws Exception {

        //Pop the file name as not parents until we reach the program
        while (dataStack.size() > 0 && ((SymbolProperties) dataStack.peek()).getType() != SymbolConstants.PROGRAM) {
            dataStack.pop();
        }

        super.visit(n);
    }

    @Override
    public void visit(AccessModeClause n) throws Exception {
        props.setOtherData1(n.nodeChoice.which);
        n.nodeChoice.choice.accept(this);
    }

    @Override
    public void visit(AlternateRecordKeyClause n) throws Exception {
        SymbolProperties saveProps = props;
        n.qualifiedDataName.accept(this);
        if (saveProps.getOtherData() == null) {
            saveProps.setOtherData(new ArrayList<Object>());
        }
        props.setDuplicateKey(n.nodeOptional3.present());
        saveProps.getOtherData().add(props);
        props = saveProps;
    }

    @Override
    public void visit(KeyClause n) throws Exception {
        SymbolProperties saveProps = props;
        n.qualifiedDataName.accept(this);
        saveProps.setOtherData2(props);
        props = saveProps;
    }

    @Override
    public void visit(RecordContainsClause n) throws Exception {
        switch (n.nodeChoice.which) {
            case 0:
                ((NodeSequence) n.nodeChoice.choice).elementAt(1).accept(this);
                props.setAdjustedLength((int) integerConstant);
                if (((NodeOptional) ((NodeSequence) n.nodeChoice.choice).elementAt(0)).present()) {
                    ((NodeOptional) ((NodeSequence) n.nodeChoice.choice).elementAt(0)).accept(this);
                    props.setLength((int) integerConstant);
                } else {
                    props.setLength(props.getAdjustedLength());
                }
                break;
            case 1:
            //TODO VARYING DEPENDING ON
        }
    }
    private boolean doNotAddChildrenToSymbolTable = false;

    @Override
    public void visit(DataDescriptionEntry n) throws Exception {
        if (context.getTraceLevel() >= 2) {
            System.out.println("Doing CobolFillTable DataDescriptionEntry " + n.line);
        }
        if (n.nodeChoice.which >= 5) {
            return;
        }
        isStatementInError = false;
        usage = Constants.DISPLAY;

        props = new SymbolProperties();

        props.setLinkageSection(doingLinkageSection);
        pictureString = null;
        redefinesName = null;
        props.setDataDescriptionEntry(n);
//        if (RESConfig.getInstance().isPrintCobolStatementsAsComments()
//                || RESConfig.getInstance().isRetainCobolComments()) {
//        }

        //Initialize
        NodeSequence v = (NodeSequence) n.nodeChoice.choice;
        int i = 0;
        for (Enumeration<Node> e = v.elements(); e.hasMoreElements();) {
            doingLevelNumber = false;
            doingDataName = false;
            if (i == 0) {
                doingLevelNumber = true;
                levelNumber = null;
            }
            if (i == 1) {
                dataName = null;
                doingDataName = true;
            }
            e.nextElement().accept(this);
            i++;
        }
        propagateRef();
        propagateMod();

        doingDataName = false;
        doingLevelNumber = false;
        //post-process

        if (levelNumber != null && levelNumber.length() > 0) {
            short lv = Short.valueOf(levelNumber);
            props.setLevelNumber(lv);
            if (props.isOccurs() && (lv == 1 || lv == 66 || lv == 77 || lv == 88)) {
                throw new ErrorInCobolSourceException(n, "OCCURS clause cannot appear in entry with level " + lv);
            }
        } else {
            //TODO Error processing
            return;
        }

        props.setDataCategory(FieldFormat.getDataCategory(pictureString));
        if (props.getLevelNumber() == 1) {
            doNotAddChildrenToSymbolTable = false;
        }
        if (doNotAddChildrenToSymbolTable) {
            return;
        }

        if (dataName == null || dataName.equalsIgnoreCase("FILLER")) {
            dataName = "FILLER" + new Integer(fillerMark++).toString().trim() + "_";
            props.setIsFiller(true);
        }

        props.setDataName(dataName);

        if (props.getLevelNumber() == 78) {
            createIndexSymbol(dataName, SymbolTable.getInstance().getCurrentProgram(), false);
            if (props.getValues() == null || props.getValues().size() <= 0) {
                throw new ErrorInCobolSourceException(n, "78 level VALUE must not be null.");
            }
            return;
        }


        /*if (props == null || dataName == null) {
            return;
        }*/
        
        if (context.getTraceLevel() >= 2) {
            System.out.println("Doing CobolFillTable symbol " + dataName);
        }
        props.setType(SymbolConstants.DATA);
        if (usage == Constants.INDEX) {//INDEX
            parent = SymbolTable.getInstance().getCurrentProgram();
        } else {
            parent = find01Level(dataStack.peek());
            if (parent != null && parent.getDataUsage() == Constants.INDEX
                    && !(props.getLevelNumber() == 77 || props.getLevelNumber() == 1
                    || props.getLevelNumber() == 66 || props.getLevelNumber() == 88)
                    && props.getLevelNumber() > parent.getLevelNumber()) {
                pictureString = INDEX_PICTURE_STRING;
                props.setDataUsage(Constants.INDEX);
                props.setIndexRedefines(true);
                props.setRedefines(parent);
                parent = SymbolTable.getInstance().getCurrentProgram();
            } else if (props.getLevelNumber() == 66) {
            	parent = SymbolTable.getInstance().getCurrentProgram();
                parent.setHasRenames(true);
            } else {
                while (dataStack.size() > 0
                        && (parent = (SymbolProperties) dataStack.peek()) != null) {
                    if ((props.getLevelNumber() == 77 || props.getLevelNumber() == 1) && parent.getLevelNumber() != 0) {
                        dataStack.pop();
                    } else if (props.getLevelNumber() <= parent.getLevelNumber()) {
                        dataStack.pop();
                    } else {
                        break;
                    }
                }
            }
        }

        if (dataStack.size() <= 0) {
            parent = null;
        }
        if (parent != null) {
            props.setParent(parent);
            if (parent.isForceCobolBytes())
            	props.setForceCobolBytes(true);
            props.setExternal(props.isExternal() || parent.isExternal());
            if (doExternal(SymbolConstants.DATA)) {
                return;
            }
            addChild(props, parent);
            props.setIsSuppressed(false);
            try {
                doRedefines(parent);
            } catch (ErrorInCobolSourceException e) {
                throw new ErrorInCobolSourceException(n, e.getMessage());
            }
            doVaryingPicture();
        }


        //do the symbol table and level stack

        if (props.isFloatingPoint()) {
            if (props.getDataUsage() == Constants.COMPUTATIONAL1) {
                props.setPictureString("-.9(8)E-99");
            } else {
                props.setPictureString("-.9(17)E-99");
            }
        } else {
            props.setPictureString(pictureString);
        }
        adjustSetJavaName(props);
        
        if (props.isGroupData()) {
            props.setDataUsage(usage);
        } else {
            if (parent != null && parent.isGroupData() && parent.getDataUsage() != Constants.DISPLAY) {
                if (usage != Constants.DISPLAY && usage != parent.getDataUsage()) {
                    throw new ErrorInCobolSourceException(n, "Conflict usage with parent");
                } else {
                    props.setDataUsage(parent.getDataUsage());
                }
            } else {
                props.setDataUsage(usage);
            }
        }
//        props.setDataUsage(usage);
        
        if (parent != null) {
        	boolean exist = false;
        	String name = props.getJavaName2();
        	for (String s : parent.getQualifiedName().split("\\.")) {
        		if (s.equalsIgnoreCase(name)) {
        			exist = true;
        			break;
        		}
        		continue;
        	}
        	if (exist) {
        	    throw new ErrorInCobolSourceException(n, "Duplicate name exist in parent group: " + props.getDataName());
        	}
        }
        
        SymbolProperties tmp = SymbolTable.getInstance().lookup(
				props.getDataName(),
				(parent == null ? null : parent.getDataName()));
        
        if (tmp != null && tmp.getType() == SymbolConstants.DATA) {
        	if (parent == null) {
        	    throw new ErrorInCobolSourceException(n, "Duplicate data name: " + props.getDataName());
        	} else {
        		String qName = parent.getQualifiedName() + "." + props.getJavaName2();
        		if (qName.equalsIgnoreCase(tmp.getQualifiedName())) {
        			throw new ErrorInCobolSourceException(n, "Duplicate data name: "
							+ props.getDataName() + " in "
							+ parent.getDataName());
        		}
        	}
        }
        
        SymbolTable.getInstance().insert(props.getDataName(), props);
        dataStack.push(props);

        if (RESConfig.getInstance().isAllSymbols()) {
            props.setRef(true);
        }

        if (dataName.equalsIgnoreCase("SQLCA") && props.getLevelNumber() == 1 && pictureString == null) {
            props.setRef(false);
            props.setMod(false);
            props.setFromRESLibrary(true);
            props.getCobolDesc().setTypeInJava(Constants.GROUP);
            SymbolProperties tempProps = createDataSymbol("sqlcode", props, "9(8)", Constants.BINARY, 02);
            tempProps.setFromRESLibrary(true);
            tempProps.setIsFormat(false);
            tempProps.setRef(true);
            tempProps = createDataSymbol("sqlwarn", props, "X(8)", Constants.DISPLAY, 02);
            tempProps.setFromRESLibrary(true);
            //tempProps.setJavaType(new CobolSymbol());
            tempProps.setIsFormat(false);
            tempProps.setRef(true);
            tempProps = createDataSymbol("sqlerrmc", props, "X(80)", Constants.DISPLAY, 02);
            tempProps.setFromRESLibrary(true);
            tempProps.getCobolDesc().setTypeInJava(Constants.STRING);
            tempProps.setIsFormat(false);
            tempProps.setRef(true);
            tempProps = createDataSymbol("sqlerrd", props, "9(8)", Constants.BINARY, 02);
            tempProps.setFromRESLibrary(true);
            //tempProps.setJavaType(new CobolSymbol());
            tempProps.setIsFormat(false);
            tempProps.setRef(true);
            tempProps.setOccurs(true);
            tempProps.setMaxOccurs(5);
            doNotAddChildrenToSymbolTable = true;
        }

        if (props.getDataCategory() == Constants.NUMERIC_EDITED
                || (FieldFormat.isString(props.getPictureString()) && !FieldFormat.isPlainString(props.getPictureString()))) {
            props.setIsFormat(true);
        }
        //if(props.getDataCategory()==Constants.NUMERIC&&props.getDataUsage()==Constants.DISPLAY)
        //props.setForceCobolBytes(true);
        props = null;
    }

    //Type can be DATA or FILE
    private boolean doExternal(int type) {
        if (props == null) {
            return false;
        }
        SymbolProperties props2 = SymbolTable.getInstance().lookup(props.getDataName(), type);
        if (props2 != null) {
            boolean b = props2.isExternal();

            if (props.getLevelNumber() == 01 || props.isFile()) {
                b = props.isExternal();
            } else {
                SymbolProperties props3 = find01Level(dataStack.peek());
                if (props3 != null && ((props3.getLevelNumber() == 1 && type == SymbolConstants.DATA)
                        || (props3.isFile() && type == SymbolConstants.FILE))) {
                    b = props3.isExternal();
                }
            }
            if (props2.isExternal() && b) {
                dataStack.push((SymbolProperties) props2);
                return true;
            }

        }
        return false;
    }

    private void doVaryingPicture() {

        if (props == null || !props.isVarying()) {
            return;
        }

        SymbolProperties tempProps;
        String key1 = props.getDataName() + "-LEN";
        String key2 = props.getDataName() + "-ARR";
        if (props != null && props.isVarying() && pictureString != null
                && (tempProps = SymbolTable.getInstance().lookup(key1, props.getDataName())) == null
                && (tempProps = SymbolTable.getInstance().lookup(key2, props.getDataName())) == null) {

            tempProps = new SymbolProperties();
            tempProps.setType(SymbolConstants.DATA);
            tempProps.setDataName(key1);
            tempProps.setParent(props);
            tempProps.setPictureString(VARYING_LEN_PICTURE_STRING);
            //tempProps.setJavaType(new CobolSymbol());
            addChild(tempProps, props);
            tempProps.setVaryingLen(true);
            SymbolTable.getInstance().insert(key1, tempProps);
            tempProps = new SymbolProperties();
            tempProps.setType(SymbolConstants.DATA);
            tempProps.setDataName(key2);
            tempProps.setParent(props);
            tempProps.setPictureString(pictureString);
            tempProps.getCobolDesc().setTypeInJava(Constants.STRING);
            addChild(tempProps, props);
            tempProps.setVaryingArray(true);
            SymbolTable.getInstance().insert(key2, tempProps);
        }
        return;


    }

    @SuppressWarnings("unused")
    private void doRedefines(SymbolProperties par) throws ErrorInCobolSourceException {
        SymbolProperties data2 = null;
        if (par.isFile() && par.getChildren().size() > 1) {
            data2 = par.getChildren().get(0);
        } else {
            if (redefinesName == null) {
                return;
            }
            /*if (par == null) {
                data2 = SymbolTable.getInstance().lookup(redefinesName);
            } else {
                data2 = SymbolTable.getInstance().lookup(redefinesName, par.getDataName());
                 
            }*/
            if (par != null && par.getChildren() != null && par.getChildren().size() > 0) {
                for (SymbolProperties child : par.getChildren()) {
                    if (child.getDataName().equalsIgnoreCase(redefinesName)) {
                        data2 = child;
                        break;
                    } else if (child == props) {
                        break;
                    }
                }
            }
        }
        
        if (data2 == null) {
            throw new ErrorInCobolSourceException("Invalid redefined identifier: " + redefinesName + " in " + par.getDataName());
        } else {        
            if (data2.isOccurs()) {
                throw new ErrorInCobolSourceException("Occurs field cannot be redefined.");
            }
            
            if (data2.getRedefines() != null) {
                throw new ErrorInCobolSourceException(redefinesName + " is not an original storage area.");
            }
            
            if (!par.isFile()) {
                ArrayList<SymbolProperties> children = par.getChildren();
                int i = children.indexOf(data2) + 1;
                int j = children.indexOf(props);
                for (; i < j; i++) {
                    SymbolProperties tmp = children.get(i);
                    if (tmp.getRedefines() == null)
                        throw new ErrorInCobolSourceException("REDEFINE entry must follow immediately the description of redefined area.");
                }
            }
        	
            props.setRedefines(data2);
//            setForceByte(props);
            props.setForceCobolBytes(true);
            setForceByte(data2);
            ArrayList<SymbolProperties> a = data2.getRedefinedBy();
            if (a == null) {
                a = new ArrayList<SymbolProperties>();
            }
            a.add(props);
            data2.setRedefinedBy(a);
            /*SymbolProperties tmp = data2;
            while ((tmp = tmp.getRedefines()) != null) {
                tmp.getRedefinedBy().add(props);
            }*/
            while (par != null && par.getType() != SymbolConstants.PROGRAM && par.getLevelNumber() != 1 && par.getLevelNumber() != 77) {
                par = par.getParent();
            }
            if (par != null) {
                par.setAChildHasRedefines(true);
            }
        }
    }

    private void setForceByte(SymbolProperties props) {
    	props.setForceCobolBytes(true);
    	if (props.getChildren() != null) {
    		for (SymbolProperties p : props.getChildren()) {
    			setForceByte(p);
    		}
    	}
    }
    
    @Override
    public void visit(Subscript n) throws Exception {
        SymbolProperties propsDataName = props;
        //for (Enumeration e=n.nodeList.elements();e.hasMoreElements();) {
        //NodeChoice ch=(NodeChoice)e.nextElement();
        switch (n.nodeChoice.which) {
            case 0:
                break;
            case 1:
                NodeSequence seq = (NodeSequence) n.nodeChoice.choice;
                seq.elementAt(0).accept(this);
                if (isStatementInError || props == null) {
                    return;
                }
                setRef(props);
                break;
            case 2:
        }
        //}
        props = propsDataName;
    }

    @Override
    public void visit(AddStatement n) throws Exception {
        expressionType = Constants.INTEGER;
        super.visit(n);
    }

    @Override
    public void visit(DivideStatement n) throws Exception {
        expressionType = Constants.INTEGER;
        super.visit(n);
    }

    @Override
    public void visit(MultiplyStatement n) throws Exception {
        expressionType = Constants.INTEGER;
        super.visit(n);
    }

    @Override
    public void visit(SubtractStatement n) throws Exception {
        expressionType = Constants.INTEGER;
        super.visit(n);
    }

    @Override
    public void visit(ArithIdentifier n) throws Exception {
        super.visit(n);
        if (props == null) {
            return;
        }
        setMod(props);
        if (FieldFormat.verifyCobolPicture(props.getPictureString()) == Constants.BIGDECIMAL) {
            //SymbolTable.programs.peek().setImportBigDecimal(true);
            expressionType = Constants.BIGDECIMAL;
        }
    }

    @Override
    public void visit(IdOrLiteral n) throws Exception {
        super.visit(n);
        switch (n.nodeChoice.which) {
            case 0:
                if (isStatementInError || props == null) {
                    return;
                }
                setRef(props);
                if (props.getPictureString() == null) {
                    return;
                }
                if (FieldFormat.verifyCobolPicture(props.getPictureString()) == Constants.BIGDECIMAL) {
                    //SymbolTable.programs.peek().setImportBigDecimal(true);
                    expressionType = Constants.BIGDECIMAL;
                }
                break;
            case 1:
                //int prevExpressionType=expressionType;
                //formatLiteral(literal);
                if (expressionType == Constants.BIGDECIMAL) {
                    SymbolTable.getInstance().getCurrentProgram().setImportBigDecimal(true);
                }
            default:
        }
    }

    @Override
    public void visit(ProgramIdParagraph n) throws Exception {
        doingProgramName = true;
        //super.visit(n);
        n.programName.accept(this);
        doingProgramName = false;
        SymbolTable.getInstance().startProgram((SymbolProperties) dataStack.peek());
    }

    @Override
    public void visit(ProgramName n) throws Exception {
        super.visit(n);
        if (doingProgramName) {
            createProgram(n.cobolWord.nodeToken.tokenImage, false);
        }
    }

    private void createProgram(String name, boolean isEntry) throws Exception {

        props = new SymbolProperties();
        props.setDataName(name);
        props.setLevelNumber((short) 0);
        props.setType(SymbolConstants.PROGRAM);

        if (isEntry) {
            adjustSetJavaName(props);
            SymbolTable.getInstance().insert(name, props);
            return;
        }

        if (dataStack == null) {
            dataStack = new Stack<SymbolProperties>();
        } else;

        while (dataStack.size() > 0
                && (parent = (SymbolProperties) dataStack.peek()) != null) {
            if (!parent.isProgram()) {
                dataStack.pop();
            } else {
                break;
            }
        }

        if (dataStack.size() <= 0) {
            parent = null;
        }
        SymbolProperties symbol = null;
        if (parent != null) {
            symbol =
                    SymbolTable.getInstance().lookup(name, (String) parent.getDataName());
            if (symbol != null) {
                throw new ErrorInCobolSourceException("Duplicate symbol"
                        + (String) props.getDataName() + " IN " + parent.getDataName());
            } else {
                props.setParent(parent);
                addChild(props, parent);
            }
        } else {
            symbol = SymbolTable.getInstance().lookup(name, SymbolConstants.DATA);
            if (symbol != null && props.getParent() == null) {
                throw new ErrorInCobolSourceException("Duplicate symbol"
                        + (String) props.getDataName());
            }
        }
        adjustSetJavaName(props);
        SymbolTable.getInstance().insert(name, props);
        dataStack.push(props);
        props = null;
    }

    @Override
    public void visit(NestedProgramIdParagraph n) throws Exception {
        doingProgramName = true;
        super.visit(n);
        SymbolTable.getInstance().startProgram((SymbolProperties) dataStack.peek());
        doingProgramName = false;
    }

    @Override
    public void visit(DataExternalClause n) throws Exception {
        props.setExternal(true);
    }

    @Override
    public void visit(DataJustifiedClause n) throws Exception {
        props.setJustifiedRight(n.nodeOptional.present());
    }

    @Override
    public void visit(DataName n) throws Exception {
        super.visit(n);
        if (insertDummyData) {
            SymbolProperties tempProps = new SymbolProperties();
            tempProps.setDataName(lastTokenString);
            tempProps.setType(SymbolConstants.DUMMY);
            //tempProps.setJavaType(new CobolSymbol());
            tempProps.getCobolDesc().setTypeInJava(Constants.OBJECT);
            SymbolTable.getInstance().insert(tempProps.getDataName(), tempProps);
        }
    }

    @Override
    public void visit(CdName n) throws Exception {
        super.visit(n);
        SymbolProperties tempProps = new SymbolProperties();
        tempProps.setDataName(lastTokenString);
        tempProps.setType(SymbolConstants.DUMMY);
       // tempProps.setJavaType(new CobolSymbol());
        tempProps.getCobolDesc().setTypeInJava(Constants.OBJECT);
        SymbolTable.getInstance().insert(tempProps.getDataName(), tempProps);
    }

    @Override
    public void visit(MoveStatement n) throws Exception {
        switch (n.nodeChoice.which) {
            case 0:
                NodeSequence nodeseq = (NodeSequence) n.nodeChoice.choice;
                if (nodeseq.size() != 3) {
                    return;
                }
                
                // validate moving rules
                Object sender = nodeseq.nodes.get(0).accept(cobol2Java, false);
                byte fromCat = 0;
                byte fromJavaType = 0;
                if (sender instanceof LiteralString) {
                    LiteralString tmp = (LiteralString) sender;
                    fromCat = tmp.category;
                    fromJavaType = tmp.javaType;
                } else {
                    CobolDataDescription desc = ((IdentifierInfo) sender).getQualifiedName().getCobolDesc();
                    fromCat = desc.getDataCategory();
                    fromJavaType = desc.getTypeInJava();
                }
                
                for (Node receiver : ((NodeList)nodeseq.nodes.get(2)).nodes) {
                    CobolDataDescription toDesc = ((IdentifierInfo) ((NodeSequence) receiver).nodes.get(0).accept(cobol2Java, null)).getQualifiedName().getCobolDesc();
                    if (!validateMoveRules(fromCat, fromJavaType, toDesc.getDataCategory())) {
                        throw new ErrorInCobolSourceException(n, "Invalid MOVE statement.");
                    }
                }
                
                // end validation
                
                SymbolProperties propsFrom = null;
                boolean isSourceANumber = false;
                int javaType1 = 0,
                 dataUsage1 = 0,
                 javaType2 = 0,
                 dataUsage2 = 0;
                NodeChoice choice2 = (NodeChoice) nodeseq.elementAt(0);
                ;
                choice2.accept(this);
                if (choice2.which == 0) {
                    propsFrom = props;
                    if (isStatementInError || props == null) {
                        return;
                    }
                    javaType1 = FieldFormat.verifyCobolPicture(propsFrom.getPictureString());
                    dataUsage1 = propsFrom.getDataUsage();
                    setRef(props);
                    isSourceANumber = (verifyCobolPicture(props.getPictureString()) <= Constants.BIGDECIMAL);
                } else {
                    formatLiteral(literalString);
                    javaType1 = expressionType;
                    dataUsage1 = Constants.BINARY;
                    isSourceANumber = (expressionType <= Constants.BIGDECIMAL);
                }
                if (nodeseq.elementAt(2) instanceof NodeList) {
                    for (Enumeration<Node> e = ((NodeList) nodeseq.elementAt(2)).elements();
                            e.hasMoreElements();) {
                        Node node = e.nextElement();
                        node.accept(this);
                        if (isStatementInError || props == null) {
                            return;
                        }
                        setMod(props);
                        javaType2 = FieldFormat.verifyCobolPicture(props.getPictureString());
                        dataUsage2 = props.getDataUsage();
                        if (javaType2 <= Constants.BIGDECIMAL) {
                            if (dataUsage2 != Constants.DISPLAY) {
                                props.setIsFormat(!props.isFromRESLibrary());
                            }
                        } else if (verifyCobolPicture(props.getPictureString()) == Constants.STRING
                                && !FieldFormat.isPlainString(props.getPictureString())) {
                            props.setIsFormat(!props.isFromRESLibrary());
                        }
                        if (propsFrom != null) {
                            if (javaType1 <= Constants.BIGDECIMAL) {
                                if (dataUsage1 != Constants.DISPLAY) {
                                    propsFrom.setIsFormat(!props.isFromRESLibrary());
                                }
                            } else if (javaType1 == Constants.STRING
                                    && !FieldFormat.isPlainString(propsFrom.getPictureString())) {
                                propsFrom.setIsFormat(!props.isFromRESLibrary());
                            }
                        }
                    }
                }
                break;
            case 1:
                nodeseq = (NodeSequence) n.nodeChoice.choice;
                propsFrom = null;
                isSourceANumber = false;
                nodeseq.elementAt(1).accept(this);
                if (isStatementInError || props == null) {
                    return;
                }
                propsFrom = props;
                setRef(props);

                if (nodeseq.elementAt(3) instanceof NodeList) {
                    for (Enumeration<Node> e = ((NodeList) nodeseq.elementAt(3)).elements();
                            e.hasMoreElements();) {
                        Node node = e.nextElement();
                        node.accept(this);
                        setMod(props);
                        doCorrespondingMove(propsFrom, props, true);
                    }
                }
        }
    }

    public static boolean validateMoveRules(byte fromCat, byte fromJavaType, byte toCat) {
        if (toCat == Constants.ALPHABETIC) {
            if (fromCat == Constants.ALPHABETIC
                    || fromCat == Constants.ALPHANUMERIC
                    || fromCat == Constants.ALPHANUMERIC_EDITED
                    || fromCat == Constants.SPACE) {
                return true;
            }
            return false;
        } else if (toCat == Constants.ALPHANUMERIC
                || toCat == Constants.ALPHANUMERIC_EDITED) {
            if (fromCat == Constants.NUMERIC && fromJavaType == Constants.BIGDECIMAL) {
                return false;
            }
            return true;
        } else if (toCat == Constants.NUMERIC
                || toCat == Constants.NUMERIC_EDITED) {
            if (fromCat == Constants.ALPHABETIC || fromCat == Constants.SPACE
                    || fromCat == Constants.ALPHANUMERIC_EDITED
                    || fromCat == Constants.QUOTE || fromCat == Constants.HIGH_VALUE
                    || fromCat == Constants.LOW_VALUE) {
                return false;
            }
            return true;
        }
        return false;
    }
    
    private int verifyCobolPicture(String picture) {
        if (picture == null) {
            return Constants.GROUP;
        } else {
            return FieldFormat.verifyCobolPicture(picture);
        }
    }

    private void doCorrespondingMove(SymbolProperties from, SymbolProperties to, boolean first) {
        propagateRefAllChildren(from);
        propagateModAllChildren(to);
        if ((from = find01Level(from)) != null) {
            from.setRef(true);
        }
        if ((to = find01Level(to)) != null) {
            to.setMod(true);
        }
    }

    private void propagateModAllChildren(SymbolProperties to) {
        to.setMod(true);
        if (to.hasChildren()) {
            for (SymbolProperties ch : to.getChildren()) {
                propagateModAllChildren(ch);
            }
        }
    }

    private void propagateRefAllChildren(SymbolProperties from) {
        from.setRef(true);
        if (from.hasChildren()) {
            for (SymbolProperties ch : from.getChildren()) {
                propagateRefAllChildren(ch);
            }
        }
    }
    private static final String INDEX_PICTURE_STRING = "9999999";
    private static final String VARYING_LEN_PICTURE_STRING = INDEX_PICTURE_STRING;
    //private static final String VARYING_ARR_PICTURE_STRING="X(132)";
    private static final short INDEX_LEVEL = 77;
    private ArrayList<SymbolProperties> dependingOnToPostProcess = null;

    @Override
    public void visit(DataOccursClause n) throws Exception {
        SymbolProperties tempprops = null;
        if (n.nodeOptional.present()) {
            NodeChoice nodechoice = ((NodeChoice) ((NodeSequence) n.nodeOptional.node).elementAt(0));
            nodechoice.accept(this);
            switch (nodechoice.which) {
                case 0:
                    break;
                case 1:
                    tempprops = SymbolTable.getInstance().lookup(lastTokenString);
                    if (tempprops == null) {
                        throw new ErrorInCobolSourceException(n, "Unknown Symbol :" + lastTokenString);
                    }
                    if (tempprops.getValues() == null || tempprops.getValues().size() <= 0) {
                        throw new ErrorInCobolSourceException(n, "May not be used in OCCURS. No VALUE in Symbol :" + lastTokenString);
                    }
                    integerConstant = Integer.parseInt(tempprops.getValues().get(0).value1.toString());
            }
            //accept(this);
            props.setMinOccurs((int) integerConstant);
            tempprops = null;
            n.nodeChoice.accept(this);
            switch (n.nodeChoice.which) {
                case 0:
                    break;
                case 1:
                    tempprops = SymbolTable.getInstance().lookup(lastTokenString);
                    if (tempprops == null) {
                        throw new ErrorInCobolSourceException(n, "Unknown Symbol :" + lastTokenString);
                    }
                    if (tempprops.getValues() == null || tempprops.getValues().size() <= 0) {
                        throw new ErrorInCobolSourceException(n, "May not be used in OCCURS. No VALUE in Symbol :" + lastTokenString);
                    }
                    integerConstant = Integer.parseInt(tempprops.getValues().get(0).value1.toString());
            }
            props.setMaxOccurs((int) integerConstant);
            tempprops = null;
            props.setOccurs(true);
            props.setVaryingOccurs(true);
        } else {
            n.nodeChoice.accept(this);
            switch (n.nodeChoice.which) {
                case 0:
                    break;
                case 1:
                    tempprops = SymbolTable.getInstance().lookup(lastTokenString);
                    if (tempprops == null) {
                        throw new ErrorInCobolSourceException(n, "Unknown Symbol :" + lastTokenString);
                    }
                    if (tempprops.getValues() == null || tempprops.getValues().size() <= 0) {
                        throw new ErrorInCobolSourceException(n, "May not be used in OCCURS. No VALUE in Symbol :" + lastTokenString);
                    }
                    integerConstant = Integer.parseInt(tempprops.getValues().get(0).value1.toString());
            }
            props.setMaxOccurs((int) integerConstant);
            props.setMinOccurs(props.getMaxOccursInt());
            props.setOccurs(true);
            props.setVaryingOccurs(false);
            tempprops = null;
        }
        if (n.nodeOptional2.present()) {
            props.setOtherData2(n.nodeOptional2.node);
            if (dependingOnToPostProcess == null) {
                dependingOnToPostProcess = new ArrayList<SymbolProperties>();
            }
            dependingOnToPostProcess.add(props);
        }
        if (n.nodeOptional3.present()) {
            NodeList nodelist = (NodeList) ((NodeSequence) n.nodeOptional3.node).elementAt(2);
            for (Enumeration<Node> e = nodelist.elements(); e.hasMoreElements();) {
                ((NodeSequence) e.nextElement()).elementAt(0).accept(this);
                tempprops = createIndexSymbol(lastTokenString, SymbolTable.getInstance().getCurrentProgram(), true);
                tempprops.setRef(true);
                if (props.getMajorIndex() == null) {
                    props.setMajorIndex(tempprops);
                }
                tempprops.setMajorIndex(props);//isOccurs() decides if it is a index or table.
            }
        }
        //super.visit(n);
    }

    private SymbolProperties createDataSymbol(String dataName, SymbolProperties parent, String picture,
            byte usage, int level) {
        SymbolProperties tempProps;
        if ((tempProps = SymbolTable.getInstance().lookup(dataName, parent.getDataName())) == null) {
            tempProps = new SymbolProperties();
            tempProps.setType(SymbolConstants.DATA);
            tempProps.setDataName(dataName);
            tempProps.setParent(parent);
            tempProps.setPictureString(picture);
            tempProps.setLevelNumber((short) level);
            tempProps.setDataUsage(usage);
            adjustSetJavaName(tempProps);
            addChild(tempProps, parent);
            if (RESConfig.getInstance().isAllSymbols()) {
                tempProps.setRef(true);
            }
            SymbolTable.getInstance().insert(dataName, tempProps);
        } else {
            return null;
        }
        return tempProps;
    }

    private SymbolProperties createIndexSymbol(String dataName, SymbolProperties parent, boolean create) {
        SymbolProperties tempProps;
        if ((tempProps = SymbolTable.getInstance().lookup(dataName, parent.getDataName())) == null) {
            if (create) {
                tempProps = new SymbolProperties();
            } else {
                tempProps = props;
            }
            tempProps.setType(SymbolConstants.DATA);
            tempProps.setDataName(dataName);
            tempProps.setParent(parent);
            tempProps.setPictureString(INDEX_PICTURE_STRING);
            tempProps.setLevelNumber(INDEX_LEVEL);
            tempProps.setDataUsage(Constants.BINARY);
            adjustSetJavaName(tempProps);
            addChild(tempProps, parent);
            SymbolTable.getInstance().insert(dataName, tempProps);
            if (RESConfig.getInstance().isAllSymbols()) {
                tempProps.setRef(true);
            }
        }
        return tempProps;
    }

    private void addChild(SymbolProperties child, SymbolProperties par) {
        if (par == null || child == null) {
            return;
        }
        if (par.getChildren() == null) {
            par.setChildren(new ArrayList<SymbolProperties>());
        } else {
            if (par.getChildren().size() > 0) {
                par.getChildren().get(par.getChildren().size() - 1).setSibling(child);
            }
        }
        par.getChildren().add(child);

    }
    private long integerConstant = 0;

    @Override
    public void visit(IntegerConstant n) throws Exception {
        super.visit(n);
        try {
            integerConstant = Long.valueOf(((NodeToken)n.nodeChoice.choice).tokenImage.replace(",", ""));
        } catch (NumberFormatException e) {
            throw new ErrorInCobolSourceException(n, "Invalid integer literal");
        }
//        integerConstant = FieldFormat.parseNumber(((NodeToken) n.nodeChoice.choice).tokenImage).longValue();
        //literal=((NodeToken)n.nodeChoice.choice).tokenImage;
    }

    @Override
    public void visit(DataPictureClause n) throws Exception {
        n.pictureString.accept(this);
        props.setVarying(n.nodeOptional1.present());
    }

    @Override
    public void visit(PictureString n) throws Exception {
        this.pictureString = "";
        doingPicture = true;
        super.visit(n);
        doingPicture = false;
    }

    @Override
    public void visit(PictureCurrency n) throws Exception {
        this.pictureString += n.nodeToken.tokenImage;
    }

    private String redefinesName = null;

    @Override
    public void visit(DataRedefinesClause n) throws Exception {
        redefinesName = n.dataName.cobolWord.nodeToken.tokenImage;
    }

    @Override
    public void visit(RenamesClause n) throws Exception {
        SymbolProperties tempProps;
        tempProps = props;
        
        n.qualifiedDataName.accept(this);
        if (props.getLevelNumber() < 2 || props.getLevelNumber() > 50) {
            throw new ErrorInCobolSourceException(n, "RENAMES cannot reference level 01 or > 50");
        }
        tempProps.setRedefinedBy(new ArrayList<SymbolProperties>());
        tempProps.getRedefinedBy().add(props);
        
        if (n.nodeOptional.present()) {
        	// renames a range
            tempProps.setPictureString(null);
            SymbolProperties from = props;
            
            n.nodeOptional.accept(this);
            
            if (props.getLevelNumber() < 2 || props.getLevelNumber() > 50) {
                throw new ErrorInCobolSourceException(n, "RENAMES cannot reference level 01 or > 50");
            }
            
            SymbolProperties to = props;

            if (from == to) {
                throw new ErrorInCobolSourceException(n, "Invalid THROUGH clause, same data name: " + from.getDataName());
            }
            
            try {
                setForceByte(from, to);
            } catch (Exception e) {
                throw new ErrorInCobolSourceException(n, e.getMessage());
            }
            
            tempProps.getRedefinedBy().add(props);
            
        } else {
        	// if renames another group/element, the renaming field has same data description as redefined field
//            tempProps.setPictureString(props.getPictureString());
            pictureString = props.getPictureString();
            props.setForceCobolBytes(true);
        }
        tempProps.setForceCobolBytes(true);
        props = tempProps;
    }

    private void setForceByte(SymbolProperties from, SymbolProperties to) throws Exception {
    	SymbolProperties parFrom, parTo;
    	parFrom = from;
    	while (parFrom.getLevelNumber() != 1) {
    		parFrom = parFrom.getParent();
    	}
    	parTo = to;
    	while (parTo.getLevelNumber() != 1) {
    		parTo = parTo.getParent();
    	}
    	
    	if (parFrom != parTo) {
    	    throw new ErrorInCobolSourceException("Invalid RENAMES clause. Must reference items within level-01 entry: " + to.getDataName());
    	}
    	
    	if (parFrom.isForceCobolBytes())
    		return;
    	
    	List<SymbolProperties> listSym = new ArrayList<SymbolProperties>();
    	Stack<SymbolProperties> tmp = new Stack<SymbolProperties>();
    	tmp.push(parFrom);
    	while (!tmp.isEmpty()) {
    		SymbolProperties p = tmp.pop();
    		listSym.add(p);
    		if (p.getChildren() != null) {
    			ArrayList<SymbolProperties> children = p.getChildren();
    			for (int i = children.size() - 1 ; i >= 0; i --) {
    				tmp.push(children.get(i));
    			}
    		}
    	}
    	
    	boolean force = false;
    	for (int i = 0; i < listSym.size(); i ++) {
    		SymbolProperties s = listSym.get(i);
    		if (s == from) {
    			/*if (i > 0 && listSym.get(i - 1).isGroupData()) {
					listSym.get(i - 1).setForceCobolBytes(true);
				}*/
    			force = true;
    		}
    		if (force) {
    			s.setForceCobolBytes(true);
    		}
    		if (s == to) {
    			/*if (i < listSym.size() - 1 && listSym.get(i + 1).isGroupData()
						&& !s.getParent().is01Group()) {
					s.getParent().setForceCobolBytes(true);
				}*/
    			if (s.isGroupData()) {
    				setForceByte(s);
    			}
    			break;
    		}
    	}
    }

	@Override
    public void visit(DataSignClause n) throws Exception {
        if (n.nodeChoice.which == 0) {
            props.setSignLeading(true);
        }
        if (n.nodeOptional1.present()) {
            props.setSignSeparate(true);
        }
        //super.visit(n);
    }

    @Override
    public void visit(DataUsageClause n) throws Exception {

        switch (n.nodeChoice.which) {
            case 3://Comp-2
            case 9://Compuational-2
                pictureString = "-.9(17)E-99";
                usage = Constants.BINARY;
                break;
            case 2://Comp-1
            case 8://Compuational-1
                pictureString = "-.9(8)E-99";
                usage = Constants.BINARY;
                break;
            case 1://Comp
            case 7://Computational
            case 4://Comp-3
            case 10://Compuational-3
            case 16://Packed-Decimal
                usage = Constants.PACKED_DECIMAL;
                break;
            case 0://Binary
            case 5://Comp-4
            case 11://Compuational-4
            case 6://Comp-5
            case 12://Compuational-5
                usage = Constants.BINARY;
                break;
            case 13://Display
                usage = Constants.DISPLAY;
                break;
            case 14://Display-1
                usage = Constants.DISPLAY_1;
                break;
            case 15://Index
                usage = Constants.INDEX;
                pictureString = INDEX_PICTURE_STRING;
                break;
            case 17://Pointer
            case 18://Function-Pointer
            case 19://Procedure-Pointer
            default://Object Reference DataName()
                usage = Constants.OBJECT;
        }
    }

    @Override
    public void visit(DataValueClause n) throws Exception {
        //super.visit(n.nodeList);
        ArrayList<SymbolProperties.CoupleValue> a = new ArrayList<SymbolProperties.CoupleValue>();
        doit:
        for (int i = 0; i < n.nodeList.size(); ++i) {
            NodeSequence seq = (NodeSequence) n.nodeList.elementAt(i);
            if (((NodeChoice) seq.elementAt(0)).which == 0) {
                SymbolProperties saveProps = props;
                ((NodeChoice) seq.elementAt(0)).choice.accept(this);
                if (props != null) {
                    LiteralString value1 = props.getValues().get(0).value1;
                    LiteralString value2 = null;
                    a.add(saveProps.new CoupleValue(value1, value2));
                }
                props = saveProps;
                break doit;
            } else {
                seq.elementAt(0).accept(this);
                LiteralString value1 = literalString;
                LiteralString value2 = null;
                if (((NodeOptional) seq.elementAt(2)).present()) {
                    NodeSequence seq2 = (NodeSequence) ((NodeOptional) seq.elementAt(2)).node;
                    seq2.elementAt(1).accept(this);
                    value2 = literalString;
                }
                a.add(props.new CoupleValue(value1, value2));
            }
        }

        props.setValues(a);
    }

    @Override
    public void visit(LevelNumber n) throws Exception {
        short lvl = Short.parseShort(n.nodeToken.tokenImage);
        props.setLevelNumber(lvl);
        super.visit(n);
    }

    @Override
    public void visit(Identifier n) throws Exception {
        n.accept(cobol2Java, null);
        super.visit(n);
    }
    
    @Override
    public void visit(QualifiedDataName n) throws Exception {
        qualified = new Stack<String>();
        doQualified = true;
        super.visit(n);
        doQualified = false;
        processQualifiedStack(n);
    }

    private void processQualifiedStack(Node n) throws Exception {

        SymbolProperties props2 = null;
        props = null;
        if (qualified == null || qualified.size() <= 0) {
            return;
        }
        String curr = null;
        String prev = null;
        curr = (String) qualified.pop();
        do {
            if (qualified.size() > 0) {
                prev = curr;
                curr = (String) qualified.pop();
                if (props2 != null) {
                    props2 = findChild(props2, curr);
                } else {
                    props2 = SymbolTable.getInstance().lookup(curr, prev);
                }
                if (props2 == null) {
                    throw new ErrorInCobolSourceException((RESNode)n, "Unknown Symbol : " + curr + " IN " + prev);
                }
            } else {
                props2 = SymbolTable.getInstance().lookup(curr);
                if (props2 == null) {
                    throw new ErrorInCobolSourceException((RESNode) n, "Unknown Symbol : " + curr);
                }
            }
            lastTokenString = curr;
            if (props2.isVaryingArray() && props2.getParent().isVarying()) {
                props2 = props2.getParent();
            }

        } while (qualified != null && qualified.size() > 0);
        props = props2;//.clone();
        if (props2 != null) {
            props.setIndexesWorkSpace(null);//subscripts will fill this
            props.setSubstringWorkSpace(null);//LeftMost... and Length will fill this
            setRef(props);
        }
    }

    private SymbolProperties findChild(SymbolProperties props, String name) {
        if (props.getChildren() == null || props.getChildren().size() <= 0) {
            return null;
        }
        for (SymbolProperties ch : props.getChildren()) {
            if (ch.getDataName().equalsIgnoreCase(name)) {
                return ch;
            }
        }
        SymbolProperties ret = null;
        for (SymbolProperties ch : props.getChildren()) {
            ret = findChild(ch, name);
            if (ret != null) {
                return ret;
            }
        }
        return ret;
    }

    @Override
    public void visit(LeftmostCharacterPosition n) throws Exception {
        SymbolProperties propsDataName = props;
        super.visit(n);
        props = propsDataName;
    }

    @Override
    public void visit(Length n) throws Exception {
        SymbolProperties propsDataName = props;
        super.visit(n);
        props = propsDataName;
    }

    private ArrayList<SymbolProperties> refList = new ArrayList<SymbolProperties>();
    private ArrayList<SymbolProperties> modList = new ArrayList<SymbolProperties>();

    private int expressionType = 0;

    @Override
    public void visit(ArithmeticExpression n) throws Exception {
        expressionType = Constants.SHORT;
        super.visit(n);
    }

    private void formatLiteral(LiteralString lit) {
        expressionType = Constants.BYTE;
        return;
    }

    @Override
    public void visit(Basis n) throws Exception {
        super.visit(n);
        if (isStatementInError) {
            return;
        }
        if (props != null) {
            setRef(props);
        }
        //Here duplicating the code structure from Cobol2Java visit(Basis)
        switch (n.nodeChoice.which) {
            case 0:
                if (props == null) {
                    return;
                }
                String pic = (String) props.getPictureString();
                int type;
                if (pic == null) {
                    type = Constants.STRING;
                } else {
                    type = FieldFormat.verifyCobolPicture(pic);
                }
                switch (type) {
                    case Constants.SHORT:
                    case Constants.INTEGER:
                    case Constants.LONG:
                        if (expressionType == Constants.BIGDECIMAL) {
                            //setImportBigDecimalInProgram(SymbolTable.programs.peek());
                            break;
                        } else if (expressionType < type) {
                            expressionType = type;
                        }
                        break;
                    case Constants.BIGDECIMAL:
                        if (expressionType == Constants.BIGDECIMAL) {
                        } else if (expressionType < type) {
                            expressionType = type;
                        }
                        //setImportBigDecimalInProgram(SymbolTable.programs.peek());
                        break;
                    case Constants.STRING:
                    case Constants.GROUP:
                    case Constants.UNKNOWN:
                        expressionType = Constants.UNKNOWN;
                }
                break;//out of case 0
            case 1:
                int prevExpressionType = expressionType;
                formatLiteral(literalString);
                if (prevExpressionType == Constants.BIGDECIMAL
                        || expressionType == Constants.BIGDECIMAL) {
                    //setImportBigDecimalInProgram(SymbolTable.programs.peek());
                } else if (prevExpressionType > Constants.BIGDECIMAL
                        || expressionType > Constants.BIGDECIMAL) {
                    expressionType = Constants.UNKNOWN;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void visit(ComputeStatement n) throws Exception {
        expressionType = Constants.INTEGER;
        int lhsType = 0;
        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {
            Node node = e.nextElement();
            node.accept(this);
            if (props == null) {
                return;
            }
            setMod(props);
            lhsType = Math.max(lhsType, FieldFormat.verifyCobolPicture(props.getPictureString()));
        }
        super.visit(n);
        /*
        n.arithmeticExpression.accept(this);
        if(expressionType<Constants.BIGDECIMAL&&lhsType==Constants.BIGDECIMAL) {
        //setImportBigDecimalInProgram(props);
        }
         */
    }

    private SymbolProperties findProgram(final SymbolProperties data) {
        SymbolProperties pgm = data;
        while (pgm != null && pgm.getType() != SymbolConstants.PROGRAM) {
            pgm = (SymbolProperties) pgm.getParent();
        }       
        return pgm;
    }

    @Override
    public void visit(AcceptStatement n) throws Exception {
        n.identifier.accept(this);
        setMod(props);
        props = findProgram(props);
        if (props != null) {
            props.setImportLib(Boolean.TRUE);
        }
        //super.visit(n);
    }

    private void propagateRef(SymbolProperties node) {
        if (node == null || node.isProgram()) {
            return;
        }
        ref(node);
        propagateRefParents(node);
    }
    /*
    private void propagateRef(ArrayList<SymbolProperties> n) throws Exception {
    if (n==null) return;int i=0;
    for(SymbolProperties child:n) throws Exception {
    propagateRef(child);
    }
    }
     */

    private void propagateRef() {
        if (refList == null) {
            return;
        }
        for (SymbolProperties child : refList) {
            propagateRef(child);
        }
        refList.clear();
    }

    private void propagateMod() {
        if (modList == null) {
            return;
        }
        for (SymbolProperties child : modList) {
            propagateRef(child);
        }
        modList.clear();
    }

    private void propagateRefParents(SymbolProperties node) {
        if (node == null) {
            return;
        }
        boolean isToBe = node.getLevelNumber() == 1 || node.getLevelNumber() == 77 || node.getIsFiller() || node.isOccurs();
        while (node.getLevelNumber() != 1 && !node.isFile() && (node = (SymbolProperties) node.getParent()) != null) {
            if (isToBe || node.getLevelNumber() == 1 || node.getLevelNumber() == 77 || node.getIsFiller() || node.isOccurs()) {
                ref(node);
            }
        }
        if (node != null) {
            ref(node);
        }
        propagateRefChildren(node);
    }

    private void propagateRefChildren(SymbolProperties node) {
        if (node == null) {
            return;
        }
        if (node.getParent().isFile()) {
            ref(node.getParent());
        }
        if (node.getChildren() == null) {
            return;
        }
//        boolean isToBe = node.getLevelNumber() == 1
//                || node.getPictureString() != null
//                || node.getLevelNumber() == 88
//                || node.getLevelNumber() == 66
//                || node.getLevelNumber() == 77;
        for (SymbolProperties child : node.getChildren()) {
//            if (isToBe || child.getLevelNumber() == 1
//                    || child.getPictureString() != null
//                    || child.getLevelNumber() == 88
//                    || child.getLevelNumber() == 66
//                    || child.getLevelNumber() == 77) {
                ref(child);
//            }
            propagateRefChildren(child);
        }
    }

    private void propagateMod(SymbolProperties node) {
        if (node == null || node.isProgram()) {
            return;
        }
        mod(node);
        propagateModParents(node);
    }

    /*
    private void propagateMod(ArrayList<SymbolProperties> n) throws Exception {
    if (n==null) return;
    for(SymbolProperties child:n) throws Exception {
    propagateMod(child);
    }
    }
     */
    private void propagateModParents(SymbolProperties node) {
        if (node == null) {
            return;
        }
        boolean isToBe = node.getLevelNumber() == 1 || node.getLevelNumber() == 77 || node.getIsFiller() || node.isOccurs();
        while (node.getLevelNumber() != 1 && !node.isFile() && (node = (SymbolProperties) node.getParent()) != null) {
            if (isToBe || node.getLevelNumber() == 1 || node.getIsFiller() || node.isOccurs()) {
                mod(node);
            }
        }
        if (node != null) {
            mod(node);
        }
        propagateModChildren(node);
    }

    private void propagateModChildren(SymbolProperties node) {
        if (node == null) {
            return;
        }
        if (node.getParent().isFile()) {
            mod(node.getParent());
        }
        if (node.getChildren() == null) {
            return;
        }
        boolean isToBe = node.getLevelNumber() == 1
                || node.getPictureString() != null
                || node.getLevelNumber() == 88
                || node.getLevelNumber() == 66
                || node.getLevelNumber() == 77;
        for (SymbolProperties child : node.getChildren()) {
            if (isToBe || child.getLevelNumber() == 1
                    || child.getPictureString() != null
                    || child.getLevelNumber() == 88
                    || child.getLevelNumber() == 66) {
                mod(child);
            }
            propagateModChildren(child);
        }
    }

    private void ref(SymbolProperties refSym) {
        if (refSym.getRef()) {
            return;
        }
        refSym.setRef(true);
        if (refSym.getRedefines() != null && !refSym.getRedefines().getRef()) {
            refSym.getRedefines().setRef(true);
            propagateRef(refSym.getRedefines());
        }
        if (refSym.getRedefinedBy() != null) {
            for (SymbolProperties t : refSym.getRedefinedBy()) {
                t.setRef(true);
                propagateRef(t);
            }
        }
        if (refSym.getIsFiller()) {
            propagateRef(refSym);
        }
    }

    private void mod(SymbolProperties modSym) {
        if (modSym.getMod()) {
            return;
        }
        modSym.setMod(true);
        if (modSym.getRedefines() != null && !modSym.getRedefines().getMod()) {
            modSym.getRedefines().setMod(true);
            propagateMod(modSym.getRedefines());
        }
        if (modSym.getRedefinedBy() != null) {
            for (SymbolProperties t : modSym.getRedefinedBy()) {
                t.setMod(true);
                propagateMod(t);
            }
        }
        if (modSym.getIsFiller()) {
            propagateMod(modSym);
        }
    }

    private void setRef(SymbolProperties refSym) {
        if (refSym == null) {
            return;
        }
        if (!refSym.getRef()) {
            ref(refSym);
            refList.add(refSym);
        }
    }

    private void setMod(SymbolProperties modSym) {
        if (modSym == null) {
            return;
        }
        if (!modSym.getMod()) {
            mod(modSym);
            modList.add(modSym);
        }
    }

    @Override
    public void visit(PerformVarying n) throws Exception {
        n.identifier.accept(this);
        if (props == null) {
            return;
        }
        setMod(props);
        super.visit(n);
    }

    @Override
    public void visit(ConvertingPhrase n) throws Exception {
        props = null;
        n.nodeChoice.accept(this);
        if (props != null) {
            setRef(props);
        }
        props = null;
        n.nodeChoice1.accept(this);
        if (props != null) {
            setRef(props);
        }
        n.nodeListOptional.accept(this);
    }

    @Override
    public void visit(PerformOption n) throws Exception {
        props = null;
        if (n.nodeChoice.which == 0) {
            ((NodeSequence) n.nodeChoice.choice).elementAt(0).accept(this);
            if (props != null) {
                setRef(props);
            }
        } else {
            n.nodeChoice.choice.accept(this);
        }
    }

    @Override
    public void visit(InspectStatement n) throws Exception {
        n.identifier.accept(this);
        if (isStatementInError || props == null) {
            return;
        }
        if (isGroupOrAlphanumeric(props)) {
            setRef(props);
            n.nodeChoice.accept(this);
        } else {
            throw new ErrorInCobolSourceException(n, props.getDataName() + " :Inspect Statement Data name must be Group or Alphannumeric.");
        }
    }

    @Override
    public void visit(ReplacingPhrase n) throws Exception {
        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {
            NodeChoice nodechoice0 = (NodeChoice) e.nextElement();
            NodeSequence seq = (NodeSequence) nodechoice0.choice;
            if (nodechoice0.which == 0) {
                doInspectPhrase1(seq.elementAt(2));
                seq.elementAt(3).accept(this);
            } else {
                NodeList nodelist = (NodeList) seq.elementAt(1);
                for (Enumeration<Node> e2 = nodelist.elements(); e2.hasMoreElements();) {
                    NodeSequence seq2 = (NodeSequence) e2.nextElement();
                    doInspectPhrase1(seq2.elementAt(0));
                    doInspectPhrase1(seq2.elementAt(2));
                    seq2.elementAt(3).accept(this);
                }
            }
        }
    }

    private void doInspectPhrase1(Node n) throws Exception {
        props = null;
        literalString = null;
        n.accept(this);
        if (props != null) {
            if (!isGroupOrAlphanumeric(props)) {
                throw new ErrorInCobolSourceException((RESNode)n, props.getDataName() + " :Inspect pharase must specify a group or alphanumeric");
            } else if (isStatementInError) {
                return;
            }
            setRef(props);
        }
    }

    @Override
    public void visit(TallyingPhrase n) throws Exception {
        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {
            NodeSequence seq = (NodeSequence) e.nextElement();
            seq.elementAt(0).accept(this);
            if (!isIntegerElement(props)) {
                throw new ErrorInCobolSourceException(n, props.getDataName() + " :Inspect Statement Tallying Counter must be an Integer element");
            }
            setMod(props);
            NodeList nodelist = (NodeList) seq.elementAt(2);
            for (Enumeration<Node> e2 = nodelist.elements(); e2.hasMoreElements();) {
                NodeChoice nodechoice = (NodeChoice) e2.nextElement();
                if (nodechoice.which == 0) {
                    nodechoice.choice.accept(this);
                } else {
                    NodeSequence seq2 = (NodeSequence) nodechoice.choice;
                    NodeList list = (NodeList) seq2.elementAt(1);
                    for (Enumeration<Node> e3 = list.elements(); e3.hasMoreElements();) {
                        NodeSequence seq3 = (NodeSequence) e3.nextElement();
                        doInspectPhrase1(seq3.elementAt(0));
                        seq3.elementAt(1).accept(this);
                    }
                }
            }
        }
        n.nodeOptional.accept(this);
    }

    @Override
    public void visit(BeforeAfterPhrase n) throws Exception {
        doInspectPhrase1(n.nodeChoice1);
    }

    @Override
    public void visit(SetStatement n) throws Exception {
        props = null;
        for (Enumeration<Node> e0 = n.nodeList.elements(); e0.hasMoreElements();) {
            NodeSequence nodeSequence  = (NodeSequence) e0.nextElement();
            nodeSequence.elementAt(1).accept(this);
            setRef(props);
            for (Enumeration<Node> e = ((NodeList)nodeSequence.elementAt(0))
                    .elements(); e.hasMoreElements();) {
                e.nextElement().accept(this);
                if (props == null) {
                    continue;
                }
                setMod(props);props=null;
            }
        }
     }

    @Override
    public void visit(ConditionNameReference n) throws Exception {
        qualified.clear();
        doQualified = true;
        n.conditionName.accept(this);
        switch (n.nodeChoice.which) {
            case 0:
                NodeSequence nodeseq = (NodeSequence) n.nodeChoice.choice;
                nodeseq.elementAt(0).accept(this);
                nodeseq.elementAt(1).accept(this);
                doQualified = false;
                processQualifiedStack(n);
                setRef(props);
                nodeseq.elementAt(2).accept(this);
                break;
            case 1:
                n.nodeChoice.choice.accept(this);
                doQualified = false;
                processQualifiedStack(n);
                setRef(props);
        }
    }

    @Override
    public void visit(ClassCondition n) throws Exception {
        super.visit(n.identifier);
        if (isStatementInError || props == null) {
            return;
        }
        setRef(props);
        switch (n.nodeChoice.which) {
            case 0:
                props.setNumericTested(true);
                break;
            case 1:
                props.setAlphabeticTested(true);
                break;
            case 2:
                props.setAlphabeticLowerTested(true);
                break;
            case 3:
                props.setAlphabeticUpperTested(true);
                break;
            case 4:
            //TODO implement ClassName in Env Section.
            case 5:
            //TODO implement DBCS test.
            case 6:
            //TODO implement KANJI test.
            default:
            //Why would I come here?
        }
    }

    private boolean isGroupOrAlphanumeric(SymbolProperties pr) {
        if ((props.getPictureString() == null && props.getChildren() != null && props.getChildren().size() > 0) ||//Group
                FieldFormat.verifyCobolPicture(props.getPictureString()) == Constants.STRING) {//Alphanumeric
            return true;
        }
        return false;
    }

    private boolean isIntegerElement(SymbolProperties pr) {
        if (props.getPictureString() != null
                && FieldFormat.verifyCobolPicture(props.getPictureString()) <= Constants.LONG) {
            return true;
        }
        return false;
    }

    @Override
    public void visit(ExecSqlStatement n) throws Exception {
        context.setSqlTranslated(true);
        super.visit(n);
    }

    @Override
    public void visit(StringStatement n) throws Exception {
        n.identifier.accept(this);
        if (props == null) {
            return;
        }
        setMod(props);
        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {
            NodeSequence nodeseq = (NodeSequence) e.nextElement();
            if (((NodeOptional) nodeseq.elementAt(1)).present()) {
                NodeSequence opt = (NodeSequence) ((NodeOptional) nodeseq.elementAt(1)).node;
                NodeChoice nodech = (NodeChoice) opt.elementAt(2);
                switch (nodech.which) {
                    case 0:
                        nodech.accept(this);
                        setRef(props);
                        break;
                    case 1:
                }
            }
            for (Enumeration<Node> e2 = ((NodeList) nodeseq.elementAt(0)).elements(); e2.hasMoreElements();) {
                props = null;
                literalString = null;
                NodeChoice nodechoice = (NodeChoice) e2.nextElement();
                switch (nodechoice.which) {
                    case 0:
                        nodechoice.accept(this);
                        setRef(props);//props.setIsFormat(true);
                        break;
                    case 1:
                }

            }
        }
    }

    @Override
    public void visit(UnstringStatement n) throws Exception {
        n.identifier.accept(this);
        if (props == null) {
            return;
        }
        setRef(props);
        if (n.nodeOptional.present()) {
            props = null;
            literalString = null;
            NodeSequence nodeseq = (NodeSequence) n.nodeOptional.node;

            if (((NodeChoice) nodeseq.elementAt(3)).which == 0) {
                nodeseq.elementAt(3).accept(this);
            }
            if (props != null) {
                setRef(props);
            }
            NodeListOptional orList = (NodeListOptional) nodeseq.elementAt(4);
            if (orList.present()) {
                for (Enumeration<Node> e = orList.elements(); e.hasMoreElements();) {
                    NodeSequence nodeseq2 = (NodeSequence) e.nextElement();
                    if (((NodeChoice) nodeseq2.elementAt(2)).which == 0) {
                        nodeseq2.elementAt(2).accept(this);
                    }
                    if (props != null) {
                        setRef(props);
                    }
                }
            }
        }
        /////
        for (Enumeration<Node> e2 = n.nodeList.elements(); e2.hasMoreElements();) {
            NodeSequence nodeseq3 = (NodeSequence) e2.nextElement();
            nodeseq3.elementAt(0).accept(this);
            setMod(props);
            NodeOptional opt = (NodeOptional) nodeseq3.elementAt(1);
            if (opt.present()) {
                setMod(props);
            }
            opt = (NodeOptional) nodeseq3.elementAt(2);
            if (opt.present()) {
                setMod(props);
            }
        }

        if (n.nodeOptional2.present()) {
            n.nodeOptional2.accept(this);
            setMod(props);
        }

        if (n.nodeOptional3.present()) {//On OverFlow
            n.nodeOptional3.accept(this);
        }
        if (n.nodeOptional4.present()) {//Not On OverFlow
            n.nodeOptional4.accept(this);
        }


    }

    @Override
    public void visit(OpenStatement n) throws Exception {
        boolean inp = false, op = false, iop = false, ext = false;
        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {
            NodeChoice nodech = (NodeChoice) e.nextElement();
            NodeSequence nodeseq = (NodeSequence) nodech.choice;
            switch (((NodeToken) nodeseq.elementAt(0)).kind) {
                case CobolParserConstants.INPUT:
                    inp = true;
                    break;
                case CobolParserConstants.OUTPUT:
                    op = true;
                    break;
                case CobolParserConstants.INPUT_OUTPUT:
                case CobolParserConstants.I_O:
                    iop = true;
                    break;
                case CobolParserConstants.EXTEND:
                    ext = true;
                    break;
                default:
            }

            for (Enumeration<Node> e2 = ((NodeList) nodeseq.elementAt(1)).elements();
                    e2.hasMoreElements();) {
                isStatementInError = false;
                e2.nextElement().accept(this);
                if (isStatementInError || props == null) {
                    continue;
                }
                props.setFileOpenedInput(props.isFileOpenedInput() || inp);
                props.setFileOpenedOutput(props.isFileOpenedOutput() || op);
                props.setFileOpenedIO(props.isFileOpenedIO() || iop);
                props.setFileOpenedExtend(props.isFileOpenedExtend() || ext);
                if (inp || iop) {
                    setRef(props);
                }
                if (op || iop || ext) {
                    setMod(props);
                }

            }
            inp = op = iop = ext = false;
        }
    }

    @Override
    public void visit(ReadStatement n) throws Exception {
        n.fileName.accept(this);
        if (props == null) {
            return;
        }
        setRef(props);
        for (SymbolProperties ch : props.getChildren()) {
            setMod(ch);
        }
        if (n.nodeOptional2.present()) {
            n.nodeOptional2.accept(this);
            setMod(props);
        }
        n.nodeOptional4.accept(this);
        n.nodeOptional5.accept(this);
        n.nodeOptional6.accept(this);
        n.nodeOptional7.accept(this);
    }

    @Override
    public void visit(WriteStatement n) throws Exception {
        n.recordName.accept(this);
        if (props == null) {
            return;
        }
        setMod(props);
        if (n.nodeOptional.present()) {
            props = null;
            n.nodeOptional.accept(this);
            if (props != null) {
                setRef(props);
            }
        }
        if (n.nodeOptional1.present()) {
            props = null;
            n.nodeOptional1.accept(this);
            if (props != null) {
                setRef(props);
            }
        }
        n.nodeOptional2.accept(this);
        n.nodeOptional3.accept(this);
        n.nodeOptional4.accept(this);
        n.nodeOptional5.accept(this);
    }

    @Override
    public void visit(RewriteStatement n) throws Exception {
        n.recordName.accept(this);
        if (props == null) {
            return;
        }
        setMod(props);
        if (n.nodeOptional.present()) {
            props = null;
            n.nodeOptional.accept(this);
            if (props != null) {
                setRef(props);
            }
        }
        if (n.nodeOptional1.present()) {
            props = null;
            n.nodeOptional1.accept(this);
            if (props != null) {
                setRef(props);
            }
        }
        n.nodeOptional1.accept(this);
        n.nodeOptional2.accept(this);
    }
    public NodeToken firstTokenInStatement = null;

    @Override
    public void visit(Statement n) throws Exception {
        if (context.getTraceLevel() >= 2) {
            System.out.println("Doing CobolFillTable Statement " + n.line);
        }

        firstTokenInStatement = null;
        isStatementInError = false;
        //if(refList==null)
        //refList=new Stack<ArrayList>();
        //if(modList==null)
        //modList=new Stack<ArrayList>();
        //refList.push(new ArrayList());
        //modList.push(new ArrayList());

        super.visit(n);
        if (isStatementInError) {
            n.deadStatement = true;
            return;
        }
        if (refList != null && refList.size() > 0) {
            if (n.ref == null) {
                n.ref = new ArrayList<Object>();
            }
            //a=refList.pop();
            //n.ref.addAll(a);
            propagateRef();
        }
        if (modList != null && modList.size() > 0) {
            if (n.mod == null) {
                n.mod = new ArrayList<Object>();
            }
            //a=modList.pop();
            //n.mod.addAll(a);
            propagateMod();
        }
    }

    @Override
    public void visit(AssignClause n) throws Exception {
        n.nodeChoice.accept(this);
        if (props != null && props.getType() == SymbolConstants.FILE) {
            if (lastTokenString.charAt(0) != '\"' && lastTokenString.charAt(0) != '\'') {
                lastTokenString = '\"' + lastTokenString + '\"';
            }
            props.setOtherName(lastTokenString.toUpperCase());
        }
    }

    @Override
    public void visit(SelectClause n) throws Exception {
        n.fileName.accept(this);

    }

    @Override
    public void visit(FileName n) throws Exception {
        super.visit(n);
        props = SymbolTable.getInstance().lookup(lastTokenString, SymbolConstants.FILE);
        if (props != null) {
            props.setRef(true);
            propagateRef(props);
        }
    }

    @Override
    public void visit(FileStatusClause n) throws Exception {
        SymbolProperties saveProps = props;
        props = null;
        n.qualifiedDataName.accept(this);
        if (saveProps != null && props != null) {
            if (saveProps.getFileStatus() == null) {
                saveProps.setFileStatus(new ArrayList<SymbolProperties>());
            }
            saveProps.getFileStatus().add(props);
            props.setMod(true);
            propagateRef(props);
        }
        props = saveProps;
    }

    @Override
    public void visit(OrganizationClause n) throws Exception {
        if (props == null) {
            throw new ErrorInCobolSourceException(n, "Unknown Symbol: " + lastTokenString);
        } else {
            props.setDataUsage((byte) n.nodeChoice.which);
        }
    }

    /*private SymbolProperties findValidParent(SymbolProperties ch) {
        if (ch.is01Group()) {
            return SymbolTable.getInstance().getFirstProgram();
        }
        do {
            ch = ch.getParent();
        } while (ch != null && ch.getIsFiller() && !ch.isProgram());
        return ch;
    }*/

    private void adjustSetJavaName(SymbolProperties p) {
        if (p.isFromRESLibrary()) {
            p.setJavaName1(NameUtil.convertCobolNameToJava(p.getDataName(), false) + "__");
            p.setJavaName2(NameUtil.convertCobolNameToJava(p.getDataName(), true) + "__");
            return;
        }
        if (p.isIndexRedefines()) {
            p.setJavaName1(p.getRedefines().getJavaName1());
            p.setJavaName2(p.getRedefines().getJavaName2());
            p.setRef(false);
            p.setMod(false);
            return;
        }
//        boolean childExists = checkChildExists(p, findValidParent(p));
        String saveDataName = p.getDataName().replace('.', '-');
//        boolean isData = !(p.getType() == SymbolConstants.PROGRAM);
        String javaName = NameUtil.convertCobolNameToJava(p.getDataName(), false);
        char ch = javaName.charAt(0);
        /*if (childExists) {
            javaName += "_" + String.valueOf(++dataRenameMark);
        }*/
        if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')); else {
            javaName = "_" + javaName;
        }
//        if (isInGroupDictionary(javaName, p.getParent())) {
//            javaName += new Integer(SymbolTable.duplicateJavaNameMark++).toString().trim();
//        }
//        addToGroupDictionary(javaName, p.getParent());
        p.setJavaName1(javaName);
        javaName = NameUtil.convertCobolNameToJava(p.getDataName(), true);
        ch = javaName.charAt(0);
        /*if (childExists) {
            javaName += "_" + String.valueOf(dataRenameMark);
        }*/
        if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')); else {
            javaName = "_" + javaName;
        }

//        if (isInGroupDictionary(javaName, p.getParent())) {
//            javaName += new Integer(SymbolTable.duplicateJavaNameMark++).toString().trim();
//        }
//        addToGroupDictionary(javaName, p.getParent());
        p.setJavaName2(javaName);
        p.setDataName(saveDataName);

        /*if (p.getType() == SymbolConstants.PROGRAM || (p.getLevelNumber() == 1 && p.getPictureString() == null)) {
            p.setDoWriteClassFile(true);
            if (!RESConfig.getInstance().isOverwriteJavaFiles()) {
                if (Files.exists(p, isData)) {
                    p.setDoWriteClassFile(false);
                } else {
                    p.setDoWriteClassFile(true);
                }
                if (!p.isDoWriteClassFile()) {
                    System.out.println("Name " + p.getDataName() + " exists. Not overwritten.");
                }
            }
        }*/
    }
    //Singleton COnstructors
    private static CobolFillTable thiz = null;
    private static RESContext context = null;

    private CobolFillTable() {
    }

    public static CobolFillTable getInstance(RESContext ctx) {
        if (thiz == null) {            
            context = ctx;
            thiz = new CobolFillTable();
        }
        return thiz;
    }

    public static void clear() {
        thiz=null;
        context=null;
    }

    @Override
    public void visit(CallStatement n) throws Exception {
        props = null;
        n.nodeChoice.choice.accept(this);
        if (props != null) {
            setRef(props);
        }
        n.nodeOptional.accept(this);
    }

    @Override
    public void visit(CallByContentArgs n) throws Exception {
        props = null;
        n.nodeChoice.choice.accept(this);
        if (props != null) {
            setRef(props);
        }
    }

    @Override
    public void visit(CallByReferenceArgs n) throws Exception {
        props = null;
        n.nodeChoice.choice.accept(this);
        if (props != null) {
            setRef(props);
            setMod(props);
        }
    }

    @Override
    public void visit(InitializeStatement n) throws Exception {
        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {
            e.nextElement().accept(this);
            if (props != null) {
                setMod(props);
            }
        }
        if (n.nodeOptional.present()) {
            NodeSequence nodeseq = (NodeSequence) n.nodeOptional.node;
            for (Enumeration<Node> e = ((NodeList) nodeseq.elementAt(1)).elements(); e.hasMoreElements();) {
                props = null;
                e.nextElement().accept(this);
                if (props != null) {
                    setRef(props);
                }
            }
        }
    }
    
    @Override
    public void visit(Declaratives n) throws Exception {
        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {
            NodeSequence nodeseq = (NodeSequence) e.nextElement();
            popToProgram();
            if (!doUseStatementsInDeclaratives) {
                ((SectionHeader) nodeseq.elementAt(0)).accept(this);
                if ((sectionProps = createSection()) == null) {
                    throw new ErrorInCobolSourceException(n, "Duplicate Section in Declaratives: " + sectionName);
                }
                nodeseq.elementAt(4).accept(this);
            } else {
                nodeseq.elementAt(2).accept(this);
            }
        }
    }

    @Override
    public void visit(SearchStatement n) throws Exception {
        n.qualifiedDataName.accept(this);
        if (!props.isOccurs()) {
            throw new ErrorInCobolSourceException(n, "Search Statement is valid only on tables with Occurs clause.");
        }
        if (props.getMajorIndex() == null) {
            throw new ErrorInCobolSourceException(n, "Search Statement is valid only on tables with INDEXED BY.");
        }
        setRef(props);
        props = null;
        n.nodeOptional1.accept(this);
        setMod(props);
        n.nodeOptional2.accept(this);
        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {
            NodeSequence nodeseq = (NodeSequence) e.nextElement();
            nodeseq.elementAt(1).accept(this);
            nodeseq.elementAt(2).accept(this);
        }
    }

    @Override
    public void visit(CancelStatement n) throws Exception {
        for (Enumeration<Node> e = n.nodeList.elements(); e.hasMoreElements();) {
            props = null;
            literalString = null;
            e.nextElement().accept(this);
            if (props != null) {
                setRef(props);
            }
        }
    }

    @Override
    public void visit(CommunicationInputClause n) throws Exception {
        insertDummyData = true;
        super.visit(n);
        insertDummyData = false;
    }
    private boolean insertDummyData = false;

    @Override
    public void visit(CommunicationIOClause n) throws Exception {
        insertDummyData = true;
        super.visit(n);
        insertDummyData = false;
    }

    @Override
    public void visit(CommunicationOutputClause n) throws Exception {
        insertDummyData = true;
        super.visit(n);
        insertDummyData = false;
    }
}
