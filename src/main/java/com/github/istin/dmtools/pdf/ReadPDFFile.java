package com.github.istin.dmtools.pdf;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ReadPDFFile {
    private static final Logger logger = LogManager.getLogger(ReadPDFFile.class);
    public static void main(String[] args) {

        String folderPath = "path/to/pdf_files_folder";
        parsePdfFilesToTickets(folderPath);

    }

    public static void parsePdfFilesToTickets(String folderPath) {
        logger.info(folderPath);
        File[] listOfFiles = new File(folderPath).listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            File file = listOfFiles[i];
            if (file.isFile() && file.getName().endsWith(".pdf")) {
                breakFileToTickets(folderPath, file);
            }
        }
    }

    private static void breakFileToTickets(String folderPath, File file) {
        PDDocument document = null;
        try {
            String cacheFolder = folderPath + "/cache/" + file.getName().split("\\.")[0];
            File fileCacheFolder = new File(cacheFolder);
            if (fileCacheFolder.exists()) {

//                return;
            }

            fileCacheFolder.mkdirs();
            document = Loader.loadPDF(file);

            PDFTextStripper pdfStripper = new PDFTextStripper();

            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageNum = 0;

            for (PDPage page : document.getPages()) {
                pageNum++;
                File currentPageCache = new File(cacheFolder + "/" + pageNum);
                currentPageCache.mkdirs();
                // Extract text
                pdfStripper.setStartPage(pageNum);
                pdfStripper.setEndPage(pageNum);
                String text = pdfStripper.getText(document);

                logger.info("Page {} Text:", pageNum);
                logger.info(text);

                // Extract links and replace in text
                StringBuilder htmlContent = new StringBuilder();
                htmlContent.append("<html><body>");
                List<PDAnnotation> annotations = page.getAnnotations();
                for (PDAnnotation annotation : annotations) {
                    if (annotation instanceof PDAnnotationLink) {
                        PDAnnotationLink link = (PDAnnotationLink) annotation;
                        if (link.getAction() instanceof PDActionURI) {
                            PDActionURI uriAction = (PDActionURI) link.getAction();
                            String uri = uriAction.getURI();
                            String annotationText = link.getContents();
                            if (annotationText == null || annotationText.isEmpty()) {
                                annotationText = uri;
                            }
                            String linkHtml = "<a href=\"" + uri + "\">" + annotationText + "</a>";
                            text = text.replace(annotationText, linkHtml);
                        }
                    }
                }
                htmlContent.append("<p>").append(text).append("</p>");
                htmlContent.append("</body></html>");

                // Save text and links as HTML
                FileUtils.write(new File(currentPageCache, "description.html"), htmlContent.toString(), "UTF-8");

                FileUtils.write(new File(currentPageCache, "description.txt"), text);
                BufferedImage bim = pdfRenderer.renderImageWithDPI(pageNum-1, 100); // 300 is the dpi (dots per inch), change it as needed

                // Save the image to a file
                ImageIO.write(bim, "png", new File(currentPageCache, "page_snapshot.png"));


                // Extract images
                PDResources resources = page.getResources();
                int imageNum = 0;

                for (COSName name : resources.getXObjectNames()) {
                    if (resources.isImageXObject(name)) {
                        PDImageXObject image = (PDImageXObject) resources.getXObject(name);
                        // Save the image to a file
                        ImageIO.write(image.getImage(), "png", new File(currentPageCache,  "attachment_" + (++imageNum) + ".png"));
                    }
                }
            }

            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (IOException ignored) {}
            }
        }
    }

}
