package com.res.cobol.visitor;

import java.util.Enumeration;

import com.res.cobol.syntaxtree.CobolWord;
import com.res.cobol.syntaxtree.CompilationUnit;
import com.res.cobol.syntaxtree.DataDescriptionEntry;
import com.res.cobol.syntaxtree.DataDescriptionEntryClause;
import com.res.cobol.syntaxtree.DataDivision;
import com.res.cobol.syntaxtree.DataDivisionSection;
import com.res.cobol.syntaxtree.DataName;
import com.res.cobol.syntaxtree.DataPictureClause;
import com.res.cobol.syntaxtree.IdentificationDivision;
import com.res.cobol.syntaxtree.LevelNumber;
import com.res.cobol.syntaxtree.Node;
import com.res.cobol.syntaxtree.NodeChoice;
import com.res.cobol.syntaxtree.NodeListOptional;
import com.res.cobol.syntaxtree.NodeOptional;
import com.res.cobol.syntaxtree.NodeSequence;
import com.res.cobol.syntaxtree.NodeToken;
import com.res.cobol.syntaxtree.PictureOccurence;
import com.res.cobol.syntaxtree.PictureString;
import com.res.cobol.syntaxtree.ProgramIdParagraph;
import com.res.cobol.syntaxtree.ProgramName;
import com.res.cobol.syntaxtree.ProgramUnit;
import com.res.cobol.syntaxtree.WorkingStorageSection;

public class PrintVisitor extends StringStringVisitor {
	OutputFormatter fmt = new OutputFormatter();

	public String visit(CompilationUnit n, String argu) {
		fmt.increaseIndent();
		String s = n.nodeListOptional.accept(this, argu);
		fmt.decreaseIndent();
		return (fmt.newLine("CompilationUnit(") + s + fmt.newLine(")"));
	}

	@Override
	public String visit(NodeListOptional n, String argu) {
		if (n.present()) {
			StringBuilder sb = new StringBuilder();
			sb.append(fmt.newLine("["));
			fmt.increaseIndent();
			for (Enumeration<Node> e = n.elements(); e.hasMoreElements();) {
				sb.append(e.nextElement().accept(this, argu));
			}
			fmt.decreaseIndent();
			sb.append(fmt.newLine("]"));
			return sb.toString();
		}
		return "";
	}

	public String visit(NodeSequence n, String argu) {
		if (n.size() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(fmt.newLine("["));
			fmt.increaseIndent();
			for (Enumeration<Node> e = n.elements(); e.hasMoreElements();) {
				sb.append(e.nextElement().accept(this, argu));
			}
			fmt.decreaseIndent();
			sb.append(fmt.newLine("]"));
			return sb.toString();
		}
		return "";
	}

	public String visit(NodeOptional n, String argu) {
		if (n.present()) {
			return fmt.newLine(n.node.accept(this, argu));
		}
		return "";
	}

	public String visit(ProgramUnit n, String argu) {
		StringBuilder sb = new StringBuilder();
		sb.append(fmt.newLine("ProgramUnit("));
		fmt.increaseIndent();
		sb.append(n.identificationDivision.accept(this, argu));
		sb.append(n.nodeOptional.accept(this, argu));
		sb.append(n.nodeOptional1.accept(this, argu));
		sb.append(n.nodeOptional2.accept(this, argu));
		fmt.decreaseIndent();
		sb.append(fmt.newLine(")"));
		return sb.toString();
	}

	public String visit(IdentificationDivision n, String argu) {
		StringBuilder sb = new StringBuilder();
		sb.append(fmt.newLine("IdentificationDivision("));
		fmt.increaseIndent();
		sb.append(n.programIdParagraph.accept(this, argu));
		sb.append(n.nodeListOptional.accept(this, argu));
		fmt.decreaseIndent();
		sb.append(fmt.newLine(")"));
		return sb.toString();
	}

	public String visit(ProgramIdParagraph n, String argu) {
		String s = n.programName.accept(this, argu);
		if (n.nodeOptional.present())
			s += fmt.newLine("INITIAL");
		return s;
	}

	public String visit(ProgramName n, String argu) {
		return fmt.newLine("Name: " + n.cobolWord.accept(this, argu));
	}

	public String visit(CobolWord n, String argu) {
		return n.nodeToken.accept(this, argu);
	}

	public String visit(NodeToken n, String argu) {
		return n.tokenImage;
	}

	public String visit(DataDivision n, String argu) {
		return fmt.newLine("DataDivision(")
				+ n.nodeListOptional.accept(this, argu) + fmt.newLine(")");
	}
	
	public String visit(DataDivisionSection n, String argu) {
		String s = null;
		switch (n.nodeChoice.which) {
		case 0:
			s = fmt.newLine("FileSection(");
			break;
		case 1:
			s = fmt.newLine("WorkingStorageSection(");
			break;
		case 2:
			s = fmt.newLine("LinkageSection(");
			break;
		case 3:
			s = fmt.newLine("CommunicationSection(");
			break;
			default:
				break;
				
		}
		fmt.increaseIndent();
		s += n.nodeChoice.accept(this, argu);
		fmt.decreaseIndent();
		return s + fmt.newLine(")");
	}
	
	public String visit(NodeChoice n, String argu) {
		return n.choice.accept(this, argu);
	}
	
	public String visit(WorkingStorageSection n, String argu) {
		return n.nodeListOptional.accept(this, argu);
	}
	
	public String visit(DataDescriptionEntry n, String argu) {
		StringBuilder sb = new StringBuilder();
		sb.append(fmt.newLine("DataEntry("));
		fmt.increaseIndent();
		sb.append(n.nodeChoice.accept(this, argu));
		fmt.decreaseIndent();
		sb.append(fmt.newLine(")"));
		return sb.toString();
	}
	
	public String visit(LevelNumber n, String argu) {
		return fmt.newLine("Level: " + n.nodeToken.accept(this, argu));
	}
	
	public String visit(DataName n, String argu) {
		return fmt.newLine("Name: " + n.cobolWord.accept(this, argu));
	}
	
	public String visit(DataDescriptionEntryClause n, String argu) {
		return n.nodeChoice.accept(this, argu);
	}
	
	public String visit(DataPictureClause n, String argu) {
		return fmt.newLine("Picture: ") + n.pictureString.accept(this, argu);
	}
	
	public String visit(PictureString n, String argu) {
		return n.pictureOccurence.accept(this, argu) + n.nodeListOptional.accept(this, argu);
	}
	
	public String visit(PictureOccurence n, String argu) {
		return n.nodeChoice.accept(this, argu);
	}
}

class OutputFormatter {
	private StringBuilder indent = new StringBuilder();
	private int len = 0;

	public void increaseIndent() {
		indent.append("  ");
		len += 2;
	}

	public void decreaseIndent() {
		len -= 2;
		indent.setLength(len);
	}

	public String newLine(String s) {
		if (s == null)
			s = "";
		return indent.toString() + s + "\n";
	}
}