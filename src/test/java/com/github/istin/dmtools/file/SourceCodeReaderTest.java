package com.github.istin.dmtools.file;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SourceCodeReaderTest {

    private static final Logger logger = Logger.getLogger(SourceCodeReaderTest.class.getName());
    private FileContentListener mockListener;
    private SourceCodeReader sourceCodeReader;
    private static final List<String> TEST_EXTENSIONS = Arrays.asList(".java", ".txt");
    private Path tempDir;

    @Before
    public void setUp() throws IOException {
        mockListener = Mockito.mock(FileContentListener.class);
        tempDir = Files.createTempDirectory("sourceReaderTest");
        // Create a 'src' directory structure within the temp directory
        Files.createDirectories(tempDir.resolve("src/com/github/istin/dmtools/file"));
        logger.info("Temporary directory created at: " + tempDir.toString());
    }

    @Test
    public void testReadSourceFiles_withMatchingExtensions() throws Exception {
        sourceCodeReader = new SourceCodeReader(TEST_EXTENSIONS, tempDir); // Use tempDir as root

        // Setup: Create files for testing
        Path tempFileJava = Files.createFile(tempDir.resolve("TestClass.java"));
        Path tempFileTxt = Files.createFile(tempDir.resolve("notes.txt"));

        logger.info("Created test files: " + tempFileJava + ", " + tempFileTxt);

        // Write dummy content to files
        Files.writeString(tempFileJava, "public class TestClass {}");
        Files.writeString(tempFileTxt, "This is a text file.");

        logger.info("Written content to test files");

        // Action: Read source files
        sourceCodeReader.readSourceFiles(mockListener);

        // Verification: Capture and verify listener interactions
        ArgumentCaptor<String> folderPathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> packageNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fileContentCaptor = ArgumentCaptor.forClass(String.class);

        verify(mockListener, times(2)).onFileRead(folderPathCaptor.capture(), packageNameCaptor.capture(),
                fileNameCaptor.capture(), fileContentCaptor.capture());

        List<String> folderPathValues = folderPathCaptor.getAllValues();
        List<String> packageNameValues = packageNameCaptor.getAllValues();
        List<String> fileNameValues = fileNameCaptor.getAllValues();
        List<String> fileContentValues = fileContentCaptor.getAllValues();

        logger.info("Captured values:");
        logger.info("Folder paths: " + folderPathValues);
        logger.info("Package names: " + packageNameValues);
        logger.info("File names: " + fileNameValues);
        logger.info("File contents: " + fileContentValues);

        // Check if both expected files are present
        assertTrue("TestClass.java should be in the results", fileNameValues.contains("TestClass.java"));
        assertTrue("notes.txt should be in the results", fileNameValues.contains("notes.txt"));

        // Check folder paths and package names
        assertEquals(Arrays.asList("", ""), folderPathValues);
        assertEquals(Arrays.asList("", ""), packageNameValues);

        // Check file contents (order-independent)
        assertTrue("File contents should include Java class content",
                fileContentValues.contains("public class TestClass {}"));
        assertTrue("File contents should include text file content",
                fileContentValues.contains("This is a text file."));

        logger.info("All assertions passed");

        // Cleanup: Delete temporary files and directories
        Files.deleteIfExists(tempFileJava);
        Files.deleteIfExists(tempFileTxt);

        // Delete all directories in tempDir
        if (Files.exists(tempDir)) {
            // Use Files.walk to traverse the directory structure
            Files.walk(tempDir)
                    .sorted((a, b) -> b.compareTo(a)) // Reverse order to delete children first
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            logger.info("Deleted: " + path);
                        } catch (IOException e) {
                            logger.warning("Failed to delete: " + path + ". Error: " + e.getMessage());
                        }
                    });
        }

        logger.info("Test cleanup completed");
    }
}