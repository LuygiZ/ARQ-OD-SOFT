package pt.psoft.genre.shared.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;

public class Photo
{
    //private long pk;
    private String photoFile;

    public Photo(Path photoFile)
    {
        setPhotoFile(photoFile.toString());
    }

    protected Photo()
    {
        // smth here
    }

    // Setter
    private void setPhotoFile(String photofile)
    {
        if (photofile == null)
        {
            throw new IllegalArgumentException("PhotoFile cannot be null");
        }

        this.photoFile = photofile;
    }

    // Getter
    public String getPhotoFile() { return this.photoFile; }

    // Helper
    public String toString() { return this.photoFile; }
}

