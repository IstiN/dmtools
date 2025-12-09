package com.github.istin.dmtools.report.productivity;

import lombok.Data;

import java.util.*;

@Data
public class ProductivityAnalyticsData {
    private Map<String, Set<String>> usersPerPattern = new HashMap<>();
    private Map<String, Map<String, Integer>> interactionsPerPatternPerUser = new HashMap<>();
    private Map<String, Map<String, List<Date>>> userInteractionDates = new HashMap<>();
    private Set<String> allUsers = new HashSet<>();
    private Map<String, String> patternNames = new LinkedHashMap<>();
    private List<String> requests = new ArrayList<>();

    public ProductivityAnalyticsData() {
    }
}

