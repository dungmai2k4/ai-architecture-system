package com.architectai.design;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api/designs")
public class DesignController {

    private final DesignService designService;

    public DesignController(DesignService designService) {
        this.designService = designService;
    }

    @PostMapping
    public ResponseEntity<DesignResponse> createDesign(@Valid @RequestBody CreateDesignRequest request) {
        return ResponseEntity.ok(designService.generateDesign(request.requirement()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDesign(@PathVariable Long id) {
        DesignResponse response = designService.getDesign(id);
        if (response == null) {
            return ResponseEntity.status(404)
                    .body(new ApiErrorResponse("Design project not found", "NOT_FOUND"));
        }
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldError() != null
                ? exception.getBindingResult().getFieldError().getDefaultMessage()
                : "Validation failed";

        return ResponseEntity.badRequest().body(new ApiErrorResponse(message, "VALIDATION_ERROR"));
    }
}
