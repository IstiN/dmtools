package com.github.istin.dmtools.server.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
public class SpaForwardController implements ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(SpaForwardController.class);
    private static final String STATIC_PREFIX = "classpath:/static/";

    private final ResourceLoader resourceLoader;

    public SpaForwardController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping("/")
    public String root() {
        String indexPath = resolveIndexPath();
        logger.debug("SPA forward root -> {}", indexPath);
        return "forward:" + indexPath;
    }

    @RequestMapping("/error")
    public String error() {
        String indexPath = resolveIndexPath();
        logger.debug("SPA forward error -> {}", indexPath);
        return "forward:" + indexPath;
    }

    private String resolveIndexPath() {
        // Prefer /index.html at static root
        if (resourceExists(STATIC_PREFIX + "index.html")) {
            return "/index.html";
        }
        // Try to find any nested SPA index: static/**/index.html
        // We don't have glob resource scanning without ResourcePatternResolver here, so probe common folder name
        // If a single top-level folder exists, try /{folder}/index.html
        String candidate = findNestedIndex();
        if (candidate != null) {
            return candidate;
        }
        // Fallback to swagger if no SPA found
        return "/swagger-ui.html";
    }

    private boolean resourceExists(String location) {
        try {
            Resource r = resourceLoader.getResource(location);
            return r.exists() && r.isReadable();
        } catch (Exception e) {
            return false;
        }
    }

    private String findNestedIndex() {
        // Heuristic: look for single-child directory under static/ and check index.html there
        try {
            Resource staticDir = resourceLoader.getResource(STATIC_PREFIX);
            java.net.URL url = staticDir.getURL();
            if (url != null && "jar".equals(url.getProtocol())) {
                // When packaged, we can list via JarURLConnection path
                String urlStr = url.toString();
                // Expect ending with !/static/
                int bang = urlStr.indexOf("!/");
                if (bang > 0) {
                    String jarPath = urlStr.substring("jar:".length(), bang);
                    try (java.util.jar.JarFile jf = new java.util.jar.JarFile(jarPath)) {
                        AntPathMatcher matcher = new AntPathMatcher();
                        String found = null;
                        for (java.util.Enumeration<java.util.jar.JarEntry> en = jf.entries(); en.hasMoreElements();) {
                            java.util.jar.JarEntry je = en.nextElement();
                            String name = je.getName();
                            if (matcher.match("BOOT-INF/classes/static/**/index.html", name)) {
                                // Return web path by stripping BOOT-INF/classes
                                String web = name.replaceFirst("^BOOT-INF/classes", "");
                                found = "/" + web.replaceFirst("^/", "");
                                break;
                            }
                        }
                        return found;
                    }
                }
            }
        } catch (IOException ignored) {
        }
        return null;
    }
}


