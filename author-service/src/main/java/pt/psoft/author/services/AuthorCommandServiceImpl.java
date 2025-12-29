package pt.psoft.author.services;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleObjectStateException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.author.api.dto.CreateAuthorRequest;
import pt.psoft.author.api.dto.UpdateAuthorRequest;
import pt.psoft.author.messaging.AuthorEventPublisher;
import pt.psoft.author.model.command.AuthorEntity;
import pt.psoft.author.repositories.jpa.AuthorRepository;
import pt.psoft.shared.events.author.AuthorCreatedEvent;
import pt.psoft.shared.events.author.AuthorDeletedEvent;
import pt.psoft.shared.events.author.AuthorUpdatedEvent;
import pt.psoft.shared.exceptions.NotFoundException;

/**
 * Implementation of Author Command Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorCommandServiceImpl implements AuthorCommandService {

    private final AuthorRepository authorRepository;
    private final AuthorEventPublisher authorEventPublisher;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public AuthorEntity createAuthor(CreateAuthorRequest request) {
        log.info("Creating author with name: {}", request.getName());

        AuthorEntity author = new AuthorEntity(
                request.getName(),
                request.getBio(),
                request.getPhotoURI()
        );

        AuthorEntity savedAuthor = authorRepository.save(author);
        log.info("Author created with number: {}", savedAuthor.getAuthorNumber());

        // Publish domain event
        AuthorCreatedEvent event = new AuthorCreatedEvent(
                savedAuthor.getAuthorNumber(),
                savedAuthor.getNameValue(),
                savedAuthor.getBioValue(),
                savedAuthor.getPhotoURI()
        );
        authorEventPublisher.publishAuthorCreated(event);

        return savedAuthor;
    }

    @Override
    @Transactional
    public AuthorEntity updateAuthor(Long authorNumber, UpdateAuthorRequest request, Long expectedVersion) {
        log.info("Updating author with number: {}", authorNumber);

        AuthorEntity author = authorRepository.findByAuthorNumber(authorNumber)
                .orElseThrow(() -> new NotFoundException("Author with number " + authorNumber + " not found"));

        if (!author.getVersion().equals(expectedVersion)) {
            throw new StaleObjectStateException("Author", authorNumber);
        }

        author.update(
                request.getName(),
                request.getBio(),
                request.getPhotoURI()
        );

        AuthorEntity updatedAuthor = authorRepository.save(author);
        entityManager.flush(); // Force version increment

        log.info("Author updated with number: {}", authorNumber);

        // Publish domain event
        AuthorUpdatedEvent event = new AuthorUpdatedEvent(
                updatedAuthor.getAuthorNumber(),
                updatedAuthor.getNameValue(),
                updatedAuthor.getBioValue(),
                updatedAuthor.getPhotoURI(),
                updatedAuthor.getVersion()
        );
        authorEventPublisher.publishAuthorUpdated(event);

        return updatedAuthor;
    }

    @Override
    @Transactional
    public void deleteAuthor(Long authorNumber, Long expectedVersion) {
        log.info("Deleting author with number: {}", authorNumber);

        AuthorEntity author = authorRepository.findByAuthorNumber(authorNumber)
                .orElseThrow(() -> new NotFoundException("Author with number " + authorNumber + " not found"));

        if (!author.getVersion().equals(expectedVersion)) {
            throw new StaleObjectStateException("Author", authorNumber);
        }

        authorRepository.delete(author);
        log.info("Author deleted with number: {}", authorNumber);

        // Publish domain event
        AuthorDeletedEvent event = new AuthorDeletedEvent(authorNumber, expectedVersion);
        authorEventPublisher.publishAuthorDeleted(event);
    }

    @Override
    @Transactional
    public AuthorEntity removeAuthorPhoto(Long authorNumber, Long expectedVersion) {
        log.info("Removing photo from author with number: {}", authorNumber);

        AuthorEntity author = authorRepository.findByAuthorNumber(authorNumber)
                .orElseThrow(() -> new NotFoundException("Author with number " + authorNumber + " not found"));

        if (!author.getVersion().equals(expectedVersion)) {
            throw new StaleObjectStateException("Author", authorNumber);
        }

        if (author.getPhotoURI() == null) {
            throw new NotFoundException("Author does not have a photo");
        }

        author.removePhoto();
        AuthorEntity updatedAuthor = authorRepository.save(author);
        entityManager.flush(); // Force version increment

        // Publish update event
        AuthorUpdatedEvent event = new AuthorUpdatedEvent(
                updatedAuthor.getAuthorNumber(),
                updatedAuthor.getNameValue(),
                updatedAuthor.getBioValue(),
                null,
                updatedAuthor.getVersion()
        );
        authorEventPublisher.publishAuthorUpdated(event);

        return updatedAuthor;
    }
}