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
import org.mockito.Mock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestLibrary {

    @Mock
    private Book book;

    @Mock
    private User user;
    private static NotificationService mockNotificationService;
    private DatabaseService mockDatabaseService;
    private ReviewService mockReviewService;
    private String validISBN;
    private Library library;


    @BeforeEach
    public void setUp() {

        // Mock services
        book = mock(Book.class);
        user = mock(User.class);
        mockNotificationService = mock(NotificationService.class);
        mockDatabaseService = mock(DatabaseService.class);
        mockReviewService = mock(ReviewService.class);

        // Initialize Library
        library = new Library(mockDatabaseService, mockReviewService);

        validISBN = "9780306406157";
    }

    // Tests for Library Class


    // add book method tests


    private static Stream<Arguments> provideInvalidBooksForAddBook() {
        return Stream.of(
                Arguments.of(null, "Test Book", "Test Author", "Invalid ISBN."),
                Arguments.of("9780306406157", null, "Test Author", "Invalid title."),
                Arguments.of("9780306406157", "Test Book", null, "Invalid author."),
                Arguments.of("9780306406157", "", "Test Author", "Invalid title."),
                Arguments.of("9780306406157", "Test Book", "Invalid@Author", "Invalid author."),
                Arguments.of("9780306406157", "Test Book", "Mary--Jane", "Invalid author."),
                Arguments.of("9780306406157", "Test Book", "John-Doe--Smith", "Invalid author."),
                Arguments.of("9780306406157", "Test Book", "Mary--Jane", "Invalid author."),
                Arguments.of("9780306406158", "Book Title", "Author", "Invalid ISBN."), // Wrong check digit
                Arguments.of("978-0A0-6406157", "Book Title", "Author", "Invalid ISBN.") // Invalid characters
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidBooksForAddBook")
    public void GivenInvalidBook_WhenAddBook_ThenIllegalArgumentException(String isbn, String title, String author, String expectedMessage) {
        when(book.getISBN()).thenReturn(isbn);
        when(book.getTitle()).thenReturn(title);
        when(book.getAuthor()).thenReturn(author);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
        assertEquals(expectedMessage, thrown.getMessage());
    }


    @Test
    public void GivenBarrowedBook_WhenaddBook_ThenIllegalArgumentException() {
        when(book.isBorrowed()).thenReturn(true);
        when(book.getISBN()).thenReturn(validISBN);
        when(book.getTitle()).thenReturn("Test Book");
        when(book.getAuthor()).thenReturn("Test Author");
        verify(book, times(0)).isBorrowed();
        verifyNoInteractions(mockDatabaseService, mockNotificationService);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.addBook(book)
                , "Book with invalid borrowed state.");
        assertEquals(thrown.getMessage(), "Book with invalid borrowed state.");
    }


    @Test
    public void GivenExistingBook_WhentestAddBook_ThenThrowsIllegalArgumentException() {
        when(book.getISBN()).thenReturn(validISBN);
        when(book.getTitle()).thenReturn("Test Book");
        when(book.getAuthor()).thenReturn("Test Author");
        when(mockDatabaseService.getBookByISBN(validISBN)).thenReturn(book);
        verify(book, times(0)).isBorrowed();
        verifyNoInteractions(mockDatabaseService, mockNotificationService);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
        assertEquals("Book already exists.", thrown.getMessage());
    }

    // registerUser function


    private static Stream<Arguments> provideInvalidUsersForRegisterUser() {
        return Stream.of(
                Arguments.of("invalidUser ", "a", mockNotificationService, "Invalid user Id."), // check
                Arguments.of("", "123456789012", mockNotificationService, "Invalid user name."), //check
                Arguments.of(null, "123456789012", mockNotificationService, "Invalid user name."), //check
                Arguments.of("invalidUser ", null, mockNotificationService, "Invalid user Id."), //check
                Arguments.of("invalidUser ", "123456789012", null, "Invalid notification service.") //check
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidUsersForRegisterUser")
    public void GivenInvalidUser_WhenRegisterUser_ThenThrowsIllegalArgumentException(String name, String id, NotificationService notificationService, String expectedMessage) {
        when(user.getName()).thenReturn(name);
        when(user.getId()).thenReturn(id);
        when(user.getNotificationService()).thenReturn(notificationService);
        verify(mockDatabaseService, times(0)).registerUser(user.getId(), user);
        verifyNoInteractions(mockDatabaseService, mockNotificationService);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.registerUser(user));
        assertEquals(expectedMessage, thrown.getMessage());
    }


    @Test
    public void GivenValidUser_WhenRegisterUser_ThenUserIsRegisteredInDatabase() {

        when(user.getId()).thenReturn("123456789012");
        when(user.getName()).thenReturn("Test User");
        when(user.getNotificationService()).thenReturn(mockNotificationService);
        when(mockDatabaseService.getUserById(user.getId())).thenReturn(null);

        library.registerUser(user);
        verify(mockDatabaseService, times(1)).registerUser(user.getId(), user);
    }


    @Test
    public void GivenExistingUser_WhenRegisterUser_ThenThrowsIllegalArgumentException() {

        when(user.getId()).thenReturn("123456789012");
        when(user.getName()).thenReturn("Test User");
        when(user.getNotificationService()).thenReturn(mockNotificationService);
        when(mockDatabaseService.getUserById(user.getId())).thenReturn(user);
        verify(mockDatabaseService, times(0)).registerUser(user.getId(), user);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> library.registerUser(user),
                "User already exists.");
        assertEquals("User already exists.", thrown.getMessage());
    }


    @Test
    public void GivenInvalidUser_WhenRegisterUser_ThenThrowsIllegalArgumentException() {

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> library.registerUser(null),
                "Invalid user.");
        verify(mockDatabaseService, times(0)).registerUser(user.getId(), user);

        assertEquals("Invalid user.", thrown.getMessage());
    }


    // borrow book function


    private static Stream<Arguments> provideInvalidISBNsForBorrowBook() {
        return Stream.of(
                Arguments.of("12345", "Invalid ISBN."), // check
                Arguments.of(null, "Invalid ISBN."),
                Arguments.of("", "Invalid ISBN."),
                Arguments.of("invalidISBN", "Invalid ISBN.")
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidISBNsForBorrowBook")
    public void GivenInvalidISBN_WhenBorrowBook_ThenThrowsIllegalArgumentException(String invalidISBN, String expectedMessage) {
        when(user.getId()).thenReturn("123456789012");
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.borrowBook(invalidISBN, user.getId()));

        verify(mockDatabaseService, times(0)).borrowBook(invalidISBN, user.getId());

        assertEquals(expectedMessage, thrown.getMessage());
    }

//    @Test
//    public void GivenValidISBNAndUser_WhenBorrowBook_ThenBookIsMarkedAsBorrowed() {
//        String userId = "123456789012";
//
//        // spy use in book
//        book = spy(new Book(validISBN, "Test Book", "Valid Author"));
//
//        when(user.getId()).thenReturn(userId);
//        when(mockDatabaseService.getBookByISBN(validISBN)).thenReturn(book);
//        when(mockDatabaseService.getUserById(userId)).thenReturn(user);
//
//        library.borrowBook(validISBN, userId);
//
//        assertTrue(book.isBorrowed(), "Book should be marked as borrowed."); // Real state is updated
//        verify(mockDatabaseService, times(1)).borrowBook(validISBN, userId);
//    }


    @Test
    public void GivenAlreadyBorrowedBook_WhenBorrowBook_ThenThrowsBookAlreadyBorrowedException() {
        when(book.isBorrowed()).thenReturn(true);
        when(book.getISBN()).thenReturn(validISBN);
        when(mockDatabaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        when(user.getId()).thenReturn("123456789012");
        when(mockDatabaseService.getUserById(user.getId())).thenReturn(user);
        verify(mockDatabaseService, times(0)).borrowBook((book.getISBN()), user.getId());
        BookAlreadyBorrowedException thrown = assertThrows(BookAlreadyBorrowedException.class,
                () -> library.borrowBook(book.getISBN(), user.getId()),
                "Book is already borrowed!");
        assertEquals("Book is already borrowed!", thrown.getMessage());
    }

    @Test
    public void GivenValidISBN_WhenReturnBook_ThenBookIsMarkedAsNotBorrowed() {
        // spy book
        book = spy(new Book(validISBN, "Test Book", "Valid Author"));
        book.borrow();
        when(mockDatabaseService.getBookByISBN(validISBN)).thenReturn(book);

        library.returnBook(validISBN);
        verify(mockDatabaseService, times(0)).borrowBook((book.getISBN()), user.getId());

        assertFalse(book.isBorrowed(), "Book should be marked as not borrowed."); // Real state is updated
        verify(mockDatabaseService, times(1)).returnBook(validISBN);
    }


    @Test
    public void GivenUnborrowedBook_WhenReturnBook_ThenThrowsBookNotBorrowedException() {
        when(book.isBorrowed()).thenReturn(false);
        when(book.getISBN()).thenReturn(validISBN);
        when(mockDatabaseService.getBookByISBN(book.getISBN())).thenReturn(book);

        verify(mockDatabaseService, times(0)).borrowBook((book.getISBN()), user.getId());

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
        user = spy(new User("user name", "123456789012", mockNotificationService));
        when(book.getISBN()).thenReturn(validISBN);
        when(book.getTitle()).thenReturn("Test Book");
        String expectedMessage = "Reviews for 'Test Book':\nGreat book!\nHighly recommend!";
        when(mockDatabaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        when(mockDatabaseService.getUserById(user.getId())).thenReturn(user);
        when(mockReviewService.getReviewsForBook(book.getISBN())).thenReturn(reviews);

        library.notifyUserWithBookReviews(book.getISBN(), user.getId());

        verify(mockNotificationService, times(1)).notifyUser(user.getId(), expectedMessage);
    }


    @Test
    public void GivenValidISBNAndNullUser_WhenNotifyUserWithBookReviews_ThenThrowsException() {
        String validISBN = book.getISBN();
        verify(mockReviewService, times(0)).getReviewsForBook((book.getISBN()));

        assertThrows(IllegalArgumentException.class,
                () -> library.notifyUserWithBookReviews(validISBN, null),
                "Calling notifyUserWithBookReviews with a null user should throw an exception.");
    }


    @Test
    public void GivenValidISBNAndUser_WhenNotifyUserWithNoReviews_ThenThrowsNoReviewsFoundException() {
        when(book.getISBN()).thenReturn(validISBN);
        when(user.getId()).thenReturn("123456789012");
        when(mockDatabaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        when(mockDatabaseService.getUserById(user.getId())).thenReturn(user);
        List<String> t = new java.util.ArrayList<>();
        when(mockReviewService.getReviewsForBook(book.getISBN())).thenReturn(t);
        verify(mockReviewService, times(0)).getReviewsForBook((book.getISBN()));

        NoReviewsFoundException thrown = assertThrows(NoReviewsFoundException.class,
                () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()),
                "No reviews found!");
        assertEquals("No reviews found!", thrown.getMessage());
    }


    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    public void GivenValidISBN_WhenGetBookByISBN_ThenReturnsBook() {
        when(book.getISBN()).thenReturn(validISBN);
        when(user.getId()).thenReturn("123456789012");
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
    public void GivenBookNotFound_WhenBorrowBook_ThenBookNotFoundException() {
        when(mockDatabaseService.getBookByISBN("9780306406157")).thenReturn(null);
        BookNotFoundException thrown = assertThrows(BookNotFoundException.class, () -> library.borrowBook("9780306406157", "123456789012"));
        assertEquals("Book not found!", thrown.getMessage());
    }


    private static Stream<Arguments> provideInvalidUserIDsForBorrowBook() {
        return Stream.of(
                Arguments.of("invalidUserId", "Invalid user Id."),
                Arguments.of(null, "Invalid user Id.")
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidUserIDsForBorrowBook")
    public void GivenInvalidUserID_WhenBorrowBook_ThenThrowsIllegalArgumentException(String userid, String expectedMessage) {
        when(book.getISBN()).thenReturn(validISBN);
        when(mockDatabaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.borrowBook(book.getISBN(), userid));
        assertEquals(expectedMessage, thrown.getMessage());
    }


    @Test
    public void GivenUserNotRegistered_WhenBorrowBook_ThenUserNotRegisteredException() {
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

    private static Stream<Arguments> provideInvalidInputsForNotifyUserWithReviews() {
        return Stream.of(
                Arguments.of("invalidISBN", "123456789012", "Invalid ISBN."),
                Arguments.of("9780306406157", "invalidUserId", "Invalid user Id."),
                Arguments.of("9780306406157", null, "Invalid user Id.")
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidInputsForNotifyUserWithReviews")
    public void GivenInvalidInputs_WhenNotifyUserWithReviews_ThenThrowsIllegalArgumentException(String isbn, String userId, String expectedMessage) {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> library.notifyUserWithBookReviews(isbn, userId));

        assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    public void GivenBookNotFound_WhenNotifyUserWithReviews_ThenBookNotFoundException() {
        when(mockDatabaseService.getBookByISBN("9780306406157")).thenReturn(null);
        BookNotFoundException thrown = assertThrows(BookNotFoundException.class, () -> library.notifyUserWithBookReviews("9780306406157", "123456789012"));
        assertEquals("Book not found!", thrown.getMessage());
    }

    @Test
    public void GivenUserNotFound_WhenNotifyUserWithReviews_ThenUserNotRegisteredException() {
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

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
            library.getBookByISBN(validISBN, invalidUserId)
        );

        assertEquals("Invalid user Id.", thrown.getMessage());
    }

    @Test
    public void GivenBookNotFound_WhenGetBookByISBN_ThenThrowBookNotFoundException() {
        String validISBN = "9780306406157";
        String validUserId = "123456789012";

        when(mockDatabaseService.getBookByISBN(validISBN)).thenReturn(null);

        BookNotFoundException thrown = assertThrows(BookNotFoundException.class, () -> library.getBookByISBN(validISBN, validUserId));

        assertEquals("Book not found!", thrown.getMessage());
    }

    @Test
    public void GivenBookAlreadyBorrowed_WhenGetBookByISBN_ThenThrowBookAlreadyBorrowedException() {
        String validISBN = "9780306406157";
        String validUserId = "123456789012";

        when(mockDatabaseService.getBookByISBN(validISBN)).thenReturn(book);
        when(book.isBorrowed()).thenReturn(true);

        BookAlreadyBorrowedException thrown = assertThrows(BookAlreadyBorrowedException.class, () -> library.getBookByISBN(validISBN, validUserId));

        assertEquals("Book was already borrowed!", thrown.getMessage());
    }

    @Test
    public void GivenNoReviews_WhenNotifyUserWithBookReviews_ThenThrowsNoReviewsFoundException() {
        when(book.getISBN()).thenReturn(validISBN);
        when(user.getId()).thenReturn("123456789012");
        when(mockDatabaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        when(mockDatabaseService.getUserById(user.getId())).thenReturn(user);
        when(mockReviewService.getReviewsForBook(book.getISBN())).thenReturn(new ArrayList<>());

        NoReviewsFoundException thrown = assertThrows(NoReviewsFoundException.class,
                () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()),
                "No reviews found!");
        assertEquals("No reviews found!", thrown.getMessage());
    }

    @Test
    public void GivenNullReviews_WhenNotifyUserWithBookReviews_ThenThrowsNoReviewsFoundException() {
        when(book.getISBN()).thenReturn(validISBN);
        when(user.getId()).thenReturn("123456789012");
        when(mockDatabaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        when(mockDatabaseService.getUserById(user.getId())).thenReturn(user);
        when(mockReviewService.getReviewsForBook(book.getISBN())).thenReturn(null);
        NoReviewsFoundException thrown = assertThrows(NoReviewsFoundException.class,
                () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()),
                "No reviews found!");
        assertEquals("No reviews found!", thrown.getMessage());
    }

    @Test
    public void GivenReviewServiceUnavailable_WhenNotifyUserWithBookReviews_ThenThrowsReviewServiceUnavailableException() {
        when(book.getISBN()).thenReturn(validISBN);
        when(user.getId()).thenReturn("123456789012");
        when(mockDatabaseService.getBookByISBN(book.getISBN())).thenReturn(book);
        when(mockDatabaseService.getUserById(user.getId())).thenReturn(user);
        when(mockReviewService.getReviewsForBook(book.getISBN())).thenThrow(new ReviewException("Service unavailable"));

        ReviewServiceUnavailableException thrown = assertThrows(ReviewServiceUnavailableException.class,
                () -> library.notifyUserWithBookReviews(book.getISBN(), user.getId()),
                "Review service unavailable!");
        assertEquals("Review service unavailable!", thrown.getMessage());
    }


    @Test
    void GivenInvalidISBN_whenReturnBook_thenThrowIllegalArgumentException() {
        // Arrange
        String invalidISBN = "123"; // Assume this is not a valid ISBN.

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> library.returnBook(invalidISBN));
        assertEquals("Invalid ISBN.", exception.getMessage());
    }

    @Test
    void GivenBookNotFound_whenReturnBook_thenThrowBookNotFoundException() {
        String validISBN = "978-3-16-148410-0";
        when(mockDatabaseService.getBookByISBN(validISBN)).thenReturn(null); // No book found.

        BookNotFoundException exception = assertThrows(BookNotFoundException.class,
                () -> library.returnBook(validISBN));
        assertEquals("Book not found!", exception.getMessage());
    }

    @Test
    public void givenAuthorStartsWithNonAlphabeticCharacter_WhenAddBook_ThenThrowIllegalArgumentException() {

        book = spy(new Book("9780306406157", "Test Book", "-JohnDoe"));
        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(book),
                "Author name starting with non-alphabetic character should throw an exception.");
    }

    @Test
    public void givenAuthorEndsWithNonAlphabeticCharacter_WhenAddBook_ThenThrowIllegalArgumentException() {
        book = spy(new Book("9780306406157", "Test Book", "JohnDoe--"));

        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(book),
                "Author name ending with non-alphabetic character should throw an exception.");
    }


    @Test
    public void givenAuthorContainsConsecutiveHyphens_WhenAddBook_ThenThrowIllegalArgumentException() {
        book = spy(new Book("9780590353427", "Test Book", "John--Doe"));

        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(book),
                "Author name with consecutive hyphens should throw an exception.");
    }

    @Test
    public void givenAuthorContainsConsecutiveApostrophes_WhenAddBook_ThenThrowIllegalArgumentException() {
        book = spy(new Book("9783161484100", "Test Book", "O'Neil''O"));

        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(book),
                "Author name with consecutive apostrophes should throw an exception.");
    }

    @Test
    public void givenAuthorisempty_WhenAddBook_ThenThrowIllegalArgumentException() {
        book = spy(new Book("9783161484100", "Test Book", ""));

        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(book),
                "Author name is empty ");
    }

    @Test
    public void givenAuthorNameContainsPeriod_WhenAddBook_ThenNoException() {
        // Arrange
        book = spy(new Book("9780306406157", "Test Book", "Dr. John Doe"));

        // Act & Assert
        assertDoesNotThrow(() -> library.addBook(book),
                "Author name containing a period should not throw an exception.");
    }


    @Test
    public void GivenNotificationFails_WhenNotifyUserWithBookReviews_ThenThrowsNotificationException() throws NotificationException {
        List<String> reviews = Arrays.asList("Excellent book!", "Must read!");

        user = spy(new User("user name", "123456789012", mockNotificationService));
        when(book.getISBN()).thenReturn(validISBN);
        when(mockDatabaseService.getBookByISBN(validISBN)).thenReturn(book);
        when(mockDatabaseService.getUserById(user.getId())).thenReturn(user);
        when(mockReviewService.getReviewsForBook(validISBN)).thenReturn(reviews);

        // Set up the exception to be thrown when notifyUser is called
        doThrow(new NotificationException("Notification failed!"))
                .when(mockNotificationService).notifyUser(eq("123456789012"), anyString());

        // Act & Assert
        NotificationException thrown = assertThrows(NotificationException.class,
                () -> library.notifyUserWithBookReviews(validISBN, user.getId()));

        assertEquals("Notification failed!", thrown.getMessage());
    }
}