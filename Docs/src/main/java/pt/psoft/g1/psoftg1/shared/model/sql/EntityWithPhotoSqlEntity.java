package pt.psoft.g1.psoftg1.shared.model.sql;

import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("sql-redis")
@Primary
@Getter
@MappedSuperclass
public abstract class EntityWithPhotoSqlEntity
{
    @Nullable
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="photo_id", nullable = true)
    @Setter
    @Getter
    private PhotoSqlEntity photo;

    protected EntityWithPhotoSqlEntity() { }

    public EntityWithPhotoSqlEntity(PhotoSqlEntity photo)
    {
        setPhotoInternal(photo);
    }

    protected void setPhotoInternal(PhotoSqlEntity photoURI)
    {
        this.photo = photoURI;
    }
    protected void setPhotoInternal(String photoURI)
    {
        setPhotoInternal(new PhotoSqlEntity(photoURI));
    }
}
