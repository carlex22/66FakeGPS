package com.carlex.drive;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


import android.os.Environment;

public class JsonFileHandler {

    private final String directoryPath;
    private final String fileName;
    
    
    public JsonFileHandler(String directoryPath, String fileName) {
        this.directoryPath = directoryPath;
        this.fileName = fileName;
    }

    public boolean createDirectoryAndFileIfNotExists() {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(Environment.getExternalStorageDirectory(), fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean saveJson(String jsonString) {
        File file = new File(Environment.getExternalStorageDirectory(), fileName);
   
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(jsonString);
            setFilePermissions(file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setFilePermissions(File file) {
        file.setReadable(true, false); // true for owner only, false for all
        file.setWritable(true, false);
        file.setExecutable(true, false);
    }
}
