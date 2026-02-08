package com.github.istin.dmtools.reporting.model;

import com.github.istin.dmtools.team.IEmployees;

import java.util.*;

/**
 * Lightweight IEmployees implementation configured from report JSON config.
 * Supports inline employee list and aliases without external JSON files.
 *
 * Behavior:
 * - employees provided: only listed names are tracked, others become "Unknown"
 * - employees empty/null but aliases provided: all names are shown, aliases are grouped
 * - both empty: all names shown as-is
 */
public class ReportEmployees implements IEmployees {

    private final Set<String> employeeNames = new HashSet<>();
    // Reverse map: alias (lowercase) -> primary name
    private final Map<String, String> aliasToName = new HashMap<>();
    // true when the user explicitly provided an employees list
    private final boolean hasExplicitEmployees;

    public ReportEmployees(List<String> employees, Map<String, List<String>> aliases) {
        hasExplicitEmployees = employees != null && !employees.isEmpty();

        if (hasExplicitEmployees) {
            for (String name : employees) {
                employeeNames.add(name.toLowerCase());
            }
        }
        if (aliases != null) {
            for (Map.Entry<String, List<String>> entry : aliases.entrySet()) {
                String primaryName = entry.getKey();
                List<String> aliasList = entry.getValue();
                if (hasExplicitEmployees) {
                    employeeNames.add(primaryName.toLowerCase());
                }
                for (String alias : aliasList) {
                    aliasToName.put(alias.toLowerCase(), primaryName);
                    if (hasExplicitEmployees) {
                        employeeNames.add(alias.toLowerCase());
                    }
                }
            }
        }
    }

    @Override
    public boolean contains(String fullName) {
        if (fullName == null) return false;
        // No explicit employees list → allow everyone (just apply alias grouping)
        if (!hasExplicitEmployees) return true;
        return employeeNames.contains(fullName.toLowerCase());
    }

    @Override
    public String transformName(String sourceFullName) {
        if (sourceFullName == null) return null;
        String primary = aliasToName.get(sourceFullName.toLowerCase());
        return primary != null ? primary : sourceFullName;
    }

    @Override
    public boolean isBot(String sourceFullName) {
        return sourceFullName != null &&
            (sourceFullName.equalsIgnoreCase("DM_scripts") ||
             sourceFullName.equalsIgnoreCase("DM_scripts_token"));
    }
}
