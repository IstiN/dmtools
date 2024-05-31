package com.github.istin.dmtools.common.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class HtmlCleaner {

    public static String cleanUselessHTMLTagsAndAdjustImageUrls(String basePath, String taggedInput) {
        Document document = Jsoup.parse(taggedInput);
        document.outputSettings(new org.jsoup.nodes.Document.OutputSettings().prettyPrint(false));
        // Remove all style attributes
        document.select("[style]").removeAttr("style");
        document.select("[class]").removeAttr("class");
        Elements paragraphs = document.select("p");
        for (Element paragraph : paragraphs) {
            if (paragraph.html().replaceAll("&nbsp;", " ").trim().isEmpty()) {
                paragraph.remove();
            }
        }
        // Remove all <span> tags but keep their content
        document.select("span").unwrap();

        adjustImageUrls(basePath, document);

        return document.body().html();
    }

    public static void adjustImageUrls(String basePath, Document document) {
        // Select all img tags with src attributes
        Elements imgTags = document.select("img[src]");

        // Attach the base path to each img tag
        for (Element img : imgTags) {
            String originalSrc = img.attr("src");
            if (!originalSrc.startsWith(basePath)) {
                String modifiedSrc = basePath + originalSrc;
                img.attr("src", modifiedSrc);
            }
        }
    }

    public static List<String> getAllImageUrls(String basePath, String htmlDescription) {
        Document document = Jsoup.parse(htmlDescription);
        Elements imgTags = document.select("img[src]");

        List<String> urls = new ArrayList<>();
        // Attach the base path to each img tag
        for (Element img : imgTags) {
            String originalSrc = img.attr("src");
            if (!originalSrc.startsWith(basePath)) {
                String modifiedSrc = basePath + originalSrc;
                urls.add(modifiedSrc);
            }
        }
        return urls;
    }

    public static List<String> getAllLinksUrls(String urlFilter, String htmlDescription) {
        Document document = Jsoup.parse(htmlDescription);
        Elements aTags = document.select("a[href]");

        List<String> urls = new ArrayList<>();
        // Attach the base path to each img tag
        for (Element a : aTags) {
            String originalHref = a.attr("href");
            if (urlFilter != null && originalHref.contains(urlFilter)) {
                urls.add(originalHref);
            }
        }
        return urls;
    }

    public static String cleanAllHtmlTags(String basePath, String html) {
        // Use jsoup to clean and minimize the HTML
        Document document = Jsoup.parse(html);
        adjustImageUrls(basePath, document);
        // Create a Safelist that allows <a> tags
        Safelist safelist = Safelist.none();
        safelist.addTags("a");
        safelist.addAttributes("a","href");
        safelist.addTags("img");
        safelist.addAttributes("img","src");
        return Jsoup.clean(document.body().html(), "", safelist, new org.jsoup.nodes.Document.OutputSettings().prettyPrint(false)).replaceAll("&nbsp;"," ");
    }

    public static String convertLinksUrlsToConfluenceFormat(String body) {
        // Parse the HTML
        Document doc = Jsoup.parse(body);
        doc.outputSettings(new org.jsoup.nodes.Document.OutputSettings().prettyPrint(false));
        // Select all anchor tags with href attributes
        Elements anchorTags = doc.select("a[href]");

        // Modify the href attributes: replacing "&", with "&amp;"
        for (Element anchor : anchorTags) {
            String originalHref = anchor.attr("href");
            System.out.println(originalHref);
        }

        // Print the modified HTML
        return doc.body().html();
    }

}
