package com.github.istin.dmtools.context;

import com.github.istin.dmtools.common.model.IAttachment;
import com.github.istin.dmtools.pdf.PdfAsTrackerClient;
import com.github.istin.dmtools.pdf.model.PdfPageAsTicket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FileToTextTransformer {

    private static final Set<String> BINARY_EXTENSIONS = Set.of(
            "pdf", "csv", "doc", "docx", "xls", "xlsx",
            "jpg", "jpeg", "png", "gif", "bmp", "tiff", "webp"
    );

    public record TransformationResult(String text, List<File> files) {
    }

    public static List<TransformationResult> transform(File file) throws Exception {
        if (file == null || !file.exists()) {
            return null;
        }

        String fileName = file.getName().toLowerCase();

        // Special handling for PDF files
        if (fileName.endsWith(".pdf")) {
            return transformPdf(file);
        }

        // Return null for binary files
        if (BINARY_EXTENSIONS.stream().anyMatch(ext -> fileName.endsWith("." + ext))) {
            return null;
        }
        // Read text files
        return List.of(new TransformationResult(file.getName() + "\n" + FileUtils.readFileToString(file), null));
    }

    private static List<TransformationResult> transformPdf(File pdfFile) throws Exception {
        // Create a folder for PDF processing in the same directory
        String folderName = pdfFile.getName().replace(".pdf", "");
        File pdfFolder = new File(pdfFile.getParent(), folderName);
        pdfFolder.mkdirs();

        // Copy PDF file to the new folder
        File pdfCopy = new File(pdfFolder, pdfFile.getName());
        FileUtils.copyFile(pdfFile, pdfCopy);

        // Initialize PdfAsTrackerClient with the new folder
        PdfAsTrackerClient trackerClient = new PdfAsTrackerClient(pdfFolder.getAbsolutePath());

        // Get all PDF pages as tickets
        List<PdfPageAsTicket> pdfPages = trackerClient.searchAndPerform(null, null);

        // Convert PdfPageAsTicket objects to TransformationResults
        List<TransformationResult> results = new ArrayList<>();

        for (PdfPageAsTicket ticket : pdfPages) {
            List<File> files = new ArrayList<>();

            // Add all attachments
            List<? extends IAttachment> attachments = ticket.getAttachments();
            if (attachments != null && !attachments.isEmpty()) {
                if (ticket.getPageSnapshot() != null) {
                    files.add(ticket.getPageSnapshot());
                }
                //TODO think about reading full page screenshot or attachments
                //files.addAll(ticket.getAttachmentsAsFiles());
            }

            // Create TransformationResult with ticket description and files
            results.add(new TransformationResult(
                    ticket.getTicketDescription(),
                    files
            ));
        }

        return results;
    }

}
