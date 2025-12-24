package pt.psoft.genre.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.genre.api.GenreViewMapper;
import pt.psoft.genre.messaging.GenreEventPublisher;
import pt.psoft.genre.model.Genre;
import pt.psoft.genre.repositories.GenreRepository;
import pt.psoft.shared.dto.genre.GenreDTO;
import pt.psoft.shared.events.genre.GenreCreatedEvent;
import pt.psoft.shared.events.genre.GenreDeletedEvent;
import pt.psoft.shared.events.genre.GenreUpdatedEvent;
import pt.psoft.shared.exceptions.ConflictException;
import pt.psoft.shared.exceptions.NotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final GenreEventPublisher eventPublisher;
    private final GenreViewMapper mapper;

    @Override
    @Cacheable(value = "genres", key = "'all'")
    public List<GenreDTO> findAll() {
        log.debug("Fetching all genres");
        return genreRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "genres", key = "#id")
    public Optional<GenreDTO> findById(Long id) {
        log.debug("Fetching genre with id: {}", id);
        return genreRepository.findById(id)
                .map(this::toDTO);
    }

    @Override
    @Cacheable(value = "genres", key = "#name")
    public Optional<GenreDTO> findByName(String name) {
        log.debug("Fetching genre with name: {}", name);
        return genreRepository.findByName(name)
                .map(this::toDTO);
    }

    @Override
    @Transactional
    @CacheEvict(value = "genres", allEntries = true)
    public GenreDTO create(String genreName) {
        log.info("Creating new genre: {}", genreName);

        // Validar se já existe
        if (genreRepository.existsByName(genreName)) {
            throw new ConflictException("Genre", "name", genreName);
        }

        // Criar entidade
        Genre genre = new Genre(genreName);
        genre = genreRepository.save(genre);

        // Publicar evento (via Outbox Pattern)
        GenreCreatedEvent event = new GenreCreatedEvent(
                genre.getId().toString(),
                genre.getName()
        );
        eventPublisher.publish(event);

        log.info("Genre created with id: {}", genre.getId());
        return toDTO(genre);
    }

    @Override
    @Transactional
    @CacheEvict(value = "genres", allEntries = true)
    public GenreDTO update(Long id, String genreName) {
        log.info("Updating genre {}: {}", id, genreName);

        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Genre", id.toString()));

        // Verificar se novo nome já existe (exceto para o próprio)
        genreRepository.findByName(genreName)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new ConflictException("Genre", "name", genreName);
                    }
                });

        genre.setName(genreName);
        genre = genreRepository.save(genre);

        // Publicar evento
        GenreUpdatedEvent event = new GenreUpdatedEvent(
                genre.getId().toString(),
                genre.getName()
        );
        eventPublisher.publish(event);

        log.info("Genre updated: {}", id);
        return toDTO(genre);
    }

    @Override
    @Transactional
    @CacheEvict(value = "genres", allEntries = true)
    public void delete(Long id) {
        log.info("Deleting genre: {}", id);

        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Genre", id.toString()));

        String genreName = genre.getName();
        genreRepository.delete(genre);

        // Publicar evento
        GenreDeletedEvent event = new GenreDeletedEvent(
                id.toString(),
                genreName
        );
        eventPublisher.publish(event);

        log.info("Genre deleted: {}", id);
    }

    private GenreDTO toDTO(Genre genre) {
        return new GenreDTO(
                genre.getId().toString(),
                genre.getName()
        );
    }
}