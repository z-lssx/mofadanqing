package com.mofadanqing.utils;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;

public class ImageUtils {

    /**
     * Merges two images with specified opacities.
     *
     * @param layer1Url URL of the bottom layer (Skeleton/Hair)
     * @param layer2Url URL of the top layer (Color/Silk)
     * @param opacity1  Opacity of layer 1 (0.0 - 1.0)
     * @param opacity2  Opacity of layer 2 (0.0 - 1.0)
     * @return InputStream of the merged image (JPEG)
     * @throws IOException if reading/writing images fails
     */
    public static InputStream mergeImages(String layer1Url, String layer2Url, float opacity1, float opacity2) throws IOException {
        // 1. Download images
        BufferedImage img1 = ImageIO.read(new URL(layer1Url));
        BufferedImage img2 = ImageIO.read(new URL(layer2Url));

        if (img1 == null || img2 == null) {
            throw new IOException("Failed to load source images");
        }

        // 2. Determine canvas size (use larger dimensions or standard)
        int width = Math.max(img1.getWidth(), img2.getWidth());
        int height = Math.max(img1.getHeight(), img2.getHeight());

        // 3. Create a new image with white background
        BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = combined.createGraphics();

        // Fill white background
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, width, height);

        // 4. Draw Layer 1 (Hair) with opacity
        if (opacity1 > 0) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity1));
            g.drawImage(img1, 0, 0, width, height, null);
        }

        // 5. Draw Layer 2 (Silk) with opacity (and Multiply blend mode if possible, but standard alpha is safer for basic implementation)
        // Note: Java 2D doesn't have a built-in "Multiply" composite easily accessible without custom logic.
        // For compatibility, we'll stick to standard Alpha blending which simulates the visual effect well enough for preview.
        if (opacity2 > 0) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity2));
            g.drawImage(img2, 0, 0, width, height, null);
        }

        g.dispose();

        // 6. Convert to InputStream
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(combined, "jpg", os);
        return new ByteArrayInputStream(os.toByteArray());
    }
}
