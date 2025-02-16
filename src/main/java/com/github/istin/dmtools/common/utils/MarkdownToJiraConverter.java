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

    /**
     * Split by blank lines; decide if chunk is HTML or Markdown.
     */
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

    // ------------------- HTML -> JIRA -------------------

    private static String convertHtmlToJiraMarkdown(String html) {
        Document doc = Jsoup.parse(html);
        Elements children = doc.body().children();

        List<String> blocks = new ArrayList<>();
        for (Element el : children) {
            switch (el.tagName().toLowerCase()) {
                case "h1":
                case "h2":
                case "h3":
                case "h4":
                case "h5":
                case "h6": {
                    int level = Integer.parseInt(el.tagName().substring(1));
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
                    // If top-level <code> with class, treat as code block
                    if (el.hasAttr("class") && !el.attr("class").trim().isEmpty()) {
                        String lang = el.attr("class").trim();
                        String codeText = el.wholeText()
                                .replaceAll("^[\\r\\n]+", "")
                                .replaceAll("[\\r\\n]+$", "");
                        blocks.add("{code:" + lang + "}\n" + codeText + "\n{code}");
                    } else {
                        // inline code
                        blocks.add("{{" + el.wholeText() + "}}");
                    }
                    break;
                case "a":
                    // anchor at top-level
                    blocks.add("[" + el.text() + "|" + el.attr("href") + "]");
                    break;
                default:
                    blocks.add(processGenericBlock(el));
                    break;
            }
        }

        return String.join("\n\n", removeEmpty(blocks)).trim();
    }

    /**
     * If <p> has exactly one <code> child with class="java" => always produce {code:java} block.
     */
    private static String processParagraph(Element p) {
        if (p.children().size() == 1 && "code".equalsIgnoreCase(p.child(0).tagName())
                && p.ownText().trim().isEmpty()) {

            Element codeEl = p.child(0);
            String codeText = codeEl.wholeText()
                    .replaceAll("^[\\r\\n]+", "")
                    .replaceAll("[\\r\\n]+$", "");

            // If there's a class => always produce a code block
            if (codeEl.hasAttr("class") && !codeEl.attr("class").trim().isEmpty()) {
                String lang = codeEl.attr("class").trim();
                return "{code:" + lang + "}\n" + codeText + "\n{code}";
            } else {
                // No class => check multiline
                if (codeText.contains("\n")) {
                    return "{code:java}\n" + codeText + "\n{code}";
                } else {
                    // single line => inline code
                    return "{{" + codeText + "}}";
                }
            }
        }

        // Otherwise inline transforms
        String text = p.html()
                .replaceAll("<a\\s+href=\"([^\"]+)\">(.*?)</a>", "[$2|$1]")
                .replaceAll("<strong>(.*?)</strong>", "*$1*")
                .replaceAll("<em>(.*?)</em>", "_$1_")
                // FORCING normal <code> => inline
                .replaceAll("<code>(.*?)</code>", "{{$1}}")
                .replaceAll("<[^>]+>", "");

        return unescapeHtml(text).trim();
    }

    /**
     * <pre><code class="java"> => forced block code
     */
    private static String processPre(Element pre) {
        Element codeEl = pre.selectFirst("code");
        if (codeEl != null) {
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
        return unescapeHtml(pre.text());
    }

    private static String processUnorderedList(Element ul) {
        StringBuilder sb = new StringBuilder();
        for (Element li : ul.select("li")) {
            String liHtml = li.html()
                    .replaceAll("<a\\s+href=\"([^\"]+)\">(.*?)</a>", "[$2|$1]")
                    .replaceAll("<strong>(.*?)</strong>", "*$1*")
                    .replaceAll("<em>(.*?)</em>", "_$1_")
                    .replaceAll("<code>(.*?)</code>", "{{$1}}")
                    .replaceAll("<[^>]+>", "");
            sb.append("* ").append(unescapeHtml(liHtml.trim())).append("\n");
        }
        return sb.toString().trim();
    }

    private static String processOrderedList(Element ol) {
        StringBuilder sb = new StringBuilder();
        for (Element li : ol.select("li")) {
            String liHtml = li.html()
                    .replaceAll("<a\\s+href=\"([^\"]+)\">(.*?)</a>", "[$2|$1]")
                    .replaceAll("<strong>(.*?)</strong>", "*$1*")
                    .replaceAll("<em>(.*?)</em>", "_$1_")
                    .replaceAll("<code>(.*?)</code>", "{{$1}}")
                    .replaceAll("<[^>]+>", "");
            sb.append("# ").append(unescapeHtml(liHtml.trim())).append("\n");
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
                    Element codeEl = cell.selectFirst("code");
                    if (codeEl != null && codeEl.hasAttr("class") && !codeEl.attr("class").trim().isEmpty()) {
                        String lang = codeEl.attr("class").trim();
                        String codeHtml = codeEl.html();
                        String codeText = Parser.unescapeEntities(codeHtml, false)
                                .replaceAll("^[\\r\\n]+", "")
                                .replaceAll("[\\r\\n]+$", "")
                                .trim();
                        sb.append("{code:").append(lang).append("}\n")
                                .append(codeText)
                                .append("\n{code}|");
                    } else {
                        String cellText = cell.html()
                                .replaceAll("<a\\s+href=\"([^\"]+)\">(.*?)</a>", "[$2|$1]")
                                .replaceAll("<strong>(.*?)</strong>", "*$1*")
                                .replaceAll("<em>(.*?)</em>", "_$1_")
                                .replaceAll("<code>(.*?)</code>", "{{$1}}")
                                .replaceAll("<[^>]+>", "");
                        sb.append(unescapeHtml(cellText.trim())).append("|");
                    }
                }
                sb.append("\n");
            }
        }
        return sb.toString().trim();
    }

    private static String processGenericBlock(Element el) {
        String html = el.html()
                .replaceAll("<a\\s+href=\"([^\"]+)\">(.*?)</a>", "[$2|$1]")
                .replaceAll("<strong>(.*?)</strong>", "*$1*")
                .replaceAll("<em>(.*?)</em>", "_$1_")
                .replaceAll("<code>(.*?)</code>", "{{$1}}")
                .replaceAll("<[^>]+>", "");
        return unescapeHtml(html).trim();
    }

    // ------------------- Markdown -> JIRA -------------------

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
                    // start code fence
                    if (paragraph.length() > 0) {
                        blocks.add(processTextParagraph(paragraph.toString()));
                        paragraph.setLength(0);
                    }
                    inCodeBlock = true;
                    codeLang = line.substring(3).trim(); // "```java" => "java"
                } else {
                    // end code fence
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
                    // flush paragraph
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

        // leftover paragraph
        if (paragraph.length() > 0) {
            blocks.add(processTextParagraph(paragraph.toString()));
        }

        return String.join("\n\n", blocks).trim();
    }

    /**
     * Process a chunk of Markdown text (no code fences),
     * splitting by lines to handle bullet lines and headings exactly.
     */
    private static String processTextParagraph(String text) {
        String[] lines = text.split("\n");
        List<String> output = new ArrayList<>();

        for (String line : lines) {
            String trimmed = line.trim();
            // If heading
            Matcher headingMatch = HEADING_PATTERN.matcher(trimmed);
            if (headingMatch.matches()) {
                String hashes = headingMatch.group(1);
                String headingText = headingMatch.group(2);
                int level = hashes.length();
                output.add("h" + level + ". " + headingText);
                continue;
            }

            // Remove "1. **..."
            trimmed = trimmed.replaceAll("^1\\. \\*\\*(.*?)\\*\\*", "*$1*");

            // If bullet line
            if (trimmed.startsWith("* ")) {
                // do inline
                output.add(processInlineMarkdown(trimmed));
            } else {
                // normal line => inline
                output.add(processInlineMarkdown(trimmed));
            }
        }

        // Join lines with \n
        return String.join("\n", output);
    }

    /**
     * Convert inline code, bold, links, etc.
     */
    private static String processInlineMarkdown(String line) {
        // Update the regex in processInlineMarkdown to handle spaces around backticks
        line = line.replaceAll("`\\s*([^`]+)\\s*`", "{{$1}}");

        // bold: **text** => *text*
        line = line.replaceAll("\\*\\*([^*]+)\\*\\*", "*$1*");


        // links: [text](url) => [text|url]
        line = line.replaceAll("\\[([^\\]]+)\\]\\(([^)]+)\\)", "[$1|$2]");

        // italic _text_ => same in JIRA, so no change needed
        return line;
    }

    // ------------------- Helpers -------------------

    private static boolean containsHtml(String s) {
        // Remove Markdown code blocks and inline code before checking for HTML
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
        return s.replace("&lt;", "<")
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
