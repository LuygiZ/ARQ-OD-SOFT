package pt.psoft.book.genremanagement.services;

import pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.genre.genremanagement.services.GenreLendingsDTO;
import pt.psoft.genre.genremanagement.services.GenreLendingsPerMonthDTO;
import pt.psoft.genre.genremanagement.services.GetAverageLendingsQuery;

import java.util.List;
import java.util.Optional;

public interface GenreService {
    Iterable<Genre> findAll();
    Genre save(Genre genre);
    Optional<Genre> findByString(String name);
    List<GenreBookCountDTO> findTopGenreByBooks();
    List<GenreLendingsDTO> getAverageLendings(GetAverageLendingsQuery query, Page page);
    List<GenreLendingsPerMonthDTO> getLendingsPerMonthLastYearByGenre();
    List<GenreLendingsPerMonthDTO> getLendingsAverageDurationPerMonth(String startDate, String endDate);
}
