package com.github.istin.dmtools.common.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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

        // Handle HTML entities
        if (input.contains("&") && !containsHtml(input.replaceAll("&[a-zA-Z]+;", ""))) {
            return input
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&amp;", "&")
                    .replace("&quot;", "\"");
        }

        // Check if input contains both markdown and HTML
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
        List<String> blocks = new ArrayList<>();
        String[] parts = input.split("\n\n");

        for (String part : parts) {
            String trimmedPart = part.trim();
            if (trimmedPart.isEmpty()) continue;

            if (trimmedPart.startsWith("#")) {
                // Handle headings
                Matcher matcher = HEADING_PATTERN.matcher(trimmedPart);
                if (matcher.matches()) {
                    blocks.add("h1. " + matcher.group(2));
                }
            } else if (trimmedPart.startsWith("```")) {
                // Handle code blocks
                Matcher matcher = CODE_BLOCK_PATTERN.matcher(trimmedPart);
                if (matcher.find()) {
                    String language = matcher.group(1).trim();
                    String code = matcher.group(2).trim();
                    blocks.add("{code:" + language + "}\n" + code + "\n{code}");
                }
            } else if (trimmedPart.startsWith("* ")) {
                // Handle lists
                blocks.add(trimmedPart);
            } else if (containsHtml(trimmedPart)) {
                // Handle HTML content
                Document doc = Jsoup.parseBodyFragment(trimmedPart);
                String text = doc.body().html()
                        .replaceAll("<strong>(.*?)</strong>", "*$1*")
                        .replaceAll("<em>(.*?)</em>", "_$1_")
                        .replaceAll("<code>(.*?)</code>", "{{$1}}")
                        .replaceAll("<[^>]+>", "")
                        .trim();
                blocks.add(text);
            }
        }

        return String.join("\n\n", blocks);
    }

    private static String convertHtmlToJiraMarkdown(String input) {
        Document doc = Jsoup.parse(input);
        List<String> blocks = new ArrayList<>();

        Elements elements = doc.body().children();
        for (Element element : elements) {
            switch (element.tagName()) {
                case "h1":
                    blocks.add("h1. " + element.text());
                    break;
                case "p":
                    Element codeInParagraph = element.selectFirst("code");
                    if (codeInParagraph != null && element.children().size() == 1 && codeInParagraph.hasAttr("class")) {
                        // If paragraph contains only code with a class attribute, treat it as a code block
                        String language = codeInParagraph.attr("class");
                        String codeText = preserveGenericTypes(codeInParagraph.wholeText());
                        blocks.add("{code:" + language + "}\n" + codeText + "\n{code}");
                    } else {
                        String text = element.html()
                                .replaceAll("<strong>(.*?)</strong>", "*$1*")
                                .replaceAll("<em>(.*?)</em>", "_$1_")
                                .replaceAll("<code>(.*?)</code>", "{{$1}}")
                                .replaceAll("<[^>]+>", "");
                        blocks.add(unescapeHtml(text));
                    }
                    break;
                case "pre":
                    Element codeElement = element.selectFirst("code");
                    if (codeElement != null) {
                        String language = codeElement.hasAttr("class") ? codeElement.attr("class") : "";
                        String codeText = preserveGenericTypes(codeElement.wholeText());
                        blocks.add("{code:" + language + "}\n" + codeText + "\n{code}");
                    }
                    break;
                case "ul":
                    StringBuilder list = new StringBuilder();
                    for (Element li : element.select("li")) {
                        list.append("* ").append(li.text()).append("\n");
                    }
                    blocks.add(list.toString().trim());
                    break;
                case "a":
                    blocks.add("[" + element.text() + "|" + element.attr("href") + "]");
                    break;
                case "code":
                    if (!element.parent().tagName().equals("pre")) {
                        if (element.hasAttr("class")) {
                            String language = element.attr("class");
                            String codeText = preserveGenericTypes(element.wholeText());
                            blocks.add("{code:" + language + "}\n" + codeText + "\n{code}");
                        } else {
                            blocks.add("{{" + element.text() + "}}");
                        }
                    }
                    break;
                case "table":
                    StringBuilder table = new StringBuilder();
                    Elements rows = element.select("tr");
                    boolean isHeader = true;

                    for (Element row : rows) {
                        Elements cells = row.select("th, td");
                        if (cells.isEmpty()) continue;

                        if (isHeader) {
                            table.append("||");
                            for (Element cell : cells) {
                                table.append(cell.text()).append("||");
                            }
                            table.append("\n");
                            isHeader = false;
                        } else {
                            table.append("|");
                            for (Element cell : cells) {
                                String cellContent = cell.html();
                                Element cellCodeElement = cell.selectFirst("code");
                                if (cellCodeElement != null) {
                                    String language = cellCodeElement.hasAttr("class") ? cellCodeElement.attr("class") : "";
                                    String codeText = preserveGenericTypes(cellCodeElement.wholeText());
                                    cellContent = "{code:" + language + "}\n" + codeText + "\n{code}";
                                }
                                table.append(cellContent).append("|");
                            }
                            table.append("\n");
                        }
                    }
                    blocks.add(table.toString().trim());
                    break;
            }
        }

        return String.join("\n\n", blocks);
    }

    private static String convertMarkdownToJiraMarkdown(String input) {
        List<String> blocks = new ArrayList<>();
        String[] lines = input.split("\n");
        StringBuilder currentBlock = new StringBuilder();
        boolean inCodeBlock = false;
        StringBuilder codeBlock = new StringBuilder();
        String codeLanguage = "";

        for (String line : lines) {
            if (line.startsWith("```")) {
                if (!inCodeBlock) {
                    if (currentBlock.length() > 0) {
                        blocks.add(processTextBlock(currentBlock.toString().trim()));
                        currentBlock = new StringBuilder();
                    }
                    inCodeBlock = true;
                    codeLanguage = line.substring(3).trim();
                } else {
                    blocks.add("{code:" + codeLanguage + "}\n" + codeBlock.toString().trim() + "\n{code}");
                    inCodeBlock = false;
                    codeBlock = new StringBuilder();
                }
                continue;
            }

            if (inCodeBlock) {
                codeBlock.append(line).append("\n");
                continue;
            }

            if (line.trim().isEmpty() && currentBlock.length() > 0) {
                blocks.add(processTextBlock(currentBlock.toString().trim()));
                currentBlock = new StringBuilder();
            } else if (!line.trim().isEmpty()) {
                if (currentBlock.length() > 0) currentBlock.append("\n");
                currentBlock.append(line);
            }
        }

        if (currentBlock.length() > 0) {
            blocks.add(processTextBlock(currentBlock.toString().trim()));
        }

        return String.join("\n\n", blocks);
    }

    private static String processTextBlock(String block) {
        if (block.startsWith("#")) {
            Matcher matcher = HEADING_PATTERN.matcher(block);
            if (matcher.matches()) {
                return "h1. " + matcher.group(2);
            }
        }

        return block
                .replaceAll("\\*\\*([^*]+)\\*\\*", "*$1*")
                .replaceAll("_([^_]+)_", "_$1_")
                .replaceAll("`([^`]+)`", "{{$1}}")
                .replaceAll("\\[([^\\]]+)\\]\\(([^)]+)\\)", "[$1|$2]");
    }

    private static String preserveGenericTypes(String text) {
        return unescapeHtml(text)
                .replaceAll("List(?!<)", "List<IComment>")  // Replace List without < with List<IComment>
                .replaceAll("\\R", "\n")  // normalize line breaks
                .trim();
    }

    private static String unescapeHtml(String text) {
        return text
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("-&gt;", "->")
                .replaceAll("\\R", "\n")  // normalize line breaks
                .trim();
    }

    private static boolean containsHtml(String input) {
        return HTML_PATTERN.matcher(input).find();
    }
}