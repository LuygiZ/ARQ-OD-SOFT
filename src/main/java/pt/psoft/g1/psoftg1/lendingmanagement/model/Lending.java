package pt.psoft.g1.psoftg1.lendingmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.StaleObjectStateException;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

public class Lending {

    private Long pk;
    private LendingNumber lendingNumber;
    private Book book;  // ✅ MANTÉM ESTE
    private ReaderDetails readerDetails;  // ✅ MANTÉM ESTE
    private LocalDate startDate;
    private LocalDate limitDate;
    private LocalDate returnedDate;
    private long version;
    private String commentary;
    private Integer daysUntilReturn;
    private Integer daysOverdue;
    private int fineValuePerDayInCents;
    // ❌ REMOVE borrowedBook e borrower - são duplicados!

    public Lending(Book book, ReaderDetails readerDetails, int seq, int lendingDuration, int fineValuePerDayInCents)
    {
        try
        {
            this.book = Objects.requireNonNull(book);
            this.readerDetails = Objects.requireNonNull(readerDetails);
        }
        catch (NullPointerException e)
        {
            throw new IllegalArgumentException("Null objects passed to lending");
        }

        this.lendingNumber = new LendingNumber(seq);
        this.startDate = LocalDate.now();
        this.limitDate = LocalDate.now().plusDays(lendingDuration);
        this.returnedDate = null;
        this.fineValuePerDayInCents = fineValuePerDayInCents;

        setDaysUntilReturn();
        setDaysOverdue();
    }

    @Builder
    public Lending(Book book, ReaderDetails readerDetails, LendingNumber lendingNumber, LocalDate startDate, LocalDate limitDate, LocalDate returnedDate, int fineValuePerDayInCents) {
        try
        {
            this.book = Objects.requireNonNull(book);
            this.readerDetails = Objects.requireNonNull(readerDetails);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Null objects passed to lending");
        }
        this.lendingNumber = lendingNumber;
        this.startDate = startDate;
        this.limitDate = limitDate;
        this.returnedDate = returnedDate;
        this.fineValuePerDayInCents = fineValuePerDayInCents;

        setDaysUntilReturn();
        setDaysOverdue();
    }

    // Getters
    public Book getBook() { return book; }
    public ReaderDetails getReaderDetails() { return readerDetails; }

    // ✅ ADICIONA estes métodos que apontam para os mesmos campos
    public Book getBorrowedBook() { return book; }
    public ReaderDetails getBorrower() { return readerDetails; }

    public LocalDate getStartDate() { return startDate; }
    public LocalDate getLimitDate() { return limitDate; }
    public LocalDate getReturnedDate() { return returnedDate; }
    public String getCommentary() { return commentary; }
    public String getLendingNumber() { return lendingNumber.toString(); }
    public long getVersion() { return version; }
    public String getTitle()
    {
        return this.book.getTitle().toString();
    }

    public int getDaysDelayed()
    {
        if(returnedDate != null)
        {
            return Math.max((int) ChronoUnit.DAYS.between(limitDate, returnedDate), 0);
        }
        else
        {
            return Math.max((int) ChronoUnit.DAYS.between(limitDate, LocalDate.now()), 0);

        }
    }

    public Optional<Integer> getFineValueInCents()
    {
        int days = getDaysDelayed();
        return days > 0 ? Optional.of(fineValuePerDayInCents * days) : Optional.empty();
    }

    public int getFineValuePerDayInCents() { return fineValuePerDayInCents; }

    public Optional<Integer> getDaysUntilReturn()
    {
        setDaysUntilReturn();
        return Optional.ofNullable(daysUntilReturn);
    }

    public Optional<Integer> getDaysOverdue()
    {
        setDaysOverdue();
        return Optional.ofNullable(daysOverdue);
    }

    public void setReturned(final long desiredVersion, final String commentary){

        if (this.returnedDate != null)
        {
            throw new IllegalArgumentException("book has already been returned!");
        }

        if (this.version != desiredVersion)
        {
            throw new StaleObjectStateException("Object was already modified by another user", this.pk);
        }

        if(commentary != null)
        {
            this.commentary = commentary;
        }

        this.returnedDate = LocalDate.now();
    }

    private void setDaysUntilReturn(){
        int daysUntilReturn = (int) ChronoUnit.DAYS.between(LocalDate.now(), this.limitDate);
        if(this.returnedDate != null || daysUntilReturn < 0){
            this.daysUntilReturn = null;
        }else{
            this.daysUntilReturn = daysUntilReturn;
        }
    }

    private void setDaysOverdue(){
        int days = getDaysDelayed();
        if(days > 0){
            this.daysOverdue = days;
        }else{
            this.daysOverdue = null;
        }
    }

    protected Lending() {}

    public static Lending newBootstrappingLending(Book book,
                                                  ReaderDetails readerDetails,
                                                  int year,
                                                  int seq,
                                                  LocalDate startDate,
                                                  LocalDate returnedDate,
                                                  int lendingDuration,
                                                  int fineValuePerDayInCents){
        Lending lending = new Lending();

        try {
            lending.book = Objects.requireNonNull(book);
            lending.readerDetails = Objects.requireNonNull(readerDetails);
        }catch (NullPointerException e){
            throw new IllegalArgumentException("Null objects passed to lending");
        }
        lending.lendingNumber = new LendingNumber(year, seq);
        lending.startDate = startDate;
        lending.limitDate = startDate.plusDays(lendingDuration);
        lending.fineValuePerDayInCents = fineValuePerDayInCents;
        lending.returnedDate = returnedDate;
        return lending;
    }
}