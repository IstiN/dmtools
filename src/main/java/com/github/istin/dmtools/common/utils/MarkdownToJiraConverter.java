package com.github.istin.dmtools.common.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownToJiraConverter {

    private static final Pattern HTML_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(\\w*)\\n([\\s\\S]*?)```");
    private static final Pattern HEADING_PATTERN = Pattern.compile("^(#{1,6})\\s+(.+)$", Pattern.MULTILINE);

    public static String convertToJiraMarkdown(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        // If it's just HTML entities, decode them plainly
        if (input.contains("&") && !containsHtml(input.replaceAll("&[a-zA-Z]+;", ""))) {
            return decodeEntities(input);
        }

        boolean hasMarkdown = input.contains("#") || input.contains("```") || input.contains("* ");
        boolean hasHtml = containsHtml(input);

        if (hasMarkdown && hasHtml) {
            return convertMixedContent(input);
        } else if (hasHtml) {
            return convertHtmlToJiraMarkdown(input);
        } else {
            return convertMarkdownToJiraMarkdown(input);
        }
    }

    private static String convertMixedContent(String input) {
        String[] chunks = input.split("\n\n");
        List<String> parts = new ArrayList<>();
        for (String chunk : chunks) {
            String trimmed = chunk.trim();
            if (trimmed.isEmpty()) continue;
            if (containsHtml(trimmed)) {
                parts.add(convertHtmlToJiraMarkdown(trimmed));
            } else {
                parts.add(convertMarkdownToJiraMarkdown(trimmed));
            }
        }
        return String.join("\n\n", parts).trim();
    }

    private static String convertHtmlToJiraMarkdown(String html) {
        HTMLCodeBlockPreserver preserver = new HTMLCodeBlockPreserver();
        String preservedHtml = preserver.preserveCodeBlocks(html);

        Document doc = Jsoup.parse(preservedHtml);
        Elements children = doc.body().children();

        List<String> blocks = new ArrayList<>();

        // We'll index over the children so we can peek ahead.
        for (int i = 0; i < children.size(); i++) {
            Element el = children.get(i);
            String tag = el.tagName().toLowerCase();

            // 1) If current is <strong> and next sibling is <ul>, combine them:
            if ("strong".equals(tag) && i + 1 < children.size()) {
                Element nextEl = children.get(i + 1);
                if ("ul".equalsIgnoreCase(nextEl.tagName())) {
                    // Convert <strong> text to heading
                    String heading = "# *" + el.text().trim() + "*";
                    // Convert <ul> to bullet lines
                    String bulletList = processUnorderedList(nextEl);

                    blocks.add(heading + "\n" + bulletList);

                    // skip the next sibling <ul> because we already processed it
                    i++;
                    continue;
                }
            }

            // Otherwise do your normal switch logic:
            switch (tag) {
                case "h1":
                case "h2":
                case "h3":
                case "h4":
                case "h5":
                case "h6": {
                    int level = Integer.parseInt(tag.substring(1));
                    blocks.add("h" + level + ". " + el.text());
                    break;
                }
                case "p":
                    blocks.add(processParagraph(el));
                    break;
                case "pre":
                    blocks.add(processPre(el));
                    break;
                case "ul":
                    blocks.add(processUnorderedList(el));
                    break;
                case "ol":
                    blocks.add(processOrderedList(el));
                    break;
                case "table":
                    blocks.add(processTable(el));
                    break;
                case "code":
                    blocks.add(processCodeElement(el));
                    break;
                case "a":
                    blocks.add("[" + el.text() + "|" + el.attr("href") + "]");
                    break;
                // If it's a top-level <strong> that didn't match <ul> next,
                // fallback to treating it as a heading or bold.
                case "strong":
                    // Maybe default to heading or just bold?
                    blocks.add("# *" + el.text().trim() + "*");
                    break;
                default:
                    blocks.add(processGenericBlock(el));
            }
        }

        String processed = String.join("\n\n", removeEmpty(blocks)).trim();
        return preserver.restoreCodeBlocks(processed);
    }



    private static String processParagraph(Element p) {
        // If it has code block placeholders, just return as-is so restore can happen
        if (p.html().contains(HTMLCodeBlockPreserver.CODE_BLOCK_PLACEHOLDER)) {
            // remove <code> tags around the placeholder to avoid <code>{code:java}...{code}</code>
            return p.html().replaceAll("(?i)</?code[^>]*>", "");
        }
        String placeholderPattern = Pattern.quote(HTMLCodeBlockPreserver.CODE_BLOCK_PLACEHOLDER) + "\\d+";
        String text = p.html()
                .replaceAll("(?i)<a\\s+href=\"([^\"]+)\">(.*?)</a>", "[$2|$1]")
                .replaceAll("(?i)<strong>(.*?)</strong>", "*$1*")
                .replaceAll("(?i)<em>(.*?)</em>", "_$1_")
                // inline code that isn't a placeholder
                .replaceAll("(?i)<code>(?!" + placeholderPattern + ")(.*?)</code>", "{{$1}}")
                // remove other tags
                .replaceAll("(?i)<[^>]+>", "");
        return unescapeHtml(text).trim();
    }

    /**
     * Helper to handle a top-level <code> element.
     * If it contains placeholders, remove <code> tags but keep placeholder text.
     * Otherwise, if multiline or has a class => treat as block code, else inline.
     */
    private static String processCodeElement(Element codeEl) {
        String html = codeEl.html(); // the inner text/HTML
        String outer = codeEl.outerHtml(); // includes <code> ... </code>

        // If placeholders are present, strip the <code> tags so the placeholder alone remains
        if (outer.contains(HTMLCodeBlockPreserver.CODE_BLOCK_PLACEHOLDER)) {
            return outer.replaceAll("(?i)</?code[^>]*>", "");
        }

        // If no placeholders => produce JIRA code block or inline:
        String codeText = Parser.unescapeEntities(html, false)
                .replaceAll("^[\\r\\n]+", "")
                .replaceAll("[\\r\\n]+$", "");
        // default
        String lang = "java";
        if (codeEl.hasAttr("class") && !codeEl.attr("class").trim().isEmpty()) {
            lang = codeEl.attr("class").trim();
        }
        // multiline => block code
        if (codeText.contains("\n")) {
            return "{code:" + lang + "}\n" + codeText + "\n{code}";
        } else {
            // single-line => inline code
            return "{{" + codeText + "}}";
        }
    }

    private static String processPre(Element pre) {
        Element codeEl = pre.selectFirst("code");
        if (codeEl != null) {
            // If it contains a preserved code block placeholder, keep it as-is but remove the <code> tags:
            if (codeEl.html().contains(HTMLCodeBlockPreserver.CODE_BLOCK_PLACEHOLDER)) {
                // remove <code> so we don't end up with <code>{code:java}..{code}</code>
                return codeEl.html().replaceAll("(?i)</?code[^>]*>", "");
            }

            // Otherwise convert to JIRA {code}
            String codeHtml = codeEl.html();
            String codeText = Parser.unescapeEntities(codeHtml, false)
                    .replaceAll("^[\\r\\n]+", "")
                    .replaceAll("[\\r\\n]+$", "");
            String lang = "java";
            if (codeEl.hasAttr("class") && !codeEl.attr("class").trim().isEmpty()) {
                lang = codeEl.attr("class").trim();
            }
            return "{code:" + lang + "}\n" + codeText + "\n{code}";
        }
        // fallback
        return unescapeHtml(pre.text());
    }

    private static String processUnorderedList(Element ul) {
        StringBuilder sb = new StringBuilder();
        Elements liList = ul.select("> li");

        for (Element li : liList) {
            // 1) We'll clone the li
            Element liClone = li.clone();
            // remove nested <ul>/<ol> so we only get the immediate text
            liClone.select("ul,ol").remove();

            // 2) Remove <code> tags around placeholders so we don't end up with <code>___CODE_BLOCK_PLACEHOLDER___0</code>
            String raw = liClone.html().replaceAll("(?i)</?code[^>]*>", "");

            // 3) Convert <em>, <strong>, <a>, etc. to Jira equivalents
            //    Also handle normal code vs placeholders
            String placeholderPattern = Pattern.quote(HTMLCodeBlockPreserver.CODE_BLOCK_PLACEHOLDER) + "\\d+";
            String liText = raw
                    .replaceAll("(?i)<a\\s+href=\"([^\"]+)\">(.*?)</a>", "[$2|$1]")
                    .replaceAll("(?i)<strong>(.*?)</strong>", "*$1*")
                    .replaceAll("(?i)<em>(.*?)</em>", "_$1_")
                    .replaceAll("(?i)<code>(?!" + placeholderPattern + ")(.*?)</code>", "{{$1}}")
                    .replaceAll("(?i)<[^>]+>", ""); // remove leftover HTML

            liText = unescapeHtml(liText).trim();

            // 4) If there's any text, prefix with "* "
            if (!liText.isEmpty()) {
                sb.append("* ").append(liText).append("\n");
            }

            // 5) Now handle any nested <ul>/<ol> from the original li
            for (Element child : li.children()) {
                if (child.tagName().equalsIgnoreCase("ul")) {
                    sb.append(processUnorderedList(child)).append("\n");
                } else if (child.tagName().equalsIgnoreCase("ol")) {
                    sb.append(processOrderedList(child)).append("\n");
                }
            }
        }

        return sb.toString().trim();
    }


    private static String processOrderedList(Element ol) {
        StringBuilder sb = new StringBuilder();
        Elements liList = ol.select("> li");

        for (Element li : liList) {
            // If <li> contains a <strong> as the first child and then a <ul>, treat it as a heading + bullet list:
            Element strongEl = li.selectFirst("> strong");
            Element nestedUl = li.selectFirst("> ul");
            if (strongEl != null && nestedUl != null) {
                // 1) Make a heading from the <strong> text
                String headingText = strongEl.text().trim();
                sb.append("# *").append(headingText).append("*\n");

                // 2) Convert the nested <ul> to bullet lines
                sb.append(processUnorderedList(nestedUl)).append("\n");
                continue;
            }

            // Otherwise, do your normal “ordered list item” logic:
            // e.g. gather immediate text, then append “# immediate text”
            if (li.html().contains(HTMLCodeBlockPreserver.CODE_BLOCK_PLACEHOLDER)) {
                // remove <code> around placeholders
                sb.append(li.html().replaceAll("(?i)</?code[^>]*>", "")).append("\n");
                continue;
            }

            // Extract immediate text from the li (excluding child <ul>/<ol>)
            Element liClone = li.clone();
            liClone.select("ul,ol").remove();

            String liText = liClone.html()
                    .replaceAll("(?i)<a\\s+href=\"([^\"]+)\">(.*?)</a>", "[$2|$1]")
                    .replaceAll("(?i)<strong>(.*?)</strong>", "*$1*")
                    .replaceAll("(?i)<em>(.*?)</em>", "_$1_")
                    .replaceAll("(?i)<code>(?!"
                            + Pattern.quote(HTMLCodeBlockPreserver.CODE_BLOCK_PLACEHOLDER)
                            + "\\d+)(.*?)</code>", "{{$1}}")
                    .replaceAll("(?i)<[^>]+>", "");

            liText = unescapeHtml(liText).trim();
            if (!liText.isEmpty()) {
                sb.append("# ").append(liText).append("\n");
            }

            // Recurse for nested <ul>/<ol> (except we removed them from liClone, so do it from original)
            for (Element child : li.children()) {
                if (child.tagName().equalsIgnoreCase("ul")) {
                    sb.append(processUnorderedList(child)).append("\n");
                } else if (child.tagName().equalsIgnoreCase("ol")) {
                    sb.append(processOrderedList(child)).append("\n");
                }
            }
        }

        return sb.toString().trim();
    }


    private static String processTable(Element table) {
        StringBuilder sb = new StringBuilder();
        Elements rows = table.select("tr");
        boolean headerDone = false;
        for (Element row : rows) {
            Elements cells = row.select("th, td");
            if (cells.isEmpty()) continue;
            if (!headerDone && row.select("th").size() > 0) {
                sb.append("||");
                for (Element th : cells) {
                    sb.append(unescapeHtml(th.text().trim())).append("||");
                }
                sb.append("\n");
                headerDone = true;
            } else {
                sb.append("|");
                for (Element cell : cells) {
                    if (cell.html().contains(HTMLCodeBlockPreserver.CODE_BLOCK_PLACEHOLDER)) {
                        String stripped = cell.html().replaceAll("(?i)</?code[^>]*>", "");
                        sb.append(stripped).append("|");
                    } else {
                        String placeholderPattern = Pattern.quote(HTMLCodeBlockPreserver.CODE_BLOCK_PLACEHOLDER) + "\\d+";
                        String cellText = cell.html()
                                .replaceAll("(?i)<a\\s+href=\"([^\"]+)\">(.*?)</a>", "[$2|$1]")
                                .replaceAll("(?i)<strong>(.*?)</strong>", "*$1*")
                                .replaceAll("(?i)<em>(.*?)</em>", "_$1_")
                                .replaceAll("(?i)<code>(?!" + placeholderPattern + ")(.*?)</code>", "{{$1}}")
                                .replaceAll("(?i)<[^>]+>", "");
                        sb.append(unescapeHtml(cellText.trim())).append("|");
                    }
                }
                sb.append("\n");
            }
        }
        return sb.toString().trim();
    }

    private static String processGenericBlock(Element el) {
        // If code placeholder is present, remove <code> tags & keep placeholder
        if (el.html().contains(HTMLCodeBlockPreserver.CODE_BLOCK_PLACEHOLDER)) {
            return el.html().replaceAll("(?i)</?code[^>]*>", "");
        }

        // Check if this block has a top-level <strong> and <ul>.
        Elements topChildren = el.children();

        // For instance, if there are exactly 2 children: <strong> + <ul>
        if (topChildren.size() == 2
                && topChildren.get(0).tagName().equalsIgnoreCase("strong")
                && topChildren.get(1).tagName().equalsIgnoreCase("ul"))
        {
            // (1) Convert <strong>...</strong> => heading
            String headingText = topChildren.get(0).text().trim();
            String headingMarkdown = "# *" + headingText + "*";

            // (2) Convert <ul> => bullet lines
            String bulletMarkdown = processUnorderedList(topChildren.get(1));

            // Return them combined
            return headingMarkdown + "\n" + bulletMarkdown;
        }

        // Otherwise fallback
        String placeholderPattern = Pattern.quote(HTMLCodeBlockPreserver.CODE_BLOCK_PLACEHOLDER) + "\\d+";
        String html = el.html()
                .replaceAll("(?i)<a\\s+href=\"([^\"]+)\">(.*?)</a>", "[$2|$1]")
                .replaceAll("(?i)<strong>(.*?)</strong>", "*$1*")
                .replaceAll("(?i)<em>(.*?)</em>", "_$1_")
                .replaceAll("(?i)<code>(?!" + placeholderPattern + ")(.*?)</code>", "{{$1}}")
                .replaceAll("(?i)<[^>]+>", "");

        return unescapeHtml(html).trim();
    }



    private static String convertMarkdownToJiraMarkdown(String markdown) {
        String[] lines = markdown.split("\n");
        List<String> blocks = new ArrayList<>();

        boolean inCodeBlock = false;
        StringBuilder codeBuf = new StringBuilder();
        String codeLang = "";
        StringBuilder paragraph = new StringBuilder();

        for (String line : lines) {
            if (line.startsWith("```")) {
                if (!inCodeBlock) {
                    if (paragraph.length() > 0) {
                        blocks.add(processTextParagraph(paragraph.toString()));
                        paragraph.setLength(0);
                    }
                    inCodeBlock = true;
                    codeLang = line.substring(3).trim();
                } else {
                    // end code block
                    String code = codeBuf.toString()
                            .replaceAll("^[\\r\\n]+", "")
                            .replaceAll("[\\r\\n]+$", "");
                    blocks.add("{code:" + (codeLang.isEmpty() ? "java" : codeLang) + "}\n" + code + "\n{code}");
                    inCodeBlock = false;
                    codeBuf.setLength(0);
                    codeLang = "";
                }
                continue;
            }

            if (inCodeBlock) {
                codeBuf.append(line).append("\n");
            } else {
                if (line.trim().isEmpty()) {
                    if (paragraph.length() > 0) {
                        blocks.add(processTextParagraph(paragraph.toString()));
                        paragraph.setLength(0);
                    }
                } else {
                    if (paragraph.length() > 0) {
                        paragraph.append("\n");
                    }
                    paragraph.append(line);
                }
            }
        }

        if (paragraph.length() > 0) {
            blocks.add(processTextParagraph(paragraph.toString()));
        }

        return String.join("\n\n", blocks).trim();
    }

    private static String processTextParagraph(String text) {
        String[] lines = text.split("\n");
        List<String> output = new ArrayList<>();

        for (String line : lines) {
            String trimmed = line.trim();
            // The code below is to handle e.g. "1. **Authentication**" => "*Authentication*"
            trimmed = trimmed.replaceAll("^1\\. \\*\\*(.*?)\\*\\*", "*$1*");

            // Check headings
            Matcher headingMatch = HEADING_PATTERN.matcher(trimmed);
            if (headingMatch.matches()) {
                String hashes = headingMatch.group(1);
                String headingText = headingMatch.group(2);
                int level = hashes.length();
                output.add("h" + level + ". " + headingText);
                continue;
            }

            // inline code, bold, links
            trimmed = trimmed.replaceAll("`\\s*([^`]+)\\s*`", "{{$1}}");
            trimmed = trimmed.replaceAll("\\*\\*([^*]+)\\*\\*", "*$1*");
            trimmed = trimmed.replaceAll("\\[([^\\]]+)\\]\\(([^)]+)\\)", "[$1|$2]");

            output.add(trimmed);
        }

        return String.join("\n", output);
    }

    private static boolean containsHtml(String s) {
        String noCodeBlocks = CODE_BLOCK_PATTERN.matcher(s).replaceAll("");
        String noCode = noCodeBlocks.replaceAll("`[^`]*`", "");
        return HTML_PATTERN.matcher(noCode).find();
    }

    private static String decodeEntities(String s) {
        return s.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'");
    }

    private static String unescapeHtml(String s) {
        return s
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("-&gt;", "->")
                .replaceAll("\\R", "\n")
                .trim();
    }

    private static List<String> removeEmpty(List<String> blocks) {
        List<String> cleaned = new ArrayList<>();
        for (String b : blocks) {
            String t = b.trim();
            if (!t.isEmpty()) {
                cleaned.add(t);
            }
        }
        return cleaned;
    }
}
