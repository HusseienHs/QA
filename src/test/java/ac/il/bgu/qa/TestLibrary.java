package ac.il.bgu.qa;

import ac.il.bgu.qa.errors.NotificationException;
import ac.il.bgu.qa.services.DatabaseService;
import ac.il.bgu.qa.services.NotificationService;
import ac.il.bgu.qa.services.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestLibrary {

    private Book book;
    private Book book_null_title;
    private Book book_null_ISBN;
    private Book book_null_author;
    private Book book_borrowed;
    private User user;
    private NotificationService mockNotificationService;
    DatabaseService databaseService = mock(DatabaseService.class);
    ReviewService mockReviewService = mock(ReviewService.class);
    Library library = new Library(databaseService, mockReviewService);

    @BeforeEach
    public void setUp() {
        book = new Book("9780306406157", "Test Book", "Test Author");
        book_null_title = new Book("9780132350884", "", "Test Author");
        book_null_ISBN = new Book("", "Test Book", "Test Author");
        book_null_author = new Book("9780590353427", "Test Book", "");
        book_borrowed = new Book("9783161484100", "Test Book", "Test Author");
        book_borrowed.borrow();

        // Set up User with mocked NotificationService
        mockNotificationService = mock(NotificationService.class);
        user = new User("TestUser", "12345", mockNotificationService);
    }

    // Book Tests

    @Test
    public void GivenValidBook_WhenGetISBN_ThenReturnsCorrectISBN() {
        assertEquals("9780306406157", book.getISBN(), "ISBN should match the initialized value.");
    }

    @Test
    public void GivenValidBook_WhenGetTitle_ThenReturnsCorrectTitle() {
        assertEquals("Test Book", book.getTitle(), "Title should match the initialized value.");
    }

    @Test
    public void GivenValidBook_WhenGetAuthor_ThenReturnsCorrectAuthor() {
        assertEquals("Test Author", book.getAuthor(), "Author should match the initialized value.");
    }

    @Test
    public void GivenNewBook_WhenCheckIsBorrowed_ThenReturnsFalse() {
        assertFalse(book.isBorrowed(), "New book should not be borrowed initially.");
    }

    @Test
    public void GivenAvailableBook_WhenBorrow_ThenBookIsMarkedAsBorrowed() {
        book.borrow();
        assertTrue(book.isBorrowed(), "Book should be marked as borrowed after calling borrow().");
    }

    @Test
    public void GivenAlreadyBorrowedBook_WhenBorrow_ThenThrowsIllegalStateException() {
        book.borrow();
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> book.borrow(),
                "Borrowing an already borrowed book should throw an exception.");
        assertEquals("Book is already borrowed!", thrown.getMessage());
    }

    @Test
    public void GivenBorrowedBook_WhenReturnBook_ThenBookIsMarkedAsNotBorrowed() {
        book.borrow();
        book.returnBook();
        assertFalse(book.isBorrowed(), "Book should be marked as not borrowed after calling returnBook().");
    }

    @Test
    public void GivenNotBorrowedBook_WhenReturnBook_ThenThrowsIllegalStateException() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> book.returnBook(),
                "Returning a book that wasn't borrowed should throw an exception.");
        assertEquals("ac.il.bgu.qa.Book wasn't borrowed!", thrown.getMessage());
    }

    // User Tests

    @Test
    public void GivenUser_WhenGetName_ThenReturnsUserName() {
        assertEquals("TestUser", user.getName(), "getName() should return 'TestUser'.");
    }

    @Test
    public void GivenUser_WhenGetId_ThenReturnsUserId() {
        assertEquals("12345", user.getId(), "getId() should return '12345'.");
    }

    @Test
    public void GivenUser_WhenGetNotificationService_ThenReturnsCorrectService() {
        assertEquals(mockNotificationService, user.getNotificationService(),
                "getNotificationService() should return the correct NotificationService instance.");
    }

    @Test
    public void GivenValidMessage_WhenSendNotification_ThenNotificationServiceIsCalledOnce() throws NotificationException {
        String message = "Hello, TestUser!";
        user.sendNotification(message);

        verify(mockNotificationService, times(1)).notifyUser("12345", message);
    }

    @Test
    public void GivenNotificationServiceFailure_WhenSendNotification_ThenThrowsNotificationException() throws NotificationException {
        String message = "This will fail";
        doThrow(new NotificationException("Error sending notification"))
                .when(mockNotificationService).notifyUser("12345", message);

        NotificationException thrown = assertThrows(NotificationException.class, () -> user.sendNotification(message),
                "sendNotification should throw NotificationException if notifyUser fails.");
        assertEquals("Error sending notification", thrown.getMessage());
    }

    // Library Tests

    @Test
    public void GivenExistingBook_WhenAddBook_ThenThrowsIllegalArgumentException() {
        when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Book already exists.");
        assertEquals("Book already exists.", thrown.getMessage());

        verify(databaseService, never()).addBook(anyString(), any());
    }

    @Test
    public void GivenNewBook_WhenAddBook_ThenBookIsAddedToDatabase() {
        when(databaseService.getBookByISBN(book.getISBN())).thenReturn(null);

        library.addBook(book);

        verify(databaseService, times(1)).addBook(book.getISBN(), book);
    }

    @Test
    public void GivenInvalidBook_WhenAddBook_ThenThrowsIllegalArgumentException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.addBook(null), "Invalid book.");
        assertEquals("Invalid book.", thrown.getMessage());
    }

    @Test
    public void GivenBookWithInvalidAuthorName_WhenAddBook_ThenThrowsIllegalArgumentException() {
        // Test author name special character checks using various cases
        Book invalidAuthor = new Book("123", "Test Book", "-Author");
        assertThrows(IllegalArgumentException.class, () -> library.addBook(invalidAuthor));
    }
}
