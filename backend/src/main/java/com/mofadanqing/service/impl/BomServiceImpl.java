package com.mofadanqing.service.impl;

import com.mofadanqing.service.BomService;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Service
public class BomServiceImpl implements BomService {

    private static final BigDecimal HAIR_UNIT_COST = new BigDecimal("0.5"); // Cost per meter of hair
    private static final BigDecimal SILK_UNIT_COST = new BigDecimal("2.0"); // Cost per gram of silk
    private static final BigDecimal BASE_FEE = new BigDecimal("100.0"); // Base craftsmanship fee

    @Override
    public Map<String, BigDecimal> calculateBom(String imageUrl) {
        Map<String, BigDecimal> bomData = new HashMap<>();
        
        try {
            // In a real scenario, we would download the image. 
            // For now, we simulate the calculation or try to read if accessible.
            // Since the URL might be external (DashScope), we'll try to read it.
            // If it fails (e.g. network issues), we'll fallback to a mock calculation based on random or fixed logic.
            
            BufferedImage image = null;
            try {
                if (imageUrl != null && !imageUrl.startsWith("http")) {
                     // Local file or mock
                } else if (imageUrl != null) {
                    image = ImageIO.read(new URL(imageUrl));
                }
            } catch (Exception e) {
                // Ignore download errors for now and use simulation
                System.out.println("Could not download image for BOM calc, using simulation: " + e.getMessage());
            }

            long totalPixels = 0;
            long darkPixels = 0;
            long colorPixels = 0;

            if (image != null) {
                int width = image.getWidth();
                int height = image.getHeight();
                totalPixels = width * height;

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int rgb = image.getRGB(x, y);
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = (rgb) & 0xFF;

                        // Calculate brightness
                        double brightness = 0.299 * r + 0.587 * g + 0.114 * b;
                        
                        // Calculate saturation (simplified)
                        int max = Math.max(r, Math.max(g, b));
                        int min = Math.min(r, Math.min(g, b));
                        double saturation = (max == 0) ? 0 : (double) (max - min) / max;

                        if (brightness < 100) {
                            darkPixels++; // Hair candidate
                        } else if (saturation > 0.3) {
                            colorPixels++; // Silk candidate
                        }
                    }
                }
            } else {
                // Simulation values
                totalPixels = 1024 * 1024;
                darkPixels = (long) (totalPixels * 0.3);
                colorPixels = (long) (totalPixels * 0.5);
            }

            // Convert pixels to physical units (Simulation logic)
            // Assume 1000 pixels = 1 cm hair
            BigDecimal hairLength = new BigDecimal(darkPixels).divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);
            
            // Assume 10000 pixels = 1g silk
            BigDecimal silkWeight = new BigDecimal(colorPixels).divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP);

            BigDecimal cost = hairLength.multiply(HAIR_UNIT_COST)
                    .add(silkWeight.multiply(SILK_UNIT_COST))
                    .add(BASE_FEE);

            bomData.put("hairLength", hairLength);
            bomData.put("silkWeight", silkWeight);
            bomData.put("estimatedCost", cost.setScale(2, RoundingMode.HALF_UP));

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback
            bomData.put("hairLength", BigDecimal.ZERO);
            bomData.put("silkWeight", BigDecimal.ZERO);
            bomData.put("estimatedCost", BASE_FEE);
        }

        return bomData;
    }
}
