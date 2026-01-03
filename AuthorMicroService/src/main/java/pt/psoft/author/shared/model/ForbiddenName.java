package pt.psoft.author.shared.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ForbiddenName
{
    private String forbiddenName;

    public ForbiddenName(String forbiddenName)
    {
        setForbiddenName(forbiddenName);
    }

    protected ForbiddenName()
    {
        //smth here?
    }

    private void setForbiddenName(String forbiddenName)
    {
        if (forbiddenName == null || forbiddenName.isBlank())
        {
            throw new IllegalArgumentException("Forbidden name cannot be null or blank");
        }

        this.forbiddenName = forbiddenName;
    }

    public String getForbiddenName()
    {
        return forbiddenName;
    }
}
