package com.res.demo;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import com.res.cobol.Main;
import com.res.cobol.parser.ParseException;
import com.res.common.RESConfig;
import com.res.common.exceptions.ErrorInCobolSourceException;

public class Demo {
    
    private JFrame mainFrame;
    
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem openItem;
    private JMenuItem exitItem;
    
    private JRadioButton fixFormatRadio;
    private JRadioButton freeFormatRadio;
    
    private JCheckBox genJavaCheckbox;
    
    private JTextField outputDirTxt;
    private JTextField dataDirTxt;
    private JTextField programDirTxt;
    private JButton outputDirBtn;
    
    private JButton doConvertBtn;
    
    private JEditorPane cobolEditor;
    private JScrollPane cobolScrollPane;
    
    private JFileChooser openFileChooser;
    private File openedFile;
    
    private JFileChooser outputDirChooser;
    
    private OutputCodeBrowser javaCodeBrowser; 
    
    private Main instance = new Main();
    
    public void display() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        
        init();
        layout();
        addListener();
        
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(770, 600);
        mainFrame.setResizable(false);
        mainFrame.setVisible(true);
    }
    
    private void init() throws IOException {
        mainFrame = new JFrame("COBOL to JAVA demo");
        
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        openItem = new JMenuItem("Open");
        exitItem = new JMenuItem("Exit");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        openItem.setMnemonic(KeyEvent.VK_O);
        exitItem.setMnemonic(KeyEvent.VK_X);
        
        fixFormatRadio = new JRadioButton("Fixed");
        fixFormatRadio.setSelected(true);
        freeFormatRadio = new JRadioButton("Free");

        genJavaCheckbox = new JCheckBox("Use Java types when possible");
        
        outputDirTxt = new JTextField();
        outputDirTxt.setText(new File(".").getCanonicalPath());
        
        dataDirTxt = new JTextField("coboldataclasses");
        programDirTxt = new JTextField("cobolprogramclasses");
        outputDirBtn = new JButton("Change");
        
        doConvertBtn = new JButton("Convert");
        
        cobolEditor = new JEditorPane();
        cobolScrollPane = new JScrollPane();
        
        openFileChooser = new JFileChooser();
        openFileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                String name = f.getName();
                return f.isDirectory() || name.endsWith(".cob") || name.endsWith(".cbl");
            }

            @Override
            public String getDescription() {
                return "COBOL source file (*.cob, *.cbl)";
            }
            
        });
        
        outputDirChooser = new JFileChooser();
        outputDirChooser.setDialogTitle("Open directory");
        outputDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        outputDirChooser.setAcceptAllFileFilterUsed(false);
    }
    
    private void layout() {        
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        
        mainFrame.setJMenuBar(menuBar);
        
        ButtonGroup formatGroup = new ButtonGroup();
        formatGroup.add(fixFormatRadio);
        formatGroup.add(freeFormatRadio);
        
        JPanel formatPanel = new JPanel();
        formatPanel.setLayout(new BoxLayout(formatPanel, BoxLayout.Y_AXIS));
        TitledBorder border = BorderFactory.createTitledBorder("Source format");
        formatPanel.setBorder(border);
        formatPanel.add(fixFormatRadio);
        formatPanel.add(freeFormatRadio);
        
        JPanel genOptionPanel = new JPanel();
        border = BorderFactory.createTitledBorder("Java code option");
        genOptionPanel.setBorder(border);
        genOptionPanel.add(genJavaCheckbox);
        
        JPanel outputTargetPanel = new JPanel();
        border = BorderFactory.createTitledBorder("Output target");
        outputTargetPanel.setBorder(border);
        SpringLayout outputLayout = new SpringLayout();
        outputTargetPanel.setLayout(outputLayout);
        
        JLabel outputDirLbl = new JLabel("Output directory: ");
        JLabel dataDirLbl = new JLabel("Data package: ");
        JLabel progDirLbl = new JLabel("Program package: ");
        
        outputTargetPanel.add(outputDirLbl);
        outputTargetPanel.add(dataDirLbl);
        outputTargetPanel.add(progDirLbl);
        outputTargetPanel.add(outputDirTxt);
        outputTargetPanel.add(outputDirBtn);
        outputTargetPanel.add(dataDirTxt);
        outputTargetPanel.add(programDirTxt);
        
        outputLayout.putConstraint(SpringLayout.NORTH, outputDirLbl, 5, SpringLayout.NORTH, outputTargetPanel);
        
        outputLayout.putConstraint(SpringLayout.NORTH, dataDirLbl, 10, SpringLayout.SOUTH, outputDirLbl);
        outputLayout.putConstraint(SpringLayout.WEST, dataDirLbl, 0, SpringLayout.WEST, outputDirLbl);
        
        outputLayout.putConstraint(SpringLayout.NORTH, progDirLbl, 10, SpringLayout.SOUTH, dataDirLbl);
        outputLayout.putConstraint(SpringLayout.WEST, progDirLbl, 0, SpringLayout.WEST, outputDirLbl);
        
        outputLayout.putConstraint(SpringLayout.NORTH, outputDirTxt, 0, SpringLayout.NORTH, outputDirLbl);
        outputLayout.putConstraint(SpringLayout.WEST, outputDirTxt, 0, SpringLayout.WEST, programDirTxt);
        outputLayout.putConstraint(SpringLayout.EAST, outputDirTxt, -5, SpringLayout.WEST, outputDirBtn);
        
        outputLayout.putConstraint(SpringLayout.NORTH, dataDirTxt, 0, SpringLayout.NORTH, dataDirLbl);
        outputLayout.putConstraint(SpringLayout.WEST, dataDirTxt, 0, SpringLayout.WEST, programDirTxt);
        outputLayout.putConstraint(SpringLayout.EAST, dataDirTxt, 0, SpringLayout.EAST, outputDirTxt);
        
        outputLayout.putConstraint(SpringLayout.NORTH, programDirTxt, 0, SpringLayout.NORTH, progDirLbl);
        outputLayout.putConstraint(SpringLayout.WEST, programDirTxt, 5, SpringLayout.EAST, progDirLbl);
        outputLayout.putConstraint(SpringLayout.EAST, programDirTxt, 0, SpringLayout.EAST, outputDirTxt);
        
        outputLayout.putConstraint(SpringLayout.NORTH, outputDirBtn, -2, SpringLayout.NORTH, outputDirLbl);
        outputLayout.putConstraint(SpringLayout.EAST, outputDirBtn, -3, SpringLayout.EAST, outputTargetPanel);
        
        cobolScrollPane.getViewport().add(cobolEditor);
        cobolScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        SpringLayout mainLayout = new SpringLayout(); 
        mainFrame.setLayout(mainLayout);
        mainFrame.add(formatPanel);
        mainFrame.add(genOptionPanel);
        mainFrame.add(outputTargetPanel);
        mainFrame.add(cobolScrollPane);
        mainFrame.add(doConvertBtn);
        
        // source format panel
        mainLayout.putConstraint(SpringLayout.WEST, formatPanel, 10, SpringLayout.WEST, mainFrame.getContentPane());
        mainLayout.putConstraint(SpringLayout.EAST, formatPanel, 100, SpringLayout.WEST, formatPanel);
        mainLayout.putConstraint(SpringLayout.NORTH, formatPanel, 0, SpringLayout.NORTH, outputTargetPanel);
        mainLayout.putConstraint(SpringLayout.SOUTH, formatPanel, 0, SpringLayout.SOUTH, outputTargetPanel);
        
        // gen options panel
        mainLayout.putConstraint(SpringLayout.WEST, genOptionPanel, 5, SpringLayout.EAST, formatPanel);
        mainLayout.putConstraint(SpringLayout.NORTH, genOptionPanel, 0, SpringLayout.NORTH, outputTargetPanel);
        mainLayout.putConstraint(SpringLayout.SOUTH, genOptionPanel, 0, SpringLayout.SOUTH, outputTargetPanel);
        
        // output target panel
        mainLayout.putConstraint(SpringLayout.NORTH, outputTargetPanel, 5, SpringLayout.NORTH, mainFrame.getContentPane());
        mainLayout.putConstraint(SpringLayout.WEST, outputTargetPanel, 5, SpringLayout.EAST, genOptionPanel);
        mainLayout.putConstraint(SpringLayout.EAST, outputTargetPanel, 325, SpringLayout.WEST, outputTargetPanel);
        mainLayout.putConstraint(SpringLayout.SOUTH, outputTargetPanel, 100, SpringLayout.NORTH, outputTargetPanel);
        
        // convert button
        mainLayout.putConstraint(SpringLayout.WEST, doConvertBtn, 10, SpringLayout.EAST, outputTargetPanel);
        mainLayout.putConstraint(SpringLayout.NORTH, doConvertBtn, 5, SpringLayout.NORTH, outputTargetPanel);
        mainLayout.putConstraint(SpringLayout.SOUTH, doConvertBtn, -3, SpringLayout.SOUTH, outputTargetPanel);
        mainLayout.putConstraint(SpringLayout.EAST, doConvertBtn, 100, SpringLayout.WEST, doConvertBtn);
        
        // editor pane
        mainLayout.putConstraint(SpringLayout.NORTH, cobolScrollPane, 10, SpringLayout.SOUTH, outputTargetPanel);
        mainLayout.putConstraint(SpringLayout.WEST, cobolScrollPane, 10, SpringLayout.WEST, mainFrame.getContentPane());
        mainLayout.putConstraint(SpringLayout.EAST, cobolScrollPane, -10, SpringLayout.EAST, mainFrame.getContentPane());
        mainLayout.putConstraint(SpringLayout.SOUTH, cobolScrollPane, -10, SpringLayout.SOUTH, mainFrame.getContentPane());
    }

    private void addListener() {
        openItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });
        
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.dispose();
                System.exit(0);
            }
        });
        
        outputDirBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File tmp = new File(outputDirTxt.getText());
                if (tmp.exists())
                    outputDirChooser.setSelectedFile(tmp);
                if (outputDirChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    try {
                        outputDirTxt.setText(outputDirChooser.getSelectedFile().getCanonicalPath());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            
        });
    
        doConvertBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doConvert();
            }
            
        });
    }

    private void openFile() {
        if (openFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            openedFile = openFileChooser.getSelectedFile();
            try {
                cobolEditor.read(new FileInputStream(openedFile), null);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    private void doConvert() {
        if (openedFile == null) {
            JOptionPane.showMessageDialog(mainFrame, "Please open a file to convert", "File not selected", JOptionPane.ERROR_MESSAGE);
            return;
        }

        RESConfig config = instance.getConfig();
        instance.setSourceFormat(fixFormatRadio.isSelected());
        instance.setGenOption(genJavaCheckbox.isSelected());
        config.setOutputDir(outputDirTxt.getText());
        config.setDataPackage(dataDirTxt.getText());
        config.setProgramPackage(programDirTxt.getText());
        new Runnable() {
            @Override
            public void run() {
                try {
                    mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    instance.execute(openedFile);
                    JOptionPane.showMessageDialog(mainFrame, "Finish");
                    mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    if (javaCodeBrowser != null) {
                        javaCodeBrowser.dispose();
                    }
                    javaCodeBrowser = new OutputCodeBrowser(new File(outputDirTxt.getText()));
                    javaCodeBrowser.setVisible(true);
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Parse exception", JOptionPane.ERROR_MESSAGE);
                } catch (ErrorInCobolSourceException e) {
                    JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Syntax error", JOptionPane.ERROR_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "I/O exception", JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        }.run();
    }
    
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException {
        new Demo().display();
    }
}
