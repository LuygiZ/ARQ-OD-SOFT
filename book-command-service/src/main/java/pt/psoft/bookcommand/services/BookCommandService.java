package pt.psoft.bookcommand.services;

import pt.psoft.bookcommand.api.UpdateBookRequest;
import pt.psoft.bookcommand.model.BookEntity;
import pt.psoft.shared.dto.book.CreateBookRequest;

public interface BookCommandService {

    BookEntity createBook(String isbn, CreateBookRequest request);

    BookEntity updateBook(String isbn, UpdateBookRequest request, Long expectedVersion);

    void deleteBook(String isbn, Long expectedVersion);

    BookEntity removeBookPhoto(String isbn, Long expectedVersion);
}
