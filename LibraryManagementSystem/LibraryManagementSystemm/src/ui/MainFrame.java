package ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import logic.*;
import models.*;

public class MainFrame extends JFrame implements Observer {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private LibraryFacade facade;
    private ConcreteUser currentUser;

    private JPanel loginPanel;
    private JTabbedPane adminTabs;
    private JTabbedPane memberTabs;

    private DefaultTableModel adminBookModel;
    private DefaultTableModel userModel;
    private DefaultTableModel memberAllBooksModel;
    private DefaultTableModel memberMyBooksModel;
    private DefaultTableModel adminLoanTrackingModel;

    private DefaultTableModel adminReturnTrackingModel;
    private JTextField txtSearchLoan = new JTextField(15);
    private JTextField txtSearchBook = new JTextField(15);
    private JTextField txtSearchUser = new JTextField(15);
    private JTextField txtSearchMyBooks = new JTextField(15);
    private JTextField txtSearchReturns = new JTextField(15);

    public MainFrame() {
        this.facade = new LibraryFacade();
        this.facade.eventManager.subscribe(this);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        initLoginPanel();
        mainPanel.add(loginPanel, "LOGIN");

        add(mainPanel);
    }

    @Override
    public void update(String eventType) {
        if (currentUser != null) {
            if (currentUser.getRole() == Role.ADMIN) {
                if (eventType.equals("BOOKS_UPDATED") || eventType.equals("USERS_UPDATED")) {
                    refreshAdminTables();
                }

                if (adminTabs != null && adminTabs.getSelectedIndex() == 4) {
                    adminTabs.setComponentAt(4, createProfilePanel());
                }

            } else {
                if (eventType.equals("BOOKS_UPDATED")) {
                    refreshMemberTables();
                }

                if (memberTabs != null) {
                    int selectedIndex = memberTabs.getSelectedIndex();
                    if (selectedIndex != -1 && memberTabs.getTitleAt(selectedIndex).equals("Profilim")) {
                        memberTabs.setComponentAt(2, createProfilePanel());
                    }
                }
            }
        }
    }

    private void initLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(new Color(240, 248, 255));

        JPanel box = new JPanel(new GridLayout(5, 1, 15, 25));
        box.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        box.setBackground(Color.WHITE);
        box.setPreferredSize(new Dimension(450, 350));

        JLabel title = new JLabel(" Kütüphane Yönetim Sistemi", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(41, 128, 185));

        JTextField txtUser = new JTextField("");
        txtUser.setBorder(BorderFactory.createTitledBorder("E-Posta"));
        txtUser.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        JPasswordField txtPass = new JPasswordField("");
        txtPass.setBorder(BorderFactory.createTitledBorder("Şifre"));
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        JButton btnLogin = new JButton("GİRİŞ YAP");
        btnLogin.setBackground(new Color(52, 152, 219));
        btnLogin.setForeground(new Color(41, 128, 185));
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnLogin.setFocusPainted(false);

        btnLogin.addActionListener(e -> {
            String enteredEmail = txtUser.getText();
            String enteredPass = new String(txtPass.getPassword());

            ConcreteUser user = facade.login(enteredEmail, enteredPass);

            if (user != null) {
                this.currentUser = user;

                if (enteredPass.equals("123")) {
                    JOptionPane.showMessageDialog(this,
                            "GÜVENLİK UYARISI: Varsayılan şifrenizi kullanıyorsunuz. Lütfen devam etmeden önce şifrenizi değiştirin.",
                            "Şifre Değiştirme Zorunluluğu",
                            JOptionPane.WARNING_MESSAGE);

                    if (user.getRole() == Role.ADMIN) {
                        initAdminPanel();
                        cardLayout.show(mainPanel, "ADMIN");
                        adminTabs.setSelectedIndex(4);
                    } else {
                        initMemberPanel();
                        cardLayout.show(mainPanel, "MEMBER");
                        memberTabs.setSelectedIndex(2);
                    }
                }
                else if (user.getRole() == Role.ADMIN) {
                    initAdminPanel();
                    cardLayout.show(mainPanel, "ADMIN");
                } else {
                    initMemberPanel();
                    cardLayout.show(mainPanel, "MEMBER");
                }
                txtUser.setText("");
                txtPass.setText("");

            } else {
                JOptionPane.showMessageDialog(this, "Hatalı kullanıcı!");
            }
        });

        box.add(title);
        box.add(txtUser);
        box.add(txtPass);
        box.add(new JLabel(""));
        box.add(btnLogin);
        loginPanel.add(box);
    }

    private void initAdminPanel() {
        if (adminTabs != null && mainPanel.isAncestorOf(adminTabs)) {
            refreshAdminTables();
            return;
        }

        adminTabs = new JTabbedPane();
        adminTabs.setFont(new Font("Segoe UI", Font.BOLD, 14));


        JPanel bookPanel = new JPanel(new BorderLayout());
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        JButton btnLogout = new JButton("🚪 Çıkış");
        btnLogout.setBackground(new Color(231, 76, 60));
        btnLogout.setForeground(Color.BLACK);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            facade.logout();
            cardLayout.show(mainPanel, "LOGIN");
        });

        JButton btnSearch = new JButton("🔍 Ara");
        btnSearch.setBackground(new Color(52, 152, 219));
        btnSearch.setForeground(Color.BLACK);
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> refreshAdminTables());

        topBar.add(new JLabel("Kitap/Yazar/Kategori/ISBN göre Ara:"));
        topBar.add(txtSearchBook);
        topBar.add(btnSearch);
        topBar.add(btnLogout);

        String[] bCols = { "ID", "Başlık", "Kategori", "Yazar", "ISBN", "Stok", "Son Alan" };
        adminBookModel = new DefaultTableModel(bCols, 0);
        JTable bookTable = new JTable(adminBookModel);

        JPanel bookForm = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        bookForm.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(41, 128, 185)), "Kitap Kaydı/Güncelleme"
        ));

        JTextField tTitle = new JTextField(8);
        JTextField tAuthor = new JTextField(8);
        JTextField tIsbn = new JTextField(8);
        JTextField tCat = new JTextField(8);
        JTextField tStock = new JTextField(3);

        tTitle.setPreferredSize(new Dimension(100, 30));
        tAuthor.setPreferredSize(new Dimension(100, 30));
        tIsbn.setPreferredSize(new Dimension(100, 30));
        tCat.setPreferredSize(new Dimension(100, 30));
        tStock.setPreferredSize(new Dimension(50, 30));

        JButton btnAddBook = new JButton("➕ Ekle");
        JButton btnUpdateBook = new JButton("🔄 Seçileni Güncelle");
        JButton btnDeleteBook = new JButton("🗑️ Sil");

        btnAddBook.setBackground(new Color(46, 204, 113));
        btnAddBook.setForeground(Color.BLACK);
        btnAddBook.setFocusPainted(false);

        btnUpdateBook.setBackground(new Color(243, 156, 18));
        btnUpdateBook.setForeground(Color.BLACK);
        btnUpdateBook.setFocusPainted(false);

        btnDeleteBook.setBackground(new Color(192, 57, 43));
        btnDeleteBook.setForeground(Color.BLACK);
        btnDeleteBook.setFocusPainted(false);


        btnAddBook.addActionListener(e -> {
            try {
                int stock = Integer.parseInt(tStock.getText());
                String title = tTitle.getText();
                String author = tAuthor.getText();
                String isbn = tIsbn.getText();
                String category = tCat.getText();

                if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || category.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Tüm alanlar boş bırakılamaz.");
                    return;
                }

                facade.addBook(title, author, isbn, category, stock);

                tTitle.setText("");
                tAuthor.setText("");
                tIsbn.setText("");
                tCat.setText("");
                tStock.setText("");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Stok sayı olmalı");
            }
        });

        btnUpdateBook.addActionListener(e -> {
            int row = bookTable.getSelectedRow();
            if (row != -1) {
                try {
                    String id = (String) adminBookModel.getValueAt(row, 0);
                    String title = tTitle.getText();
                    String cat = tCat.getText();
                    int stock = Integer.parseInt(tStock.getText());

                    facade.updateBook(id, title, cat, stock);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Stok sayı olmalı");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Güncellemek için bir kitap seçin.");
            }
        });

        btnDeleteBook.addActionListener(e -> {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow != -1) {
                String bookId = (String) adminBookModel.getValueAt(selectedRow, 0);

                int confirm = JOptionPane.showConfirmDialog(this,
                        "Seçili kitabı silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.",
                        "Kitap Silme Onayı", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    facade.deleteBook(bookId);
                    JOptionPane.showMessageDialog(this, "Kitap başarıyla silindi.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Lütfen silmek için bir kitap seçin.");
            }
        });

        bookForm.add(new JLabel("Başlık:"));
        bookForm.add(tTitle);
        bookForm.add(new JLabel("Yazar:"));
        bookForm.add(tAuthor);
        bookForm.add(new JLabel("ISBN:"));
        bookForm.add(tIsbn);
        bookForm.add(new JLabel("Kat:"));
        bookForm.add(tCat);
        bookForm.add(new JLabel("Stok:"));
        bookForm.add(tStock);

        JPanel bookActionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        bookActionPanel.add(btnAddBook);
        bookActionPanel.add(btnUpdateBook);
        bookActionPanel.add(btnDeleteBook);

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));

        bookForm.setAlignmentX(Component.CENTER_ALIGNMENT);
        southPanel.add(bookForm);
        southPanel.add(Box.createVerticalStrut(10));

        bookActionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        southPanel.add(bookActionPanel);
        southPanel.add(Box.createVerticalStrut(10));

        bookPanel.add(topBar, BorderLayout.NORTH);
        bookPanel.add(new JScrollPane(bookTable), BorderLayout.CENTER);
        bookPanel.add(southPanel, BorderLayout.SOUTH);


        JPanel userPanel = new JPanel(new BorderLayout());
        JPanel userTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnSearchUser = new JButton("👤 Üye Ara");
        btnSearchUser.setBackground(new Color(52, 152, 219));
        btnSearchUser.setForeground(Color.BLACK);
        btnSearchUser.setFocusPainted(false);
        btnSearchUser.addActionListener(e -> refreshAdminTables());
        userTop.add(new JLabel("Üye Adı/ID:"));
        userTop.add(txtSearchUser);
        userTop.add(btnSearchUser);

        String[] uCols = { "ID", "Ad Soyad", "E-Posta", "Rol", "Ceza Durumu" };
        userModel = new DefaultTableModel(uCols, 0);
        JTable userTable = new JTable(userModel);

        JPanel userForm = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        userForm.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(41, 128, 185)), "Yeni Kullanıcı Ekleme"
        ));

        JTextField tUName = new JTextField(10);
        JTextField tUEmail = new JTextField(10);
        JPasswordField tUPass = new JPasswordField(10);
        JComboBox<Role> cmbRole = new JComboBox<>(Role.values());
        cmbRole.setSelectedItem(Role.MEMBER);

        JButton btnAddUser = new JButton("➕ Kullanıcı Ekle");
        JButton btnDelUser = new JButton("🗑️ Kullanıcı Sil");

        btnAddUser.setBackground(new Color(46, 204, 113));
        btnAddUser.setForeground(Color.BLACK);
        btnAddUser.setFocusPainted(false);

        btnDelUser.setBackground(new Color(192, 57, 43));
        btnDelUser.setForeground(Color.BLACK);
        btnDelUser.setFocusPainted(false);


        btnAddUser.addActionListener(e -> {
            String password = new String(tUPass.getPassword());
            String name = tUName.getText();
            String email = tUEmail.getText();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tüm alanlar boş bırakılamaz!");
                return;
            }
            facade.addUser(name, email, password, (Role) cmbRole.getSelectedItem());
            tUName.setText("");
            tUEmail.setText("");
            tUPass.setText("");
        });

        btnDelUser.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row != -1) {
                String userIdToDelete = (String) userModel.getValueAt(row, 0);

                if (currentUser.getId().equals(userIdToDelete)) {
                    JOptionPane.showMessageDialog(this, "Kendi hesabınızı silemezsiniz!", "Hata", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(this,
                        "Seçili üyeyi silmek istediğinizden emin misiniz?",
                        "Üye Silme Onayı", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    facade.deleteUser(userIdToDelete);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Silmek için bir kullanıcı seçin.");
            }
        });

        userForm.add(new JLabel("Ad:"));
        userForm.add(tUName);
        userForm.add(new JLabel("Email:"));
        userForm.add(tUEmail);
        userForm.add(new JLabel("Şifre:"));
        userForm.add(tUPass);
        userForm.add(new JLabel("Rol:"));
        userForm.add(cmbRole);
        userForm.add(btnAddUser);
        userForm.add(btnDelUser);

        userPanel.add(userTop, BorderLayout.NORTH);
        userPanel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        userPanel.add(userForm, BorderLayout.SOUTH);


        JPanel trackingPanel = new JPanel(new BorderLayout());
        String[] tCols = { "Kitap Adı", "Ödünç Alan", "Son Teslim Tarihi", "Gecikme Cezası" };
        adminLoanTrackingModel = new DefaultTableModel(tCols, 0);
        JTable trackingTable = new JTable(adminLoanTrackingModel);

        JPanel trackingTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnSearchLoan = new JButton("Üyeye Göre Filtrele");
        btnSearchLoan.setBackground(new Color(52, 152, 219));
        btnSearchLoan.setForeground(Color.BLACK);
        btnSearchLoan.setFocusPainted(false);
        btnSearchLoan.addActionListener(e -> refreshAdminTables());

        trackingTop.add(new JLabel("Üye Adı/ID/E-posta:"));
        trackingTop.add(txtSearchLoan);
        trackingTop.add(btnSearchLoan);

        trackingPanel.add(trackingTop, BorderLayout.NORTH);
        trackingPanel.add(new JScrollPane(trackingTable), BorderLayout.CENTER);


        JPanel returnTrackingPanel = new JPanel(new BorderLayout());

        String[] rCols = { "İşlem ID", "Kitap Adı", "Üye Adı", "Son Teslim Tarihi", "İade Tarihi", "Ceza Tutarı" };
        adminReturnTrackingModel = new DefaultTableModel(rCols, 0);
        JTable returnTable = new JTable(adminReturnTrackingModel);

        JPanel returnTrackingTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnSearchReturns = new JButton("🔍 Ara/Filtrele");
        btnSearchReturns.setBackground(new Color(52, 152, 219));
        btnSearchReturns.setForeground(Color.BLACK);
        btnSearchReturns.setFocusPainted(false);
        btnSearchReturns.addActionListener(e -> refreshAdminTables());

        returnTrackingTop.add(new JLabel("Kitap Adı/Üye Adı/ISBN göre Ara:"));
        returnTrackingTop.add(txtSearchReturns);
        returnTrackingTop.add(btnSearchReturns);

        returnTrackingPanel.add(returnTrackingTop, BorderLayout.NORTH);
        returnTrackingPanel.add(new JScrollPane(returnTable), BorderLayout.CENTER);



        adminTabs.addTab(" Kitap İşlemleri", bookPanel);
        adminTabs.addTab(" Üye İşlemleri", userPanel);
        adminTabs.addTab(" Ödünç Kitap Takibi", trackingPanel);
        adminTabs.addTab(" İade Kitap Takibi", returnTrackingPanel);
        adminTabs.addTab(" Profilim", createProfilePanel());

        mainPanel.add(adminTabs, "ADMIN");
        refreshAdminTables();
    }

    private void initMemberPanel() {
        if (memberTabs != null && mainPanel.isAncestorOf(memberTabs)) {
            refreshMemberTables();
            return;
        }

        memberTabs = new JTabbedPane();
        memberTabs.setFont(new Font("Segoe UI", Font.BOLD, 14));


        JPanel allBooksPanel = new JPanel(new BorderLayout());
        String[] cols = { "ID", "Kitap", "Kategori", "Yazar", "Stok", "Durum" };
        memberAllBooksModel = new DefaultTableModel(cols, 0);
        JTable allTable = new JTable(memberAllBooksModel);

        JPanel topMem = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topMem.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        JButton btnMemberSearch = new JButton("🔍 Ara");
        btnMemberSearch.setBackground(new Color(52, 152, 219));
        btnMemberSearch.setForeground(Color.BLACK);
        btnMemberSearch.setFocusPainted(false);
        btnMemberSearch.addActionListener(e -> refreshMemberTables());

        JButton btnOut = new JButton("🚪 Çıkış");
        btnOut.setBackground(new Color(231, 76, 60));
        btnOut.setForeground(Color.BLACK);
        btnOut.setFocusPainted(false);
        btnOut.addActionListener(e -> {
            facade.logout();
            cardLayout.show(mainPanel, "LOGIN");
        });

        topMem.add(new JLabel("Kitap/Yazar/Kategori/ISBN göre Ara:"));
        topMem.add(txtSearchBook);
        topMem.add(btnMemberSearch);
        topMem.add(Box.createHorizontalGlue());
        topMem.add(btnOut);

        JButton btnBorrow = new JButton(" Ödünç Al");
        btnBorrow.setBackground(new Color(39, 174, 96));
        btnBorrow.setForeground(Color.BLACK);
        btnBorrow.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBorrow.setFocusPainted(false);
        btnBorrow.addActionListener(e -> {
            int row = allTable.getSelectedRow();
            if (row != -1) {
                String bid = (String) memberAllBooksModel.getValueAt(row, 0);
                Book b = facade.findBook(bid);
                if (b != null && b.getStock() <= 0) {
                    JOptionPane.showMessageDialog(this, "Stokta yok, bu kitabı alamazsınız!", "Hata",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                new BorrowCommand(facade, bid, currentUser.getId()).execute();
                JOptionPane.showMessageDialog(this, "Kitap başarıyla ödünç alındı.");

                refreshMemberTables();

            } else {
                JOptionPane.showMessageDialog(this, "Ödünç almak için bir kitap seçin.");
            }
        });

        allBooksPanel.add(topMem, BorderLayout.NORTH);
        allBooksPanel.add(new JScrollPane(allTable), BorderLayout.CENTER);
        allBooksPanel.add(btnBorrow, BorderLayout.SOUTH);


        JPanel myBooksPanel = new JPanel(new BorderLayout());
        String[] myCols = { "ID", "Kitap", "Durum", "Son Teslim/İade Tarihi", "Gecikme Cezası" };
        memberMyBooksModel = new DefaultTableModel(myCols, 0);
        JTable myTable = new JTable(memberMyBooksModel);
        myTable.setForeground(new Color(0, 71, 171));

        JPanel myBooksTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnMyBooksSearch = new JButton("🔍 Ara");
        btnMyBooksSearch.setBackground(new Color(52, 152, 219));
        btnMyBooksSearch.setForeground(Color.BLACK);
        btnMyBooksSearch.setFocusPainted(false);
        btnMyBooksSearch.addActionListener(e -> refreshMemberTables());

        myBooksTop.add(new JLabel("Kitap Adı/Durum göre Ara:"));
        myBooksTop.add(txtSearchMyBooks);
        myBooksTop.add(btnMyBooksSearch);

        JButton btnReturn = new JButton(" İade Et");
        btnReturn.setBackground(new Color(142, 68, 173));
        btnReturn.setForeground(Color.BLACK);
        btnReturn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnReturn.setFocusPainted(false);
        btnReturn.addActionListener(e -> {
            int row = myTable.getSelectedRow();
            if (row != -1) {
                String bid = (String) memberMyBooksModel.getValueAt(row, 0);
                String status = (String) memberMyBooksModel.getValueAt(row, 2);

                if (status.equals("İADE EDİLDİ")) {
                    JOptionPane.showMessageDialog(this, "Seçilen kayıt zaten iade edilmiş bir işlemdir.", "Hata", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                new ReturnCommand(facade, bid).execute();
                JOptionPane.showMessageDialog(this, "Kitap iade edildi.");

                refreshMemberTables();

            } else {
                JOptionPane.showMessageDialog(this, "İade etmek için bir kitap seçin.");
            }
        });

        myBooksPanel.add(myBooksTop, BorderLayout.NORTH);
        myBooksPanel.add(new JScrollPane(myTable), BorderLayout.CENTER);
        myBooksPanel.add(btnReturn, BorderLayout.SOUTH);


        memberTabs.addTab(" Kütüphane", allBooksPanel);
        memberTabs.addTab(" Kitaplarım & Cezalar", myBooksPanel);
        memberTabs.addTab(" Profilim", createProfilePanel());

        mainPanel.add(memberTabs, "MEMBER");
        refreshMemberTables();
    }

    private JPanel createProfilePanel() {
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel infoBox = new JPanel(new GridLayout(5, 2, 10, 10));
        infoBox.setMaximumSize(new Dimension(450, 250));
        infoBox.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(41, 128, 185), 2, true),
                " Kullanıcı Bilgileri",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(41, 128, 185)
        ));

        infoBox.add(new JLabel("Ad Soyad:"));
        infoBox.add(new JLabel(currentUser.getName()));

        infoBox.add(new JLabel("E-Posta:"));
        infoBox.add(new JLabel(currentUser.getEmail()));

        infoBox.add(new JLabel("Rol:"));
        infoBox.add(new JLabel(currentUser.getRole().toString()));

        infoBox.add(new JLabel("Toplam Ceza:"));
        double totalPenalty = facade.getTotalPenalty(currentUser);
        JLabel lblPenalty = new JLabel(currentUser.getRole() == Role.ADMIN ? "Muaf" : totalPenalty + " TL");
        lblPenalty.setForeground(Color.RED);
        lblPenalty.setFont(new Font("Arial", Font.BOLD, 14));
        infoBox.add(lblPenalty);

        JPanel passUpdateBox = new JPanel(new GridLayout(1, 2, 10, 10));
        passUpdateBox.setMaximumSize(new Dimension(450, 70));
        passUpdateBox.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true),
                " Şifre Güncelleme",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12)
        ));

        passUpdateBox.add(new JLabel("Yeni Şifre:"));
        JPasswordField txtNewPass = new JPasswordField();
        passUpdateBox.add(txtNewPass);


        JButton btnUpdate = new JButton("Şifreyi Güncelle");
        btnUpdate.setBackground(new Color(155, 89, 182));
        btnUpdate.setForeground(Color.BLACK);
        btnUpdate.setFocusPainted(false);
        btnUpdate.addActionListener(e -> {
            String newPass = new String(txtNewPass.getPassword());
            if (newPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Şifre boş bırakılamaz.");
                return;
            }
            if (newPass.length() < 3) {
                JOptionPane.showMessageDialog(this, "Şifre en az 3 karakter olmalıdır.");
                return;
            }

            facade.updateUser(currentUser.getId(), newPass);
            JOptionPane.showMessageDialog(this, "Şifreniz başarıyla güncellendi.");
            txtNewPass.setText("");

            update("USERS_UPDATED");
        });

        profilePanel.add(infoBox);
        profilePanel.add(Box.createVerticalStrut(20));
        profilePanel.add(passUpdateBox);
        profilePanel.add(Box.createVerticalStrut(10));
        profilePanel.add(btnUpdate);

        return profilePanel;
    }

    private void refreshAdminTables() {
        adminBookModel.setRowCount(0);

        List<Book> allBooks = facade.searchBooks(txtSearchBook.getText());

        for (Book b : allBooks) {
            String borrower = b.getLoanedToUserId() != null ? facade.getUserNameById(b.getLoanedToUserId()) : "-";

            adminBookModel.addRow(new Object[] {
                    b.getId(),
                    b.getTitle(),
                    b.getCategory(),
                    b.getAuthor(),
                    b.getIsbn(),
                    b.getStock(),
                    borrower
            });
        }

        userModel.setRowCount(0);
        List<ConcreteUser> filteredUsers = facade.searchUsers(txtSearchUser.getText());
        for (ConcreteUser u : filteredUsers) {
            String cezasi = u.getRole() == Role.ADMIN ? "Muaf" : (facade.getTotalPenalty(u) + " TL");
            userModel.addRow(new Object[] {
                    u.getId(), u.getName(), u.getEmail(), u.getRole(), cezasi
            });
        }

        if (adminLoanTrackingModel != null) {
            adminLoanTrackingModel.setRowCount(0);

            String loanQuery = txtSearchLoan.getText().trim();
            List<Book> trackingBooks = facade.getAllBooks();

            String targetUserId = null;

            if (!loanQuery.isEmpty()) {
                ConcreteUser targetUser = facade.findUser(loanQuery);

                if (targetUser != null) {
                    targetUserId = targetUser.getId();
                }
            }

            for (Book b : trackingBooks) {
                if (b.getLoanedToUserId() != null) {

                    if (targetUserId != null && !targetUserId.equals(b.getLoanedToUserId())) {
                        continue;
                    }

                    String borrowerName = facade.getUserNameById(b.getLoanedToUserId());
                    LocalDate dueDate = b.getDueDate();
                    double penalty = facade.calculatePenalty(dueDate);

                    String penaltyText = (penalty > 0) ? String.format("%.2f TL", penalty) : "Yok";

                    adminLoanTrackingModel.addRow(new Object[] {
                            b.getTitle(),
                            borrowerName,
                            dueDate != null ? dueDate.toString() : "-",
                            penaltyText
                    });
                }
            }
        }

        if (adminReturnTrackingModel != null) {
            adminReturnTrackingModel.setRowCount(0);

            List<Transaction> returnedTransactions = facade.searchReturns(txtSearchReturns.getText());

            for (Transaction t : returnedTransactions) {
                Book book = facade.findBook(t.getBookId());
                String bookName = book != null ? book.getTitle() : "Bilinmeyen Kitap (" + t.getBookId() + ")";
                String userName = facade.getUserNameById(t.getUserId());

                String penaltyText = (t.getPenalty() > 0) ? String.format("%.2f TL", t.getPenalty()) : "Yok";

                adminReturnTrackingModel.addRow(new Object[] {
                        t.getId(),
                        bookName,
                        userName,
                        t.getDueDate() != null ? t.getDueDate().toString() : "-",
                        t.getTransactionDate() != null ? t.getTransactionDate().toString() : "-",
                        penaltyText
                });
            }
        }
    }

    private void refreshMemberTables() {
        memberAllBooksModel.setRowCount(0);
        memberMyBooksModel.setRowCount(0);

        String libraryQuery = txtSearchBook.getText();

        String myBooksQuery = txtSearchMyBooks.getText().toLowerCase();



        List<Book> allBooks = facade.searchBooks(libraryQuery);

        for (Book b : allBooks) {
            memberAllBooksModel.addRow(new Object[] {
                    b.getId(), b.getTitle(), b.getCategory(), b.getAuthor(), b.getStock(), b.getStatus()
            });


            if (currentUser != null && b.getLoanedToUserId() != null && b.getLoanedToUserId().equals(currentUser.getId())) {

                String status = "ÖDÜNÇTE (Aktif)";
                String remaining = "-";
                double penalty = 0.0;

                if (b.getDueDate() != null) {
                    penalty = facade.calculatePenalty(b.getDueDate());
                    long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), b.getDueDate());

                    if (days < 0) {
                        remaining = b.getDueDate().toString() + " (GECİKTİ: " + Math.abs(days) + " Gün)";
                        status = "GECİKTİ";
                    } else {
                        remaining = b.getDueDate().toString();
                    }
                }

                boolean matchesActive = myBooksQuery.isEmpty() ||
                        b.getTitle().toLowerCase().contains(myBooksQuery) ||
                        status.toLowerCase().contains(myBooksQuery) ||
                        remaining.toLowerCase().contains(myBooksQuery);

                if (matchesActive) {
                    String pText = (penalty > 0) ? String.format("%.2f TL", penalty) : "Yok";
                    if (currentUser.getRole() == Role.ADMIN)
                        pText = "Muaf";

                    memberMyBooksModel.addRow(new Object[] {
                            b.getId(),
                            b.getTitle(),
                            status,
                            remaining,
                            pText
                    });
                }
            }
        }


        List<Transaction> userReturnHistory = facade.getMemberReturnHistory(currentUser.getId(), myBooksQuery);

        for(Transaction t : userReturnHistory) {
            Book historicalBook = facade.findBook(t.getBookId());
            String bookTitle = historicalBook != null ? historicalBook.getTitle() : "Bilinmeyen Kitap";

            String pText = (t.getPenalty() > 0) ? String.format("%.2f TL", t.getPenalty()) : "Yok";

            boolean matchesReturned = myBooksQuery.isEmpty() ||
                    bookTitle.toLowerCase().contains(myBooksQuery) ||
                    "iade edildi".contains(myBooksQuery);

            if (matchesReturned) {
                memberMyBooksModel.addRow(new Object[] {
                        t.getBookId(),
                        bookTitle,
                        "İADE EDİLDİ",
                        t.getTransactionDate().toString(),
                        pText
                });
            }
        }
    }
}