package pt.psoft.author.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.author.api.dto.AuthorView;
import pt.psoft.author.model.query.AuthorReadModel;
import pt.psoft.author.repositories.mongo.AuthorQueryRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of Author Query Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorQueryServiceImpl implements AuthorQueryService {

    private final AuthorQueryRepository authorQueryRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "authors", key = "#authorNumber", unless = "#result == null")
    public AuthorView findByAuthorNumber(Long authorNumber) {
        log.debug("Finding author by number: {}", authorNumber);
        return authorQueryRepository.findByAuthorNumber(authorNumber)
                .map(this::toAuthorView)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "authors-search", key = "#name", unless = "#result == null || #result.isEmpty()")
    public List<AuthorView> searchByName(String name) {
        log.debug("Searching authors by name: {}", name);
        if (name == null || name.isBlank()) {
            return findAll();
        }
        return authorQueryRepository.findByNameStartingWithIgnoreCase(name.trim())
                .stream()
                .map(this::toAuthorView)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "authors-all", unless = "#result == null || #result.isEmpty()")
    public List<AuthorView> findAll() {
        log.debug("Finding all authors");
        return authorQueryRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toAuthorView)
                .collect(Collectors.toList());
    }

    /**
     * Convert AuthorReadModel to AuthorView
     */
    private AuthorView toAuthorView(AuthorReadModel readModel) {
        return new AuthorView(
                readModel.getAuthorNumber(),
                readModel.getName(),
                readModel.getBio(),
                readModel.getPhotoURI(),
                readModel.getVersion()
        );
    }
}