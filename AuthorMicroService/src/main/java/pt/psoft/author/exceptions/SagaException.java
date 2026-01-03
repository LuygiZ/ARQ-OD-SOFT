package pt.psoft.author.exceptions;

public class SagaException extends BusinessException {

    private final String sagaId;
    private final String step;

    public SagaException(String sagaId, String step, String message) {
        super(String.format("Saga %s failed at step %s: %s", sagaId, step, message), "SAGA_FAILED");
        this.sagaId = sagaId;
        this.step = step;
    }

    public String getSagaId() {
        return sagaId;
    }

    public String getStep() {
        return step;
    }
}