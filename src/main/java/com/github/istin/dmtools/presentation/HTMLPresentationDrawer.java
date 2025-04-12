package com.github.istin.dmtools.presentation;

import com.github.istin.dmtools.report.ReportUtils;
import freemarker.template.TemplateException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HTMLPresentationDrawer {

    public File printPresentation(String topic, JSONObject presentation) throws TemplateException, IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("presentationData", presentation);
        File file = new ReportUtils().write(topic + "_presentation", "presentation", map);
        System.out.println("=== PRESENTATION FILE ===");
        System.out.println(file.getAbsolutePath());
        return file;
    }

}
