package pt.psoft.author.shared.model;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public abstract class EntityWithPhoto
{
    protected Photo photo;

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
            setPhotoInternal((Photo) null);
            return;
        }

        try
        {
            setPhotoInternal(new Photo(Path.of(photo)));
        }
        catch (InvalidPathException e)
        {
            setPhotoInternal((Photo) null);
        }
    }

    protected void setPhotoInternal(Photo photo) {
        this.photo = photo;
    }

    // Getter
    public Photo getPhoto()
    {
        return photo;
    }
}

