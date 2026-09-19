package com.hackathon.ambulance_dispatch.services;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class BedrockTriageService {
    private final BedrockRuntimeClient bedrockClient;

    public BedrockTriageService() {
        this.bedrockClient = BedrockRuntimeClient.builder()
                .region(Region.AP_SOUTH_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public Map<String, String> triageEmergency(String description) {
        try {
            String payload = buildBedrockPayload(description);

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId("anthropic.claude-3-haiku-20240307-v1:0")
                    .contentType("application/json")
                    .accept("application/json")
                    .body(software.amazon.awssdk.core.SdkBytes.fromString(payload, StandardCharsets.UTF_8))
                    .build();

            InvokeModelResponse response = bedrockClient.invokeModel(request);

            String responseBody = new String(response.body().asByteArray(), StandardCharsets.UTF_8);
            return parseTriageResponse(responseBody);
        } catch (Exception e) {
            Map<String, String> fallback = new HashMap<>();
            fallback.put("severity", "HIGH");
            fallback.put("ambulanceType", "ADVANCED");
            return fallback;
        }
    }

    private String buildBedrockPayload(String description) {
        return "{\n" +
                "  \"anthropic_version\": \"bedrock-2023-05-31\",\n" +
                "  \"max_tokens\": 150,\n" +
                "  \"messages\": [\n" +
                "    {\n" +
                "      \"role\": \"user\",\n" +
                "      \"content\": \"Classify the following emergency description into a JSON object with keys 'severity' (HIGH, MED, or LOW) and 'ambulanceType' (ADVANCED or BASIC). Return ONLY the JSON object, no other text.\\n\\nEmergency: " + escapeJson(description) + "\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    private String escapeJson(String input) {
        return input.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private Map<String, String> parseTriageResponse(String responseBody) {
        Map<String, String> result = new HashMap<>();
        
        try {
            int contentStart = responseBody.indexOf("\"text\"");
            if (contentStart != -1) {
                int jsonStart = responseBody.indexOf("{", contentStart);
                int jsonEnd = responseBody.lastIndexOf("}");
                
                if (jsonStart != -1 && jsonEnd != -1 && jsonStart < jsonEnd) {
                    String jsonStr = responseBody.substring(jsonStart, jsonEnd + 1);
                    
                    String severity = extractJsonValue(jsonStr, "severity");
                    String ambulanceType = extractJsonValue(jsonStr, "ambulanceType");
                    
                    if (severity != null && ambulanceType != null) {
                        result.put("severity", severity);
                        result.put("ambulanceType", ambulanceType);
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            // Fall through to default
        }

        result.put("severity", "HIGH");
        result.put("ambulanceType", "ADVANCED");
        return result;
    }

    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}
