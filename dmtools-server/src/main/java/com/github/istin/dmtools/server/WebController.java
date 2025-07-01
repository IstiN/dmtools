package com.github.istin.dmtools.server;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WebController {

    private final SystemCommandService systemCommandService;

    public WebController(SystemCommandService systemCommandService) {
        this.systemCommandService = systemCommandService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/swagger-ui.html";
    }

    // Removed create-agent redirect since the HTML file was deleted
    // @GetMapping("/create-agent")
    // public String createAgent() {
    //     return "redirect:/create-agent.html";
    // }

    @PostMapping("/shutdown")
    @ResponseBody
    public String shutdown() {
        systemCommandService.shutdown();
        return "Server is shutting down...";
    }

    @GetMapping("/is-local")
    @ResponseBody
    public boolean isLocal() {
        return systemCommandService.isLocalEnvironment();
    }
} 