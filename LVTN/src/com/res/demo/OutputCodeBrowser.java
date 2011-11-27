package com.res.demo;

import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import jsyntaxpane.DefaultSyntaxKit;

@SuppressWarnings("serial")
public class OutputCodeBrowser extends JPanel {
    private File outputDir;
    private JTree tree;
    private JEditorPane javaEditor;
    private JScrollPane treeScroll;
    private JScrollPane editorScroll;
    private JSplitPane splitPane;
    
    public OutputCodeBrowser(File outputDir) {
        this.outputDir = outputDir;
        init();
        setLayout(new GridLayout(1,1));
    }
    
    public void setDividerLocation(double location) {
        splitPane.setDividerLocation(location);
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
                        JOptionPane.showMessageDialog(null, e1.getMessage(), "IOException", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            
        });
        
        DefaultSyntaxKit.initKit();
        javaEditor = new JEditorPane();
        editorScroll = new JScrollPane(javaEditor);
        javaEditor.setContentType("text/java");
        javaEditor.setEditable(false);
        javaEditor.setFont(new Font("Courier", 0, 12));
        
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, editorScroll);
        add(splitPane);
    }
    
    private DefaultMutableTreeNode getDirectoryTree(File f) {
        DefaultMutableTreeNode rs = new FileTreeNode(f);
        if (f.isDirectory()) {
            for (File child : f.listFiles()) {
                rs.add(getDirectoryTree(child));
            }
        }
        return rs;
    }
    
    class FileTreeNode extends DefaultMutableTreeNode {
        public FileTreeNode() {
            super();
        }
        
        public FileTreeNode(Object userObject) {
            super(userObject);
        }
        
        public FileTreeNode(Object userObject, boolean allowsChildren) {
            super(userObject, allowsChildren);
        }
        
        @Override
        public String toString() {
            if (userObject instanceof File) {
                if (!isRoot()) {
                    return ((File) userObject).getName();
                }
            }
            return super.toString();
        }
    }
}
