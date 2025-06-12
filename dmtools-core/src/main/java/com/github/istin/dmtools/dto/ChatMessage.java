package com.github.istin.dmtools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    private String role;
    private String content;
    private List<String> fileNames; // For tracking file names in JSON requests
} 