package logic;

public class BorrowCommand implements Command {
    private LibraryFacade facade;
    private String bookId;
    private String userId;

    public BorrowCommand(LibraryFacade facade, String bookId, String userId) {
        this.facade = facade;
        this.bookId = bookId;
        this.userId = userId;
    }

    @Override
    public void execute() {
        facade.borrowBookInternal(bookId, userId);
    }
}