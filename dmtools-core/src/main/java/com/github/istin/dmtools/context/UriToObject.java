package com.github.istin.dmtools.context;

import java.util.Set;

public interface UriToObject {

    Set<String> parseUris(String object) throws Exception;

    Object uriToObject(String uri) throws Exception;

}
