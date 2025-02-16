package com.github.istin.dmtools.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLCodeBlockPreserver {

    public static final String CODE_BLOCK_PLACEHOLDER = "___CODE_BLOCK_PLACEHOLDER___";
    public static final Pattern CODE_PATTERN = Pattern.compile("<code[^>]*>(.*?)</code>", Pattern.DOTALL);

    private final List<CodeBlock> preservedCodeBlocks = new ArrayList<>();

    static class CodeBlock {
        final String content;
        final String language;
        final boolean isInline;

        CodeBlock(String content, String language, boolean isInline) {
            this.content = content;
            this.language = language;
            this.isInline = isInline;
        }
    }

    public String preserveCodeBlocks(String html) {
        Matcher matcher = CODE_PATTERN.matcher(html);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String fullMatch = matcher.group(0);
            String codeContent = matcher.group(1);

            // Extract language from class=... if present
            String language = "java";
            Pattern classPattern = Pattern.compile("class=[\"']([^\"']*)[\"']");
            Matcher classMatcher = classPattern.matcher(fullMatch);
            if (classMatcher.find()) {
                language = classMatcher.group(1);
            }

            // If no "class=" or no multiline, treat as inline
            boolean isInline = !fullMatch.contains("class=") && !codeContent.contains("\n");

            preservedCodeBlocks.add(new CodeBlock(codeContent, language, !fullMatch.contains("class=") && !codeContent.contains("\n")));

            // Insert placeholder
            String replacement = Matcher.quoteReplacement(
                    "<code>" + CODE_BLOCK_PLACEHOLDER + (preservedCodeBlocks.size() - 1) + "</code>"
            );
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public String restoreCodeBlocks(String processedHtml) {
        for (int i = 0; i < preservedCodeBlocks.size(); i++) {
            CodeBlock block = preservedCodeBlocks.get(i);
            String placeholder = CODE_BLOCK_PLACEHOLDER + i;

            String content = block.content;

            // Remove any leading indentation from each line
            String[] lines = content.split("\\r?\\n");
            StringBuilder sb = new StringBuilder();
            for (String line : lines) {
                // strip leading spaces/tabs
                sb.append(line.replaceFirst("^[ \t]+", "")).append("\n");
            }
            String normalized = sb.toString().replaceAll("[\\r\\n]+$", "");

            String replacement;
            if (block.isInline) {
                replacement = "{{" + normalized.trim() + "}}";
            } else {
                replacement = String.format(
                        "{code:%s}\n%s\n{code}",
                        block.language,
                        normalized
                );
            }

            processedHtml = processedHtml.replace(placeholder, replacement);
        }
        return processedHtml;
    }

}
