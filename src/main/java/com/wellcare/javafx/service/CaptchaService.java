package com.wellcare.javafx.service;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.Random;

/**
 * Service to generate and validate security CAPTCHAs
 */
public class CaptchaService {
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Removed similar chars like 0, O, 1, I
    private String currentCode;
    private final Random random = new Random();

    public String generateCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        currentCode = sb.toString();
        return currentCode;
    }

    public Image generateCaptchaImage(double width, double height) {
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // 1. Background
        gc.setFill(randomColor(0.9, 1.0)); // Light pastel background
        gc.fillRect(0, 0, width, height);

        // 2. Noise - Lines
        gc.setLineWidth(1);
        for (int i = 0; i < 8; i++) {
            gc.setStroke(randomColor(0.5, 0.8));
            gc.strokeLine(random.nextDouble() * width, random.nextDouble() * height,
                          random.nextDouble() * width, random.nextDouble() * height);
        }

        // 3. Characters
        double charSpacing = width / (currentCode.length() + 1);
        for (int i = 0; i < currentCode.length(); i++) {
            String c = String.valueOf(currentCode.charAt(i));
            
            // Random font size 28-36px
            double fontSize = 28 + random.nextDouble() * 8;
            gc.setFont(Font.font("System", FontWeight.BOLD, fontSize));
            
            // Random color
            gc.setFill(randomColor(0.0, 0.5)); // Darker colors for text
            
            // Random rotation -20 to +20 degrees
            double angle = -20 + random.nextDouble() * 40;
            
            gc.save();
            double x = (i + 0.5) * charSpacing;
            double y = height / 2 + (random.nextDouble() * 10 - 5);
            
            gc.translate(x, y);
            gc.rotate(angle);
            gc.fillText(c, 0, 0);
            gc.restore();
        }

        // 4. Noise - Dots
        for (int i = 0; i < 50; i++) {
            gc.setFill(randomColor(0.4, 0.7));
            gc.fillOval(random.nextDouble() * width, random.nextDouble() * height, 2, 2);
        }

        WritableImage img = new WritableImage((int)width, (int)height);
        canvas.snapshot(null, img);
        return img;
    }

    public boolean validate(String input) {
        if (input == null || currentCode == null) return false;
        return input.equalsIgnoreCase(currentCode);
    }

    private Color randomColor(double minBrightness, double maxBrightness) {
        double r = minBrightness + random.nextDouble() * (maxBrightness - minBrightness);
        double g = minBrightness + random.nextDouble() * (maxBrightness - minBrightness);
        double b = minBrightness + random.nextDouble() * (maxBrightness - minBrightness);
        return Color.color(r, g, b);
    }

    public String getCurrentCode() {
        return currentCode;
    }
}
