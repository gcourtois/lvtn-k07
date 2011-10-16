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
import java.util.Stack;

import com.res.cobol.Main;
import com.res.common.exceptions.ErrorInCobolSourceException;
import com.res.java.lib.Constants;
import com.res.java.translation.symbol.SymbolConstants;
import com.res.java.translation.symbol.SymbolProperties;
import com.res.java.translation.symbol.SymbolTable;
import com.res.java.translation.symbol.Visitor;
import com.res.java.util.FieldAttributes;

//Called through SymbolTable.visit() which is in turn 
//Invoked from CobolFillTable.visit(ProcedureDivision n)
public class CalculateSymbolLength implements Visitor {

    @Override
    public void visitPreprocess(SymbolProperties props) throws Exception {
        if (Main.getContext().getTraceLevel() >= 2) {
            System.out.println("Doing CalculateSymbolLength symbol " + props.getDataName());
        }

        if (props.isOccurs() && props.getType() != SymbolConstants.PROGRAM) {
            if (props.getLevelNumber() == 1 || props.getLevelNumber() == 66
                    || props.getLevelNumber() == 88 || props.getLevelNumber() == 77) {
                props.setOccurs(false);
            } else {
                subscriptParents.push(props);
            }
        }
 
        
        if (props.getRedefines() != null) {
//            if (props.isGroupData()) {
            offset.pop();
            offset.push(props.getRedefines().getOffset());
            adjustedOffset.pop();
            adjustedOffset.push(props.getRedefines().getAdjustedOffset());
            globalOffset = props.getRedefines().getGlobalOffset();
            globalAdjustedOffset = props.getRedefines().getGlobalAdjustedOffset();
//            }
        } else if (props.getType() == SymbolConstants.PROGRAM
                /*|| props.isGroupData()*/) {
            offset.push(0);
            adjustedOffset.push(0);
            globalOffset = 0;
            globalAdjustedOffset = 0;
        }

        
        if (props.getType() != SymbolConstants.PROGRAM) {
            props.setAParentInOccurs(props.getParent().isOccurs() || props.getParent().isAParentInOccurs());
        }

//        if (props.is01Group() && props.hasRenames()) {
//            alwaysCobolBytes = true;
//        }

        if (props.getDataUsage() == Constants.INDEX) {
            props.setDataUsage(Constants.BINARY);
            isIndexSetToBinary = true;
        } else {
            isIndexSetToBinary = false;
        }
    }
    private boolean isIndexSetToBinary = false;

    @Override
    public void visitPostprocess(SymbolProperties props) throws Exception {

        if (Main.getContext().getTraceLevel() >= 2) {
            System.out.println("Done CalculateSymbolLength symbol " + props.getDataName() + " O=" + props.getOffset()
                    + " L=" + props.getLength() + " UO=" + props.getAdjustedOffset() + " AL=" + props.getAdjustedLength());
        }

        if (alwaysNoFormat) {
            props.setIsFormat(false);
        }
//        if (alwaysCobolBytes) {
//            props.setUsedNativeJavaTypes(false);
//        } else if (props.getType() != SymbolConstants.PROGRAM
//                && RESConfig.getInstance().getOptimizeAlgorithm() == 1);
        if (props.isOccurs()) {
            subscriptParents.pop();
        }
        if (props.is01Group() && props.getDataName().equalsIgnoreCase("SQLCA")) {
            props.setLength(100);//Hack
        }

        if (props.getRedefines() != null) {
            // new
            props.setOffset(props.getRedefines().getOffset());
            props.setAdjustedOffset(props.getRedefines().getAdjustedOffset());
            
            int len = Math.max(props.getLength(), props.getRedefines().getLength());
            int adjustedLen = Math.max(props.getAdjustedLength(), props.getRedefines().getAdjustedLength());
            
            offset.pop();
            offset.push(props.getOffset() + len);
            
            adjustedOffset.pop();
            adjustedOffset.push(props.getAdjustedOffset() + adjustedLen);
            
            globalOffset = props.getRedefines().getGlobalOffset() + len;
            globalAdjustedOffset = props.getRedefines().getGlobalAdjustedOffset() + adjustedLen;
            // end new
            
            SymbolProperties props2 = props;
            while (props2.getRedefines() != null) {
                if (props2.getLength() > props2.getRedefines().getLength()) {
                    if (props.getParent().isIndexedFile() && props.getRedefines().getParent().isIndexedFile()) {
                        if (props.getParent().getDataName().equalsIgnoreCase(props.getRedefines().getParent().getDataName())) {
                            props.setIndexedFileRecord(true);
                            props.getRedefines().setIndexedFileRecord(false);
                        } else {
                            props.getRedefines().setIndexedFileRecord(true);
                        }
                    }
                } else {
                    if (props2.getParent().isIndexedFile() && props2.getRedefines().getParent().isIndexedFile()) {
                        if (props2.getParent().getDataName().equalsIgnoreCase(props2.getRedefines().getParent().getDataName())) {
                            props2.setIndexedFileRecord(false);
                            props2.getRedefines().setIndexedFileRecord(true);
                        } else {
                            props2.setIndexedFileRecord(true);
                        }
                    }
                }
                props2 = props2.getRedefines();
            }
            /*props2 = props;
            props2.setLength(maxLen);
            props2.setAdjustedLength(maxAdjLen);
            if (props2.getRedefinedBy() != null) {
                for (SymbolProperties r : props2.getRedefinedBy()) {
                    r.setLength(maxLen);
                    r.setAdjustedLength(maxAdjLen);
                }
            }
            while (props2.getRedefines() != null) {
                props2.getRedefines().setLength(maxLen);
                props2.getRedefines().setAdjustedLength(maxAdjLen);
                props2 = props2.getRedefines();
                if (props2.getRedefinedBy() != null) {
                    for (SymbolProperties r : props2.getRedefinedBy()) {
                        r.setLength(maxLen);
                        r.setAdjustedLength(maxAdjLen);
                    }
                }
            }*/

        } else {
            if (props.getType() == SymbolConstants.PROGRAM /*|| props.isGroupData()*/) {
                offset.pop();
                adjustedOffset.pop();
            }
            if (props.getParent() != null && props.getParent().isIndexedFile()) {
                props.setIndexedFileRecord(true);
            }
        }
//        if (props.is01Group() && props.hasRenames()) {
//            alwaysCobolBytes = false;
//        }
        if (isIndexSetToBinary && props.getDataUsage() == Constants.BINARY) {
            props.setDataUsage(Constants.INDEX);
        }
    }
//    private boolean alwaysCobolBytes = false;
    private boolean alwaysNoFormat = false;

    @Override
    public void visitChildPreprocess(SymbolProperties props) throws Exception {
    }

    @Override
    public void visitChildPostprocess(SymbolProperties props) throws Exception {
    }

    @Override
    public void visit01Element(SymbolProperties props) throws Exception {
        calculateElementLength(props);
    }

    @Override
    public void visit01Group(SymbolProperties props) throws Exception {
        if (props.getDataName().equalsIgnoreCase("sqlca")) {
            alwaysNoFormat = true;
        }
        calculateGroupLength(props);
        alwaysNoFormat = false;
    }

    @Override
    public void visit77Element(SymbolProperties props) throws Exception {
        calculateElementLength(props);
    }

    @Override
    public void visit88Element(SymbolProperties props) throws Exception {
        //props.setJavaType(new com.res.java.translation.symbol.SymbolProperties.CobolSymbol());
        props.getCobolDesc().setTypeInJava(props.getParent().getCobolDesc().getTypeInJava());
    }

    @Override
    public void visitInnerElement(SymbolProperties props) throws Exception {
        calculateElementLength(props);
    }

    @Override
    public void visitInnerGroup(SymbolProperties props) throws Exception {
        calculateGroupLength(props);
    }

    @Override
    public void visitProgram(SymbolProperties props) throws Exception {
        calculateGroupLength(props);
    }
    private static Stack<SymbolProperties> subscriptParents = new Stack<SymbolProperties>();
    private static Stack<Integer> offset = new Stack<Integer>();
    private static Stack<Integer> adjustedOffset = new Stack<Integer>();
    private static int globalOffset = 0;
    private static int globalAdjustedOffset = 0;
    
    private void calculateGroupLength(SymbolProperties props) throws Exception {

        props.setFormat(false);

        if (props.isFromRESLibrary()) {
            return;
        }


        if (props.getLevelNumber() == 66) {
//        		byte type = props.getRedefinedBy().get(0).getCobolDesc().getTypeInJava();
//        		props.getCobolDesc().setTypeInJava(type);
            processRenames(props);
            return;
        }

        if (props.isOccurs() || props.isAParentInOccurs()) {
            ArrayList<SymbolProperties> b = new ArrayList<SymbolProperties>();
            b.addAll(subscriptParents);
            props.setOccursParents(b);
            props.setNoOccursSubscripts(b.size());
        }
        
        
        props.setOffset(offset.peek());
        props.setAdjustedOffset(adjustedOffset.peek());
        
        props.setGlobalOffset(globalOffset);
        props.setGlobalAdjustedOffset(globalAdjustedOffset);
        
        int prevGlobalOffset = globalOffset;
        int prevGlobalAdjustedOffset = globalAdjustedOffset;
        
        offset.push(0);
        adjustedOffset.push(0);
        
        if (props.hasChildren()) {
            SymbolTable.visit(props.getChildren(), this);
        }
        

        int len = offset.pop();
        int adjustedLen = adjustedOffset.pop();
        
        props.setLength(len);
        props.setAdjustedLength(adjustedLen);
        
        if (props.isOccurs() && props.getMaxOccursInt() > 0) {
            len *= props.getMaxOccursInt();
            adjustedLen *= props.getMaxOccursInt();
        }
        

        /*if (props.getRedefines() != null) {
            offset.pop();
            adjustedOffset.pop();
        } else {*/
        if (props.getRedefines() == null) {
            offset.push(offset.pop() + len);
            adjustedOffset.push(adjustedOffset.pop() + adjustedLen);
            globalOffset = prevGlobalOffset + len;
            globalAdjustedOffset = prevGlobalAdjustedOffset + adjustedLen;
        }
    }

    private void processRenames(SymbolProperties props) throws Exception {
        SymbolProperties from = props.getRedefinedBy().get(0);

        int len = 0;
        
        if (props.getRedefinedBy().size() > 1) {
            // renames ... thru ...
            SymbolProperties thru = props.getRedefinedBy().get(1);
            if (thru.getGlobalOffset() - from.getGlobalOffset() - from.getLength() < 0) {
                throw new ErrorInCobolSourceException(props.getDataDescriptionEntry(),"Invalid RENAMES clause in symbol: " + props.getDataName() + ". Storage area is indeterminate.");
            }
            len = thru.getGlobalOffset() - from.getGlobalOffset() + thru.getLength();
        } else {
            // renames ...
            len = from.getLength();
        }
        
        props.setOffset(from.getOffset());
        props.setAdjustedOffset(from.getAdjustedOffset());
        props.setGlobalOffset(from.getGlobalOffset());
        props.setGlobalAdjustedOffset(from.getGlobalAdjustedOffset());
        
        props.setLength(len);
        props.setAdjustedLength(len);
    }

    private void calculateElementLength(SymbolProperties props) throws Exception {
        if (props.getLevelNumber() == 78) {
            return;
        }
    	
//    	FieldAttributes.processPicture(props);

    	if (props.getLevelNumber() == 66) {
    	    processRenames(props);
    	    return;
    	}
    	
//        boolean isSuppressed = (Boolean) props.getIsSuppressed() || !(props.getRef() || props.getMod()) ;//|| !props.isForceCobolBytes();

        if (/*isSuppressed ||*/ props.isFromRESLibrary()) {
            return;
        }
        
        if (props.getDataUsage() == Constants.COMPUTATIONAL1) {
            props.getCobolDesc().setTypeInJava(Constants.FLOAT);
        } else if (props.getDataUsage() == Constants.COMPUTATIONAL2) {
            props.getCobolDesc().setTypeInJava(Constants.DOUBLE);
        }
        
        if (props.isOccurs() || props.isAParentInOccurs()) {
            ArrayList<SymbolProperties> b = new ArrayList<SymbolProperties>();
            b.addAll(subscriptParents);
            props.setOccursParents(b);
            props.setNoOccursSubscripts(b.size());
        }

        int len = FieldAttributes.calculateBytesLength(props);
        int adjustedLen = props.isForceCobolBytes() ? len : 0;
        
        props.setLength(len);
        props.setAdjustedLength(adjustedLen);
        props.setGlobalOffset(globalOffset);
        props.setGlobalAdjustedOffset(globalAdjustedOffset);
        
        if (props.getRedefines() == null) {
            props.setOffset(offset.peek());
            props.setAdjustedOffset(adjustedOffset.peek());
            
            if (props.isOccurs() && props.getMaxOccursInt() > 1) {
                len *= props.getMaxOccursInt();
                adjustedLen *= props.getMaxOccursInt();
            }
            
            offset.push(offset.pop() + len);
            adjustedOffset.push(adjustedOffset.pop() + adjustedLen);
            
            globalOffset += len;
            globalAdjustedOffset += adjustedLen;
            
        } else {
            /*props.setOffset(props.getRedefines().getOffset());
            props.setAdjustedOffset(props.getRedefines().getAdjustedOffset());
            
            len = Math.max(len, props.getRedefines().getLength());
            adjustedLen = Math.max(adjustedLen, props.getRedefines().getAdjustedLength());
            
            offset.pop();
            offset.push(props.getOffset() + len);
            
            adjustedOffset.pop();
            adjustedOffset.push(props.getAdjustedOffset() + adjustedLen);
            
            globalOffset = props.getRedefines().getGlobalOffset() + len;
            globalAdjustedOffset = props.getRedefines().getGlobalAdjustedOffset() + adjustedLen;*/
        }

        SymbolTable.visit(props.getChildren(), this); // visit lv-88 entry

        return;
    }

    @Override
    public void visitParagraph(SymbolProperties props) throws Exception {
    }

    @Override
    public void visitSection(SymbolProperties props) throws Exception {
    }

    @Override
    public void visitFile(SymbolProperties props) throws Exception {
//        alwaysCobolBytes = true;
        SymbolTable.visit(props.getChildren(), this);
//        alwaysCobolBytes = false;
        if (props.hasChildren()) {
            for (SymbolProperties child : props.getChildren()) {
                props.setLength(Math.max(props.getLength(), child.getLength()));
            }
            if (props.getAdjustedLength() < props.getLength()) {
                props.setAdjustedLength(props.getLength());
            }
        }
        //props.setAdjustedLength(0);
    }
}
