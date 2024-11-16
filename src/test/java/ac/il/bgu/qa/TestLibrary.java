package ac.il.bgu.qa;

import ac.il.bgu.qa.errors.*;
import ac.il.bgu.qa.services.DatabaseService;
import ac.il.bgu.qa.services.NotificationService;
import ac.il.bgu.qa.services.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestLibrary {

    private Book book;
    private User user;
    private static NotificationService mockNotificationService;
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


    private static Stream<Arguments> provideInvalidBooksForAddBook() {
        return Stream.of(
                Arguments.of(new Book(null, "Test Book", "Test Author"), "Invalid ISBN."),
                Arguments.of(new Book("9780306406157", null, "Test Author"), "Invalid title."),
                Arguments.of(new Book("9780306406157", "Test Book", null), "Invalid author."),
                Arguments.of(new Book("9780306406157", "", "Test Author"), "Invalid title."),
                Arguments.of(new Book("9780306406157", "Test Book", "Invalid@Author"), "Invalid author."),
                Arguments.of(new Book("9780306406158", "Book Title", "Author"), "Invalid ISBN."), // Wrong check digit
                Arguments.of(new Book("978-0A0-6406157", "Book Title", "Author"), "Invalid ISBN.") // Invalid characters
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidBooksForAddBook")
    public void GivenInvalidBook_WhenAddBook_ThenIllegalArgumentException(Book invalidBook, String expectedMessage) {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.addBook(invalidBook));
        assertEquals(expectedMessage, thrown.getMessage());
    }


    @Test
    public void GivenBarrowedBook_WhenaddBook_ThenIllegalArgumentException() {
        book.borrow();
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.addBook(book)
                , "Book with invalid borrowed state.");
        assertEquals(thrown.getMessage(), "Book with invalid borrowed state.");

    }

    // Test when book is not borrowed
    @Test
    public void GivenNotBorrowedBook_WhenBorrow_ThenBorrowedSuccessfully() {
        Book book = new Book("123", "Test Book", "Author");
        assertFalse(book.isBorrowed());
        book.borrow();
        assertTrue(book.isBorrowed());
    }

    // Test when book is already borrowed
    @Test
    public void GivenAlreadyBorrowedBook_WhenBorrow_ThenThrowsException() {
        Book book = new Book("123", "Test Book", "Author");
        book.borrow();
        assertThrows(IllegalStateException.class, book::borrow);
    }

    // Test when book is borrowed
    @Test
    public void GivenBorrowedBook_WhenReturn_ThenReturnedSuccessfully() {
        Book book = new Book("123", "Test Book", "Author");
        book.borrow();
        assertTrue(book.isBorrowed());
        book.returnBook();
        assertFalse(book.isBorrowed());
    }

    // Test when book is not borrowed
    @Test
    public void GivenNotBorrowedBook_WhenReturn_ThenThrowsException() {
        Book book = new Book("123", "Test Book", "Author");
        assertThrows(IllegalStateException.class, book::returnBook);
    }



    @Test
    public void testAddBook_BookAlreadyExists() {
        when(mockDatabaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Book already exists.");
        assertEquals("Book already exists.", thrown.getMessage());
    }

    // registerUser function


    private static Stream<Arguments> provideInvalidUsersForRegisterUser () {
        return Stream.of(
                Arguments.of(new User("invalidUser ", "a", mockNotificationService), "Invalid user Id."), // check
                Arguments.of(new User("", "123456789012", mockNotificationService), "Invalid user name."), //check
                Arguments.of(new User(null, "123456789012", mockNotificationService), "Invalid user name."), //check
                Arguments.of(new User("invalidUser ", null, mockNotificationService), "Invalid user Id."), //check
                Arguments.of(new User("invalidUser ", "123456789012", null), "Invalid notification service.") //check
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidUsersForRegisterUser")
    public void GivenInvalidUser_WhenRegisterUser_ThenThrowsIllegalArgumentException(User invalidUser , String expectedMessage) {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.registerUser (invalidUser ));
        assertEquals(expectedMessage, thrown.getMessage());
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




    // borrow book function


    private static Stream<Arguments> provideInvalidISBNsForBorrowBook() {
        return Stream.of(
                Arguments.of("12345", "Invalid ISBN."), // check
                Arguments.of(null, "Invalid ISBN."),
                Arguments.of("", "Invalid ISBN.")
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidISBNsForBorrowBook")
    public void GivenInvalidISBN_WhenBorrowBook_ThenThrowsIllegalArgumentException(String invalidISBN, String expectedMessage) {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.borrowBook(invalidISBN, user.getId()));
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void GivenNonExistingBookByISBN_WhenBorrowBook_ThenThrowsIllegalArgumentException() {
        String isbn = "9750306406167";
        BookNotFoundException thrown = assertThrows(BookNotFoundException.class, () -> library.borrowBook(isbn, user.getId()),
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
    public void GivenValidISBNAndNullUser_WhenNotifyUserWithBookReviews_ThenThrowsException() {
        String validISBN = book.getISBN();
        assertThrows(IllegalArgumentException.class,
                () -> library.notifyUserWithBookReviews(validISBN, null),
                "Calling notifyUserWithBookReviews with a null user should throw an exception.");
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


    @Test
    public void GivennullID_WhenGetBookByISBN_ThenThrowsIllegalArgumentException() {
        String invalidISBN = "9780306406157";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> library.getBookByISBN(invalidISBN, null),
                "Invalid user Id.");
        assertEquals("Invalid user Id.", thrown.getMessage());
    }

    @Test
    public void GivenNullBook_WhenaddBook_ThenIllegalArgumentException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.addBook(null));
        assertEquals("Invalid book.", thrown.getMessage());
    }

    @Test
    public void GivenInvalidISBN_WhenaddBook_ThenIllegalArgumentException() {
        Book invalidBook = new Book(null, "Test Book", "Test Author");
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.addBook(invalidBook));
        assertEquals("Invalid ISBN.", thrown.getMessage());
    }

    @Test
    public void GivenInvalidISBN_WhenBorrowBook_ThenIllegalArgumentException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.borrowBook("invalidISBN", "123456789012"));
        assertEquals("Invalid ISBN.", thrown.getMessage());
    }

    @Test
    public void GivenBookNotFound_WhenBorrowBook_ThenBookNotFoundException() {
        when(mockDatabaseService.getBookByISBN("9780306406157")).thenReturn(null);
        BookNotFoundException thrown = assertThrows(BookNotFoundException.class, () -> library.borrowBook("9780306406157", "123456789012"));
        assertEquals("Book not found!", thrown.getMessage());
    }

    @Test
    public void GivenInvalidUserId_WhenBorrowBook_ThenIllegalArgumentException() {
        Book book = new Book("9780306406157", "Some Book", "Some Author");
        when(mockDatabaseService.getBookByISBN("9780306406157")).thenReturn(book);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> library.borrowBook("9780306406157", "invalidUserId"));
        assertEquals("Invalid user Id.", thrown.getMessage());
    }


    @Test
    public void GivenUserNotRegistered_WhenBorrowBook_ThenUserNotRegisteredException() {
        Book book = new Book("9780306406157", "Some Book", "Some Author");
        when(mockDatabaseService.getBookByISBN("9780306406157")).thenReturn(book);
        when(mockDatabaseService.getUserById("123456789012")).thenReturn(null);
        UserNotRegisteredException thrown = assertThrows(UserNotRegisteredException.class,
                () -> library.borrowBook("9780306406157", "123456789012"));
        assertEquals("User not found!", thrown.getMessage());
    }


    @Test
    public void GivenValidBorrow_WhenBorrowBook_ThenBookBorrowed() {
        when(mockDatabaseService.getBookByISBN("9780306406157")).thenReturn(book);
        when(mockDatabaseService.getUserById("123456789012")).thenReturn(user);
        library.borrowBook("9780306406157", "123456789012");
        verify(mockDatabaseService).borrowBook("9780306406157", "123456789012");
    }

    @Test
    public void GivenInvalidISBN_WhenNotifyUserWithReviews_ThenIllegalArgumentException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.notifyUserWithBookReviews("invalidISBN", "123456789012"));
        assertEquals("Invalid ISBN.", thrown.getMessage());
    }

    @Test
    public void GivenInvalidUserId_WhenNotifyUserWithReviews_ThenIllegalArgumentException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.notifyUserWithBookReviews("9780306406157", "invalidUserId"));
        assertEquals("Invalid user Id.", thrown.getMessage());
    }

    @Test
    public void GivenBookNotFound_WhenNotifyUserWithReviews_ThenBookNotFoundException() {
        when(mockDatabaseService.getBookByISBN("9780306406157")).thenReturn(null);
        BookNotFoundException thrown = assertThrows(BookNotFoundException.class, () -> library.notifyUserWithBookReviews("9780306406157", "123456789012"));
        assertEquals("Book not found!", thrown.getMessage());
    }

    @Test
    public void GivenUserNotFound_WhenNotifyUserWithReviews_ThenUserNotRegisteredException() {
        Book book = new Book("9780306406157", "Some Book", "Some Author");
        when(mockDatabaseService.getBookByISBN("9780306406157")).thenReturn(book);
        when(mockDatabaseService.getUserById("123456789012")).thenReturn(null);
        UserNotRegisteredException thrown = assertThrows(UserNotRegisteredException.class,
                () -> library.notifyUserWithBookReviews("9780306406157", "123456789012"));
        assertEquals("User not found!", thrown.getMessage());
    }

    @Test
    public void GivenInvalidUserId_WhenGetBookByISBN_ThenThrowIllegalArgumentException() {
        String validISBN = "9780306406157";
        String invalidUserId = "123";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            library.getBookByISBN(validISBN, invalidUserId);
        });

        assertEquals("Invalid user Id.", thrown.getMessage());
    }

    @Test
    public void GivenBookNotFound_WhenGetBookByISBN_ThenThrowBookNotFoundException() {
        String validISBN = "9780306406157";
        String validUserId = "123456789012";

        when(mockDatabaseService.getBookByISBN(validISBN)).thenReturn(null);

        BookNotFoundException thrown = assertThrows(BookNotFoundException.class, () -> {
            library.getBookByISBN(validISBN, validUserId);
        });

        assertEquals("Book not found!", thrown.getMessage());
    }

    @Test
    public void GivenBookAlreadyBorrowed_WhenGetBookByISBN_ThenThrowBookAlreadyBorrowedException() {
        String validISBN = "9780306406157";
        String validUserId = "123456789012";
        Book borrowedBook = mock(Book.class);

        when(mockDatabaseService.getBookByISBN(validISBN)).thenReturn(borrowedBook);
        when(borrowedBook.isBorrowed()).thenReturn(true);

        BookAlreadyBorrowedException thrown = assertThrows(BookAlreadyBorrowedException.class, () -> {
            library.getBookByISBN(validISBN, validUserId);
        });

        assertEquals("Book was already borrowed!", thrown.getMessage());
    }
    @Test
    public void GivenNoReviews_WhenNotifyUserWithBookReviews_ThenThrowsNoReviewsFoundException() {
        when(mockDatabaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        when(mockDatabaseService.getUserById(user.getId())).thenReturn(user);
        when(mockReviewService.getReviewsForBook(book.getISBN())).thenReturn(new ArrayList<>());

        NoReviewsFoundException thrown = assertThrows(NoReviewsFoundException.class,
                () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()),
                "No reviews found!");
        assertEquals("No reviews found!", thrown.getMessage());
    }
    @Test
    public void GivenReviewServiceUnavailable_WhenNotifyUserWithBookReviews_ThenThrowsReviewServiceUnavailableException() {
        when(mockDatabaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        when(mockDatabaseService.getUserById(user.getId())).thenReturn(user);
        when(mockReviewService.getReviewsForBook(book.getISBN())).thenThrow(new ReviewException("Service unavailable"));

        ReviewServiceUnavailableException thrown = assertThrows(ReviewServiceUnavailableException.class,
                () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()),
                "Review service unavailable!");
        assertEquals("Review service unavailable!", thrown.getMessage());
    }


    @Test
    void borrow_WhenAlreadyBorrowed_ShouldThrowException() {
        book.borrow();
        IllegalStateException exception = assertThrows(IllegalStateException.class, book::borrow);
        assertEquals("Book is already borrowed!", exception.getMessage());
    }

    @Test
    void returnBook_InvalidISBN_ShouldThrowIllegalArgumentException() {
        // Arrange
        String invalidISBN = "123"; // Assume this is not a valid ISBN.

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> library.returnBook(invalidISBN));
        assertEquals("Invalid ISBN.", exception.getMessage());
    }

    @Test
    void returnBook_BookNotFound_ShouldThrowBookNotFoundException() {
        // Arrange
        String validISBN = "978-3-16-148410-0"; // Assume this is a valid ISBN.
        when(mockDatabaseService.getBookByISBN(validISBN)).thenReturn(null); // No book found.

        // Act & Assert
        BookNotFoundException exception = assertThrows(BookNotFoundException.class,
                () -> library.returnBook(validISBN));
        assertEquals("Book not found!", exception.getMessage());
    }

    @Test
    public void givenAuthorStartsWithNonAlphabeticCharacter_WhenAddBook_ThenThrowIllegalArgumentException() {
        Book invalidAuthorStart = new Book("9780306406157", "Test Book", "-JohnDoe");

        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(invalidAuthorStart),
                "Author name starting with non-alphabetic character should throw an exception.");
    }

    @Test
    public void givenAuthorEndsWithNonAlphabeticCharacter_WhenAddBook_ThenThrowIllegalArgumentException() {
        Book invalidAuthorEnd = new Book("9780306406157", "Test Book", "JohnDoe--");

        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(invalidAuthorEnd),
                "Author name ending with non-alphabetic character should throw an exception.");
    }


    @Test
    public void givenAuthorContainsConsecutiveHyphens_WhenAddBook_ThenThrowIllegalArgumentException() {
        Book invalidAuthorWithConsecutiveHyphens = new Book("9780590353427", "Test Book", "John--Doe");

        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(invalidAuthorWithConsecutiveHyphens),
                "Author name with consecutive hyphens should throw an exception.");
    }

    @Test
    public void givenAuthorContainsConsecutiveApostrophes_WhenAddBook_ThenThrowIllegalArgumentException() {
        Book invalidAuthorWithConsecutiveApostrophes = new Book("9783161484100", "Test Book", "O'Neil''O");

        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(invalidAuthorWithConsecutiveApostrophes),
                "Author name with consecutive apostrophes should throw an exception.");
    }
    @Test
    public void givenAuthorisempty_WhenAddBook_ThenThrowIllegalArgumentException() {
        Book invalidAuthorWithConsecutiveApostrophes = new Book("9783161484100", "Test Book", "");

        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(invalidAuthorWithConsecutiveApostrophes),
                "Author name is empty ");
    }

    @Test
    public void givenAuthorNameContainsPeriod_WhenAddBook_ThenNoException() {
        // Arrange
        Book validAuthorWithPeriod = new Book("9780306406157", "Test Book", "Dr. John Doe");

        // Act & Assert
        assertDoesNotThrow(() -> library.addBook(validAuthorWithPeriod),
                "Author name containing a period should not throw an exception.");
    }


    @Test
    public void GivenNotificationFails_WhenNotifyUserWithBookReviews_ThenThrowsNotificationException() throws NotificationException {
        List<String> reviews = Arrays.asList("Excellent book!", "Must read!");
        when(mockDatabaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        when(mockDatabaseService.getUserById(user.getId())).thenReturn(user);
        when(mockReviewService.getReviewsForBook(book.getISBN())).thenReturn(reviews);

        doThrow(new NotificationException("Failed"))
                .when(mockNotificationService).notifyUser(eq(user.getId()), anyString());

        NotificationException thrown = assertThrows(NotificationException.class,
                () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()),
                "Notification failed!");
        assertEquals("Notification failed!", thrown.getMessage());
    }

}
