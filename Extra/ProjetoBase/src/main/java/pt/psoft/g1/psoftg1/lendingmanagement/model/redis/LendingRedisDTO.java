package pt.psoft.g1.psoftg1.lendingmanagement.model.redis;

import java.io.Serializable;
import java.time.LocalDate;

public class LendingRedisDTO implements Serializable {
    private Long pk;
    private Long version;
    private String lendingNumber;
    private String bookIsbn;
    private String readerNumber;
    private LocalDate startDate;
    private LocalDate limitDate;
    private LocalDate returnedDate;
    private String commentary;
    private int fineValuePerDayInCents;

    public LendingRedisDTO() {}

    public LendingRedisDTO(Long pk, Long version, String lendingNumber, String bookIsbn,
                           String readerNumber, LocalDate startDate, LocalDate limitDate,
                           LocalDate returnedDate, String commentary, int fineValuePerDayInCents) {
        this.pk = pk;
        this.version = version;
        this.lendingNumber = lendingNumber;
        this.bookIsbn = bookIsbn;
        this.readerNumber = readerNumber;
        this.startDate = startDate;
        this.limitDate = limitDate;
        this.returnedDate = returnedDate;
        this.commentary = commentary;
        this.fineValuePerDayInCents = fineValuePerDayInCents;
    }

    // Getters e Setters
    public Long getPk() { return pk; }
    public void setPk(Long pk) { this.pk = pk; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public String getLendingNumber() { return lendingNumber; }
    public void setLendingNumber(String lendingNumber) { this.lendingNumber = lendingNumber; }

    public String getBookIsbn() { return bookIsbn; }
    public void setBookIsbn(String bookIsbn) { this.bookIsbn = bookIsbn; }

    public String getReaderNumber() { return readerNumber; }
    public void setReaderNumber(String readerNumber) { this.readerNumber = readerNumber; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getLimitDate() { return limitDate; }
    public void setLimitDate(LocalDate limitDate) { this.limitDate = limitDate; }

    public LocalDate getReturnedDate() { return returnedDate; }
    public void setReturnedDate(LocalDate returnedDate) { this.returnedDate = returnedDate; }

    public String getCommentary() { return commentary; }
    public void setCommentary(String commentary) { this.commentary = commentary; }

    public int getFineValuePerDayInCents() { return fineValuePerDayInCents; }
    public void setFineValuePerDayInCents(int fineValuePerDayInCents) {
        this.fineValuePerDayInCents = fineValuePerDayInCents;
    }
}