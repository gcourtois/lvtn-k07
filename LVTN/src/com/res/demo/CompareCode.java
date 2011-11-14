package com.res.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Element;
import javax.swing.text.Highlighter.HighlightPainter;

import jsyntaxpane.DefaultSyntaxKit;

import com.res.demo.util.GenDetails;
import com.res.demo.util.GenDetails.OutputInfo;

public class CompareCode extends JFrame {
    
    private GenDetails genDetails = GenDetails.getInstance();
    private HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(new Color(0xF5D310));
    private String currentJavaFile = "";
    
    public CompareCode(File cobolSrc) throws IOException {
        init();
        cobolLabel.setText(cobolSrc.getCanonicalPath());
        cobolEditor.read(new FileInputStream(cobolSrc), null);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }
    
    private JSplitPane splitPane;
    private JEditorPane cobolEditor;
    private JEditorPane javaEditor;
    private JScrollPane cobolScrollPane;
    private JScrollPane javaScrollPane;
    private JPanel cobolPanel;
    private JPanel javaPanel;
    private JLabel cobolLabel;
    private JLabel javaLabel;
    
    private void init() {
        DefaultSyntaxKit.initKit();
        
        cobolEditor = new JEditorPane();
        cobolScrollPane = new JScrollPane(cobolEditor);
        cobolEditor.setContentType("text/c");
        
        javaEditor = new JEditorPane();
        javaScrollPane = new JScrollPane(javaEditor);
        javaEditor.setContentType("text/java");
        
        cobolLabel = new JLabel();
        javaLabel = new JLabel();
        
        cobolPanel = new JPanel(new BorderLayout());
        cobolPanel.setBorder(BorderFactory.createTitledBorder("COBOL source"));
        javaPanel = new JPanel(new BorderLayout());
        javaPanel.setBorder(BorderFactory.createTitledBorder("Generated JAVA source"));
        
        cobolPanel.add(cobolLabel, BorderLayout.NORTH);
        cobolPanel.add(cobolScrollPane, BorderLayout.CENTER);
        
        javaPanel.add(javaLabel, BorderLayout.NORTH);
        javaPanel.add(javaScrollPane, BorderLayout.CENTER);
        
//        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cobolScrollPane, javaScrollPane);
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cobolPanel, javaPanel);
        add(splitPane);
        
        cobolEditor.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
//                int line = cobolEditor.getDocument().getDefaultRootElement()
//                        .getElementIndex(cobolEditor.getSelectionStart()) + 1;
                int line = cobolEditor.getDocument().getDefaultRootElement()
                        .getElementIndex(e.getDot()) + 1;
                OutputInfo info = genDetails.get(line);
                if (info != null) {
                    doHighlight(info);
                }
            }
        });
    }
    
    private void doHighlight(OutputInfo info) {
        if (!currentJavaFile.equals(info.fileName)) {
            currentJavaFile = info.fileName;
            try {
                File f = new File(info.fileName);
                javaLabel.setText(f.getCanonicalPath());
                javaEditor.read(new FileInputStream(f), null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        Element root = javaEditor.getDocument().getDefaultRootElement();
        javaEditor.getHighlighter().removeAllHighlights();
        final int begin = root.getElement(info.beginLine - 1).getStartOffset();
        final int end = root.getElement(info.endLine - 1).getEndOffset();
        try {
            javaEditor.getHighlighter().addHighlight(begin, end, painter);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    javaScrollPane.getVerticalScrollBar().setValue(javaEditor.modelToView(begin).y);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
