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
            if (props.isGroupData()) {
                offset.push(props.getRedefines().getOffset());
                adjustedOffset.push(props.getRedefines().getAdjustedOffset());
            }
        } else if (props.getType() == SymbolConstants.PROGRAM
                /*|| props.is01Group()*/) {
            offset.push(0);
            adjustedOffset.push(0);
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
            int maxLen = props.getLength();
            int maxAdjLen = props.getAdjustedLength();
            SymbolProperties props2 = props;
            while (props2.getRedefines() != null) {
                maxLen = Math.max(maxLen, props2.getRedefines().getLength());
                maxAdjLen = Math.max(maxAdjLen, props2.getRedefines().getAdjustedLength());
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
            if (props.getType() == SymbolConstants.PROGRAM /*|| props.is01Group()*/) {
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
    
    private void calculateGroupLength(SymbolProperties props) throws Exception {

        int leng;
        props.setFormat(false);

        if (props.isFromRESLibrary()) {
            return;
        }


        if (props.getLevelNumber() == 66) {
        	if (props.getRedefinedBy().size() == 1) {
        		byte type = props.getRedefinedBy().get(0).getCobolDesc().getTypeInJava();
        		props.getCobolDesc().setTypeInJava(type);
        	} else {
        		props.getCobolDesc().setTypeInJava(Constants.GROUP);
        	}
            processRenames(props);
            return;
        }

        props.getCobolDesc().setTypeInJava(Constants.GROUP);

//        int prevOffset = unAdjustedOffset.peek();
        int prevAdjustedOffset = offset.peek();
        props.setOffset(offset.peek());
        props.setAdjustedOffset(adjustedOffset.peek());

//        switch (RESConfig.getInstance().getOptimizeAlgorithm()) {
//            case 0:
//                SymbolUtil.setCheckUseNativeJavaTypes(props, alwaysCobolBytes);
//                break;
//            case 1:
//                //SymbolUtil.setCheckUseNativeJavaTypes(props,alwaysCobolBytes);
//                props.setUsedNativeJavaTypes(
//                        (props.getRef() || props.getMod())
//                        && (props.getLevelNumber() == 1 || props.getType() == SymbolConstants.PROGRAM));
//                props.setUsedNativeJavaTypes(props.isUsedNativeJavaTypes() && !alwaysCobolBytes
//                        && !props.isForceCobolBytes());
//                break;
//            case 2:                
//                SymbolUtil.setCheckUseNativeJavaTypes(props, alwaysCobolBytes);
//        }

        adjustedOffset.push(0);
        if (props.hasChildren()) {
            SymbolTable.visit(props.getChildren(), this);
        }
        adjustedOffset.pop();

        if (props.isOccurs() || props.isAParentInOccurs()) {
            ArrayList<SymbolProperties> b = new ArrayList<SymbolProperties>();
            b.addAll(subscriptParents);
            props.setOccursParents(b);
            props.setNoOccursSubscripts(b.size());
        }


//        leng = unAdjustedOffset.peek() - prevOffset;
//        int adjustedLength = offset.peek() - prevAdjustedOffset;

        leng = offset.peek() - prevAdjustedOffset;
//        int adjustedLength = unAdjustedOffset.peek() - prevOffset;
        
        props.setLength(leng);
        if (props.hasChildren()) {
            int adjustLen = 0;
            for (SymbolProperties p : props.getChildren()) {
                int childLen = 0;
                if (p.getLevelNumber() != 66 && p.getRedefines() == null) {
                    if (p.isOccurs()) {
                        childLen = p.getAdjustedLength() * p.getMaxOccursInt();
                    } else {
                        childLen = p.getAdjustedLength();
                    }
                    if (p.getRedefinedBy() != null) {
                        for (SymbolProperties redefier : p.getRedefinedBy()) {
                            childLen = Math.max(childLen, redefier.getAdjustedLength());
                        }
                    }
                    adjustLen += childLen;
                }
            }
            props.setAdjustedLength(adjustLen);
        }

        if (props.isOccurs() && props.getMaxOccursInt() > 0) {
            leng *= props.getMaxOccursInt();
        }/* else {
            leng = 0;
        }*/

        if (props.getRedefines() != null) {
            offset.pop();
            adjustedOffset.pop();
        } else {
            adjustedOffset.push(adjustedOffset.pop() + leng);
            if (!props.isUsedNativeJavaTypes() /*&& adjustedLength > 0*/) {
//                offset.push(offset.pop() + leng);
            	offset.pop();
            	offset.push(prevAdjustedOffset + leng);
            }
        }
    }

    private void processRenames(SymbolProperties props) throws Exception {
        SymbolProperties from = props.getRedefinedBy().get(0);
        if (from == null) {
            throw new ErrorInCobolSourceException(props.getDataDescriptionEntry(), "Invalid RENAMES clause in symbol: " + props.getDataName());
        }
        SymbolProperties thru = from;
        if (props.getRedefinedBy().size() > 1) {
            thru = props.getRedefinedBy().get(1);
        }
        if (thru == null) {
            throw new ErrorInCobolSourceException(props.getDataDescriptionEntry(),"Invalid RENAMES clause in symbol: " + props.getDataName());
        }
        /*if (thru.getUnAdjustedOffset() - from.getUnAdjustedOffset() + thru.getLength() <= 0) {
            RunTimeUtil.getInstance().reportError("Invalid RENAMES clause in symbol: " + props.getDataName(), true);
        }
        props.setOffset(from.getOffset());
        props.setUnAdjustedOffset(from.getUnAdjustedOffset());
        props.setAdjustedLength(thru.getUnAdjustedOffset() - from.getUnAdjustedOffset() + thru.getLength());
        props.setLength(thru.getUnAdjustedOffset() - from.getUnAdjustedOffset() + thru.getLength());*/
        if (thru.getOffset() - from.getOffset() + thru.getLength() <= 0) {
            throw new ErrorInCobolSourceException(props.getDataDescriptionEntry(),"Invalid RENAMES clause in symbol: " + props.getDataName());
        }
        props.setOffset(from.getOffset());
        props.setAdjustedOffset(from.getAdjustedOffset());
        props.setLength(thru.getOffset() - from.getOffset() + thru.getLength());
        props.setAdjustedLength(props.getLength());
    }

    private void calculateElementLength(SymbolProperties props) throws Exception {
    	
    	FieldAttributes.processPicture(props);

        int leng;
//        boolean isSuppressed = (Boolean) props.getIsSuppressed() || !(props.getRef() || props.getMod()) ;//|| !props.isForceCobolBytes();

        if (/*isSuppressed ||*/ props.isFromRESLibrary()) {
            return;
        }


       /*// com.res.java.translation.symbol.SymbolProperties.CobolSymbol sym = new com.res.java.translation.symbol.SymbolProperties.CobolSymbol();
        props.getCobolDesc().setPic((String) props.getPictureString());

        String u = props.getCobolDesc().getPic();
       // props.getJavaType().setUsage((byte) props.getDataUsage());

        if (FieldFormat.verifyCobolPicture(u) == Constants.BIGDECIMAL) {
            FieldAttributes.processDecimal(u, props.getCobolDesc(), props.getDataCategory() == Constants.NUMERIC_EDITED);
            props.setCurrency(props.getCobolDesc().isCurrency());
            props.setSigned(props.getCobolDesc().isSigned());
        } else if (FieldFormat.verifyCobolPicture(u) == Constants.INTEGER) {
            FieldAttributes.processDecimal(u, props.getCobolDesc(), props.getDataCategory() == Constants.NUMERIC_EDITED);
            props.setSigned(props.getCobolDesc().isSigned());
            props.setCurrency(props.getCobolDesc().isCurrency());
        } else {
            if (FieldFormat.verifyCobolPicture(u) == Constants.STRING) {
                FieldAttributes.processAlpha(u, props.getCobolDesc());
            } else {
                SymbolUtil.getInstance().reportError("Error In Usage or Picture of: " + props.getDataName()
                        + ((props.getParent() != null) ? " IN " + props.getParent().getDataName() : "")
                        + ((props.getPictureString() != null) ? " PICTURE " + props.getPictureString() : ""));
            }
        }*/
        
        if (props.getDataUsage() == Constants.COMPUTATIONAL1) {
            props.getCobolDesc().setTypeInJava(Constants.FLOAT);
        } else if (props.getDataUsage() == Constants.COMPUTATIONAL2) {
            props.getCobolDesc().setTypeInJava(Constants.DOUBLE);
        }

//        props.setCobolDesc(props.getCobolDesc());
        leng = FieldAttributes.calculateBytesLength(props);
        if (props.getLevelNumber() == 78) {
            return;
        }

        if (props.isOccurs() || props.isAParentInOccurs()) {
            ArrayList<SymbolProperties> b = new ArrayList<SymbolProperties>();
            b.addAll(subscriptParents);
            props.setOccursParents(b);
            props.setNoOccursSubscripts(b.size());
        }
        props.setLength(leng);
        if (props.isForceCobolBytes()) {
            props.setAdjustedLength(leng);
        } else {
            props.setAdjustedLength(0);
        }
//        boolean b = SymbolUtil.setCheckUseNativeJavaTypes(props, alwaysCobolBytes);
        if (props.getRedefines() == null) {
            props.setOffset(offset.peek());
            props.setAdjustedOffset(adjustedOffset.peek());
            if (props.isOccurs() && props.getMaxOccursInt() > 1) {
                leng *= (props.getMaxOccursInt());
            }
//            if (b) {
//                unAdjustedOffset.push(unAdjustedOffset.pop() + leng);
//                leng = 0;
//            } else {
                offset.push(offset.pop() + leng);
                adjustedOffset.push(adjustedOffset.pop() + leng);
//            }
        } else {
            props.setOffset(props.getRedefines().getOffset());
            props.setAdjustedOffset(props.getRedefines().getAdjustedOffset());
            leng = Math.max(leng, props.getRedefines().getLength());
            offset.pop();
            offset.push(props.getOffset() + leng);
        }


        SymbolTable.visit(props.getChildren(), this);

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
