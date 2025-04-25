package com.github.istin.dmtools.ai;

import com.github.istin.dmtools.common.model.ToText;
import com.github.istin.dmtools.common.utils.PropertyReader;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChunkPreparation {
    // Configuration fields
    private final int tokenLimit;
    private final long maxSingleFileSize;
    private final long maxTotalFilesSize;
    private final int maxFilesPerChunk;

    private final TokenCounter tokenCounter;

    public ChunkPreparation() {
        this(new Claude35TokenCounter());
    }

    public ChunkPreparation(TokenCounter tokenCounter) {
        this.tokenCounter = tokenCounter;
        PropertyReader propertyReader = new PropertyReader();
        // Initialize configuration fields from PropertyReader
        this.tokenLimit = propertyReader.getPromptChunkTokenLimit();
        this.maxSingleFileSize = propertyReader.getPromptChunkMaxSingleFileSize();
        this.maxTotalFilesSize = propertyReader.getPromptChunkMaxTotalFilesSize();
        this.maxFilesPerChunk = propertyReader.getPromptChunkMaxFiles();
    }

    /**
     * Represents a chunk of content including both text and files
     */
    public static class Chunk {
        private final String text;
        private List<File> files;
        private final long totalFilesSize;

        public Chunk(String text, List<File> files, long totalFilesSize) {
            this.text = text;
            if (files != null) {
                this.files = Collections.unmodifiableList(files);
            }
            this.totalFilesSize = totalFilesSize;
        }

        public String getText() {
            return text;
        }

        public List<File> getFiles() {
            return files;
        }

        public long getTotalFilesSize() {
            return totalFilesSize;
        }
    }

    private static class ChunkAccumulator {
        StringBuilder text = new StringBuilder();
        List<File> files = new ArrayList<>();
        int tokenCount = 0;
        long filesSize = 0;

        void reset() {
            text.setLength(0);
            files.clear();
            tokenCount = 0;
            filesSize = 0;
        }

        Chunk toChunk() {
            return new Chunk(text.toString(), new ArrayList<>(files), filesSize);
        }

        boolean isEmpty() {
            return text.isEmpty() && files.isEmpty();
        }
    }

    /**
     * Attempts to add a file to the current chunk accumulator
     * @return true if the file was added, false if a new chunk needs to be created
     */
    private boolean tryAddFile(File file, ChunkAccumulator current) {
        if (file.length() > maxSingleFileSize) {
            return true; // Skip this file but don't create new chunk
        }

        if (current.files.size() >= maxFilesPerChunk ||
                current.filesSize + file.length() > maxTotalFilesSize) {
            return false; // Need new chunk
        }

        current.files.add(file);
        current.filesSize += file.length();
        return true;
    }

    /**
     * Prepares a collection of objects (Strings, Files, and Map.Entries) into chunks
     */
    public List<Chunk> prepareChunks(Collection<?> objects, int tokenLimit) throws IOException {
        List<Chunk> chunks = new ArrayList<>();
        ChunkAccumulator current = new ChunkAccumulator();

        for (Object obj : objects) {
            if (obj instanceof Map.Entry<?, ?> entry) {
                Object value = entry.getValue();

                if (value instanceof File file) {
                    String keyText = entry.getKey().toString();
                    int keyTokens = tokenCounter.countTokens(keyText);

                    // Check if adding the key text would exceed token limit
                    if (current.tokenCount + keyTokens > tokenLimit) {
                        if (!current.isEmpty()) {
                            chunks.add(current.toChunk());
                            current.reset();
                        }
                    }

                    // Try to add the file
                    if (!tryAddFile(file, current)) {
                        if (!current.isEmpty()) {
                            chunks.add(current.toChunk());
                            current.reset();
                        }
                        tryAddFile(file, current); // Now it should succeed with empty chunk
                    }

                    // Add key text
                    if (!current.text.isEmpty()) {
                        current.text.append(",\n");
                    }
                    current.text.append(keyText);
                    current.tokenCount += keyTokens;
                } else {
                    // Handle non-File Map.Entry as regular text
                    processText(entry.toString(), chunks, current, tokenLimit);
                }
            } else if (obj instanceof File file) {
                if (!tryAddFile(file, current)) {
                    if (!current.isEmpty()) {
                        chunks.add(current.toChunk());
                        current.reset();
                    }
                    tryAddFile(file, current); // Now it should succeed with empty chunk
                }
            } else if (obj instanceof ToText) {
                processText(((ToText)obj).toText(), chunks, current, tokenLimit);
            } else {
                processText(obj.toString(), chunks, current, tokenLimit);
            }
        }

        // Add the last chunk if it's not empty
        if (!current.isEmpty()) {
            chunks.add(current.toChunk());
        }

        return chunks;
    }

    private void processText(String text, List<Chunk> chunks, ChunkAccumulator current, int tokenLimit) {
        int textTokens = tokenCounter.countTokens(text);

        // If single text exceeds token limit, split it
        if (textTokens > tokenLimit) {
            if (!current.isEmpty()) {
                chunks.add(current.toChunk());
                current.reset();
            }
            chunks.addAll(splitLargeObject(text, tokenLimit));
            return;
        }

        // Check if adding this text would exceed the token limit
        if (current.tokenCount + textTokens > tokenLimit) {
            chunks.add(current.toChunk());
            current.reset();
        }

        // Add text to current chunk
        if (!current.text.isEmpty()) {
            current.text.append("\n");
        }
        current.text.append(text);
        current.tokenCount += textTokens;
    }

    /**
     * Overloaded method using default token limit
     */
    public List<Chunk> prepareChunks(Collection<?> objects) throws IOException {
        return prepareChunks(objects, tokenLimit);
    }

    /**
     * Splits a large text that exceeds token limit into smaller chunks
     */
    private List<Chunk> splitLargeObject(String largeString, int tokenLimit) {
        List<Chunk> chunks = new ArrayList<>();
        StringBuilder chunk = new StringBuilder();
        int currentTokens = 0;

        String[] words = largeString.split("\\s+");

        for (String word : words) {
            int wordTokens = tokenCounter.countTokens(word + " ");

            if (currentTokens + wordTokens > tokenLimit) {
                chunks.add(new Chunk(chunk.toString(), new ArrayList<>(), 0));
                chunk = new StringBuilder();
                currentTokens = 0;
            }

            chunk.append(word).append(" ");
            currentTokens += wordTokens;
        }

        if (!chunk.isEmpty()) {
            chunks.add(new Chunk(chunk.toString().trim(), new ArrayList<>(), 0));
        }

        return chunks;
    }
}

/**
 * Interface for token counting implementation
 */
interface TokenCounter {
    int countTokens(String text);
}

/**
 * Example implementation of TokenCounter for Claude 3.5
 * You would need to implement actual token counting logic here
 */
class Claude35TokenCounter implements TokenCounter {
    @Override
    public int countTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        // Use a regex pattern that defines a word as a sequence of alphanumeric characters
        // This handles punctuation and special characters better than simple space splitting
        Pattern pattern = Pattern.compile("\\b[\\w'-]+\\b");
        Matcher matcher = pattern.matcher(text);

        int count = 0;
        while (matcher.find()) {
            count++;
        }

        return count;
    }
}
