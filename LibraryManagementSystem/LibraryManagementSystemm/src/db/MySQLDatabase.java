package db;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import models.*;

public class MySQLDatabase extends AbstractDatabase {

    private Connection conn;

    public MySQLDatabase() {
        connect();
    }

    @Override
    public void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/librarydb",
                    "root",
                    "12345"
            );
            System.out.println("MySQL bağlantısı başarılı");
        } catch (Exception e) {
            System.err.println("MySQL bağlantı hatası!");
            e.printStackTrace();
        }
    }

    @Override
    public List<Book> loadBooks() {
        List<Book> books = new ArrayList<>();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM books");
            while (rs.next()) {
                Book b = new Book(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getString("category"),
                        rs.getInt("stock")
                );
                b.setLoanedToUserId(rs.getString("loaned_user_id"));

                Date dueDateSql = rs.getDate("due_date");
                b.setDueDate(dueDateSql != null ? dueDateSql.toLocalDate() : null);

                if (b.getStock() <= 0 && b.getLoanedToUserId() == null) {
                    b.setStatus(BookStatus.OUT_OF_STOCK);
                }
                else if (b.getLoanedToUserId() != null) {
                    b.setStatus(BookStatus.LOANED);
                }
                else {
                    b.setStatus(BookStatus.AVAILABLE);
                }

                books.add(b);
            }
            rs.close();
            st.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return books;
    }

    @Override
    public void saveBook(Book b) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO books (id, title, author, isbn, category, stock, loaned_user_id, due_date) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE title=?, author=?, isbn=?, category=?, stock=?, loaned_user_id=?, due_date=?"
            );

            ps.setString(1, b.getId());
            ps.setString(2, b.getTitle());
            ps.setString(3, b.getAuthor());
            ps.setString(4, b.getIsbn());
            ps.setString(5, b.getCategory());
            ps.setInt(6, b.getStock());
            ps.setString(7, b.getLoanedToUserId());
            ps.setDate(8, b.getDueDate() != null ? Date.valueOf(b.getDueDate()) : null);

            ps.setString(9, b.getTitle());
            ps.setString(10, b.getAuthor());
            ps.setString(11, b.getIsbn());
            ps.setString(12, b.getCategory());
            ps.setInt(13, b.getStock());
            ps.setString(14, b.getLoanedToUserId());
            ps.setDate(15, b.getDueDate() != null ? Date.valueOf(b.getDueDate()) : null);

            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ConcreteUser> loadUsers() {
        List<ConcreteUser> users = new ArrayList<>();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM users");
            while (rs.next()) {
                ConcreteUser u = new ConcreteUser(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        Role.valueOf(rs.getString("role"))
                );
                users.add(u);
            }
            rs.close();
            st.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public void saveUser(ConcreteUser u) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (id, name, email, password, role) VALUES (?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE name=?, email=?, password=?, role=?"
            );

            ps.setString(1, u.getId());
            ps.setString(2, u.getName());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPassword());
            ps.setString(5, u.getRole().name());

            ps.setString(6, u.getName());
            ps.setString(7, u.getEmail());
            ps.setString(8, u.getPassword());
            ps.setString(9, u.getRole().name());

            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteUser(String id) {
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id=?");
            ps.setString(1, id);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteBook(String id) {
        if (conn == null) return;
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM books WHERE id=?");
            ps.setString(1, id);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Transaction> loadTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM transactions ORDER BY transaction_date DESC");

            while (rs.next()) {
                Date dateSql = rs.getDate("transaction_date");
                LocalDate transactionDate = dateSql != null ? dateSql.toLocalDate() : null;

                Date dueDateSql = rs.getDate("due_date");
                LocalDate dueDate = dueDateSql != null ? dueDateSql.toLocalDate() : null;

                Transaction t = new Transaction(
                        rs.getString("id"),
                        rs.getString("book_id"),
                        rs.getString("user_id"),
                        rs.getString("transaction_type"),
                        transactionDate,
                        dueDate,
                        rs.getDouble("penalty")
                );
                transactions.add(t);
            }
            rs.close();
            st.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transactions;
    }

    @Override
    public void saveTransaction(Transaction t) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO transactions (id, book_id, user_id, transaction_type, transaction_date, due_date, penalty) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)"
            );
            ps.setString(1, t.getId());
            ps.setString(2, t.getBookId());
            ps.setString(3, t.getUserId());
            ps.setString(4, t.getTransactionType());
            ps.setDate(5, t.getTransactionDate() != null ? Date.valueOf(t.getTransactionDate()) : null);
            ps.setDate(6, t.getDueDate() != null ? Date.valueOf(t.getDueDate()) : null);
            ps.setDouble(7, t.getPenalty());

            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void savePenalty(String id, String userId, String bookId, double amount) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO penalties (id, user_id, book_id, amount, created_at) VALUES (?, ?, ?, ?, ?)"
            );
            ps.setString(1, id);
            ps.setString(2, userId);
            ps.setString(3, bookId);
            ps.setDouble(4, amount);
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Double> getUserPenalties(String userId) {
        List<Double> penalties = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT amount FROM penalties WHERE user_id=?"
            );
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                penalties.add(rs.getDouble("amount"));
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return penalties;
    }
}