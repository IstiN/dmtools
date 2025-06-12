package com.github.istin.dmtools.server;

import com.github.istin.dmtools.dto.AgentExecutionRequest;
import com.github.istin.dmtools.dto.AgentExecutionResponse;
import com.github.istin.dmtools.dto.AgentInfo;
import com.github.istin.dmtools.dto.AgentListResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/agents")
@CrossOrigin(origins = "*")
public class AgentController {

    private static final Logger logger = LogManager.getLogger(AgentController.class);

    @Autowired
    private AgentService agentService;

    @GetMapping("/available")
    public ResponseEntity<AgentListResponse> getAvailableAgentsAndOrchestrators(
            @RequestParam(value = "detailed", defaultValue = "false") boolean detailed) {
        
        if (detailed) {
            logger.info("Fetching detailed agents and orchestrators");
        } else {
            logger.info("Fetching available agents and orchestrators");
        }
        
        AgentListResponse response = agentService.getAgentsAndOrchestrators(detailed);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/agents")
    public ResponseEntity<?> getAvailableAgents(
            @RequestParam(value = "detailed", defaultValue = "false") boolean detailed) {
        
        if (detailed) {
            logger.info("Fetching detailed agents");
            List<AgentInfo> agents = agentService.getDetailedAgents();
            return ResponseEntity.ok(agents);
        } else {
            logger.info("Fetching available agents");
            List<String> agents = agentService.getAvailableAgents();
            return ResponseEntity.ok(agents);
        }
    }

    @GetMapping("/orchestrators")
    public ResponseEntity<?> getAvailableOrchestrators(
            @RequestParam(value = "detailed", defaultValue = "false") boolean detailed) {
        
        if (detailed) {
            logger.info("Fetching detailed orchestrators");
            List<AgentInfo> orchestrators = agentService.getDetailedOrchestrators();
            return ResponseEntity.ok(orchestrators);
        } else {
            logger.info("Fetching available orchestrators");
            List<String> orchestrators = agentService.getAvailableOrchestrators();
            return ResponseEntity.ok(orchestrators);
        }
    }

    @GetMapping("/agents/{agentName}/info")
    public ResponseEntity<AgentInfo> getAgentInfo(@PathVariable String agentName) {
        logger.info("Fetching detailed info for agent: {}", agentName);
        
        List<AgentInfo> agents = agentService.getDetailedAgents();
        AgentInfo agentInfo = agents.stream()
                .filter(agent -> agent.getName().equals(agentName))
                .findFirst()
                .orElse(null);
                
        if (agentInfo == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(agentInfo);
    }

    @GetMapping("/orchestrators/{orchestratorName}/info")
    public ResponseEntity<AgentInfo> getOrchestratorInfo(@PathVariable String orchestratorName) {
        logger.info("Fetching detailed info for orchestrator: {}", orchestratorName);
        
        List<AgentInfo> orchestrators = agentService.getDetailedOrchestrators();
        AgentInfo orchestratorInfo = orchestrators.stream()
                .filter(orchestrator -> orchestrator.getName().equals(orchestratorName))
                .findFirst()
                .orElse(null);
                
        if (orchestratorInfo == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(orchestratorInfo);
    }

    @PostMapping("/execute")
    public ResponseEntity<AgentExecutionResponse> executeAgent(@RequestBody AgentExecutionRequest request) {
        logger.info("Executing agent: {}", request.getAgentName());
        
        if (request.getAgentName() == null || request.getAgentName().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(AgentExecutionResponse.error("", "Agent name cannot be empty", "agent"));
        }
        
        AgentExecutionResponse response = agentService.executeAgent(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/execute/{agentName}")
    public ResponseEntity<AgentExecutionResponse> executeAgentByPath(
            @PathVariable String agentName,
            @RequestBody Map<String, Object> parameters) {
        
        logger.info("Executing agent by path: {}", agentName);
        
        AgentExecutionRequest request = new AgentExecutionRequest(agentName, parameters);
        AgentExecutionResponse response = agentService.executeAgent(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/orchestrators/execute")
    public ResponseEntity<AgentExecutionResponse> executeOrchestrator(@RequestBody AgentExecutionRequest request) {
        logger.info("Executing orchestrator: {}", request.getAgentName());
        
        if (request.getAgentName() == null || request.getAgentName().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(AgentExecutionResponse.error("", "Orchestrator name cannot be empty", "orchestrator"));
        }
        
        AgentExecutionResponse response = agentService.executeOrchestrator(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/orchestrators/execute/{orchestratorName}")
    public ResponseEntity<AgentExecutionResponse> executeOrchestratorByPath(
            @PathVariable String orchestratorName,
            @RequestBody Map<String, Object> parameters) {
        
        logger.info("Executing orchestrator by path: {}", orchestratorName);
        
        AgentExecutionRequest request = new AgentExecutionRequest(orchestratorName, parameters);
        AgentExecutionResponse response = agentService.executeOrchestrator(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Agent service is running");
    }
} 