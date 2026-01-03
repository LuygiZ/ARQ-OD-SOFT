package pt.psoft.genre.services;

import pt.psoft.genre.model.Genre;
import pt.psoft.shared.dto.genre.GenreDTO;


import java.util.List;
import java.util.Optional;

public interface GenreService {

    List<GenreDTO> findAll();

    Optional<GenreDTO> findById(Long id);

    Optional<GenreDTO> findByName(String name);

    GenreDTO create(String genreName);

    GenreDTO update(Long id, String genreName);

    void delete(Long id);
}