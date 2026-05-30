package com.architectai.ai;

import com.architectai.design.DesignBrief;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class RequirementExtractor {

    private static final Logger log = LoggerFactory.getLogger(RequirementExtractor.class);

    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper;
    private String systemPrompt;

    public RequirementExtractor(OllamaClient ollamaClient, ObjectMapper objectMapper) {
        this.ollamaClient = ollamaClient;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void loadSystemPrompt() throws IOException {
        ClassPathResource resource = new ClassPathResource("prompts/design-brief-extraction.md");
        systemPrompt = resource.getContentAsString(StandardCharsets.UTF_8);
    }

    public DesignBrief extract(String requirement) {
        return extractWithRawResponse(requirement).designBrief();
    }

    public RequirementExtractionResult extractWithRawResponse(String requirement) {
        String rawResponse = ollamaClient.complete(systemPrompt, requirement);
        log.debug("Raw design brief AI response: {}", rawResponse);

        DesignBrief designBrief = parseAndValidate(rawResponse);
        return new RequirementExtractionResult(designBrief, rawResponse);
    }

    private DesignBrief parseAndValidate(String rawResponse) {
        try {
            DesignBrief designBrief = objectMapper.readValue(stripMarkdownFences(rawResponse), DesignBrief.class);
            if (designBrief.siteWidthMeters() <= 0 || designBrief.siteDepthMeters() <= 0) {
                throw new IllegalArgumentException("Site width and depth must be greater than 0 meters");
            }
            return designBrief;
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Unable to parse DesignBrief JSON from AI response", exception);
        }
    }

    private String stripMarkdownFences(String response) {
        String trimmed = response == null ? "" : response.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }

        String withoutOpeningFence = trimmed.replaceFirst("^```(?:json)?\\s*", "");
        return withoutOpeningFence.replaceFirst("\\s*```$", "").trim();
    }
}
