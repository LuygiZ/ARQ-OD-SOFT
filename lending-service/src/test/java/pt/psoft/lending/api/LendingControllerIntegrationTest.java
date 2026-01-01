package pt.psoft.lending.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pt.psoft.lending.api.dto.CreateLendingRequest;
import pt.psoft.lending.api.dto.ReturnLendingRequest;
import pt.psoft.lending.model.command.LendingEntity;
import pt.psoft.lending.services.LendingCommandService;
import pt.psoft.lending.services.LendingQueryService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Lending Controller
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Lending Controller Integration Tests")
class LendingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LendingCommandService lendingCommandService;

    @MockBean
    private LendingQueryService lendingQueryService;

    @Test
    @DisplayName("POST /api/v1/lendings - Should create lending")
    void shouldCreateLending() throws Exception {
        CreateLendingRequest request = new CreateLendingRequest("9782826012092", "2024/1");
        LendingEntity lending = new LendingEntity("9782826012092", 1L, "2024/1", 1, 14, 50);

        when(lendingCommandService.createLending(any(CreateLendingRequest.class))).thenReturn(lending);

        mockMvc.perform(post("/api/v1/lendings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookId").value("9782826012092"))
                .andExpect(jsonPath("$.readerNumber").value("2024/1"));
    }

    @Test
    @DisplayName("POST /api/v1/lendings - Should return 400 for missing ISBN")
    void shouldReturn400ForMissingIsbn() throws Exception {
        CreateLendingRequest request = new CreateLendingRequest("", "2024/1");

        mockMvc.perform(post("/api/v1/lendings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/lendings/{id}/return - Should return lending with rating")
    void shouldReturnLendingWithRating() throws Exception {
        ReturnLendingRequest request = new ReturnLendingRequest("Great book!", 8);
        LendingEntity lending = new LendingEntity("9782826012092", 1L, "2024/1", 1, 14, 50);
        lending.setReturned(0L, "Great book!", 8);

        when(lendingCommandService.returnLending(eq("2024/1"), any(ReturnLendingRequest.class), eq(0L)))
                .thenReturn(lending);

        mockMvc.perform(post("/api/v1/lendings/2024/1/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "0")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lendingNumber").exists())
                .andExpect(jsonPath("$.review.rating").value(8))
                .andExpect(jsonPath("$.review.comment").value("Great book!"));
    }

    @Test
    @DisplayName("POST /api/v1/lendings/{id}/return - Should return 400 for invalid rating")
    void shouldReturn400ForInvalidRating() throws Exception {
        ReturnLendingRequest request = new ReturnLendingRequest("Comment", 15);

        mockMvc.perform(post("/api/v1/lendings/2024/1/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "0")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/lendings/{id}/return - Should return 400 for negative rating")
    void shouldReturn400ForNegativeRating() throws Exception {
        ReturnLendingRequest request = new ReturnLendingRequest("Comment", -1);

        mockMvc.perform(post("/api/v1/lendings/2024/1/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("If-Match", "0")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/lendings/{id} - Should return lending")
    void shouldReturnLending() throws Exception {
        LendingEntity lending = new LendingEntity("9782826012092", 1L, "2024/1", 1, 14, 50);

        when(lendingQueryService.findByLendingNumber("2024/1")).thenReturn(Optional.of(lending));

        mockMvc.perform(get("/api/v1/lendings/2024/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value("9782826012092"))
                .andExpect(jsonPath("$.readerNumber").value("2024/1"));
    }

    @Test
    @DisplayName("GET /api/v1/lendings/{id} - Should return 404 for not found")
    void shouldReturn404ForNotFound() throws Exception {
        when(lendingQueryService.findByLendingNumber("2024/999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/lendings/2024/999"))
                .andExpect(status().isNotFound());
    }
}
