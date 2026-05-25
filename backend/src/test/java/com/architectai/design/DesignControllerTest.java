package com.architectai.design;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DesignController.class)
class DesignControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DesignService designService;

    @Test
    void createDesignReturnsValidationErrorWhenRequirementIsBlank() throws Exception {
        mockMvc.perform(post("/api/designs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requirement": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error").value("requirement is required"));
    }

    @Test
    void getDesignReturnsNotFoundErrorPayloadWhenProjectDoesNotExist() throws Exception {
        when(designService.getDesign(999L)).thenReturn(null);

        mockMvc.perform(get("/api/designs/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.error").value("Design project not found"));
    }
}
