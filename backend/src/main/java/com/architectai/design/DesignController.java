package com.architectai.design;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/designs")
public class DesignController {

    private final DesignService designService;

    public DesignController(DesignService designService) {
        this.designService = designService;
    }

    @PostMapping
    public ResponseEntity<?> createDesign(@RequestBody Map<String, String> request) {
        String requirement = request.get("requirement");
        if (requirement == null || requirement.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "requirement is required"));
        }

        return ResponseEntity.ok(designService.generateDesign(requirement));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DesignResponse> getDesign(@PathVariable Long id) {
        DesignResponse response = designService.getDesign(id);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
}
