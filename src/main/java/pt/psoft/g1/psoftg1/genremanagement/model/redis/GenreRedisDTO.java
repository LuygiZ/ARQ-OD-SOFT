package pt.psoft.g1.psoftg1.genremanagement.model.redis;

import java.io.Serializable;

public class GenreRedisDTO implements Serializable {
    private Long pk;
    private String genre;

    public GenreRedisDTO() {}

    public GenreRedisDTO(Long pk, String genre) {
        this.pk = pk;
        this.genre = genre;
    }

    // Getters e Setters
    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}