package com.github.istin.dmtools.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating presentations")
public class PresentationRequest {

    @Schema(description = "JavaScript configuration as JSON string containing the script and client name", 
           example = "{\"jsScript\": \"function generatePresentationJs(params, bridge) { bridge.jsLogInfo('Creating presentation'); return JSON.stringify({title: 'My Presentation', slides: [{title: 'Slide 1', content: 'Content'}]}); }\", \"clientName\": \"TestClient\"}")
    private String jsConfig;

    @Schema(description = "Parameters for the presentation generation as JSON string", 
           example = "{\"presenter\":\"AI Assistant\",\"topic\":\"Quarterly Review\"}")
    private String presentationParams;

    @Schema(description = "Optional custom topic for naming the presentation file", 
           example = "Quarterly_Review_2024")
    private String customTopic;

} 