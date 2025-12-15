package db;

import java.util.List;
import models.Book;
import models.ConcreteUser;
import models.Transaction;

public abstract class AbstractDatabase {

    public AbstractDatabase() {
    }

    public abstract void connect();

    public abstract List<Book> loadBooks();
    public abstract List<ConcreteUser> loadUsers();
    public abstract List<Transaction> loadTransactions();

    public abstract void saveBook(Book book);
    public abstract void saveUser(ConcreteUser user);
    public abstract void saveTransaction(Transaction transaction);

    public abstract void deleteUser(String userId);

    public abstract void deleteBook(String bookId);
}