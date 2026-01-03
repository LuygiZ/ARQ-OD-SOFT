package pt.psoft.author.exceptions;

public class NotFoundException extends BusinessException {

    public NotFoundException(String resource, String identifier) {
        super(String.format("%s not found with identifier: %s", resource, identifier), "NOT_FOUND");
    }

    public NotFoundException(String message) {
        super(message, "NOT_FOUND");
    }
}