package pt.psoft.genre.shared.model;

import pt.psoft.shared.shared.model.Photo;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public abstract class EntityWithPhoto
{
    protected pt.psoft.shared.shared.model.Photo photo;

    // Setter
    //This method is used by the mapper in order to set the photo. This will call the setPhotoInternal method that
    //will contain all the logic to set the photo
    public void setPhoto(String photo)
    {
        setPhotoInternal(photo);
    }

    protected void setPhotoInternal(String photo)
    {
        if (photo == null)
        {
            setPhotoInternal((pt.psoft.shared.shared.model.Photo) null);
            return;
        }

        try
        {
            setPhotoInternal(new pt.psoft.shared.shared.model.Photo(Path.of(photo)));
        }
        catch (InvalidPathException e)
        {
            setPhotoInternal((pt.psoft.shared.shared.model.Photo) null);
        }
    }

    protected void setPhotoInternal(pt.psoft.shared.shared.model.Photo photo) {
        this.photo = photo;
    }

    // Getter
    public Photo getPhoto()
    {
        return photo;
    }
}

