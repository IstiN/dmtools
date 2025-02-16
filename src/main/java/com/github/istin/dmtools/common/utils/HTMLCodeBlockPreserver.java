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

            // Extract language from class attribute if present
            String language = "java"; // default
            Pattern classPattern = Pattern.compile("class=[\"']([^\"']*)[\"']");
            Matcher classMatcher = classPattern.matcher(fullMatch);
            if (classMatcher.find()) {
                language = classMatcher.group(1);
            }

            // Determine if this is an inline code block
            boolean isInline = !codeContent.contains("\n") &&
                    !fullMatch.contains("class=") &&
                    codeContent.length() < 50; // arbitrary length for inline code

            preservedCodeBlocks.add(new CodeBlock(codeContent, language, isInline));

            String replacement = Matcher.quoteReplacement("<code>" + CODE_BLOCK_PLACEHOLDER +
                    (preservedCodeBlocks.size() - 1) + "</code>");
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public String restoreCodeBlocks(String processedHtml) {
        for (int i = 0; i < preservedCodeBlocks.size(); i++) {
            CodeBlock block = preservedCodeBlocks.get(i);
            String placeholder = CODE_BLOCK_PLACEHOLDER + i;

            String replacement;
            if (block.isInline) {
                // Use inline code format
                replacement = "{{" + block.content.trim() + "}}";
            } else {
                // Use block code format
                replacement = String.format("{code:%s}\n%s\n{code}",
                        block.language, block.content.trim());
            }

            processedHtml = processedHtml.replace(
                    "<code>" + placeholder + "</code>",
                    replacement
            );
        }
        return processedHtml;
    }
}