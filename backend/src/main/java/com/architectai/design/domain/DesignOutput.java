package com.architectai.design.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "design_outputs")
public class DesignOutput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private DesignProject project;

    @Column(name = "design_brief_json", columnDefinition = "LONGTEXT")
    private String designBriefJson;

    @Column(name = "rule_result_json", columnDefinition = "LONGTEXT")
    private String ruleResultJson;

    @Column(name = "layout_plan_json", columnDefinition = "LONGTEXT")
    private String layoutPlanJson;

    @Column(name = "floorplan_json", columnDefinition = "LONGTEXT")
    private String floorplanJson;

    @Column(name = "architectural_design_package_json", columnDefinition = "LONGTEXT")
    private String architecturalDesignPackageJson;

    @Column(name = "svg_path", columnDefinition = "LONGTEXT")
    private String svgPath;

    @Column(name = "render_prompt", columnDefinition = "LONGTEXT")
    private String renderPrompt;

    @Column(name = "render_image_path", columnDefinition = "LONGTEXT")
    private String renderImagePath;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public DesignProject getProject() {
        return project;
    }

    public void setProject(DesignProject project) {
        this.project = project;
    }

    public String getDesignBriefJson() {
        return designBriefJson;
    }

    public void setDesignBriefJson(String designBriefJson) {
        this.designBriefJson = designBriefJson;
    }

    public String getRuleResultJson() {
        return ruleResultJson;
    }

    public void setRuleResultJson(String ruleResultJson) {
        this.ruleResultJson = ruleResultJson;
    }

    public String getLayoutPlanJson() {
        return layoutPlanJson;
    }

    public void setLayoutPlanJson(String layoutPlanJson) {
        this.layoutPlanJson = layoutPlanJson;
    }

    public String getFloorplanJson() {
        return floorplanJson;
    }

    public void setFloorplanJson(String floorplanJson) {
        this.floorplanJson = floorplanJson;
    }

    public String getArchitecturalDesignPackageJson() {
        return architecturalDesignPackageJson;
    }

    public void setArchitecturalDesignPackageJson(String architecturalDesignPackageJson) {
        this.architecturalDesignPackageJson = architecturalDesignPackageJson;
    }

    public String getSvgPath() {
        return svgPath;
    }

    public void setSvgPath(String svgPath) {
        this.svgPath = svgPath;
    }

    public String getRenderPrompt() {
        return renderPrompt;
    }

    public void setRenderPrompt(String renderPrompt) {
        this.renderPrompt = renderPrompt;
    }

    public String getRenderImagePath() {
        return renderImagePath;
    }

    public void setRenderImagePath(String renderImagePath) {
        this.renderImagePath = renderImagePath;
    }
}
