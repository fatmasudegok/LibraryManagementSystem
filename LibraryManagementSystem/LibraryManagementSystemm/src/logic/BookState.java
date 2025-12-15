package logic;

import java.time.LocalDate;
import models.Book;
import models.BookStatus;

public interface BookState {
    void borrow(Book book, String userId);

    void returnBook(Book book);
}

class AvailableState implements BookState {
    @Override
    public void borrow(Book book, String userId) {
        book.setStatus(BookStatus.LOANED);
        book.setLoanedToUserId(userId);
        book.setDueDate(LocalDate.now().plusDays(15));
        System.out.println("Book borrowed successfully.");
    }

    @Override
    public void returnBook(Book book) {
        System.out.println("Error: Book is already available.");
    }
}

class LoanedState implements BookState {
    @Override
    public void borrow(Book book, String userId) {
        System.out.println("Error: Book is already loaned.");
    }

    @Override
    public void returnBook(Book book) {
        book.setStatus(BookStatus.AVAILABLE);
        book.setLoanedToUserId(null);
        book.setDueDate(null);
        System.out.println("Book returned successfully.");
    }
}