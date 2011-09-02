package com.res.java.translation.engine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.res.cobol.syntaxtree.ProgramIdParagraph;
import com.res.cobol.syntaxtree.ProgramUnit;
import com.res.cobol.visitor.GJDepthFirst;
import com.res.common.RESConfig;
import com.res.java.translation.symbol.SymbolProperties;
import com.res.java.translation.symbol.SymbolTable;
import com.res.java.util.JavaCodePrinter;
import com.res.java.util.NameUtil;

public class Cobol2Java extends GJDepthFirst<Object, Object> {
	@Override
	public Object visit(ProgramUnit n, Object argu) {
		try {
			File f = new File(RESConfig.getInstance().getProgramPackage());
			if (!f.exists()) {
				f.mkdir();
			}
			f = new File(RESConfig.getInstance().getDataPackage());
			if (!f.exists()) {
				f.mkdir();
			}

			String programName = n.identificationDivision.programIdParagraph.programName.cobolWord.nodeToken.tokenImage;

			SymbolProperties props = SymbolTable.getInstance().lookup(
					programName);
			// create new file
			String fileName = RESConfig.getInstance().getProgramPackage()
					+ File.separatorChar + NameUtil.getFileName(props);
			System.out.println("Create file " + fileName);

			// TODO: create printer associated with fileName
			JavaCodePrinter printer;
			//			printer = new JavaCodePrinter(System.out);
			printer = new JavaCodePrinter(new FileOutputStream(fileName));

			// print package
			printer.println("package "
					+ RESConfig.getInstance().getProgramPackage() + ";");
			printer.println();

			// print import
			printer.println("import "
					+ RESConfig.getInstance().getDataPackage() + ".*;");
			printer.println();

			// print class definition
			printer.println("public class " + props.getJavaName2() + " {");

			super.visit(n, printer);

			// end class
			printer.println("}");

			printer.close();

			// throw new IOException();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return null;
	}

	@Override
	public Object visit(ProgramIdParagraph n, Object o) {
		String programName = n.programName.cobolWord.nodeToken.tokenImage;
		SymbolProperties props = SymbolTable.getInstance().lookup(programName);

		JavaCodePrinter printer = (JavaCodePrinter) o;

		// print all data field
		try {
			new DataPrinter().printDataForProgram(props, printer);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

		return null;
	}
}