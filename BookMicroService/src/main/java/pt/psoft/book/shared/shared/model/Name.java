package pt.psoft.book.shared.shared.model;

public class Name
{
    private String name;

    public Name(String name)
    {
        setName(name);
    }

    protected Name()
    {
        // for frameworks if needed
    }

    // Setter
    public void setName(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if (name.isBlank())
        {
            throw new IllegalArgumentException("Name cannot be blank or only white spaces");
        }
//        if (!StringUtilsCustom.isAlphanumeric(name))
//        {
//            throw new IllegalArgumentException("Name can only contain alphanumeric characters");
//        }

        this.name = name;
    }

    // Getter
    public String getName() { return this.name; }

    // Helper
    @Override
    public String toString() { return this.name; }
}
