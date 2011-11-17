package com.res.java.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.res.common.RESConfig;
import com.res.java.translation.symbol.SymbolProperties;

public class FileUtil {
    private static RESConfig config = RESConfig.getInstance();
    
    public static String getJavaFileName(SymbolProperties props) {
        return props.getJavaName2() + ".java";
    }
    
    public static String getDataPackagePath() {
        return config.getOutputDir() + File.separatorChar + config.getDataPackage().replace('.', File.separatorChar);
    }
    
    public static String getProgramPackagePath() {
        return config.getOutputDir() + File.separatorChar + config.getProgramPackage().replace('.', File.separatorChar);
    }
    
    public static String getDataFilePath(String fileName) {
        return getDataPackagePath() + File.separatorChar + fileName;
    }
    
    public static String getProgramFilePath(String fileName) {
        return getProgramPackagePath() + File.separatorChar + fileName;
    }
    
    public static void createOutputDirectory() throws IOException {
        createDirectories(config.getOutputDir());
    }
    
    public static void createDataPackageDirectory() throws IOException {
        createDirectories(getDataPackagePath());
    }
    
    public static void createProgramPackageDirectory() throws IOException {
        createDirectories(getProgramPackagePath());
    }
    
    public static FileOutputStream newDataFile(String fileName) throws IOException {
        createDataPackageDirectory();
        return new FileOutputStream(getDataFilePath(fileName));
    }
    
    public static FileOutputStream newProgramFile(String fileName) throws IOException {
        createProgramPackageDirectory();
        return new FileOutputStream(getProgramFilePath(fileName));
    }
    
    private static void createDirectories(String path) throws IOException {
        File f = new File(path);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                throw new IOException("Cannot create directories for path: " + f.getCanonicalPath());
            }
        }
    }
}
