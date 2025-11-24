package pt.psoft.g1.psoftg1.bookmanagement.model;

public class Title {
    public static final int TITLE_MAX_LENGTH = 128;

    private String title;

    public Title(String title)
    {
        setTitle(title);
    }

    // Getter
    public String getTitle()
    {
        return this.title;
    }

    //
    public void setTitle(String title) {
        this.title = title;  // Estava vazio!
    }

    // Helpers
    @Override
    public String toString()
    {
        return this.title;
    }
}

