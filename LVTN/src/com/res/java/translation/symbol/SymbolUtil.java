package com.res.java.translation.symbol;

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
import com.res.common.RESConfig;
import com.res.java.lib.Constants;

public class SymbolUtil {

    private static SymbolUtil symbolUtil = null;

    private SymbolUtil() {
    }

    public static SymbolUtil getInstance() {
        if (symbolUtil == null) {
            symbolUtil = new SymbolUtil();
        }
        return symbolUtil;
    }

    public static boolean setCheckUseNativeJavaTypes(SymbolProperties props, boolean forceCobolBytes) {
        if (props.isFloatingPoint()) {
            props.setUsedNativeJavaTypes(true);
            return true;
        }
        props.setUsedNativeJavaTypes(false);
        if (props.getIsFiller() || props.getIsSuppressed() || props.isConsolidateParagraph() || props.isForceCobolBytes()) {
            return false;
        }
        if (RESConfig.getInstance().getOptimizeAlgorithm() == 0 || forceCobolBytes) {
            return false;
        }
        SymbolProperties props2 = props;
        if (RESConfig.getInstance().getOptimizeAlgorithm() == 1) {
            boolean ret = true;
            while ((props2 = props2.getParent()) != null) {
                ret &= (props2.isUsedNativeJavaTypes() || props2.isProgram());
            }
            if (!ret) {
                return false;
            }
        }

        props2 = props;
        if (!(props2.getRedefines() != null
                && props2.getRedefines().getDataUsage() == Constants.INDEX
                && props2.getRedefines().is01Group())) {
            while (props2 != null) {
                if (props2.getRedefines() != null || (props2.getRedefinedBy() != null && props2.getRedefinedBy().size() > 0)) {
                    return false;
                }
                if ((props2.getLevelNumber() == 1 || props2.getLevelNumber() == 77) && props2.isAChildHasRedefines()) {
                    return false;
                }

                props2 = props2.getParent();
            }
        }
        props.setUsedNativeJavaTypes(true);
        return true;
    }
}
