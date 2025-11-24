package pt.psoft.g1.psoftg1.shared.infrastructure.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.shared.infrastructure.repositories.impl.sqlmapper.PhotoEntityMapper;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;

@Profile("sql")
@Primary
@Repository
@RequiredArgsConstructor
public class PhotoRepositoryImpl implements PhotoRepository
{
    private final SpringDataPhotoRepository photoRepository;
    private final PhotoEntityMapper photoEntityMapper;

    @Override
    public void deleteByPhotoFile(String photoFile)
    {
        photoRepository.deleteByPhotoFile(photoFile);
    }
}
