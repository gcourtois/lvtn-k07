package com.res.java.translation.engine;

import java.util.ArrayList;

import com.res.common.exceptions.ErrorInCobolSourceException;
import com.res.java.lib.Constants;
import com.res.java.translation.symbol.SymbolProperties;
import com.res.java.translation.symbol.SymbolTable;
import com.res.java.translation.symbol.Visitor;
import com.res.java.translation.symbol.SymbolProperties.CobolDataDescription;
import com.res.java.translation.symbol.SymbolProperties.CoupleValue;
import com.res.java.util.FieldAttributes;

public class SymbolValidation implements Visitor {

    @Override
    public void visit01Element(SymbolProperties props) throws Exception {
        validateElement(props);
    }

    @Override
    public void visit01Group(SymbolProperties props) throws Exception {
        validateGroup(props);
    }

    @Override
    public void visit77Element(SymbolProperties props) throws Exception {
        validateElement(props);
    }

    @Override
    public void visit88Element(SymbolProperties props) throws Exception {
        ArrayList<CoupleValue> values = props.getValues();
        if (values == null || values.size() == 0) {
            throw new ErrorInCobolSourceException(props.getDataDescriptionEntry(), props.getDataName() + ": condition-name must have VALUES.");
        }
        byte toCat = props.getParent().getCobolDesc().getDataCategory();
        for (CoupleValue c : values) {
            LiteralString val = c.value1;
            if (!CobolFillTable.validateMoveRules(val.category, val.javaType, toCat)) {
                throw new ErrorInCobolSourceException(props.getDataDescriptionEntry(), "Invalid value: " + val.toString());
            }
        }
    }

    @Override
    public void visitChildPostprocess(SymbolProperties props) throws Exception {
        
    }

    @Override
    public void visitChildPreprocess(SymbolProperties props) throws Exception {
        
    }

    @Override
    public void visitFile(SymbolProperties props) throws Exception {
        
    }

    @Override
    public void visitInnerElement(SymbolProperties props) throws Exception {
        validateElement(props);
    }

    @Override
    public void visitInnerGroup(SymbolProperties props) throws Exception {
        validateGroup(props);
    }

    @Override
    public void visitParagraph(SymbolProperties props) throws Exception {
        
    }

    @Override
    public void visitPostprocess(SymbolProperties props) throws Exception {
        
    }

    @Override
    public void visitPreprocess(SymbolProperties props) throws Exception {
        
    }

    @Override
    public void visitProgram(SymbolProperties props) throws Exception {
        validateGroup(props);
    }

    @Override
    public void visitSection(SymbolProperties props) throws Exception {
        
    }

    private void validateElement(SymbolProperties props) throws Exception {
        if (props.getLevelNumber() == 78)
            return;
        
        FieldAttributes.processPicture(props);
        
        validateBlankWhenZero(props);
        
        validateJustified(props);
        
        validateValue(props);
    }
    
    private void validateGroup(SymbolProperties props) throws Exception {
        props.getCobolDesc().setTypeInJava(Constants.GROUP);
        props.getCobolDesc().setDataCategory(Constants.ALPHANUMERIC);
        
        validateBlankWhenZero(props);
        
        validateJustified(props);
        
        validateValue(props);
        
        SymbolTable.visit(props.getChildren(), this);
    }
    
    private void validateBlankWhenZero(SymbolProperties props) throws Exception {
        CobolDataDescription desc = props.getCobolDesc();
        if (props.isBlankWhenZero()) {
            if (props.isGroupData()) {
                throw new ErrorInCobolSourceException(props.getDataDescriptionEntry(), "BLANK WHEN ZERO for group item: " + props.getDataName());
            } else {
                if (props.getLevelNumber() == 66 || props.getLevelNumber() == 88) {
                    throw new ErrorInCobolSourceException(props.getDataDescriptionEntry(), String.format("Level %s item cannot have BLANK WHEN ZERO", props.getLevelNumber()));
                }
                
                // if numeric -> change to numeric-edited
                if (desc.getDataCategory() == Constants.NUMERIC) {
                    desc.setDataCategory(Constants.NUMERIC_EDITED);
                    desc.setTypeInJava(Constants.STRING);
                    if (desc.getPic().indexOf("V") > 0) {
                        desc.setMaxStringLength(desc.getPic().length() - 1);
                    } else {
                        desc.setMaxStringLength(desc.getPic().length());
                    }
                }
                
                // check
                if (desc.getDataCategory() == Constants.NUMERIC_EDITED) {
                    if (desc.getUsage() != Constants.DISPLAY) {
                        throw new ErrorInCobolSourceException(props.getDataDescriptionEntry(), "BLANK WHEN ZERO only apply with usage DISPLAY.");
                    }
                    if (desc.getPic().contains("S") || desc.getPic().contains("*")) {
                        throw new ErrorInCobolSourceException(props.getDataDescriptionEntry(), "BLANK WHEN ZERO clause cannot apply for picture contains 'S' or '*'.");
                    }
                } else {
                    throw new ErrorInCobolSourceException(props.getDataDescriptionEntry(), "Invalid data category for BLANK WHEN ZERO clause.");
                }
            }
        }
    }
    
    private void validateJustified(SymbolProperties props) throws Exception {
        if (props.isJustifiedRight()) {
            if (props.isGroupData()) {
               throw new ErrorInCobolSourceException(props.getDataDescriptionEntry(), "JUSTIFIED RIGHT for group item: " + props.getDataName());
            } else {
                if (props.getLevelNumber() == 66 || props.getLevelNumber() == 88) {
                    throw new ErrorInCobolSourceException(props.getDataDescriptionEntry(), String.format("Level %s cannot have JUSTIFIED [RIGHT] clause.", props.getLevelNumber()));
                }
                CobolDataDescription desc = props.getCobolDesc();
                if (desc.getDataCategory() == Constants.NUMERIC
                        || desc.getDataCategory() == Constants.NUMERIC_EDITED) {
                    throw new ErrorInCobolSourceException(props.getDataDescriptionEntry(), "JUSTIFIED [RIGHT] clause cannot applied to NUMERIC or NUMERIC-EDITED item.");
                }
            }
        }
    }
    
    private void validateValue(SymbolProperties props) throws Exception {
        CobolDataDescription desc = props.getCobolDesc();
        if (props.getValues() != null && props.getValues().size() > 0) {
            // has value
            if (props.getRedefines() != null) {
                throw new ErrorInCobolSourceException(props.getDataDescriptionEntry(), props.getDataName() + ": entry contain REDEFINES cannot have VALUES.");
            }
            if (props.isGroupData()) {
                if (desc.getUsage() != Constants.DISPLAY) {
                    throw new ErrorInCobolSourceException(props.getDataDescriptionEntry(), props.getDataName() + ": group entry with usage other than DISPLAY cannot have VALUES.");
                }
                if (props.hasChildren()) {
                    for (SymbolProperties child : props.getChildren()) {
                        if (child.getValues() != null && child.getValues().size() > 0) {
                            throw new ErrorInCobolSourceException(props.getDataDescriptionEntry(), props.getDataName() + ": group entry contains another entry with VALUES.");
                        }
                    }
                }
            }
            
            LiteralString val = props.getValues().get(0).value1;
            byte toCat = props.getCobolDesc().getDataCategory();
            
            if (!CobolFillTable.validateMoveRules(val.category, val.javaType, toCat)) {
                throw new ErrorInCobolSourceException(props.getDataDescriptionEntry(), "Invalid category in VALUES clause.");
            }
            if (val.category == Constants.NUMERIC
                    && (val.literal.charAt(0) == '-' || val.literal.charAt(0) == '+')
                    && props.getCobolDesc().getPic().charAt(0) != 'S') {
                throw new ErrorInCobolSourceException(props.getDataDescriptionEntry(), "Data entry not signed");
            }
            int len = FieldAttributes.calculateBytesLength(props);
            if (val.isAll
                    || val.category == Constants.SPACE
                    || val.category == Constants.QUOTE) {
                val.fillToSize(len);
                val.category = Constants.ALPHANUMERIC;
                val.javaType = Constants.STRING;
            }
            if (val.category == Constants.ZERO) {
                if (toCat == Constants.NUMERIC) {
                    val.category = Constants.NUMERIC;
                    val.javaType = Constants.LONG;
                } else {
                    val.fillToSize(len);
                    val.category = Constants.ALPHANUMERIC;
                    val.javaType = Constants.STRING;
                }
            }
        } else {
            boolean hasDefaultValue = !props.isGroupData() && (props.getLevelNumber() != 66)
                                        && (props.getRedefines() == null);
            if (hasDefaultValue) {
                //create default value
                LiteralString v = new LiteralString();
                byte category = desc.getDataCategory();
                if (category == Constants.NUMERIC
                        || category == Constants.NUMERIC_EDITED) {
                    v.literal.append("0");
                    v.category = Constants.NUMERIC;
                    v.javaType = Constants.LONG;
                } else {
                    v.literal.append(" ");
                    v.category = Constants.ALPHANUMERIC;
                    v.javaType = Constants.STRING;
                }
                
                ArrayList<CoupleValue> values = new ArrayList<CoupleValue>();
                values.add(props.new CoupleValue(v, null));
                props.setValues(values);
            }
        }
    }
}
