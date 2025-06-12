package com.github.istin.dmtools.robot;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KeyboardSimulator {
    private static final Logger logger = LoggerFactory.getLogger(KeyboardSimulator.class);
    private static final int DEFAULT_DELAY = 100;
    private static final int REGION_WIDTH = 150;
    private static final int REGION_HEIGHT = 50;
    private static final double SCREEN_SCALE = getScreenScale();

    private final Robot robot;
    private Tesseract tesseract;
    private boolean ocrAvailable;

    public KeyboardSimulator() throws AWTException {
        this.robot = new Robot();
        this.robot.setAutoDelay(DEFAULT_DELAY);
        initializeTesseract();
    }

    private static double getScreenScale() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        AffineTransform tx = gc.getDefaultTransform();
        return tx.getScaleX();
    }

    private static Rectangle getScreenBounds() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        return gc.getBounds();
    }

    private void initializeTesseract() {
        try {
            System.setProperty("jna.library.path", "/opt/homebrew/lib");
            tesseract = new Tesseract();
            String tessdataPath = "/opt/homebrew/share/tessdata";
            File tessdataDir = new File(tessdataPath);
            if (!tessdataDir.exists()) {
                logger.info("Creating tessdata directory at: {}", tessdataPath);
                tessdataDir.mkdirs();
            }

            File engTrainedData = new File(tessdataDir, "eng.traineddata");
            if (!engTrainedData.exists()) {
                logger.info("Downloading English language data...");
                downloadTrainedData(engTrainedData);
            }

            tesseract.setDatapath(tessdataPath);
            tesseract.setLanguage("eng");
            tesseract.setPageSegMode(3);
            tesseract.setOcrEngineMode(1);
            tesseract.setTessVariable("debug_file", "/dev/null");

            BufferedImage testImage = createTestImage();
            String testResult = tesseract.doOCR(testImage);
            ocrAvailable = true;
            logger.info("Tesseract initialized with test result: {}", testResult.trim());
        } catch (Exception e) {
            logger.error("Failed to initialize Tesseract", e);
            ocrAvailable = false;
        }
    }

    private void downloadTrainedData(File destination) throws IOException {
        String url = "https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata";
        ProcessBuilder pb = new ProcessBuilder("curl", "-L", "-o", destination.getAbsolutePath(), url);
        Process p = pb.start();
        try {
            int exitCode = p.waitFor();
            if (exitCode != 0) {
                throw new IOException("Failed to download trained data: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Download interrupted", e);
        }
    }

    private BufferedImage preprocessImage(BufferedImage input) {
        BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = output.createGraphics();
        g2d.drawImage(input, 0, 0, null);
        g2d.dispose();

        float[] sharpen = {
                0.0f, -1.0f, 0.0f,
                -1.0f, 5.0f, -1.0f,
                0.0f, -1.0f, 0.0f
        };
        ConvolveOp op = new ConvolveOp(new Kernel(3, 3, sharpen));
        output = op.filter(output, null);

        for (int x = 0; x < output.getWidth(); x++) {
            for (int y = 0; y < output.getHeight(); y++) {
                int pixel = output.getRGB(x, y) & 0xFF;
                int newPixel = (pixel < 128) ? 0 : 255;
                output.setRGB(x, y, (newPixel << 16) | (newPixel << 8) | newPixel);
            }
        }

        BufferedImage scaled = new BufferedImage(
                input.getWidth() * 2,
                input.getHeight() * 2,
                BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(output, 0, 0, scaled.getWidth(), scaled.getHeight(), null);
        g.dispose();

        return scaled;
    }

    private BufferedImage createTestImage() {
        BufferedImage image = new BufferedImage(100, 30, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 100, 30);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("TEST", 10, 20);
        g.dispose();
        return image;
    }

    public Optional<Point> findTextOnScreen(String text) {
        if (!ocrAvailable || text == null || text.isEmpty()) {
            return Optional.empty();
        }

        try {
            Rectangle screenRect = getScreenBounds();
            BufferedImage screenshot = robot.createScreenCapture(screenRect);

            File debugFile = new File("debug_screenshot.png");
            ImageIO.write(screenshot, "PNG", debugFile);
            logger.info("Screenshot dimensions: {}x{}", screenshot.getWidth(), screenshot.getHeight());

            int overlap = 25;

            for (int y = 0; y < screenRect.height - REGION_HEIGHT; y += REGION_HEIGHT - overlap) {
                for (int x = 0; x < screenRect.width - REGION_WIDTH; x += REGION_WIDTH - overlap) {
                    BufferedImage region = screenshot.getSubimage(x, y, REGION_WIDTH, REGION_HEIGHT);
                    BufferedImage processed = preprocessImage(region);
                    String result = tesseract.doOCR(processed).trim();

                    if (result.contains(text)) {
                        Point center = new Point(
                                x + REGION_WIDTH/2,
                                y + REGION_HEIGHT/2
                        );

                        File regionFile = new File(String.format("found_region_%d_%d.png", x, y));
                        ImageIO.write(region, "PNG", regionFile);

                        logger.info("Found text '{}' at screen coordinates: {}", text, center);
                        return Optional.of(center);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to find text: {}", text, e);
        }
        return Optional.empty();
    }

    public boolean clickOnText(String text) {
        Optional<Point> location = findTextOnScreen(text);
        if (location.isPresent()) {
            Point point = location.get();
            logger.info("Clicking at: {}", point);
            clickAt(point);
            return true;
        }
        return false;
    }

    public void clickAt(Point point) {
        try {
            // Convert from scaled coordinates to actual screen coordinates
            int actualX = (int)(point.x / SCREEN_SCALE);
            int actualY = (int)(point.y / SCREEN_SCALE);

            logger.info("Moving mouse from scaled ({},{}) to actual ({},{})",
                    point.x, point.y, actualX, actualY);

            Point beforeClick = MouseInfo.getPointerInfo().getLocation();
            takeScreenshot("before_click.png");

            robot.mouseMove(actualX, actualY);
            robot.delay(100);

            Rectangle captureRect = new Rectangle(actualX - 25, actualY - 25, 50, 50);
            BufferedImage targetArea = robot.createScreenCapture(captureRect);
            ImageIO.write(targetArea, "png", new File("click_target.png"));

            performClick();
            robot.delay(100);

            Point afterClick = MouseInfo.getPointerInfo().getLocation();
            takeScreenshot("after_click.png");

            logger.info("Mouse moved from ({},{}) to ({},{})",
                    beforeClick.x, beforeClick.y, afterClick.x, afterClick.y);

        } catch (Exception e) {
            logger.error("Failed to click at point: " + point, e);
        }
    }

    private void performClick() {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(DEFAULT_DELAY);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public void clickInCenter() {
        Rectangle bounds = getScreenBounds();
        robot.mouseMove(bounds.width / 2, bounds.height / 2);
        performClick();
    }

    public void typeString(String input) {
        if (input == null || input.isEmpty()) return;
        for (char c : input.toCharArray()) {
            typeCharacter(c);
            robot.delay(DEFAULT_DELAY);
        }
    }

    private void typeCharacter(char c) {
        try {
            boolean isUpperCase = Character.isUpperCase(c);
            int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);

            if (keyCode == KeyEvent.VK_UNDEFINED) {
                throw new IllegalArgumentException("No key code for: " + c);
            }

            if (isUpperCase) {
                robot.keyPress(KeyEvent.VK_SHIFT);
            }

            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);

            if (isUpperCase) {
                robot.keyRelease(KeyEvent.VK_SHIFT);
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Cannot type character: {}", c, e);
        }
    }

    public Optional<File> takeScreenshot(String filename) {
        try {
            Rectangle screenRect = getScreenBounds();
            BufferedImage screenCapture = robot.createScreenCapture(screenRect);
            File file = new File(filename);
            ImageIO.write(screenCapture, "png", file);
            return Optional.of(file);
        } catch (IOException e) {
            logger.error("Failed to take screenshot", e);
            return Optional.empty();
        }
    }

    public List<String> getAllTextFromScreen() {
        List<String> textElements = new ArrayList<>();
        if (!ocrAvailable) return textElements;

        try {
            Rectangle screenRect = getScreenBounds();
            BufferedImage screenshot = robot.createScreenCapture(screenRect);
            BufferedImage processed = preprocessImage(screenshot);
            String result = tesseract.doOCR(processed);

            for (String line : result.split("\n")) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    textElements.add(trimmed);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get screen text", e);
        }

        return textElements;
    }

    public void setDelay(int ms) {
        robot.setAutoDelay(ms);
    }

    public boolean isOcrAvailable() {
        return ocrAvailable;
    }

    public static void main(String[] args) {
        try {
            System.out.println("OS: " + System.getProperty("os.name"));
            System.out.println("Architecture: " + System.getProperty("os.arch"));
            System.out.println("Java Version: " + System.getProperty("java.version"));
            System.out.println("Screen Scale: " + getScreenScale());

            Rectangle bounds = getScreenBounds();
            System.out.println("Logical Screen Bounds: " + bounds.width + "x" + bounds.height);
            System.out.println("Scaled Screen Bounds: " +
                    (bounds.width * getScreenScale()) + "x" +
                    (bounds.height * getScreenScale()));

            KeyboardSimulator ks = new KeyboardSimulator();

            if (ks.isOcrAvailable()) {
                System.out.println("\nScanning for text...");
                List<String> textElements = ks.getAllTextFromScreen();
                System.out.println("Found " + textElements.size() + " text elements:");
                textElements.forEach(text -> System.out.println("- " + text));

                String targetText = "File";
                System.out.println("\nSearching for: " + targetText);
                Optional<Point> location = ks.findTextOnScreen(targetText);
                if (location.isPresent()) {
                    Point point = location.get();
                    Point mousePos = MouseInfo.getPointerInfo().getLocation();
                    System.out.println("Found at: " + point.x + ", " + point.y);
                    System.out.println("Mouse before click: " + mousePos.x + ", " + mousePos.y);
                    ks.clickAt(point);
                    ks.robot.delay(500);
                    mousePos = MouseInfo.getPointerInfo().getLocation();
                    System.out.println("Mouse after click: " + mousePos.x + ", " + mousePos.y);
                } else {
                    System.out.println("Text not found");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}