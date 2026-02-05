package com.mofadanqing.controller;

import com.mofadanqing.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private OssService ossService;

    @PostMapping("/image")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        Map<String, Object> result = new HashMap<>();
        
        // 强制使用 OSS 上传，不再支持本地存储或 Mock
        // 确保返回的是阿里云 OSS 的 HTTPS URL
        String url = ossService.uploadFile(file, "designs/layer2");
        result.put("url", url);
        return result;
    }

    @PostMapping("/file")
    public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile file,
                                          @RequestParam(value = "storage", defaultValue = "oss") String storage) throws IOException {
        if ("local".equalsIgnoreCase(storage)) {
            return uploadToLocal(file);
        }

        // Determine directory based on file type
        String contentType = file.getContentType();
        String dir = "files";
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                dir = "images";
            } else if (contentType.startsWith("video/")) {
                dir = "videos";
            }
        }

        String url = ossService.uploadFile(file, dir);
        Map<String, Object> result = new HashMap<>();
        result.put("url", url);
        return result;
    }

    private Map<String, Object> uploadToLocal(MultipartFile file) throws IOException {
        Map<String, Object> result = new HashMap<>();
        Path uploadDir = Paths.get(System.getProperty("user.dir"), "backend", "uploads");
        Files.createDirectories(uploadDir);
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path target = uploadDir.resolve(filename);
        file.transferTo(target.toFile());
        String accessUrl = "/api/upload/file/" + filename;
        result.put("url", accessUrl);
        return result;
    }

    @GetMapping("/file/{filename}")
    public ResponseEntity<FileSystemResource> getFile(@PathVariable String filename) throws IOException {
        Path filePath = Paths.get(System.getProperty("user.dir"), "backend", "uploads", filename);
        File file = filePath.toFile();
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        String contentType = Files.probeContentType(filePath);
        return ResponseEntity.ok()
                .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(file));
    }
}
