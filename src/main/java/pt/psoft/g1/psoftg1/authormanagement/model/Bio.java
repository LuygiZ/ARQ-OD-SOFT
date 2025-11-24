package pt.psoft.g1.psoftg1.authormanagement.model;

import pt.psoft.g1.psoftg1.shared.model.StringUtilsCustom;

public class Bio
{
    public static final int BIO_MAX_LENGTH = 4096;

    private String bio;

    public Bio(String bio)
    {
        setBio(bio);
    }

    protected Bio()
    {
        // for frameworks if needed
    }

    public void setBio(String bio)
    {
        if (bio == null)
        {
            throw new IllegalArgumentException("Bio cannot be null");
        }
        if (bio.isBlank())
        {
            throw new IllegalArgumentException("Bio cannot be blank");
        }
        if (bio.length() > BIO_MAX_LENGTH)
        {
            throw new IllegalArgumentException("Bio has a maximum of " + BIO_MAX_LENGTH + " characters");
        }
        this.bio = StringUtilsCustom.sanitizeHtml(bio);
    }

    public String toString()
    {
        return bio;
    }

    public String getBio()
    {
        return bio;
    }

    public String getValue()
    {
        return bio;
    }
}
