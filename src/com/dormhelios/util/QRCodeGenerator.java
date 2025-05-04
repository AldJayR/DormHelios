package com.dormhelios.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO; // If generating byte array

/**
 * Utility class for generating QR Code images using the ZXing library.
 */
public class QRCodeGenerator {

    private static final Logger LOGGER = Logger.getLogger(QRCodeGenerator.class.getName());

    /**
     * Private constructor to prevent instantiation.
     */
    private QRCodeGenerator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Generates a QR Code image for the given text data.
     *
     * @param text   The text data to encode in the QR code. Cannot be null or empty.
     * @param width  The desired width of the QR code image in pixels.
     * @param height The desired height of the QR code image in pixels.
     * @return A BufferedImage representing the QR code, or null if generation fails.
     */
    public static BufferedImage generateQRCodeImage(String text, int width, int height) {
        if (text == null || text.trim().isEmpty()) {
            LOGGER.warning("Cannot generate QR code for null or empty text.");
            return null;
        }
        if (width <= 0 || height <= 0) {
             LOGGER.warning("Cannot generate QR code with zero or negative dimensions.");
             return null;
        }

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        // Configure encoding hints (optional but recommended)
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L); // Error correction level (L, M, Q, H)
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8"); // Ensure proper character encoding
        hints.put(EncodeHintType.MARGIN, 1); // Set margin around QR code (e.g., 1 module)

        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
            // Convert BitMatrix to BufferedImage
            return MatrixToImageWriter.toBufferedImage(bitMatrix);

        } catch (WriterException e) {
            LOGGER.log(Level.SEVERE, "Could not generate QR Code image due to WriterException", e);
        } catch (Exception e) {
            // Catch unexpected errors
            LOGGER.log(Level.SEVERE, "An unexpected error occurred during QR Code generation", e);
        }
        return null; // Return null on failure
    }

    /**
     * Generates a QR Code image and returns it as a byte array (e.g., for saving to DB or file).
     *
     * @param text   The text data to encode.
     * @param width  The desired width.
     * @param height The desired height.
     * @param format The image format (e.g., "PNG", "JPG").
     * @return A byte array containing the image data, or null on failure.
     */
    public static byte[] generateQRCodeByteArray(String text, int width, int height, String format) {
         BufferedImage image = generateQRCodeImage(text, width, height);
         if (image == null) {
             return null;
         }

         try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
             boolean written = ImageIO.write(image, format, baos);
             if (!written) {
                  LOGGER.log(Level.SEVERE, "Could not find writer for image format: " + format);
                  return null;
             }
             baos.flush();
             return baos.toByteArray();
         } catch (IOException e) {
              LOGGER.log(Level.SEVERE, "Could not write QR Code image to byte array", e);
              return null;
         }
    }

    // --- Example Usage (Can be run standalone for testing) ---
    /*
    public static void main(String[] args) {
        String testData = "Tenant:Juan Dela Cruz\nRoom:A101\nDate:2024-01-15\nAmount:5000.00\nPeriod:Jan 2024";
        int width = 200;
        int height = 200;

        BufferedImage qrImage = generateQRCodeImage(testData, width, height);

        if (qrImage != null) {
            // Display in a simple JFrame for testing
            javax.swing.JFrame frame = new javax.swing.JFrame("QR Code Test");
            frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new javax.swing.JLabel(new javax.swing.ImageIcon(qrImage)));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            System.out.println("QR Code generated successfully.");

            // Test byte array generation
            // byte[] pngBytes = generateQRCodeByteArray(testData, width, height, "PNG");
            // if (pngBytes != null) {
            //     System.out.println("PNG byte array generated, size: " + pngBytes.length);
            //     // You could save these bytes to a file here
            // } else {
            //      System.err.println("Failed to generate PNG byte array.");
            // }

        } else {
            System.err.println("Failed to generate QR Code image.");
        }
    }
    */
}