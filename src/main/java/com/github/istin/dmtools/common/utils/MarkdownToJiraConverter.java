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
        // First preserve code blocks
        HTMLCodeBlockPreserver preserver = new HTMLCodeBlockPreserver();
        String preservedHtml = preserver.preserveCodeBlocks(html);

        Document doc = Jsoup.parse(preservedHtml);
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
                    // Just keep the preserved code block placeholder
                    blocks.add(el.outerHtml());
                    break;
                case "a":
                    blocks.add("[" + el.text() + "|" + el.attr("href") + "]");
                    break;
                default:
                    blocks.add(processGenericBlock(el));
                    break;
            }
        }

        String processed = String.join("\n\n", removeEmpty(blocks)).trim();

        // Restore code blocks with JIRA formatting
        return preserver.restoreCodeBlocks(processed);
    }

    private static String processParagraph(Element p) {
        // If paragraph contains preserved code block, keep it as is
        if (p.html().contains(HTMLCodeBlockPreserver.CODE_BLOCK_PLACEHOLDER)) {
            return p.html();
        }

        String text = p.html()
                .replaceAll("<a\\s+href=\"([^\"]+)\">(.*?)</a>", "[$2|$1]")
                .replaceAll("<strong>(.*?)</strong>", "*$1*")
                .replaceAll("<em>(.*?)</em>", "_$1_")
                .replaceAll("<code>(.*?)</code>", "{{$1}}")
                .replaceAll("<[^>]+>", "");

        return unescapeHtml(text).trim();
    }

    private static String processPre(Element pre) {
        Element codeEl = pre.selectFirst("code");
        if (codeEl != null) {
            // If it contains a preserved code block, keep it as is
            if (codeEl.html().contains(HTMLCodeBlockPreserver.CODE_BLOCK_PLACEHOLDER)) {
                return codeEl.outerHtml();
            }

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
                    // If cell contains preserved code block, keep it as is
                    if (cell.html().contains(HTMLCodeBlockPreserver.CODE_BLOCK_PLACEHOLDER)) {
                        sb.append(cell.html()).append("|");
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
        // If element contains preserved code block, keep it as is
        if (el.html().contains(HTMLCodeBlockPreserver.CODE_BLOCK_PLACEHOLDER)) {
            return el.html();
        }

        String html = el.html()
                .replaceAll("<a\\s+href=\"([^\"]+)\">(.*?)</a>", "[$2|$1]")
                .replaceAll("<strong>(.*?)</strong>", "*$1*")
                .replaceAll("<em>(.*?)</em>", "_$1_")
                .replaceAll("<code>(.*?)</code>", "{{$1}}")
                .replaceAll("<[^>]+>", "");
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
            Matcher headingMatch = HEADING_PATTERN.matcher(trimmed);
            if (headingMatch.matches()) {
                String hashes = headingMatch.group(1);
                String headingText = headingMatch.group(2);
                int level = hashes.length();
                output.add("h" + level + ". " + headingText);
                continue;
            }

            trimmed = trimmed.replaceAll("^1\\. \\*\\*(.*?)\\*\\*", "*$1*");

            if (trimmed.startsWith("* ")) {
                output.add(processInlineMarkdown(trimmed));
            } else {
                output.add(processInlineMarkdown(trimmed));
            }
        }

        return String.join("\n", output);
    }

    private static String processInlineMarkdown(String line) {
        line = line.replaceAll("`\\s*([^`]+)\\s*`", "{{$1}}");
        line = line.replaceAll("\\*\\*([^*]+)\\*\\*", "*$1*");
        line = line.replaceAll("\\[([^\\]]+)\\]\\(([^)]+)\\)", "[$1|$2]");
        return line;
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