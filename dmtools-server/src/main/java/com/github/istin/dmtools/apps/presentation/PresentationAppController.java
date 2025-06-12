package com.github.istin.dmtools.apps.presentation;

import com.github.istin.dmtools.ai.agent.DataToPresentationScriptGeneratorAgent;
import com.github.istin.dmtools.apps.presentation.model.GeneratePresentationRequest;
import com.github.istin.dmtools.apps.presentation.model.ScriptGenerationRequest;
import com.github.istin.dmtools.common.utils.IOUtils;
import com.github.istin.dmtools.presentation.HTMLPresentationDrawer;
import com.github.istin.dmtools.presentation.JSPresentationMakerBridge;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/presentation")
public class PresentationAppController {

    @PostMapping(value = "/script", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/javascript")
    public ResponseEntity<String> generateScript(@RequestBody ScriptGenerationRequest request) throws Exception {
        return generateScriptInternal(request.getUserRequest(), null);
    }

    @PostMapping(value = "/script", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "application/javascript")
    public ResponseEntity<String> generateScript(
            @RequestParam("userRequest") String userRequest,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) throws Exception {
        return generateScriptInternal(userRequest, files);
    }

    private ResponseEntity<String> generateScriptInternal(String userRequest, List<MultipartFile> files) throws Exception {
        List<File> tempFiles = Collections.emptyList();
        if (files != null && !files.isEmpty()) {
            tempFiles = files.stream().map(IOUtils::multipartToFile).collect(Collectors.toList());
        }

        DataToPresentationScriptGeneratorAgent agent = new DataToPresentationScriptGeneratorAgent();
        DataToPresentationScriptGeneratorAgent.DataToPresentationParams params = new DataToPresentationScriptGeneratorAgent.DataToPresentationParams(userRequest, "", tempFiles);
        String generatedScript = agent.run(params);

        IOUtils.deleteFiles(tempFiles);

        return ResponseEntity.ok(generatedScript);
    }

    @PostMapping(value = "/generate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> generatePresentation(@RequestBody GeneratePresentationRequest request) throws Exception {
        File presentationFile = null;
        String htmlContent;

        try {
            // Generate from JavaScript
            JSONObject bridgeConfig = new JSONObject();
            bridgeConfig.put("jsScript", request.getJsScript());
            bridgeConfig.put("clientName", "ApiExecution");

            JSPresentationMakerBridge presentationBridge = new JSPresentationMakerBridge(bridgeConfig);

            String paramsForJs = (request.getParamsForJs() != null) ? request.getParamsForJs().toString() : "{}";
            presentationFile = presentationBridge.createPresentationFile(paramsForJs, null);

            if (presentationFile == null || !presentationFile.exists()) {
                throw new IOException("Failed to generate presentation file.");
            }

            htmlContent = new String(Files.readAllBytes(presentationFile.toPath()), StandardCharsets.UTF_8);

        } finally {
            if (presentationFile != null && presentationFile.exists()) {
                if (!presentationFile.delete()) {
                    System.err.println("Failed to clean up generated presentation file: " + presentationFile.getAbsolutePath());
                }
            }
        }

        return ResponseEntity.ok(htmlContent);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("healthy");
    }
} 