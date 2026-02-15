package com.github.istin.dmtools.teammate;

import com.github.istin.dmtools.common.model.IAttachment;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.common.utils.CommandLineUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CliExecutionHelperTest {
    
    private CliExecutionHelper cliHelper;
    private ITicket mockTicket;
    private IAttachment mockAttachment;
    private TrackerClient<?> mockTrackerClient;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        cliHelper = new CliExecutionHelper();
        mockTicket = mock(ITicket.class);
        mockAttachment = mock(IAttachment.class);
        mockTrackerClient = mock(TrackerClient.class);
    }
    
    @AfterEach
    void tearDown() {
        // Clean up any created folders
        Path inputFolder = tempDir.resolve("input");
        if (Files.exists(inputFolder)) {
            try {
                FileUtils.deleteDirectory(inputFolder.toFile());
            } catch (IOException e) {
                // Ignore cleanup errors in tests
            }
        }
    }
    
    @Test
    void testCreateInputContext_Success() throws IOException {
        // Arrange
        String ticketKey = "TEST-123";
        String inputParams = "Test input parameters";
        when(mockTicket.getTicketKey()).thenReturn(ticketKey);
        when(mockTicket.getAttachments()).thenReturn(Collections.emptyList());
        
        // Act
        Path result = cliHelper.createInputContext(mockTicket, inputParams, mockTrackerClient);
        
        // Assert
        assertNotNull(result);
        assertTrue(Files.exists(result));
        assertEquals("input/" + ticketKey, result.toString());
        
        // Check request.md file was created
        Path requestFile = result.resolve("request.md");
        assertTrue(Files.exists(requestFile));
        String fileContent = Files.readString(requestFile, StandardCharsets.UTF_8);
        assertEquals(inputParams, fileContent);
    }
    
    @Test
    void testCreateInputContext_WithAttachments() throws IOException, Exception {
        // Arrange
        String ticketKey = "TEST-456";
        String inputParams = "Test input";
        byte[] attachmentContent = "Test attachment content".getBytes();
        
        when(mockTicket.getTicketKey()).thenReturn(ticketKey);
        when(mockAttachment.getName()).thenReturn("test-file.txt");
        when(mockAttachment.getUrl()).thenReturn("http://example.com/attachment");
        
        // Mock tracker client download
        File mockFile = new File(tempDir.toFile(), "temp-attachment.txt");
        try { FileUtils.writeByteArrayToFile(mockFile, attachmentContent); } catch (IOException e) {}
        when(mockTrackerClient.convertUrlToFile("http://example.com/attachment")).thenReturn(mockFile);
        doReturn(Arrays.asList(mockAttachment)).when(mockTicket).getAttachments();
        
        // Act
        Path result = cliHelper.createInputContext(mockTicket, inputParams, mockTrackerClient);
        
        // Assert
        Path attachmentFile = result.resolve("test-file.txt");
        assertTrue(Files.exists(attachmentFile));
        byte[] savedContent = Files.readAllBytes(attachmentFile);
        assertArrayEquals(attachmentContent, savedContent);
    }
    
    @Test
    void testCreateInputContext_NullTicket() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cliHelper.createInputContext(null, "test", mockTrackerClient)
        );
        assertEquals("Ticket cannot be null", exception.getMessage());
    }
    
    @Test
    void testCreateInputContext_EmptyTicketKey() {
        // Arrange
        when(mockTicket.getTicketKey()).thenReturn("");
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cliHelper.createInputContext(mockTicket, "test", mockTrackerClient)
        );
        assertEquals("Ticket key cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testCreateInputContext_EmptyInputParams() throws IOException {
        // Arrange
        String ticketKey = "TEST-789";
        when(mockTicket.getTicketKey()).thenReturn(ticketKey);
        when(mockTicket.getAttachments()).thenReturn(Collections.emptyList());
        
        // Act
        Path result = cliHelper.createInputContext(mockTicket, "", mockTrackerClient);
        
        // Assert
        assertTrue(Files.exists(result));
        Path requestFile = result.resolve("request.md");
        assertFalse(Files.exists(requestFile)); // Should not create empty file
    }
    
    @Test
    void testExecuteCliCommands_Success() {
        // Arrange
        String[] commands = {"echo hello", "echo world"};
        
        try (MockedStatic<CommandLineUtils> mockedUtils = Mockito.mockStatic(CommandLineUtils.class)) {
            mockedUtils.when(() -> CommandLineUtils.runCommand(eq("echo hello"), isNull(), any(Map.class)))
                      .thenReturn("hello\nExit Code: 0");
            mockedUtils.when(() -> CommandLineUtils.runCommand(eq("echo world"), isNull(), any(Map.class)))
                      .thenReturn("world\nExit Code: 0");
            // Mock the environment loading
            mockedUtils.when(() -> CommandLineUtils.loadEnvironmentFromFile("dmtools.env"))
                      .thenReturn(Map.of());
            
            // Act
            StringBuilder result = cliHelper.executeCliCommands(commands, null, "dmtools.env");
            
            // Assert
            String resultString = result.toString();
            assertTrue(resultString.contains("CLI Command: echo hello"));
            assertTrue(resultString.contains("hello"));
            assertTrue(resultString.contains("CLI Command: echo world"));
            assertTrue(resultString.contains("world"));
        }
    }
    
    @Test
    void testExecuteCliCommands_WithError() {
        // Arrange
        String[] commands = {"invalid-command"};
        
        try (MockedStatic<CommandLineUtils> mockedUtils = Mockito.mockStatic(CommandLineUtils.class)) {
            mockedUtils.when(() -> CommandLineUtils.runCommand(eq("invalid-command"), isNull(), any(Map.class)))
                      .thenThrow(new IOException("Command not found"));
            // Mock the environment loading
            mockedUtils.when(() -> CommandLineUtils.loadEnvironmentFromFile("dmtools.env"))
                      .thenReturn(Map.of());
            
            // Act
            StringBuilder result = cliHelper.executeCliCommands(commands, null, "dmtools.env");
            
            // Assert
            String resultString = result.toString();
            assertTrue(resultString.contains("CLI Command: invalid-command"));
            assertTrue(resultString.contains("Error:"));
            assertTrue(resultString.contains("Command not found"));
        }
    }
    
    @Test
    void testExecuteCliCommands_EmptyCommands() {
        // Act
        StringBuilder result = cliHelper.executeCliCommands(new String[0], null, "dmtools.env");
        
        // Assert
        assertEquals(0, result.length());
    }
    
    @Test
    void testExecuteCliCommands_NullCommands() {
        // Act
        StringBuilder result = cliHelper.executeCliCommands(null, null, "dmtools.env");
        
        // Assert
        assertEquals(0, result.length());
    }
    
    @Test
    void testExecuteCliCommands_WithWorkingDirectory() throws IOException {
        // Arrange
        Path workingDir = Files.createTempDirectory(tempDir, "working");
        String[] commands = {"echo test"};
        
        try (MockedStatic<CommandLineUtils> mockedUtils = Mockito.mockStatic(CommandLineUtils.class)) {
            mockedUtils.when(() -> CommandLineUtils.runCommand(eq("echo test"), any(File.class), any(Map.class)))
                      .thenReturn("test\nExit Code: 0");
            // Mock the environment loading
            mockedUtils.when(() -> CommandLineUtils.loadEnvironmentFromFile("dmtools.env"))
                      .thenReturn(Map.of());

            // Act
            StringBuilder result = cliHelper.executeCliCommands(commands, workingDir, "dmtools.env");

            // Assert
            assertTrue(result.toString().contains("test"));
            // Verify that CommandLineUtils was called with the correct working directory
            mockedUtils.verify(() -> CommandLineUtils.runCommand(eq("echo test"), eq(workingDir.toFile()), any(Map.class)));
        }
    }
    
    @Test
    void testProcessOutputResponse_FileExists() throws IOException {
        // Arrange
        Path outputDir = tempDir.resolve("outputs");
        Files.createDirectories(outputDir);
        Path responseFile = outputDir.resolve("response.md");
        String expectedContent = "CLI response content";
        Files.write(responseFile, expectedContent.getBytes(StandardCharsets.UTF_8));
        
        // Act
        String result = cliHelper.processOutputResponse(tempDir);
        
        // Assert
        assertEquals(expectedContent, result);
    }
    
    @Test
    void testProcessOutputResponse_FileNotExists() {
        // Act - test with a directory that doesn't have outputs/response.md
        String result = cliHelper.processOutputResponse(tempDir);
        
        // Assert
        assertNull(result);
    }
    
    @Test
    void testProcessOutputResponse_EmptyFile() throws IOException {
        // Arrange
        Path outputDir = tempDir.resolve("outputs");
        Files.createDirectories(outputDir);
        Path responseFile = outputDir.resolve("response.md");
        Files.write(responseFile, "".getBytes(StandardCharsets.UTF_8));
        
        // Act
        String result = cliHelper.processOutputResponse(tempDir);
        
        // Assert
        assertNull(result);
    }
    
    @Test
    void testCleanupInputContext_Success() throws IOException {
        // Arrange
        Path inputFolder = tempDir.resolve("input/TEST-123");
        Files.createDirectories(inputFolder);
        Files.write(inputFolder.resolve("test.txt"), "test".getBytes());
        assertTrue(Files.exists(inputFolder));
        
        // Act
        cliHelper.cleanupInputContext(inputFolder);
        
        // Assert
        assertFalse(Files.exists(inputFolder));
    }
    
    @Test
    void testCleanupInputContext_NonExistentFolder() {
        // Arrange
        Path nonExistentFolder = tempDir.resolve("non-existent");
        
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> cliHelper.cleanupInputContext(nonExistentFolder));
    }
    
    @Test
    void testCleanupInputContext_NullPath() {
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> cliHelper.cleanupInputContext(null));
    }
    
    @Test
    void testAttachmentFilenameSanitization() throws IOException, Exception {
        // Arrange
        String ticketKey = "TEST-SANITIZE";
        when(mockTicket.getTicketKey()).thenReturn(ticketKey);
        when(mockAttachment.getName()).thenReturn("path/to/file.txt");
        when(mockAttachment.getUrl()).thenReturn("http://example.com/path-file");
        
        // Mock tracker client download
        File mockFile = new File(tempDir.toFile(), "temp-path-file.txt");
        try { FileUtils.writeStringToFile(mockFile, "content", StandardCharsets.UTF_8); } catch (IOException e) {}
        when(mockTrackerClient.convertUrlToFile("http://example.com/path-file")).thenReturn(mockFile);
        doReturn(Arrays.asList(mockAttachment)).when(mockTicket).getAttachments();
        
        // Act
        Path result = cliHelper.createInputContext(mockTicket, "test", mockTrackerClient);
        
        // Assert
        Path sanitizedFile = result.resolve("path_to_file.txt");
        assertTrue(Files.exists(sanitizedFile));
        assertEquals("content", Files.readString(sanitizedFile));
    }
    
    @Test
    void testExecuteCliCommandsWithResult() throws IOException {
        // Arrange
        Path workingDir = Files.createTempDirectory(tempDir, "working");
        String[] commands = {"echo hello", "echo world"};
        
        try (MockedStatic<CommandLineUtils> mockedUtils = Mockito.mockStatic(CommandLineUtils.class)) {
            mockedUtils.when(() -> CommandLineUtils.runCommand(eq("echo hello"), any(File.class), any(Map.class)))
                      .thenReturn("hello\nExit Code: 0");
            mockedUtils.when(() -> CommandLineUtils.runCommand(eq("echo world"), any(File.class), any(Map.class)))
                      .thenReturn("world\nExit Code: 0");
            // Mock the environment loading
            mockedUtils.when(() -> CommandLineUtils.loadEnvironmentFromFile("dmtools.env"))
                      .thenReturn(Map.of());
            
            // Act
            CliExecutionHelper.CliExecutionResult result = cliHelper.executeCliCommandsWithResult(commands, workingDir, "dmtools.env");
            
            // Assert
            assertNotNull(result);
            assertTrue(result.getCommandResponses().toString().contains("hello"));
            assertTrue(result.getCommandResponses().toString().contains("world"));
            assertFalse(result.hasOutputResponse()); // No outputs/response.md file created
            assertNull(result.getOutputResponse());
        }
    }
    
    @Test
    void testExecuteCliCommandsWithResult_WithOutputFile() throws IOException {
        // Arrange
        Path workingDir = Files.createTempDirectory(tempDir, "working");
        Path outputDir = workingDir.resolve("outputs");
        Files.createDirectories(outputDir);
        Path responseFile = outputDir.resolve("response.md");
        String outputContent = "CLI generated response content";
        Files.write(responseFile, outputContent.getBytes(StandardCharsets.UTF_8));
        
        String[] commands = {"echo test"};
        
        try (MockedStatic<CommandLineUtils> mockedUtils = Mockito.mockStatic(CommandLineUtils.class)) {
            mockedUtils.when(() -> CommandLineUtils.runCommand(eq("echo test"), any(File.class), any(Map.class)))
                      .thenReturn("test\nExit Code: 0");
            // Mock the environment loading
            mockedUtils.when(() -> CommandLineUtils.loadEnvironmentFromFile("dmtools.env"))
                      .thenReturn(Map.of());
            
            // Act
            CliExecutionHelper.CliExecutionResult result = cliHelper.executeCliCommandsWithResult(commands, workingDir, "dmtools.env");
            
            // Assert
            assertNotNull(result);
            assertTrue(result.getCommandResponses().toString().contains("test"));
            assertTrue(result.hasOutputResponse());
            assertEquals(outputContent, result.getOutputResponse());
        }
    }
    
    @Test
    void testProcessOutputResponse_NoConcurrencyIssues() throws IOException {
        // This test verifies that processOutputResponse doesn't rely on global working directory
        // and can work correctly even when the JVM working directory is different

        // Arrange
        Path workingDir = Files.createTempDirectory(tempDir, "working");
        Path outputDir = workingDir.resolve("outputs");
        Files.createDirectories(outputDir);
        Path responseFile = outputDir.resolve("response.md");
        String expectedContent = "Thread-safe CLI response";
        Files.write(responseFile, expectedContent.getBytes(StandardCharsets.UTF_8));

        // Act - call with specific working directory (thread-safe approach)
        String result = cliHelper.processOutputResponse(workingDir);

        // Assert
        assertEquals(expectedContent, result);

        // Verify that the parameterless version doesn't find the file
        // (since it looks in the current JVM working directory, not our test directory)
        String resultWithoutParam = cliHelper.processOutputResponse();
        assertNull(resultWithoutParam);
    }

    // ========================================================================
    // Tests for appendPromptToCommands (cliPrompt feature)
    // ========================================================================

    @Test
    void testAppendPromptToCommands_PlainText() {
        String[] commands = {"echo", "./script.sh"};
        String prompt = "This is a test prompt";

        String[] result = CliExecutionHelper.appendPromptToCommands(commands, prompt);

        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("echo \"This is a test prompt\"", result[0]);
        assertEquals("./script.sh \"This is a test prompt\"", result[1]);
    }

    @Test
    void testAppendPromptToCommands_NullPrompt() {
        String[] commands = {"echo", "./script.sh"};
        String prompt = null;

        String[] result = CliExecutionHelper.appendPromptToCommands(commands, prompt);

        assertNotNull(result);
        assertSame(commands, result, "Should return same array reference when prompt is null");
    }

    @Test
    void testAppendPromptToCommands_EmptyPrompt() {
        String[] commands = {"echo", "./script.sh"};
        String prompt = "";

        String[] result = CliExecutionHelper.appendPromptToCommands(commands, prompt);

        assertNotNull(result);
        assertSame(commands, result, "Should return same array reference when prompt is empty");
    }

    @Test
    void testAppendPromptToCommands_WhitespacePrompt() {
        String[] commands = {"echo", "./script.sh"};
        String prompt = "   \t\n  ";

        String[] result = CliExecutionHelper.appendPromptToCommands(commands, prompt);

        assertNotNull(result);
        assertSame(commands, result, "Should return same array reference when prompt is whitespace");
    }

    @Test
    void testAppendPromptToCommands_EmptyArray() {
        String[] commands = {};
        String prompt = "Test prompt";

        String[] result = CliExecutionHelper.appendPromptToCommands(commands, prompt);

        assertNotNull(result);
        assertSame(commands, result, "Should return same array reference when commands are empty");
    }

    @Test
    void testAppendPromptToCommands_NullCommands() {
        String[] commands = null;
        String prompt = "Test prompt";

        String[] result = CliExecutionHelper.appendPromptToCommands(commands, prompt);

        assertNull(result, "Should return null when commands are null");
    }

    @Test
    void testAppendPromptToCommands_EscapeDoubleQuotes() {
        String[] commands = {"echo"};
        String prompt = "Test \"quoted\" text";

        String[] result = CliExecutionHelper.appendPromptToCommands(commands, prompt);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("echo \"Test \\\"quoted\\\" text\"", result[0]);
    }

    @Test
    void testAppendPromptToCommands_EscapeDollarSigns() {
        String[] commands = {"echo"};
        String prompt = "Price is $100";

        String[] result = CliExecutionHelper.appendPromptToCommands(commands, prompt);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("echo \"Price is \\$100\"", result[0]);
    }

    @Test
    void testAppendPromptToCommands_EscapeBackticks() {
        String[] commands = {"echo"};
        String prompt = "Run `ls -la` here";

        String[] result = CliExecutionHelper.appendPromptToCommands(commands, prompt);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("echo \"Run \\`ls -la\\` here\"", result[0]);
    }

    @Test
    void testAppendPromptToCommands_EscapeBackslashes() {
        String[] commands = {"echo"};
        String prompt = "Path: C:\\Users\\test";

        String[] result = CliExecutionHelper.appendPromptToCommands(commands, prompt);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("echo \"Path: C:\\\\Users\\\\test\"", result[0]);
    }

    @Test
    void testAppendPromptToCommands_EscapeMultipleSpecialChars() {
        String[] commands = {"./script.sh"};
        String prompt = "Text with \"quotes\", $vars, `commands`, and \\backslashes";

        String[] result = CliExecutionHelper.appendPromptToCommands(commands, prompt);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("./script.sh \"Text with \\\"quotes\\\", \\$vars, \\`commands\\`, and \\\\backslashes\"", result[0]);
    }

    @Test
    void testAppendPromptToCommands_MultipleCommands() {
        String[] commands = {
            "./script1.sh",
            "python script2.py",
            "node script3.js"
        };
        String prompt = "Execute with this prompt";

        String[] result = CliExecutionHelper.appendPromptToCommands(commands, prompt);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("./script1.sh \"Execute with this prompt\"", result[0]);
        assertEquals("python script2.py \"Execute with this prompt\"", result[1]);
        assertEquals("node script3.js \"Execute with this prompt\"", result[2]);
    }

    @Test
    void testAppendPromptToCommands_NullCommandElement() {
        String[] commands = {"echo", null, "./script.sh"};
        String prompt = "Test prompt";

        String[] result = CliExecutionHelper.appendPromptToCommands(commands, prompt);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("echo \"Test prompt\"", result[0]);
        assertNull(result[1]);
        assertEquals("./script.sh \"Test prompt\"", result[2]);
    }

    @Test
    void testAppendPromptToCommands_EmptyCommandElement() {
        String[] commands = {"echo", "", "./script.sh"};
        String prompt = "Test prompt";

        String[] result = CliExecutionHelper.appendPromptToCommands(commands, prompt);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("echo \"Test prompt\"", result[0]);
        assertEquals("", result[1]);
        assertEquals("./script.sh \"Test prompt\"", result[2]);
    }

    @Test
    void testAppendPromptToCommands_LongPrompt() {
        String[] commands = {"echo"};
        // Create a 5000 character prompt
        StringBuilder longPrompt = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            longPrompt.append("This is a long prompt text. ");
        }
        String prompt = longPrompt.toString();
        assertTrue(prompt.length() > 5000, "Prompt should be over 5000 chars");

        String[] result = CliExecutionHelper.appendPromptToCommands(commands, prompt);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertTrue(result[0].startsWith("echo \""));
        assertTrue(result[0].endsWith("\""));
        assertTrue(result[0].contains("This is a long prompt text."));
    }

    @Test
    void testAppendPromptToCommands_BackwardsCompatibility() {
        String[] commands = {"./existing-script.sh arg1 arg2"};
        String prompt = null; // cliPrompt not set in existing configs

        String[] result = CliExecutionHelper.appendPromptToCommands(commands, prompt);

        assertNotNull(result);
        assertSame(commands, result, "Commands should remain unchanged for backwards compatibility");
    }
}
