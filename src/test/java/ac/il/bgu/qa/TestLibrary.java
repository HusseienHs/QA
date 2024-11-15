// Import statements
package ac.il.bgu.qa;

import ac.il.bgu.qa.errors.*;
import ac.il.bgu.qa.services.DatabaseService;
import ac.il.bgu.qa.services.NotificationService;
import ac.il.bgu.qa.services.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestLibrary {

    private Book book;
    private User user;
    private NotificationService mockNotificationService;
    private DatabaseService mockDatabaseService;
    private ReviewService mockReviewService;
    private Library library;

    @BeforeEach
    public void setUp() {
        book = new Book("9780306406157", "Test Book", "Test Author");

        // Mock services
        mockNotificationService = mock(NotificationService.class);
        mockDatabaseService = mock(DatabaseService.class);
        mockReviewService = mock(ReviewService.class);

        // Initialize Library
        library = new Library(mockDatabaseService, mockReviewService);

        // Set up a mock User
        user = new User("TestUser", "123456789012", mockNotificationService);
    }

    // Tests for Library Class


    // add book method tests
    @Test
    public void GivenNullBook_WhenaddBook_ThenIllegalArgumentException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.addBook(null)
                , "Invalid book.");
        assertEquals(thrown.getMessage(), "Invalid book.");
    }

    @Test
    public void GivenInvalidISBNBook_WhenaddBook_ThenIllegalArgumentException() {
        Book invalidbook = new Book(null, "Test Book", "Test Author");
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.addBook(invalidbook)
                , "Invalid ISBN.");
        assertEquals(thrown.getMessage(), "Invalid ISBN.");
    }

    @Test
    public void GivenInvalidTitleBook_WhenaddBook_ThenIllegalArgumentException() {
        Book invalidbook = new Book("9780306406157", null, "Test Author");
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.addBook(invalidbook)
                , "Invalid title.");
        assertEquals(thrown.getMessage(), "Invalid title.");
    }


    @Test
    public void GivenInvalidAuthorBook_WhenaddBook_ThenIllegalArgumentException() {
        Book invalidbook = new Book("9780306406157", "Test Book", null);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.addBook(invalidbook)
                , "Invalid author.");
        assertEquals(thrown.getMessage(), "Invalid author.");
    }

    @Test
    public void GivenBarrowedBook_WhenaddBook_ThenIllegalArgumentException() {
        book.borrow();
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.addBook(book)
                , "Book with invalid borrowed state.");
        assertEquals(thrown.getMessage(), "Book with invalid borrowed state.");

    }


    @Test
    public void GivenEmptyTitleBook_WhenaddBook_ThenIllegalArgumentException() {
        Book invalidbook = new Book("9780306406157", "", "Test Author");
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.addBook(invalidbook)
                , "Invalid title.");
        assertEquals(thrown.getMessage(), "Invalid title.");
    }

    //    TODO: TO be investigated later
    @Test
    public void GivenExistsBook_WhenaddBook_ThenIllegalArgumentException() {
        library.addBook(book);
        Book duplicate = new Book(book.getISBN(), "Test Book", "Test Author");
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.addBook(duplicate)
                , "Book already exists.");
        assertEquals(thrown.getMessage(), "Book already exists.");
    }

    // registerUser function
    @Test
    public void GivenInValidIDUser_WhenRegisterUser_ThenThrowsIllegalArgumentException() {
        User invaliduser = new User("invalidUser","a",mockNotificationService);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.registerUser(invaliduser)
                , "Invalid user Id.");

        assertEquals(thrown.getMessage(), "Invalid user Id.");
    }

    @Test
    public void GivenInValidNameUser_WhenRegisterUser_ThenThrowsIllegalArgumentException() {
        User invaliduser = new User("","123456789012",mockNotificationService);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.registerUser(invaliduser)
                , "Invalid user name.");

        assertEquals(thrown.getMessage(), "Invalid user name.");
    }

    @Test
    public void GivenValidUser_WhenRegisterUser_ThenUserIsRegisteredInDatabase() {
        when(mockDatabaseService.getUserById(user.getId())).thenReturn(null);

        library.registerUser(user);

        verify(mockDatabaseService, times(1)).registerUser(user.getId(), user);
    }

    @Test
    public void GivenExistingUser_WhenRegisterUser_ThenThrowsIllegalArgumentException() {
        when(mockDatabaseService.getUserById(user.getId())).thenReturn(user);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> library.registerUser(user),
                "User already exists.");
        assertEquals("User already exists.", thrown.getMessage());
    }




    @Test
    public void GivenInvalidUser_WhenRegisterUser_ThenThrowsIllegalArgumentException() {
        User invalidUser = null;

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> library.registerUser(invalidUser),
                "Invalid user.");
        assertEquals("Invalid user.", thrown.getMessage());
    }

    @Test
    public void GivenInvalidUserName_WhenRegisterUser_ThenThrowsIllegalArgumentException() {
        User invalidUser = new User(null, "123456789012", mockNotificationService);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> library.registerUser(invalidUser),
                "Invalid user name.");
        assertEquals("Invalid user name.", thrown.getMessage());
    }

    @Test
    public void GivenInvalidIDUser_WhenRegisterUser_ThenThrowsIllegalArgumentException() {
        User invalidUser = new User("invalidUser", null, mockNotificationService);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> library.registerUser(invalidUser),
                "Invalid user Id.");
        assertEquals("Invalid user Id.", thrown.getMessage());
    }

    @Test
    public void GivenInvalidUserService_WhenRegisterUser_ThenThrowsIllegalArgumentException() {
        User invalidUser = new User("invalidUser", "123456789012", null);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> library.registerUser(invalidUser),
                "Invalid notification service.");
        assertEquals("Invalid notification service.", thrown.getMessage());
    }


    // borrow book function

    @Test
    public void GivenNonExistingBookByISBN_WhenBorrowBook_ThenThrowsIllegalArgumentException() {
        String isbn = "9750306406167";
        BookNotFoundException thrown = assertThrows(BookNotFoundException.class,()-> library.borrowBook(isbn, user.getId()),
                "Book not found!");

        assertEquals(thrown.getMessage(), "Book not found!");
    }





    @Test
    public void GivenValidISBNAndUser_WhenBorrowBook_ThenBookIsMarkedAsBorrowed() {
        when(mockDatabaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        when(mockDatabaseService.getUserById(user.getId())).thenReturn(user);

        library.borrowBook(book.getISBN(), user.getId());

        assertTrue(book.isBorrowed(), "Book should be marked as borrowed.");
        verify(mockDatabaseService, times(1)).borrowBook(book.getISBN(), user.getId());
    }

    @Test
    public void GivenInvalidISBN_WhenBorrowBook_ThenThrowsIllegalArgumentException() {
        String invalidISBN = "12345";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> library.borrowBook(invalidISBN, user.getId()),
                "Invalid ISBN.");
        assertEquals("Invalid ISBN.", thrown.getMessage());
    }

    @Test
    public void GivenAlreadyBorrowedBook_WhenBorrowBook_ThenThrowsBookAlreadyBorrowedException() {
        book.borrow();
        when(mockDatabaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        when(mockDatabaseService.getUserById(user.getId())).thenReturn(user);

        BookAlreadyBorrowedException thrown = assertThrows(BookAlreadyBorrowedException.class,
                () -> library.borrowBook(book.getISBN(), user.getId()),
                "Book is already borrowed!");
        assertEquals("Book is already borrowed!", thrown.getMessage());
    }

    @Test
    public void GivenValidISBN_WhenReturnBook_ThenBookIsMarkedAsNotBorrowed() {
        book.borrow();
        when(mockDatabaseService.getBookByISBN(book.getISBN())).thenReturn(book);

        library.returnBook(book.getISBN());

        assertFalse(book.isBorrowed(), "Book should be marked as not borrowed.");
        verify(mockDatabaseService, times(1)).returnBook(book.getISBN());
    }

    @Test
    public void GivenUnborrowedBook_WhenReturnBook_ThenThrowsBookNotBorrowedException() {
        when(mockDatabaseService.getBookByISBN(book.getISBN())).thenReturn(book);

        BookNotBorrowedException thrown = assertThrows(BookNotBorrowedException.class,
                () -> library.returnBook(book.getISBN()),
                "Book wasn't borrowed!");
        assertEquals("Book wasn't borrowed!", thrown.getMessage());
    }

    @Test
    public void GivenValidISBNAndUser_WhenNotifyUserWithBookReviews_ThenSendsNotification() throws NotificationException {
        List<String> reviews = new ArrayList<>();
        reviews.add("Great book!");
        reviews.add("Highly recommend!");
        when(mockDatabaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        when(mockDatabaseService.getUserById(user.getId())).thenReturn(user);
        when(mockReviewService.getReviewsForBook(book.getISBN())).thenReturn(reviews);

        library.notifyUserWithBookReviews(book.getISBN(), user.getId());

        String expectedMessage = "Reviews for 'Test Book':\nGreat book!\nHighly recommend!";
        verify(mockNotificationService, times(1)).notifyUser(user.getId(), expectedMessage);
    }

    @Test
    public void GivenValidISBNAndUser_WhenNotifyUserWithNoReviews_ThenThrowsNoReviewsFoundException() {
        when(mockDatabaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        when(mockDatabaseService.getUserById(user.getId())).thenReturn(user);
        List<String> t = new java.util.ArrayList<>();
        when(mockReviewService.getReviewsForBook(book.getISBN())).thenReturn(t);

        NoReviewsFoundException thrown = assertThrows(NoReviewsFoundException.class,
                () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()),
                "No reviews found!");
        assertEquals("No reviews found!", thrown.getMessage());
    }

    @Test
    public void GivenValidISBN_WhenGetBookByISBN_ThenReturnsBook() {
        when(mockDatabaseService.getBookByISBN(book.getISBN())).thenReturn(book);

        Book fetchedBook = library.getBookByISBN(book.getISBN(), user.getId());

        assertEquals(book, fetchedBook, "The fetched book should match the expected book.");
    }

    @Test
    public void GivenInvalidISBN_WhenGetBookByISBN_ThenThrowsIllegalArgumentException() {
        String invalidISBN = "12345";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> library.getBookByISBN(invalidISBN, user.getId()),
                "Invalid ISBN.");
        assertEquals("Invalid ISBN.", thrown.getMessage());
    }
}
