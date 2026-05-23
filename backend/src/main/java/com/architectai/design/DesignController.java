package com.architectai.design;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/designs")
public class DesignController {

    private final DesignRepository designRepository;

    public DesignController(DesignRepository designRepository) {
        this.designRepository = designRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createDesign(@RequestBody Map<String, String> request) {
        String requirement = request.get("requirement");
        if (requirement == null || requirement.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "requirement is required"));
        }

        DesignProject project = new DesignProject();
        project.setTitle("Design " + Instant.now().toEpochMilli());
        project.setRawRequirement(requirement);
        project.setStatus("PENDING");

        DesignProject savedProject = designRepository.save(project);

        return ResponseEntity.ok(Map.of(
                "projectId", savedProject.getId(),
                "status", savedProject.getStatus()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DesignProject> getDesign(@PathVariable Long id) {
        return designRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
