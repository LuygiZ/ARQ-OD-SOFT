package pt.psoft.book.services;

import pt.psoft.book.api.CreateBookRequest;
import pt.psoft.book.api.UpdateBookRequest;
import pt.psoft.book.model.command.BookEntity;

public interface BookCommandService {

    BookEntity createBook(String isbn, CreateBookRequest request);

    BookEntity updateBook(String isbn, UpdateBookRequest request, Long expectedVersion);

    void deleteBook(String isbn, Long expectedVersion);

    BookEntity removeBookPhoto(String isbn, Long expectedVersion);
}