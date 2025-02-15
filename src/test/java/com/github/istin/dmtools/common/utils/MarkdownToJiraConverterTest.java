package com.github.istin.dmtools.common.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class MarkdownToJiraConverterTest {

    @Test
    public void testHtmlInput() {
        String html = "<p>This is <strong>bold</strong> and <em>italic</em></p>" +
                "<pre><code class=\"java\">public class Test {}</code></pre>";
        String expected = "This is *bold* and _italic_\n\n" +
                "{code:java}\npublic class Test {}\n{code}";
        assertEquals(expected, MarkdownToJiraConverter.convertToJiraMarkdown(html));
    }

    @Test
    public void testMarkdownInput() {
        String markdown = "This is **bold** and _italic_\n\n" +
                "```java\npublic class Test {}\n```";
        String expected = "This is *bold* and _italic_\n\n" +
                "{code:java}\npublic class Test {}\n{code}";
        assertEquals(expected, MarkdownToJiraConverter.convertToJiraMarkdown(markdown));
    }

    @Test
    public void testMixedInput() {
        String input = "# Heading\n\n<p>This is <strong>bold</strong></p>\n\n* List item\n\n```java\ncode\n```";
        String expected = "h1. Heading\n\nThis is *bold*\n\n* List item\n\n{code:java}\ncode\n{code}";
        assertEquals(expected, MarkdownToJiraConverter.convertToJiraMarkdown(input));
    }

    @Test
    public void testComplexHtmlInput() {
        String input = "<h1>Title</h1><p><strong>Bold text</strong> and <em>italic text</em></p>" +
                "<pre><code class=\"java\">public class Test {}</code></pre>" +
                "<ul><li>Item 1</li><li>Item 2</li></ul>" +
                "<a href=\"http://example.com\">Link</a>" +
                "<code>inline code</code>";
        String expected = "h1. Title\n\n" +
                "*Bold text* and _italic text_\n\n" +
                "{code:java}\npublic class Test {}\n{code}\n\n" +
                "* Item 1\n* Item 2\n\n" +
                "[Link|http://example.com]\n\n" +
                "{{inline code}}";
        assertEquals(expected, MarkdownToJiraConverter.convertToJiraMarkdown(input));
    }

    @Test
    public void testComplexMarkdownInput() {
        String markdown = "# Title\n\n" +
                "**Bold text** and _italic text_\n\n" +
                "```java\npublic class Test {}\n```\n\n" +
                "* Item 1\n* Item 2\n\n" +
                "[Link](http://example.com)\n\n" +
                "`inline code`";

        String expected = "h1. Title\n\n" +
                "*Bold text* and _italic text_\n\n" +
                "{code:java}\npublic class Test {}\n{code}\n\n" +
                "* Item 1\n* Item 2\n\n" +
                "[Link|http://example.com]\n\n" +
                "{{inline code}}";

        assertEquals(expected, MarkdownToJiraConverter.convertToJiraMarkdown(markdown));
    }

    @Test
    public void testEdgeCases() {
        // Empty input
        assertEquals("", MarkdownToJiraConverter.convertToJiraMarkdown(""));

        // Only whitespace
        assertEquals("", MarkdownToJiraConverter.convertToJiraMarkdown("  \n  \t  "));

        // HTML entities
        assertEquals("< > & \"",
                MarkdownToJiraConverter.convertToJiraMarkdown("&lt; &gt; &amp; &quot;"));
    }
}