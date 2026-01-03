package pt.psoft.book.shared.exceptions;

import pt.psoft.shared.exceptions.BusinessException;

public class NotFoundException extends BusinessException {

    public NotFoundException(String resource, String identifier) {
        super(String.format("%s not found with identifier: %s", resource, identifier), "NOT_FOUND");
    }

    public NotFoundException(String message) {
        super(message, "NOT_FOUND");
    }
}