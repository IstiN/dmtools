package com.github.istin.dmtools.presale;

import com.github.istin.dmtools.common.model.JSONModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PreSaleSupportParams extends JSONModel {

    public static final String CONFLUENCE_ROOT_PAGE = "confluenceRootPage";
    public static final String PLATFORMS = "platforms";
    public static final String FOLDER_WITH_PDF_ASSETS = "folderWithPdfAssets";

    public PreSaleSupportParams() {
    }

    public PreSaleSupportParams(String json) throws JSONException {
        super(json);
    }

    public PreSaleSupportParams(JSONObject json) {
        super(json);
    }

    public String getConfluenceRootPage() {
        return getString(CONFLUENCE_ROOT_PAGE);
    }

    public String getFolderWithPdfAssets() {
        return getString(FOLDER_WITH_PDF_ASSETS);
    }

    public List<String> getPlatforms() {
        final JSONArray jsonArray = getJSONArray(PLATFORMS);

        if (jsonArray == null) {
            return Collections.emptyList();
        }

        return IntStream.range(0, jsonArray.length())
                .mapToObj(jsonArray::getString)
                .collect(Collectors.toList());

    }

    public void setFolderWithPdfAssets(String folderWithPdfAssets) {
        set(FOLDER_WITH_PDF_ASSETS, folderWithPdfAssets);
    }

    public void setConfluenceRootPage(String confluenceRootPage) {
        set(CONFLUENCE_ROOT_PAGE, confluenceRootPage);
    }
}