package logic;

public class ReturnCommand implements Command {
    private LibraryFacade facade;
    private String bookId;

    public ReturnCommand(LibraryFacade facade, String bookId) {
        this.facade = facade;
        this.bookId = bookId;
    }

    @Override
    public void execute() {
        facade.returnBook(bookId);
    }
}