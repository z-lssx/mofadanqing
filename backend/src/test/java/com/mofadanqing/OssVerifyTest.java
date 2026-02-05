package com.mofadanqing;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;

public class OssVerifyTest {

    @Test
    public void testOssUpload() {
        String endpoint = "oss-cn-beijing.aliyuncs.com";
        String accessKeyId = System.getenv("ALIYUN_OSS_ACCESS_KEY_ID");
        String accessKeySecret = System.getenv("ALIYUN_OSS_ACCESS_KEY_SECRET");
        String bucketName = "tzbmofadanqing";

        if (accessKeyId == null || accessKeySecret == null) {
            System.out.println("Skipping OSS test because environment variables are not set.");
            return;
        }

        System.out.println("Starting OSS verification...");
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            String objectName = "test_verify_" + System.currentTimeMillis() + ".txt";
            String content = "Hello OSS from Verification Test";
            
            // Upload
            ossClient.putObject(new PutObjectRequest(bucketName, objectName, new ByteArrayInputStream(content.getBytes())));
            System.out.println("Upload successful: " + objectName);

            // Check existence
            boolean exists = ossClient.doesObjectExist(bucketName, objectName);
            if (exists) {
                System.out.println("File exists verification passed.");
            } else {
                throw new RuntimeException("File should exist but does not.");
            }

            // Delete
            ossClient.deleteObject(bucketName, objectName);
            System.out.println("Delete successful.");
            System.out.println("OSS Verification Passed!");
        } catch (Exception e) {
            System.err.println("OSS Verification Failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
