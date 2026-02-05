package com.mofadanqing.service;

import org.springframework.web.multipart.MultipartFile;

public interface OssService {
    /**
     * 上传文件到OSS
     * @param file 文件
     * @param dir 目录（可选）
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String dir);

    /**
     * 删除文件
     * @param url 文件URL
     */
    void deleteFile(String url);

    /**
     * Upload file from URL (download and re-upload to OSS)
     * @param url Source URL
     * @param dir Target directory
     * @return New OSS URL
     */
    String uploadFromUrl(String url, String dir);
}
