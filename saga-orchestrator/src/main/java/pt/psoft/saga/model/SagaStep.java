package pt.psoft.saga.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Saga Step - Represents a single step in the saga
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaStep implements Serializable {

    private String stepName;
    private String service;
    private String action;  // CREATE, UPDATE, DELETE, COMPENSATE

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime executedAt;

    private boolean success;
    private String response;
    private String errorMessage;

    /**
     * Create successful step
     */
    public static SagaStep success(String stepName, String service, String action, String response) {
        return SagaStep.builder()
                .stepName(stepName)
                .service(service)
                .action(action)
                .executedAt(LocalDateTime.now())
                .success(true)
                .response(response)
                .build();
    }

    /**
     * Create failed step
     */
    public static SagaStep failure(String stepName, String service, String action, String errorMessage) {
        return SagaStep.builder()
                .stepName(stepName)
                .service(service)
                .action(action)
                .executedAt(LocalDateTime.now())
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}