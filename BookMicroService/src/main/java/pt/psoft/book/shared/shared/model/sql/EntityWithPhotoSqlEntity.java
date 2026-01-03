package pt.psoft.book.shared.shared.model.sql;

import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import pt.psoft.shared.shared.model.sql.PhotoSqlEntity;

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
    private pt.psoft.shared.shared.model.sql.PhotoSqlEntity photo;

    protected EntityWithPhotoSqlEntity() { }

    public EntityWithPhotoSqlEntity(pt.psoft.shared.shared.model.sql.PhotoSqlEntity photo)
    {
        setPhotoInternal(photo);
    }

    protected void setPhotoInternal(pt.psoft.shared.shared.model.sql.PhotoSqlEntity photoURI)
    {
        this.photo = photoURI;
    }
    protected void setPhotoInternal(String photoURI)
    {
        setPhotoInternal(new PhotoSqlEntity(photoURI));
    }
}
