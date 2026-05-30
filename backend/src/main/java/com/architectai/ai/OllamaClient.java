package com.architectai.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class OllamaClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String model;

    public OllamaClient(
            @Value("${ollama.base-url:http://localhost:11434/api/generate}") String baseUrl,
            @Value("${ollama.model:qwen2.5-coder:7b}") String model
    ) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
        this.model = model;
    }

    public String complete(String systemPrompt, String userMessage) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "prompt", systemPrompt + "\n\nUser requirement:\n" + userMessage,
                "stream", false,
                "options", Map.of(
                        "temperature", 0,
                        "num_predict", 1000
                )
        );

        try {
            ResponseEntity<GenerateResponse> response = restTemplate.postForEntity(
                    baseUrl,
                    new HttpEntity<>(requestBody),
                    GenerateResponse.class
            );

            GenerateResponse body = response.getBody();
            if (body == null || body.response() == null || body.response().isBlank()) {
                throw new RuntimeException("Ollama response did not contain generated text");
            }

            return body.response();
        } catch (HttpStatusCodeException exception) {
            throw new RuntimeException(
                    "Ollama generate failed with HTTP " + exception.getStatusCode()
                            + ": " + exception.getResponseBodyAsString(),
                    exception
            );
        } catch (RestClientException exception) {
            throw new RuntimeException("Ollama generate request failed: " + exception.getMessage(), exception);
        }
    }

    private record GenerateResponse(String response) {
    }
}
