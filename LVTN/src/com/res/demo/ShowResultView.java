package com.res.demo;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class ShowResultView extends JFrame {
    private JTabbedPane tabbedPane = new JTabbedPane();
    private OutputCodeBrowser outputCode;
    private CompareCode compareCode;
    
    public ShowResultView(File cobolSrc, File outputDir) throws IOException {
        outputCode = new OutputCodeBrowser(outputDir);
        compareCode = new CompareCode(cobolSrc);
        tabbedPane.add("Generated codes", outputCode);
        tabbedPane.add("Compare", compareCode);
        add(tabbedPane);
        setTitle("Result");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        outputCode.setDividerLocation(0.25);
    }
}
