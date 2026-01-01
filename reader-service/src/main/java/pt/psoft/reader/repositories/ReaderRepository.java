package pt.psoft.reader.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.psoft.reader.model.Reader;

import java.util.Optional;

/**
 * Repository for Reader entities
 */
@Repository
public interface ReaderRepository extends JpaRepository<Reader, Long> {

    Optional<Reader> findByReaderNumber(String readerNumber);

    Optional<Reader> findByUsername(String username);

    boolean existsByReaderNumber(String readerNumber);

    @Query("SELECT r FROM Reader r WHERE r.fullName LIKE %:name%")
    Page<Reader> findByNameContaining(@Param("name") String name, Pageable pageable);

    @Query("SELECT r FROM Reader r WHERE r.phoneNumber = :phoneNumber")
    Optional<Reader> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Query("SELECT COUNT(r) FROM Reader r")
    long countReaders();

    @Query("SELECT MAX(CAST(SUBSTRING(r.readerNumber, 6) AS int)) FROM Reader r WHERE r.readerNumber LIKE :yearPrefix%")
    Integer findMaxSequenceForYear(@Param("yearPrefix") String yearPrefix);
}
