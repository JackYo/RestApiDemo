package com.example.restservice.controller;

import com.example.restservice.FileStorageService;
import com.example.restservice.payload.DownloadFileResponse;
import com.example.restservice.payload.FileListResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileStorageService fileStorageService;
    
    @RequestMapping(value = "/file/**", method = RequestMethod.POST,
                produces = MediaType.APPLICATION_JSON_VALUE)
    public DownloadFileResponse uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String pathStr = request.getRequestURI().split(request.getContextPath() + "/file/")[1];
        if (fileStorageService.isFilePathExists(pathStr)) {
            throw new ResponseStatusException(HttpStatus.CREATED, "file existed");
        } else {
            String fileName = fileStorageService.storeFile(pathStr, file);
    
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/file/")
                    .path(fileName)
                    .toUriString();
    
            return new DownloadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
        }
    }

    @RequestMapping(value = "/file/**", method = RequestMethod.PATCH,
                produces = MediaType.APPLICATION_JSON_VALUE)
    public DownloadFileResponse patchFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String pathStr = request.getRequestURI().split(request.getContextPath() + "/file/")[1];
        if (fileStorageService.isFilePathExists(pathStr)) {
            String fileName = fileStorageService.storeFile(pathStr, file);
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/file/")
                    .path(fileName)
                    .toUriString();

            return new DownloadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "file not exists");
        }
    }

    @RequestMapping(value = "/file/**", method = RequestMethod.DELETE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> patchFile(HttpServletRequest request) {
        String pathStr = request.getRequestURI().split(request.getContextPath() + "/file/")[1];
        boolean isSuccess = fileStorageService.deleteFile(pathStr);
        if (isSuccess) {
            return ResponseEntity.ok().body("Delete successed!");
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "file not exists");
        }
    }

    @RequestMapping(value = "/file/**", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> getFileOrFileList(HttpServletRequest request,
            @RequestParam(value = "orderBy", defaultValue = "lastModified") String orderBy,
            @RequestParam(value = "orderByDirection", defaultValue = "Descending") String orderByDirection,
            @RequestParam(value = "filterByName", required = false) String filterByName) {
        String pathStr = request.getRequestURI().split(request.getContextPath() + "/file/")[1];
        if (fileStorageService.isFilePathDirectory(pathStr)) {
            List<File> files = fileStorageService.getFileList(pathStr, orderBy, orderByDirection, filterByName);
            FileListResponse res = new FileListResponse(files);
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(res);
        } else {
            if (!fileStorageService.isFilePathExists(pathStr)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "file not exists");
            }
            // Load file as Resource
            Resource resource = fileStorageService.loadFileAsResource(pathStr);
            // Try to determine file's content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                logger.info("Could not determine file type.");
            }
    
            // Fallback to the default content type if type could not be determined
            if(contentType == null) {
                contentType = "application/octet-stream";
            }
    
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
        }
    }

    // @PostMapping("/uploadMultipleFiles")
    // public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
    //     return Arrays.asList(files)
    //             .stream()
    //             .map(file -> uploadFile(file))
    //             .collect(Collectors.toList());
    // }
    
}