package com.architectai.design.application;

import com.architectai.design.api.DesignResponse;
import com.architectai.design.domain.DesignBrief;
import com.architectai.design.domain.DesignOutput;
import com.architectai.design.domain.DesignProject;
import com.architectai.design.layout.Floorplan;
import com.architectai.design.layout.FloorplanGenerator;
import com.architectai.design.layout.LayoutPlan;
import com.architectai.design.layout.LayoutPlanner;
import com.architectai.design.render.RenderPromptGenerator;
import com.architectai.design.repository.DesignOutputRepository;
import com.architectai.design.repository.DesignRepository;
import com.architectai.design.rules.RuleResult;
import com.architectai.design.rules.VietnameseRuleEngine;
import com.architectai.ai.AiCall;
import com.architectai.ai.AiCallRepository;
import com.architectai.ai.RequirementExtractionResult;
import com.architectai.ai.RequirementExtractor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class DesignService {

    private final DesignRepository designRepository;
    private final DesignOutputRepository designOutputRepository;
    private final AiCallRepository aiCallRepository;
    private final RequirementExtractor requirementExtractor;
    private final VietnameseRuleEngine vietnameseRuleEngine;
    private final LayoutPlanner layoutPlanner;
    private final FloorplanGenerator floorplanGenerator;
    private final RenderPromptGenerator renderPromptGenerator;
    private final ObjectMapper objectMapper;
    private final String model;

    public DesignService(
            DesignRepository designRepository,
            DesignOutputRepository designOutputRepository,
            AiCallRepository aiCallRepository,
            RequirementExtractor requirementExtractor,
            VietnameseRuleEngine vietnameseRuleEngine,
            LayoutPlanner layoutPlanner,
            FloorplanGenerator floorplanGenerator,
            RenderPromptGenerator renderPromptGenerator,
            ObjectMapper objectMapper,
            @Value("${ollama.model:qwen2.5-coder:7b}") String model
    ) {
        this.designRepository = designRepository;
        this.designOutputRepository = designOutputRepository;
        this.aiCallRepository = aiCallRepository;
        this.requirementExtractor = requirementExtractor;
        this.vietnameseRuleEngine = vietnameseRuleEngine;
        this.layoutPlanner = layoutPlanner;
        this.floorplanGenerator = floorplanGenerator;
        this.renderPromptGenerator = renderPromptGenerator;
        this.objectMapper = objectMapper;
        this.model = model;
    }

    @Transactional
    public DesignResponse generateDesign(String requirement) {
        DesignProject project = new DesignProject();
        project.setTitle("Design " + Instant.now().toEpochMilli());
        project.setRawRequirement(requirement);
        project.setStatus("PENDING");

        DesignProject savedProject = designRepository.save(project);
        String rawAiResponse = null;

        try {
            RequirementExtractionResult extractionResult = requirementExtractor.extractWithRawResponse(requirement);
            rawAiResponse = extractionResult.rawResponse();
            DesignBrief designBrief = extractionResult.designBrief();
            RuleResult ruleResult = vietnameseRuleEngine.evaluate(designBrief);
            LayoutPlan layoutPlan = layoutPlanner.plan(designBrief);
            Floorplan floorplan = floorplanGenerator.generate(designBrief);
            String renderPrompt = renderPromptGenerator.generate(designBrief, layoutPlan, floorplan);

            DesignOutput output = new DesignOutput();
            output.setProject(savedProject);
            output.setDesignBriefJson(objectMapper.writeValueAsString(designBrief));
            output.setRuleResultJson(objectMapper.writeValueAsString(ruleResult));
            output.setLayoutPlanJson(objectMapper.writeValueAsString(layoutPlan));
            output.setFloorplanJson(objectMapper.writeValueAsString(floorplan));
            output.setSvgPath(floorplan.svg());
            output.setRenderPrompt(renderPrompt);
            designOutputRepository.save(output);

            savedProject.setStatus("COMPLETED");
            designRepository.save(savedProject);
            saveAiCall(savedProject, requirement, rawAiResponse, true, null);

            return new DesignResponse(savedProject.getId(), savedProject.getStatus(), designBrief, ruleResult, layoutPlan, floorplan, renderPrompt, null, null);
        } catch (Exception exception) {
            savedProject.setStatus("FAILED");
            designRepository.save(savedProject);
            saveAiCall(savedProject, requirement, rawAiResponse, false, exception.getMessage());

            return new DesignResponse(savedProject.getId(), savedProject.getStatus(), null, null, null, null, null, null, exception.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public DesignResponse getDesign(Long id) {
        return designRepository.findById(id)
                .map(project -> new DesignResponse(
                        project.getId(),
                        project.getStatus(),
                        loadDesignBrief(project.getId()),
                        loadRuleResult(project.getId()),
                        loadLayoutPlan(project.getId()),
                        loadFloorplan(project.getId()),
                        loadRenderPrompt(project.getId()),
                        loadRenderImagePath(project.getId()),
                        null
                ))
                .orElse(null);
    }

    private DesignBrief loadDesignBrief(Long projectId) {
        return designOutputRepository.findByProjectId(projectId)
                .map(DesignOutput::getDesignBriefJson)
                .filter(json -> json != null && !json.isBlank())
                .map(this::readDesignBrief)
                .orElse(null);
    }

    private RuleResult loadRuleResult(Long projectId) {
        return designOutputRepository.findByProjectId(projectId)
                .map(DesignOutput::getRuleResultJson)
                .filter(json -> json != null && !json.isBlank())
                .map(this::readRuleResult)
                .orElse(null);
    }

    private DesignBrief readDesignBrief(String json) {
        try {
            return objectMapper.readValue(json, DesignBrief.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored DesignBrief JSON is invalid", exception);
        }
    }

    private RuleResult readRuleResult(String json) {
        try {
            return objectMapper.readValue(json, RuleResult.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored rule result JSON is invalid", exception);
        }
    }

    private LayoutPlan loadLayoutPlan(Long projectId) {
        return designOutputRepository.findByProjectId(projectId)
                .map(DesignOutput::getLayoutPlanJson)
                .filter(json -> json != null && !json.isBlank())
                .map(this::readLayoutPlan)
                .orElse(null);
    }

    private LayoutPlan readLayoutPlan(String json) {
        try {
            return objectMapper.readValue(json, LayoutPlan.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored layout plan JSON is invalid", exception);
        }
    }

    private String loadRenderPrompt(Long projectId) {
        return designOutputRepository.findByProjectId(projectId)
                .map(DesignOutput::getRenderPrompt)
                .filter(prompt -> prompt != null && !prompt.isBlank())
                .orElse(null);
    }

    private String loadRenderImagePath(Long projectId) {
        return designOutputRepository.findByProjectId(projectId)
                .map(DesignOutput::getRenderImagePath)
                .filter(path -> path != null && !path.isBlank())
                .orElse(null);
    }

    private Floorplan loadFloorplan(Long projectId) {
        return designOutputRepository.findByProjectId(projectId)
                .map(DesignOutput::getFloorplanJson)
                .filter(json -> json != null && !json.isBlank())
                .map(this::readFloorplan)
                .orElse(null);
    }

    private Floorplan readFloorplan(String json) {
        try {
            return objectMapper.readValue(json, Floorplan.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored floorplan JSON is invalid", exception);
        }
    }

    private void saveAiCall(
            DesignProject project,
            String promptText,
            String responseText,
            boolean success,
            String errorMessage
    ) {
        AiCall aiCall = new AiCall();
        aiCall.setProject(project);
        aiCall.setStage("DESIGN_BRIEF");
        aiCall.setModel(model);
        aiCall.setPromptText(promptText);
        aiCall.setResponseText(responseText);
        aiCall.setSuccess(success);
        aiCall.setErrorMessage(errorMessage);
        aiCallRepository.save(aiCall);
    }
}
