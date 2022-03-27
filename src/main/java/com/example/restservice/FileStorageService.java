package com.example.restservice;

import com.example.restservice.exception.FileStorageException;
import com.example.restservice.exception.MyFileNotFoundException;
import com.example.restservice.property.FileStorageProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public boolean isFilePathDirectory(String filePath) {
        Path targetLocation = this.fileStorageLocation.resolve(filePath);
        return Files.isDirectory(targetLocation);
    }
    
    public boolean isFilePathExists(String filePath) {
        Path targetLocation = this.fileStorageLocation.resolve(filePath);
        return Files.exists(targetLocation);
    }

    public List<File> getFileList(String filePath, String orderBy, String orderByDirection, String filterByName) {
        // Reading only files in the directory(filePath)
        boolean isAscending = orderByDirection.equals("Ascending");
        boolean isDescending = orderByDirection.equals("Descending");
        Path targetLocation = this.fileStorageLocation.resolve(filePath);
        try {
            List<File> files = Files.list(targetLocation)
                .map(Path::toFile)
                .filter(File::isFile)
                .filter(file -> filterByName != null ? file.getName().toString().toLowerCase().contains(filterByName.toLowerCase()): true)
                .sorted(new Comparator<File>() { 
                    @Override
                    public int compare(File t1, File t2) {
                        if (!isAscending && !isDescending) {
                            return 0;
                        }
                        switch (orderBy) {
                            case "size":
                                if (isAscending) {
                                    return t1.length() - t2.length() > 0 ? 1: -1;
                                } else {
                                    return t1.length() - t2.length() > 0 ? -1: 1;
                                }
                            case "fileName":
                                if (isAscending) {
                                    return t1.getName().compareTo(t2.getName());
                                } else {
                                    return -t1.getName().compareTo(t2.getName());
                                }
                            case "lastModified":
                            default:
                                if (isAscending) {
                                    return t1.lastModified() - t2.lastModified() > 0 ? 1 : -1;
                                } else {
                                    return t1.lastModified() - t2.lastModified() > 0 ? -1 : 1;
                                }
                        }
                        
                    }
                })
                .collect(Collectors.toList());
        
            // files.forEach(System.out::println);
            // System.out.println("===");
            return files;
        } catch (IOException ex) {
            throw new FileStorageException("Could not list files under path " + filePath + ". Please try again!", ex);
        }
    }
    
    // public boolean autoMakirDir(String filePath) {
    //     Path targetLocation = this.fileStorageLocation.resolve(filePath);
    //     return !Files.createFile(targetLocation);
    // }

    public String storeFile(String filePath, MultipartFile file) {
        // Normalize file name
        // String filePath = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            // Check if the file's name contains invalid characters
            if(filePath.contains("..")) {
                throw new FileStorageException("Sorry! File path contains invalid sequence " + filePath);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(filePath);
            Files.createDirectories(targetLocation.getParent());
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return filePath;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + filePath + ". Please try again!", ex);
        }
    }

    public boolean deleteFile(String filePath) {
        Path targetLocation = this.fileStorageLocation.resolve(filePath);
        try {
            return Files.deleteIfExists(targetLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file " + filePath + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String filePath) {
        try {
            Path fileFullPath = this.fileStorageLocation.resolve(filePath).normalize();
            Resource resource = new UrlResource(fileFullPath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + filePath);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + filePath, ex);
        }
    }
}