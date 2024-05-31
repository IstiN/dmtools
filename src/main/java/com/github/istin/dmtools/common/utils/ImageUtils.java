package com.github.istin.dmtools.common.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

public class ImageUtils {

    public static String getExtension(File file) {
        // Get the file extension
        String extension = "";
        String fileName = file.getName();
        int i = fileName.lastIndexOf('.');
        if (i >= 0) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }
    public static String convertToBase64(File imageFile, String formatName) throws IOException {
        // Read the image from the file
        BufferedImage image = ImageIO.read(imageFile);
        // Convert the image to a base64 string
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, formatName, output);
        return Base64.getEncoder().encodeToString(output.toByteArray());
    }

    public static int[] getImageDimensions(File imageFile) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        return new int[] {width, height};
    }

}
