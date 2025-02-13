package com.github.istin.dmtools.graphql;

import com.github.istin.dmtools.common.networking.GenericRequest;
import com.github.istin.dmtools.networking.AbstractRestClient;
import okhttp3.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;

public class GraphQLClient extends AbstractRestClient {
    private static final Logger logger = LogManager.getLogger(GraphQLClient.class);

    public GraphQLClient(String basePath, String authorization) throws IOException {
        super(basePath, authorization);
        setClearCache(true);
        setCacheGetRequestsEnabled(false);
    }

    @Override
    public String path(String path) {
        return getBasePath() + "/" + path;
    }

    @Override
    public Request.Builder sign(Request.Builder builder) {
        return builder
                .header("Authorization", authorization)
                .header("X-Atlassian-Token", "nocheck")
                .header("Content-Type", "application/json");
    }

    /**
     * Executes a GraphQL query with variables
     */
    public String executeGraphQL(String operationName, String query, JSONObject variables) throws IOException {
        GenericRequest request = new GenericRequest(this, path("graphql"));

        JSONObject body = new JSONObject()
                .put("operationName", operationName)
                .put("query", query)
                .put("variables", variables);

        request.setBody(body.toString());
        String response = request.post();

        // Log the response for debugging if needed
        logger.debug("GraphQL Response: {}", response);

        return response;
    }

    /**
     * Executes a GraphQL query without variables
     */
    public String executeGraphQL(String operationName, String query) throws IOException {
        return executeGraphQL(operationName, query, new JSONObject());
    }

    /**
     * Parse GraphQL response data
     */
    protected JSONObject parseGraphQLResponse(String response) throws IOException {
        try {
            JSONObject jsonResponse = new JSONObject(response);

            // Check for errors in the response
            if (jsonResponse.has("errors")) {
                String errorMessage = jsonResponse.getJSONArray("errors")
                        .getJSONObject(0)
                        .getString("message");
                throw new IOException("GraphQL Error: " + errorMessage);
            }

            return jsonResponse.getJSONObject("data");
        } catch (Exception e) {
            logger.error("Failed to parse GraphQL response: " + response, e);
            throw new IOException("Failed to parse GraphQL response", e);
        }
    }
}