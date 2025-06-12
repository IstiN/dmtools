package com.github.istin.dmtools.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing presentation data and status")
public class PresentationResponse {

    @Schema(description = "Generated presentation data as JSON string", example = "{\"title\":\"My Presentation\",\"slides\":[{\"title\":\"Slide 1\",\"content\":\"Content\"}]}")
    private String presentationJson;
    
    @Schema(description = "File path of the generated HTML presentation file (if created)", example = "/path/to/presentation.html")
    private String filePath;
    
    @Schema(description = "Status message describing the result", example = "Presentation created successfully")
    private String message;
    
    @Schema(description = "Whether the operation was successful", example = "true")
    private boolean success;

    public static PresentationResponse success(String presentationJson, String filePath) {
        return new PresentationResponse(presentationJson, filePath, "Presentation created successfully", true);
    }

    public static PresentationResponse error(String message) {
        return new PresentationResponse(null, null, message, false);
    }

} 