package com.mofadanqing.service;

import java.math.BigDecimal;
import java.util.Map;

public interface BomService {
    /**
     * Calculate BOM based on the generated image
     * @param imageUrl The URL of the generated image
     * @return A map containing hairLength, silkWeight, and estimatedCost
     */
    Map<String, BigDecimal> calculateBom(String imageUrl);
}
