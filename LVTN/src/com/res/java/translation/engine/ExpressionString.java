package com.res.java.translation.engine;

import com.res.java.lib.Constants;
import com.res.java.translation.symbol.SymbolProperties;
import com.res.java.util.NameUtil;

public class ExpressionString implements Cloneable {

	public StringBuilder literal;
	public String raw;
	public SymbolProperties props = null;
	public int type = -1;
	public boolean isAll = false;
	public boolean isJustRight = false;
	public int length = -1;
	public boolean isIdSymbol = false;
	public boolean isRounded = false;

	public String toString() {
		return literal.toString();
	}

	public String toString(boolean isO) {
		if (isO)
			return super.toString();
		return literal.toString();
	}

	public ExpressionString(SymbolProperties sym) {
		this(sym, 0);
	}

	public ExpressionString(SymbolProperties sym, int scale) {
		this(sym, scale, false);
	}

	public ExpressionString(SymbolProperties sym, int scale, boolean isPlainGet) {
		if (sym == null) {
			literal = new StringBuilder("");
		} else {
			isIdSymbol = true;
//			literal = new StringBuilder(
//					NameUtil.getJavaName(props = sym, false));
			literal = new StringBuilder(props.getDataName());
			type = sym.getIdentifierType();
			if (scale != 0) {
				if (scale < 0) {

					literal.insert(0, "__scale(")
							.append(',')
							.append(scale - sym.getJavaType().getMaxIntLength())
							.append(')');
				} else if (scale > 0) {

					literal.insert(0, "__scale(").append(',').append(scale)
							.append(')');
				}
				type = Constants.BIGDECIMAL;
			}

			length = sym.getLength();
		}
	}

	public ExpressionString(SymbolProperties sym, boolean isRnd) {
		this(sym, 0);
		isRounded = isRnd;
	}

	public ExpressionString(String lit) {
		if (lit == null) {
			lit = "";
		}
		literal = new StringBuilder(lit);
	}

	public ExpressionString(String lit, int type) {
		this(lit);
		this.type = type;
	}

	public ExpressionString(ExpressionString lit) {
		isJustRight = lit.isJustRight;
		literal = lit.literal;

		raw = lit.raw;
		props = lit.props;
		literal = new StringBuilder(lit.literal);

		length = lit.length;
		isAll = lit.isAll;
		type = lit.type;
		props = lit.props;
		isIdSymbol = lit.isIdSymbol;
		isRounded = lit.isRounded;
	}

	public ExpressionString() {
		literal = new StringBuilder();
	}

	public ExpressionString(int lit) {
		this(String.valueOf(lit));
	}

	public ExpressionString set(String lit) {
		literal = new StringBuilder(lit);
		return this;
	}

	public ExpressionString set(int lit) {
		literal = new StringBuilder(String.valueOf(lit));
		return this;
	}

	public ExpressionString setAll(boolean b) {
		isAll = b;
		return this;
	}

	public ExpressionString setType(int t) {
		type = t;
		return this;
	}

	public ExpressionString setLength(int l) {
		length = l;
		return this;
	}

	public void setString(String str) {
		literal = new StringBuilder(str);
	}

	public void setString(String str, int type) {
		literal = new StringBuilder(str);
		this.type = type;
	}

	public void setString(StringBuilder str) {
		literal = str;
	}

	// The Three Methods below for upward compatibility with old IdOrLit class.
	public SymbolProperties Id() {
		return props;
	}

	public String Lit() {
		return literal.toString();
	}

	public boolean IsRounded() {
		return isRounded;
	}

	public ExpressionString append(String s) {
		if (literal != null) {
			literal.append(s);
		}
		return this;
	}

	public ExpressionString insert(int i, String s) {
		if (literal != null) {
			literal.insert(i, s);
		}
		return this;
	}

	public ExpressionString replace(int i, int j, String s) {
		if (literal != null) {
			literal.replace(i, j, s);
		}
		return this;
	}
}
