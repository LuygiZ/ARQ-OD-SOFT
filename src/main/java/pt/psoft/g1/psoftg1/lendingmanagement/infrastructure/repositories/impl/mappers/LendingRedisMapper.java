package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl.sql.BookRepositoryImpl;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.model.LendingNumber;
import pt.psoft.g1.psoftg1.lendingmanagement.model.redis.LendingRedisDTO;
import pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl.sql.ReaderDetailsRepositoryImpl;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;

@Component
@RequiredArgsConstructor
public class LendingRedisMapper {

    private final BookRepositoryImpl bookRepo;
    private final ReaderDetailsRepositoryImpl readerDetailsRepo;

    public LendingRedisDTO toDTO(Lending lending) {
        return new LendingRedisDTO(
                null, // pk não é acessível
                lending.getVersion(),
                lending.getLendingNumber(),
                lending.getBook().getIsbn().toString(),
                lending.getReaderDetails().getReaderNumber(),
                lending.getStartDate(),
                lending.getLimitDate(),
                lending.getReturnedDate(),
                lending.getCommentary(),
                lending.getFineValuePerDayInCents()
        );
    }

    public Lending toDomain(LendingRedisDTO dto) {
        // Buscar Book e ReaderDetails (podem vir do cache também!)
        Book book = bookRepo.findByIsbn(dto.getBookIsbn())
                .orElseThrow(() -> new RuntimeException("Book not found: " + dto.getBookIsbn()));

        ReaderDetails readerDetails = readerDetailsRepo.findByReaderNumber(dto.getReaderNumber())
                .orElseThrow(() -> new RuntimeException("Reader not found: " + dto.getReaderNumber()));

        // Criar Lending
        return Lending.builder()
                .book(book)
                .readerDetails(readerDetails)
                .lendingNumber(new LendingNumber(dto.getLendingNumber()))
                .startDate(dto.getStartDate())
                .limitDate(dto.getLimitDate())
                .returnedDate(dto.getReturnedDate())
                .fineValuePerDayInCents(dto.getFineValuePerDayInCents())
                .build();
    }
}