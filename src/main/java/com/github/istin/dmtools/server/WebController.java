package com.github.istin.dmtools.server;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "redirect:/index.html";
    }

    @GetMapping("/create-agent")
    public String createAgent() {
        return "redirect:/create-agent.html";
    }
} 