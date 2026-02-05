package com.mofadanqing.service;

public interface BlockchainService {
    /**
     * Register a digital asset on the blockchain (simulated)
     * @param content The content to hash (e.g., prompt + style + timestamp)
     * @param ownerId The owner's user ID
     * @return The transaction hash or certificate ID
     */
    String registerDigitalAsset(String content, String ownerId);
}
