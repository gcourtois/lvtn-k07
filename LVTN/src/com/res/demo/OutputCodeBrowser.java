package com.res.demo;

import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import jsyntaxpane.DefaultSyntaxKit;

public class OutputCodeBrowser extends JFrame {
    private File outputDir;
    private JTree tree;
    private JEditorPane javaEditor;
    private JScrollPane treeScroll;
    private JScrollPane editorScroll;
    private JSplitPane splitPane;
    
    public OutputCodeBrowser(File outputDir) {
        this.outputDir = outputDir;
        try {
            setTitle(outputDir.getCanonicalPath());
        } catch (IOException e) {
        }
        init();
        pack();
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }
    
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        splitPane.setDividerLocation(0.25);
    }
    
    private void init() {
        tree = new JTree(getDirectoryTree(outputDir));
        treeScroll = new JScrollPane(tree);
        
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
                File f = (File) node.getUserObject();
                if (f.isFile()) {
                    try {
                        FileInputStream fis = new FileInputStream(f);
                        javaEditor.read(fis, null);
                        fis.close();
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(getContentPane(), e1.getMessage(), "IOException", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            
        });
        
        DefaultSyntaxKit.initKit();
        javaEditor = new JEditorPane();
        editorScroll = new JScrollPane(javaEditor);
        javaEditor.setContentType("text/java");
//        javaEditor.setEditable(false);
        javaEditor.setFont(new Font("Courier", 0, 12));
        
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, editorScroll);
        add(splitPane);
    }
    
    private DefaultMutableTreeNode getDirectoryTree(File f) {
        DefaultMutableTreeNode rs = new DefaultMutableTreeNode(f);
        if (f.isDirectory()) {
            for (File child : f.listFiles()) {
                rs.add(getDirectoryTree(child));
            }
        }
        return rs;
    }
    
    public static void main(String[] args) {
        OutputCodeBrowser b = new OutputCodeBrowser(new File("D:/opt"));
        b.setVisible(true);
//        b.splitPane.setDividerLocation(0.5);
    }
}
