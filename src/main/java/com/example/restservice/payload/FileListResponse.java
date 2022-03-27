package com.example.restservice.payload;

import java.io.File;
import java.util.List;

public class FileListResponse {
    private List<File> files;
    private boolean isDirectory = true;

    public FileListResponse(List<File> files) {
        this.files = files;
    }

    // Getters and Setters
    public String[] getFiles() {
        String[] output = new String[files.size()];
        int i = -1;
        for (File f: files) {
            i++;
            output[i] = f.getName();
        }
        return output;
    }

    public boolean getIsDirectory() {
        return isDirectory;
    }
}