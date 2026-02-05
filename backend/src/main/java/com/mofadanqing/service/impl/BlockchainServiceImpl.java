package com.mofadanqing.service.impl;

import com.mofadanqing.service.BlockchainService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BlockchainServiceImpl implements BlockchainService {

    @Override
    public String registerDigitalAsset(String content, String ownerId) {
        try {
            // 1. Prepare data payload
            String timestamp = LocalDateTime.now().toString();
            String payload = content + "|" + ownerId + "|" + timestamp + "|" + UUID.randomUUID().toString();
            
            // 2. Generate SHA-256 Hash
            String hash = sha256(payload);
            
            // 3. Simulate "On-Chain" storage
            // In a real system, we would invoke the Hyperledger Fabric SDK here.
            // For now, we log it and return the hash as the "Certificate ID".
            System.out.println("Blockchain Transaction Recorded: " + hash);
            System.out.println("Payload: " + payload);
            
            return hash;
            
        } catch (Exception e) {
            throw new RuntimeException("Blockchain registration failed", e);
        }
    }

    private String sha256(String originalString) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(originalString.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encodedhash);
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
