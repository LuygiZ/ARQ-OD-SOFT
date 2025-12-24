package pt.psoft.g1.psoftg1.genremanagement.model;

public class Genre
{
    public static final int GENRE_MAX_LENGTH = 100;

    //TODO: Substituir por ID e nao é suposto ser publico
    public long pk;
    private String genre;

    protected Genre() { }

    public Genre(String genre)
    {
        setGenre(genre);
    }

    private void setGenre(String genre)
    {
        if (genre == null)
        {
            throw new IllegalArgumentException("Genre cannot be null");
        }
        if (genre.isBlank())
        {
            throw new IllegalArgumentException("Genre cannot be blank");
        }
        if (genre.length() > GENRE_MAX_LENGTH)
        {
            throw new IllegalArgumentException("Genre has a maximum of " + GENRE_MAX_LENGTH + " characters");
        }

        this.genre = genre.strip();
    }

    // Getters
    public long getPk()
    {
        return pk;
    }

    public String getGenre()
    {
        return genre;
    }

    // Helper
    @Override
    public String toString()
    {
        return genre;
    }
}