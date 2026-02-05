package com.mofadanqing.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.mofadanqing.service.OssService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class OssServiceImpl implements OssService {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Override
    public String uploadFile(MultipartFile file, String dir) {
        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            InputStream inputStream = file.getInputStream();
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            
            // 生成文件名: dir/yyyy/MM/dd/uuid.ext
            String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            String fileName = (dir != null && !dir.isEmpty() ? dir + "/" : "") + datePath + "/" + UUID.randomUUID().toString().replace("-", "") + extension;

            // 创建PutObjectRequest对象
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream);
            
            // 上传文件
            ossClient.putObject(putObjectRequest);

            // 返回文件URL
            // URL格式: https://bucketName.endpoint/fileName
            String url = "https://" + bucketName + "." + endpoint + "/" + fileName;
            return url;

        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public void deleteFile(String url) {
        // 从URL中解析出objectName
        // URL格式: https://bucketName.endpoint/objectName
        try {
            String host = "https://" + bucketName + "." + endpoint + "/";
            if (url.startsWith(host)) {
                String objectName = url.substring(host.length());
                
                OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
                try {
                    ossClient.deleteObject(bucketName, objectName);
                } finally {
                    if (ossClient != null) {
                        ossClient.shutdown();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String uploadFromUrl(String url, String dir) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // System.out.println("Starting download from URL: " + url);
            // Download from URL with timeout
            java.net.URL sourceUrl = new java.net.URL(url);
            java.net.URLConnection conn = sourceUrl.openConnection();
            conn.setConnectTimeout(10000); // 10s timeout
            conn.setReadTimeout(30000);    // 30s read timeout
            InputStream inputStream = conn.getInputStream();
            
            // Determine extension (default to .png if not found)
            String extension = ".png";
            if (url.contains(".")) {
                // Remove query params first
                String path = sourceUrl.getPath();
                if (path != null && path.contains(".")) {
                    extension = path.substring(path.lastIndexOf("."));
                }
            }
            
            // Generate Filename
            String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            String fileName = (dir != null && !dir.isEmpty() ? dir + "/" : "") + datePath + "/" + UUID.randomUUID().toString().replace("-", "") + extension;
            
            // System.out.println("Uploading to OSS: " + fileName);
            // Upload
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream);
            ossClient.putObject(putObjectRequest);
            
            String ossUrl = "https://" + bucketName + "." + endpoint + "/" + fileName;
            // System.out.println("Upload success: " + ossUrl);
            return ossUrl;
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to upload from URL: " + url + " Error: " + e.getMessage());
            throw new RuntimeException("Failed to upload image from URL to OSS: " + e.getMessage(), e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
