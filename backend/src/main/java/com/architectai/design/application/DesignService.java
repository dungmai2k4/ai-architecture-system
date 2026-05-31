package com.architectai.design.application;

import com.architectai.design.api.DesignResponse;
import com.architectai.design.architecture.ArchitecturalDesignPackage;
import com.architectai.design.architecture.ArchitecturalDesignPackageGenerator;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

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
    private final ArchitecturalDesignPackageGenerator architecturalDesignPackageGenerator;
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
            ArchitecturalDesignPackageGenerator architecturalDesignPackageGenerator,
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
        this.architecturalDesignPackageGenerator = architecturalDesignPackageGenerator;
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
            DesignBrief designBrief = applyLearnedDesignMemory(extractionResult.designBrief());
            RuleResult ruleResult = vietnameseRuleEngine.evaluate(designBrief);
            LayoutPlan layoutPlan = layoutPlanner.plan(designBrief);
            Floorplan floorplan = floorplanGenerator.generate(designBrief);
            String renderPrompt = renderPromptGenerator.generate(designBrief, layoutPlan, floorplan);
            ArchitecturalDesignPackage architecturalDesignPackage = architecturalDesignPackageGenerator.generate(designBrief, layoutPlan, floorplan, renderPrompt);

            DesignOutput output = new DesignOutput();
            output.setProject(savedProject);
            output.setDesignBriefJson(objectMapper.writeValueAsString(designBrief));
            output.setRuleResultJson(objectMapper.writeValueAsString(ruleResult));
            output.setLayoutPlanJson(objectMapper.writeValueAsString(layoutPlan));
            output.setFloorplanJson(objectMapper.writeValueAsString(floorplan));
            output.setArchitecturalDesignPackageJson(objectMapper.writeValueAsString(architecturalDesignPackage));
            output.setSvgPath(floorplan.svg());
            output.setRenderPrompt(renderPrompt);
            designOutputRepository.save(output);

            savedProject.setStatus("COMPLETED");
            designRepository.save(savedProject);
            saveAiCall(savedProject, requirement, rawAiResponse, true, null);

            return new DesignResponse(savedProject.getId(), savedProject.getStatus(), designBrief, ruleResult, layoutPlan, floorplan, architecturalDesignPackage, renderPrompt, null, null);
        } catch (Exception exception) {
            savedProject.setStatus("FAILED");
            designRepository.save(savedProject);
            saveAiCall(savedProject, requirement, rawAiResponse, false, exception.getMessage());

            return new DesignResponse(savedProject.getId(), savedProject.getStatus(), null, null, null, null, null, null, null, exception.getMessage());
        }
    }

    private DesignBrief applyLearnedDesignMemory(DesignBrief brief) {
        List<DesignOutput> recentOutputs = designOutputRepository.findTop8ByOrderByUpdatedAtDesc();
        List<DesignBrief> recentBriefs = recentOutputs.stream()
                .map(DesignOutput::getDesignBriefJson)
                .filter(json -> json != null && !json.isBlank())
                .map(this::readNullableDesignBrief)
                .filter(Objects::nonNull)
                .toList();

        List<String> learnedPreferences = new ArrayList<>(brief.preferences());
        mostCommon(recentBriefs.stream()
                .map(DesignBrief::style)
                .filter(style -> style != null && !style.isBlank() && !"unknown".equalsIgnoreCase(style))
                .map(style -> "học từ mẫu trước: biến tấu phong cách " + style.toLowerCase(Locale.ROOT) + " thay vì sao chép nguyên mẫu")
                .toList(), 2)
                .ifPresent(preference -> addIfMissing(learnedPreferences, preference));
        mostCommon(recentBriefs.stream()
                .flatMap(previous -> previous.preferences().stream())
                .filter(preference -> preference != null && !preference.isBlank())
                .map(preference -> "học từ mẫu trước: cân nhắc " + preference.toLowerCase(Locale.ROOT))
                .toList(), 2)
                .ifPresent(preference -> addIfMissing(learnedPreferences, preference));
        addIfMissing(learnedPreferences, "variant:" + selectDiverseVariant(brief, recentBriefs));

        return new DesignBrief(
                brief.siteWidthMeters(),
                brief.siteDepthMeters(),
                brief.floors(),
                brief.bedrooms(),
                brief.bathrooms(),
                brief.style(),
                brief.rooms(),
                learnedPreferences,
                brief.constraints(),
                brief.orientation(),
                brief.location(),
                brief.parkingRequired(),
                brief.lightwellRequired(),
                brief.frontYardRequired(),
                brief.rearGardenRequired(),
                brief.openKitchen(),
                brief.stairPreference(),
                brief.adjacencyPreferences(),
                brief.floorRequirements()
        );
    }

    private DesignBrief readNullableDesignBrief(String json) {
        try {
            return objectMapper.readValue(json, DesignBrief.class);
        } catch (JsonProcessingException exception) {
            return null;
        }
    }

    private java.util.Optional<String> mostCommon(List<String> values, int minimumCount) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String value : values) {
            counts.merge(value, 1, Integer::sum);
        }
        return counts.entrySet().stream()
                .filter(entry -> entry.getValue() >= minimumCount)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    private String selectDiverseVariant(DesignBrief brief, List<DesignBrief> recentBriefs) {
        int[] usage = new int[4];
        for (DesignBrief recentBrief : recentBriefs) {
            usage[Math.floorMod(variantSignal(recentBrief).hashCode(), usage.length)]++;
        }
        int leastUsed = 0;
        for (int index = 1; index < usage.length; index++) {
            if (usage[index] < usage[leastUsed]) {
                leastUsed = index;
            }
        }
        return leastUsed + "-" + Math.floorMod(variantSignal(brief).hashCode(), 1000);
    }

    private String variantSignal(DesignBrief brief) {
        return String.join("|", safe(brief.style()), safe(brief.orientation()), safe(brief.stairPreference()), String.join(",", brief.rooms()));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void addIfMissing(List<String> values, String value) {
        if (values.stream().noneMatch(existing -> existing.equalsIgnoreCase(value))) {
            values.add(value);
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
                        loadArchitecturalDesignPackage(project.getId()),
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

    private ArchitecturalDesignPackage loadArchitecturalDesignPackage(Long projectId) {
        return designOutputRepository.findByProjectId(projectId)
                .map(DesignOutput::getArchitecturalDesignPackageJson)
                .filter(json -> json != null && !json.isBlank())
                .map(this::readArchitecturalDesignPackage)
                .orElse(null);
    }

    private ArchitecturalDesignPackage readArchitecturalDesignPackage(String json) {
        try {
            return objectMapper.readValue(json, ArchitecturalDesignPackage.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored architectural design package JSON is invalid", exception);
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
