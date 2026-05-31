package com.architectai.design.architecture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ArchitecturalKnowledgeBase {

    private final ObjectMapper objectMapper;
    private final Map<String, JsonNode> cache = new ConcurrentHashMap<>();

    public ArchitecturalKnowledgeBase(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode dataset(String name) {
        return cache.computeIfAbsent(name, this::readDataset);
    }

    private JsonNode readDataset(String name) {
        ClassPathResource resource = new ClassPathResource("knowledge/" + name + ".json");
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readTree(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load architectural knowledge dataset: " + name, exception);
        }
    }
}
