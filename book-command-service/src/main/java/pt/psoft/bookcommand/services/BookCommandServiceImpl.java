package pt.psoft.bookcommand.services;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleObjectStateException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.bookcommand.api.UpdateBookRequest;
import pt.psoft.bookcommand.messaging.BookEventPublisher;
import pt.psoft.bookcommand.model.BookEntity;
import pt.psoft.bookcommand.repositories.BookRepository;
import pt.psoft.shared.dto.book.CreateBookRequest;
import pt.psoft.shared.events.book.BookCreatedEvent;
import pt.psoft.shared.events.book.BookDeletedEvent;
import pt.psoft.shared.events.book.BookUpdatedEvent;
import pt.psoft.shared.exceptions.ConflictException;
import pt.psoft.shared.exceptions.NotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookCommandServiceImpl implements BookCommandService {

    private final BookRepository bookCommandRepository;
    private final BookEventPublisher bookEventPublisher;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public BookEntity createBook(String isbn, CreateBookRequest request) {
        log.info("Creating book with ISBN: {}", isbn);

        if (bookCommandRepository.existsByIsbn(isbn)) {
            throw new ConflictException("Book with ISBN " + isbn + " already exists");
        }

        BookEntity book = new BookEntity(
                isbn,
                request.getTitle(),
                request.getDescription(),
                request.getGenre(),
                request.getAuthorIds(),
                request.getPhotoURI()
        );

        BookEntity savedBook = bookCommandRepository.save(book);
        log.info("Book created with ISBN: {}", isbn);

        BookCreatedEvent event = new BookCreatedEvent(
                savedBook.getIsbnValue(),
                savedBook.getTitleValue(),
                savedBook.getDescriptionValue(),
                savedBook.getGenreName(),
                savedBook.getAuthorIds(),
                savedBook.getPhotoURI()
        );
        bookEventPublisher.publishBookCreated(event);

        return savedBook;
    }

    @Override
    @Transactional
    public BookEntity updateBook(String isbn, UpdateBookRequest request, Long expectedVersion) {
        log.info("Updating book with ISBN: {}", isbn);

        BookEntity book = bookCommandRepository.findByIsbn(isbn)
                .orElseThrow(() -> new NotFoundException("Book with ISBN " + isbn + " not found"));

        if (!book.getVersion().equals(expectedVersion)) {
            throw new StaleObjectStateException("Book", book.getPk());
        }

        book.update(
                request.getTitle(),
                request.getDescription(),
                request.getGenre(),
                request.getAuthorIds(),
                request.getPhotoURI()
        );

        BookEntity updatedBook = bookCommandRepository.save(book);
        entityManager.flush();

        log.info("Book updated with ISBN: {}", isbn);

        BookUpdatedEvent event = new BookUpdatedEvent(
                updatedBook.getIsbnValue(),
                updatedBook.getTitleValue(),
                updatedBook.getDescriptionValue(),
                updatedBook.getGenreName(),
                updatedBook.getAuthorIds(),
                updatedBook.getPhotoURI(),
                updatedBook.getVersion()
        );
        bookEventPublisher.publishBookUpdated(event);

        return updatedBook;
    }

    @Override
    @Transactional
    public void deleteBook(String isbn, Long expectedVersion) {
        log.info("Deleting book with ISBN: {}", isbn);

        BookEntity book = bookCommandRepository.findByIsbn(isbn)
                .orElseThrow(() -> new NotFoundException("Book with ISBN " + isbn + " not found"));

        if (!book.getVersion().equals(expectedVersion)) {
            throw new StaleObjectStateException("Book", book.getPk());
        }

        bookCommandRepository.delete(book);
        log.info("Book deleted with ISBN: {}", isbn);

        BookDeletedEvent event = new BookDeletedEvent(isbn, expectedVersion);
        bookEventPublisher.publishBookDeleted(event);
    }

    @Override
    @Transactional
    public BookEntity removeBookPhoto(String isbn, Long expectedVersion) {
        log.info("Removing photo from book with ISBN: {}", isbn);

        BookEntity book = bookCommandRepository.findByIsbn(isbn)
                .orElseThrow(() -> new NotFoundException("Book with ISBN " + isbn + " not found"));

        if (!book.getVersion().equals(expectedVersion)) {
            throw new StaleObjectStateException("Book", book.getPk());
        }

        if (book.getPhotoURI() == null) {
            throw new NotFoundException("Book does not have a photo");
        }

        book.removePhoto();
        BookEntity updatedBook = bookCommandRepository.save(book);
        entityManager.flush();

        BookUpdatedEvent event = new BookUpdatedEvent(
                updatedBook.getIsbnValue(),
                updatedBook.getTitleValue(),
                updatedBook.getDescriptionValue(),
                updatedBook.getGenreName(),
                updatedBook.getAuthorIds(),
                null,
                updatedBook.getVersion()
        );
        bookEventPublisher.publishBookUpdated(event);

        return updatedBook;
    }
}
