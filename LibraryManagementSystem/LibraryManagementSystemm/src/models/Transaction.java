package models;

import java.time.LocalDate;

public class Transaction {
    private String id;
    private String bookId;
    private String userId;
    private String transactionType;
    private LocalDate transactionDate;
    private LocalDate dueDate;
    private double penalty;

    public Transaction(String id, String bookId, String userId, String type, LocalDate date, LocalDate dueDate, double penalty) {
        this.id = id;
        this.bookId = bookId;
        this.userId = userId;
        this.transactionType = type;
        this.transactionDate = date;
        this.dueDate = dueDate;
        this.penalty = penalty;
    }


    public String getId() { return id; }
    public String getBookId() { return bookId; }
    public String getUserId() { return userId; }
    public String getTransactionType() { return transactionType; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public LocalDate getDueDate() { return dueDate; }
    public double getPenalty() { return penalty; }
}