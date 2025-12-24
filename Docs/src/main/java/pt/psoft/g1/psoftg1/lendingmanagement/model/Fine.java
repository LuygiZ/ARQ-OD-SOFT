package pt.psoft.g1.psoftg1.lendingmanagement.model;

import jakarta.persistence.*;

import java.util.Objects;

/**
 * The {@code Fine} class models a fine applied when a lending is past its due date.
 * <p>It stores its current value, and the associated {@code Lending}.
 * @author  rmfranca*/
public class Fine {
    private final int fineValuePerDayInCents;
    private final int centsValue;

    private final Lending lending;

    public Fine(Lending lending)
    {
        if (lending == null)
        {
            throw new IllegalArgumentException("Lending cannot be null");
        }

        if (lending.getDaysDelayed() <= 0)
        {
            throw new IllegalArgumentException("Lending is not overdue");
        }

        this.fineValuePerDayInCents = lending.getFineValuePerDayInCents();
        this.centsValue = fineValuePerDayInCents * lending.getDaysDelayed();
        this.lending = lending;
    }

    protected Fine(int fineValuePerDayInCents, int centsValue, Lending lending)
    {
        this.fineValuePerDayInCents = fineValuePerDayInCents;
        this.centsValue = centsValue;
        this.lending = Objects.requireNonNull(lending);
    }

    // Getters
    public int getFineValuePerDayInCents() { return fineValuePerDayInCents; }
    public int getCentsValue() { return centsValue; }
    public Lending getLending() { return lending; }
}
