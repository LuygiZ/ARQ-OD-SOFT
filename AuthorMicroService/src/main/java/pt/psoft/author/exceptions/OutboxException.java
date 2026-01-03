package pt.psoft.author.exceptions;

public class OutboxException extends BusinessException {

    public OutboxException(String message) {
        super(message, "OUTBOX_ERROR");
    }

    public OutboxException(String message, Throwable cause) {
        super(message, cause);
    }
}