package pt.psoft.g1.psoftg1.authormanagement.model.redis;

import java.io.Serializable;

public class AuthorRedisDTO implements Serializable {
    private String authorNumber;
    private long version;
    private String name;
    private String bio;
    private String photoURI;

    public AuthorRedisDTO() {
    }

    public AuthorRedisDTO(String authorNumber, long version, String name, String bio, String photoURI) {
        this.authorNumber = authorNumber;
        this.version = version;
        this.name = name;
        this.bio = bio;
        this.photoURI = photoURI;
    }


    // Getters e setters
    public String getAuthorNumber() {
        return authorNumber;
    }

    public void setAuthorNumber(String authorNumber) {
        this.authorNumber = authorNumber;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPhotoURI() {
        return photoURI;
    }

    public void setPhotoURI(String photoURI) {
        this.photoURI = photoURI;
    }
}