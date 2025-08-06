package com.github.istin.dmtools.server.service;

import com.github.istin.dmtools.server.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Service for validating nested parameter structures and array constraints.
 * Supports validation of job configuration parameters including dot notation structures.
 * Note: Confluence URL content extraction is handled in the core module, not here.
 */
@Service
public class ParameterValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern USER_ID_PATTERN = Pattern.compile(
        "^[0-9]+:[a-fA-F0-9-]+$"
    );

    private static final Pattern JQL_PATTERN = Pattern.compile("^.+$");

    /**
     * Validates teammate job parameters according to schema requirements.
     * 
     * @param parameters Map of parameters to validate
     * @throws ValidationException if validation fails
     */
    public void validateTeammateParameters(Map<String, Object> parameters) throws ValidationException {
        if (parameters == null) {
            throw new ValidationException("Parameters cannot be null");
        }

        List<String> errors = new ArrayList<>();

        // Validate required fields
        validateRequiredField(parameters, "inputJql", errors);
        validateRequiredField(parameters, "initiator", errors);
        
        // Validate JQL format
        validateJqlFormat(parameters.get("inputJql"), errors);
        
        // Validate initiator format (email)
        validateEmailFormat(parameters.get("initiator"), errors);

        // Validate nested agentParams if present
        Object agentParams = parameters.get("agentParams");
        if (agentParams instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> agentParamsMap = (Map<String, Object>) agentParams;
            validateAgentParameters(agentParamsMap, errors);
        }

        // Validate flat dot notation parameters
        validateDotNotationParameters(parameters, errors);

        if (!errors.isEmpty()) {
            throw new ValidationException("Parameter validation failed: " + String.join(", ", errors));
        }
    }

    /**
     * Validates agent parameters structure.
     * 
     * @param agentParams Map of agent parameters
     * @param errors List to collect validation errors
     */
    @SuppressWarnings("unchecked")
    private void validateAgentParameters(Map<String, Object> agentParams, List<String> errors) {
        // Validate aiRole is required if agentParams exists
        if (!agentParams.containsKey("aiRole") || agentParams.get("aiRole") == null) {
            errors.add("agentParams.aiRole is required");
        } else {
            Object aiRole = agentParams.get("aiRole");
            if (!(aiRole instanceof String)) {
                errors.add("agentParams.aiRole must be a string");
            } else {
                validateStringLength("agentParams.aiRole", (String) aiRole, 1, 1000, errors);
            }
        }

        // Validate instructions array if present
        Object instructions = agentParams.get("instructions");
        if (instructions != null) {
            if (instructions instanceof List) {
                List<Object> instructionsList = (List<Object>) instructions;
                if (instructionsList.size() > 10) {
                    errors.add("agentParams.instructions cannot have more than 10 items");
                }
                
                for (int i = 0; i < instructionsList.size(); i++) {
                    Object instruction = instructionsList.get(i);
                    if (!(instruction instanceof String)) {
                        errors.add("agentParams.instructions[" + i + "] must be a string");
                    } else {
                        validateStringLength("agentParams.instructions[" + i + "]", (String) instruction, 1, 500, errors);
                    }
                }
            } else if (!(instructions instanceof String)) {
                errors.add("agentParams.instructions must be an array or string");
            }
        }

        // Validate optional text fields
        validateOptionalTextField(agentParams, "formattingRules", 5000, errors);
        validateOptionalTextField(agentParams, "fewShots", 5000, errors);
        validateOptionalTextField(agentParams, "knownInfo", 5000, errors);
    }

    /**
     * Validates flat dot notation parameters.
     * 
     * @param parameters Map of all parameters
     * @param errors List to collect validation errors
     */
    private void validateDotNotationParameters(Map<String, Object> parameters, List<String> errors) {
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (key.startsWith("agentParams.")) {
                String subKey = key.substring("agentParams.".length());
                
                switch (subKey) {
                    case "aiRole":
                        if (value == null) {
                            errors.add(key + " is required");
                        } else if (!(value instanceof String)) {
                            errors.add(key + " must be a string");
                        } else {
                            validateStringLength(key, (String) value, 1, 1000, errors);
                        }
                        break;
                        
                    case "instructions":
                        if (value != null) {
                            if (value instanceof List) {
                                List<?> list = (List<?>) value;
                                if (list.size() > 10) {
                                    errors.add(key + " cannot have more than 10 items");
                                }
                            } else if (!(value instanceof String)) {
                                errors.add(key + " must be an array or string");
                            }
                        }
                        break;
                        
                    case "formattingRules":
                    case "fewShots":
                    case "knownInfo":
                        if (value != null && !(value instanceof String)) {
                            errors.add(key + " must be a string");
                        } else if (value instanceof String) {
                            validateStringLength(key, (String) value, 0, 5000, errors);
                        }
                        break;
                }
            }
        }
    }

    /**
     * Validates that a required field is present and not null.
     */
    private void validateRequiredField(Map<String, Object> parameters, String fieldName, List<String> errors) {
        if (!parameters.containsKey(fieldName) || parameters.get(fieldName) == null) {
            errors.add(fieldName + " is required");
        }
    }

    /**
     * Validates JQL format.
     */
    private void validateJqlFormat(Object jql, List<String> errors) {
        if (jql instanceof String) {
            String jqlString = (String) jql;
            if (jqlString.trim().isEmpty()) {
                errors.add("inputJql cannot be empty");
            } else if (!JQL_PATTERN.matcher(jqlString).matches()) {
                errors.add("inputJql format is invalid");
            }
        } else if (jql != null) {
            errors.add("inputJql must be a string");
        }
    }

    /**
     * Validates email format or user ID format.
     */
    private void validateEmailFormat(Object email, List<String> errors) {
        if (email instanceof String) {
            String emailString = (String) email;
            if (!EMAIL_PATTERN.matcher(emailString).matches() && 
                !USER_ID_PATTERN.matcher(emailString).matches()) {
                errors.add("initiator must be a valid email address or user ID");
            }
        } else if (email != null) {
            errors.add("initiator must be a string");
        }
    }

    /**
     * Validates string length constraints.
     */
    private void validateStringLength(String fieldName, String value, int minLength, int maxLength, List<String> errors) {
        if (value != null) {
            if (value.length() < minLength) {
                errors.add(fieldName + " must be at least " + minLength + " characters");
            }
            if (value.length() > maxLength) {
                errors.add(fieldName + " must be no more than " + maxLength + " characters");
            }
        }
    }

    /**
     * Validates optional text field constraints.
     */
    private void validateOptionalTextField(Map<String, Object> params, String fieldName, int maxLength, List<String> errors) {
        Object value = params.get(fieldName);
        if (value != null) {
            if (!(value instanceof String)) {
                errors.add("agentParams." + fieldName + " must be a string");
            } else {
                validateStringLength("agentParams." + fieldName, (String) value, 0, maxLength, errors);
            }
        }
    }
}