package pt.psoft.book.shared.exceptions;

import pt.psoft.shared.exceptions.BusinessException;

public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(message, "CONFLICT");
    }

    public ConflictException(String resource, String field, String value) {
        super(String.format("%s with %s '%s' already exists", resource, field, value), "CONFLICT");
    }
}