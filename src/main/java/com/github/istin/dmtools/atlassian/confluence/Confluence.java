package com.github.istin.dmtools.atlassian.confluence;

import com.github.istin.dmtools.atlassian.common.networking.AtlassianRestClient;
import com.github.istin.dmtools.atlassian.confluence.model.Content;
import com.github.istin.dmtools.atlassian.confluence.model.ContentResult;
import com.github.istin.dmtools.common.networking.GenericRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class Confluence extends AtlassianRestClient {

    public Confluence(String basePath, String authorization) throws IOException {
        super(basePath, authorization);
        setClearCache(true);
        setCacheGetRequestsEnabled(false);
    }

    @Override
    public String path(String path) {
        return getBasePath() + "/rest/api/" + path;
    }

    public ContentResult content(String title, String space) throws IOException {
        GenericRequest content = new GenericRequest(this, path("content?expand=body.storage"));
        content.param("title", title);
        content.param("spaceKey", space);
        String response = execute(content);
        try {
            return new ContentResult(response);
        } catch (Exception e) {
            System.err.println(response);
            throw e;
        }
    }

    public Content findContent(String title, String space) throws IOException {
        List<Content> contents = content(title, space).getContents();
        if (contents.isEmpty()) {
            return null;
        } else {
            return contents.get(0);
        }
    }

    public Content createPage(String title, String parentId, String body, String space) throws IOException {
        GenericRequest content = new GenericRequest(this, path("content"));
        content.setBody(new JSONObject()
                .put("type", "page")
                .put("title", title)
                .put("ancestors", new JSONArray().put(new JSONObject().put("id", parentId)))
                .put("space", new JSONObject().put("key", space))
                .put("body", new JSONObject().put("storage", new JSONObject()
                        .put("value", body)
                        .put("representation", "storage"))).toString());
        return new Content(content.post());
    }

    public Content updatePage(String contentId, String title, String parentId, String body, String space) throws IOException {
        Content oldContent = new Content(new GenericRequest(this, path("content/" + contentId + "?expand=version")).execute());

        GenericRequest content = new GenericRequest(this, path("content/"+contentId));
        String value = body.contains("ac:name=\"html\"") ? body : body
                .replaceAll("M&S", "M&amp;S")
                .replaceAll("s&S", "s&amp;S")
                .replaceAll(" & ", " &amp; ")
                .replaceAll("g&d", "g&amp;D")
                .replaceAll("o&S", "o&amp;S");


        value = value.replace("&C", "&amp;C");
        content.setBody(new JSONObject()
                .put("id", contentId)
                .put("type", "page")
                .put("title", title)
                .put("ancestors", new JSONArray().put(new JSONObject().put("id", parentId)))
                .put("space", new JSONObject().put("key", space))
                .put("version", new JSONObject().put("number", oldContent.getVersionNumber() + 1))
                .put("body", new JSONObject().put("storage", new JSONObject()
                        .put("value", value)
                        .put("representation", "storage"))).toString());
        String putResponse = content.put();
        System.out.println(putResponse);
        return new Content(putResponse);
    }

    public static String macroHTML(String body) {
        return "<ac:structured-macro ac:macro-id=\""+System.currentTimeMillis()+"\" ac:name=\"html\" ac:schema-version=\"1\">\n" +
                "  <ac:plain-text-body><![CDATA["+body+"]]></ac:plain-text-body>\n" +
                "</ac:structured-macro>";
    }

    public static String macroCloudHTML(String body) {
        return  "<ac:structured-macro ac:name=\"swc-macro-html-input\" ac:schema-version=\"1\" data-layout=\"default\" ac:local-id=\"cbe31bea-aec4-454a-ac9d-7c0611b882d0\" ac:macro-id=\"8c4516f5-9f3d-4ff9-830d-ab2747611451\"><ac:rich-text-body><ac:structured-macro ac:name=\"code\" ac:schema-version=\"1\" ac:macro-id=\"da0a0dc5-5e02-40cc-9acc-303202ad6165\"><ac:plain-text-body><![CDATA[<body>"
 +body+
        "</body>]]></ac:plain-text-body></ac:structured-macro></ac:rich-text-body></ac:structured-macro><p />";
    }

    /*
    <ac:structured-macro ac:name="swc-macro-html-input" ac:schema-version="1" data-layout="default" ac:local-id="cbe31bea-aec4-454a-ac9d-7c0611b882d0" ac:macro-id="8c4516f5-9f3d-4ff9-830d-ab2747611451"><ac:rich-text-body><ac:structured-macro ac:name="code" ac:schema-version="1" ac:macro-id="da0a0dc5-5e02-40cc-9acc-303202ad6165"><ac:plain-text-body><![CDATA[<body>

</body>]]></ac:plain-text-body></ac:structured-macro></ac:rich-text-body></ac:structured-macro><p />
     */



}