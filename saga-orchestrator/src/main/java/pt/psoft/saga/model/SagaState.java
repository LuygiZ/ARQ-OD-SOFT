package pt.psoft.saga.model;

/**
 * Saga State Machine States
 */
public enum SagaState {
    // Initial state
    STARTED,

    // Genre creation
    CREATING_GENRE,
    GENRE_CREATED,
    GENRE_CREATION_FAILED,

    // Author creation
    CREATING_AUTHOR,
    AUTHOR_CREATED,
    AUTHOR_CREATION_FAILED,

    // Book creation
    CREATING_BOOK,
    BOOK_CREATED,
    BOOK_CREATION_FAILED,

    // Final states
    COMPLETED,
    FAILED,

    // Compensation states
    COMPENSATING,
    COMPENSATED,
    COMPENSATION_FAILED
}