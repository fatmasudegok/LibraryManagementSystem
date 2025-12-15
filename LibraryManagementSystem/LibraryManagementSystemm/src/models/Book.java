package models;

import java.time.LocalDate;

public class Book {
    private String id;
    private String title;
    private String author;
    private String isbn;
    private String category;
    private int stock;
    private BookStatus status;
    private String loanedToUserId;
    private LocalDate dueDate;

    public Book(String id, String title, String author, String isbn, String category, int stock) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
        this.stock = stock;
        this.status = (stock > 0) ? BookStatus.AVAILABLE : BookStatus.OUT_OF_STOCK;
    }


    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getCategory() {
        return category;
    }

    public int getStock() {
        return stock;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setStock(int stock) {
        this.stock = stock;
        if (this.stock > 0 && this.status == BookStatus.OUT_OF_STOCK) {
            this.status = BookStatus.AVAILABLE;
        } else if (this.stock <= 0) {
            this.status = BookStatus.OUT_OF_STOCK;
        }
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    public String getLoanedToUserId() {
        return loanedToUserId;
    }

    public void setLoanedToUserId(String userId) {
        this.loanedToUserId = userId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate date) {
        this.dueDate = date;
    }
}