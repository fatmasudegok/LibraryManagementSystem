package logic;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import models.*;
import db.*;

public class LibraryFacade {

    private AbstractDatabase db;
    public EventManager eventManager;
    private List<Book> books;
    private List<ConcreteUser> users;
    private List<Transaction> transactions;

    public LibraryFacade() {
        this.eventManager = new EventManager();

        this.db = new MySQLDatabase();

        this.books = db.loadBooks();
        this.users = db.loadUsers();
        this.transactions = db.loadTransactions();
    }


    public ConcreteUser login(String email, String password) {
        for (ConcreteUser u : users) {
            if (u.getEmail().equals(email) && u.getPassword().equals(password)) {
                SessionManager.getInstance().login(u);
                return u;
            }
        }
        return null;
    }

    public void logout() {
        SessionManager.getInstance().logout();
    }

    public ConcreteUser getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser();
    }

    public void addUser(String name, String email, String password, Role role) {
        String newId = String.valueOf(System.currentTimeMillis());
        AbstractUser newUserAbs = UserFactory.createUser(role, newId, name, email, password);

        if (newUserAbs instanceof ConcreteUser) {
            ConcreteUser newUser = (ConcreteUser) newUserAbs;
            users.add(newUser);
            db.saveUser(newUser);
            eventManager.notify("USERS_UPDATED");
        }
    }

    public void updateUser(String userId, String newPass) {
        for (ConcreteUser u : users) {
            if (u.getId().equals(userId)) {
                u.setPassword(newPass);
                db.saveUser(u);
                break;
            }
        }
    }

    public void deleteUser(String userId) {
        users.removeIf(u -> u.getId().equals(userId));
        db.deleteUser(userId);
        eventManager.notify("USERS_UPDATED");
    }

    public List<ConcreteUser> searchUsers(String query) {
        if (query == null || query.isEmpty())
            return users;

        String q = query.toLowerCase();

        return users.stream()
                .filter(u -> u.getName().toLowerCase().contains(q)
                        || u.getId().contains(q)
                        || u.getEmail().toLowerCase().contains(q)
                )
                .collect(Collectors.toList());
    }

    public String getUserNameById(String id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .map(ConcreteUser::getName)
                .findFirst()
                .orElse("Bilinmiyor");
    }


    public List<Book> getAllBooks() {
        return books;
    }

    public List<Book> searchBooks(String query) {
        if (query == null || query.isEmpty())
            return books;

        String q = query.toLowerCase();

        return books.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(q)
                        || b.getCategory().toLowerCase().contains(q)
                        || b.getAuthor().toLowerCase().contains(q)
                        || b.getIsbn().toLowerCase().contains(q)
                )
                .collect(Collectors.toList());
    }

    public void addBook(String title, String author, String isbn, String category, int stock) {

        long timestamp = System.currentTimeMillis();
        int randomSuffix = new Random().nextInt(1000);
        String newId = String.valueOf(timestamp) + String.format("%03d", randomSuffix);

        Book b = new Book(
                newId,
                title, author, isbn, category, stock
        );
        books.add(b);
        db.saveBook(b);
        eventManager.notify("BOOKS_UPDATED");
    }

    public void updateBook(String id, String title, String category, int stock) {
        Book b = findBook(id);
        if (b != null) {
            b.setTitle(title);
            b.setCategory(category);
            b.setStock(stock);
            db.saveBook(b);
            eventManager.notify("BOOKS_UPDATED");
        }
    }

    public ConcreteUser findUser(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        String q = query.toLowerCase();

        return users.stream()
                .filter(u -> u.getName().toLowerCase().contains(q)
                        || u.getId().contains(q)
                        || u.getEmail().toLowerCase().contains(q))
                .findFirst()
                .orElse(null);
    }

    public void deleteBook(String bookId) {
        books.removeIf(b -> b.getId().equals(bookId));

        db.deleteBook(bookId);

        eventManager.notify("BOOKS_UPDATED");
    }

    public void borrowBookInternal(String bookId, String userId) {
        Book book = findBook(bookId);
        if (book != null && book.getStock() > 0) {
            book.setStock(book.getStock() - 1);
            book.setLoanedToUserId(userId);
            book.setDueDate(LocalDate.now().plusDays(15));

            String transactionId = String.valueOf(System.currentTimeMillis() + 1);
            Transaction t = new Transaction(
                    transactionId,
                    bookId,
                    userId,
                    "Borrowed",
                    LocalDate.now(),
                    LocalDate.now().plusDays(15),
                    0.0
            );
            transactions.add(t);
            db.saveTransaction(t);

            db.saveBook(book);
            eventManager.notify("BOOKS_UPDATED");
        }
    }


    public void returnBook(String bookId) {
        Book book = findBook(bookId);
        if (book != null && book.getLoanedToUserId() != null) {
            String userId = book.getLoanedToUserId();
            LocalDate dueDate = book.getDueDate();
            double penalty = calculatePenalty(dueDate);

            if (penalty > 0) {
                String penaltyId = String.valueOf(System.currentTimeMillis());
                if (db instanceof MySQLDatabase) {
                    ((MySQLDatabase) db).savePenalty(penaltyId, book.getLoanedToUserId(), book.getId(), penalty);
                }
            }

            book.setStock(book.getStock() + 1);
            book.setLoanedToUserId(null);
            book.setDueDate(null);

            String transactionId = String.valueOf(System.currentTimeMillis() + 2);
            Transaction t = new Transaction(
                    transactionId,
                    bookId,
                    userId,
                    "Returned",
                    LocalDate.now(),
                    dueDate,
                    penalty
            );
            transactions.add(t);
            db.saveTransaction(t);

            db.saveBook(book);
            eventManager.notify("BOOKS_UPDATED");
        }
    }


    public double calculatePenalty(LocalDate dueDate) {
        if (dueDate == null) return 0.0;
        long days = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        return days > 0 ? days * 5.0 : 0.0;
    }


    public Book findBook(String id) {
        return books.stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Transaction> searchReturns(String query) {
        List<Transaction> filteredList = transactions.stream()
                .filter(t -> t.getTransactionType().equals("Returned") || t.getPenalty() > 0)
                .collect(Collectors.toList());

        if (query == null || query.isEmpty())
            return filteredList;

        String q = query.toLowerCase();

        return filteredList.stream()
                .filter(t -> {
                    Book b = findBook(t.getBookId());
                    String userName = getUserNameById(t.getUserId()).toLowerCase();

                    return userName.contains(q) ||
                            (b != null && (b.getTitle().toLowerCase().contains(q) || b.getIsbn().toLowerCase().contains(q)));
                })
                .collect(Collectors.toList());
    }

    public List<Transaction> getMemberReturnHistory(String userId, String query) {
        List<Transaction> filteredList = transactions.stream()
                .filter(t -> t.getUserId().equals(userId))
                .filter(t -> t.getTransactionType().equals("Returned"))
                .collect(Collectors.toList());

        if (query == null || query.isEmpty()) {
            return filteredList;
        }

        String q = query.toLowerCase();

        return filteredList.stream()
                .filter(t -> {
                    Book b = findBook(t.getBookId());
                    String bookTitle = b != null ? b.getTitle().toLowerCase() : "";

                    boolean statusMatch = "iade edildi".contains(q);

                    return bookTitle.contains(q) || statusMatch;
                })
                .collect(Collectors.toList());
    }

    public double getTotalPenalty(ConcreteUser user) {
        if (user.getRole() == Role.ADMIN) return 0.0;

        if (db instanceof MySQLDatabase) {
            return ((MySQLDatabase) db).getUserPenalties(user.getId())
                    .stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();
        }
        return 0.0;
    }
}