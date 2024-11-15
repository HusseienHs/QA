package ac.il.bgu.qa;

import ac.il.bgu.qa.services.DatabaseService;
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
    }
    @Test
    public void author_name_special_character_checks() {
        // Test for author names that do not start with an alphabetic character
        Book invalidAuthorStart = new Book("9780306406157", "Test Book", "-JohnDoe");
        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(invalidAuthorStart), "Author name starting with non-alphabetic character should throw an exception.");

        // Test for author names that do not end with an alphabetic character
        Book invalidAuthorend = new Book("9780306406157", "Test Book", "JohnDoe--");
        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(invalidAuthorend), "Author name starting with non-alphabetic character should throw an exception.");


        Book invalidAuthorWithDigit = new Book("9780306406157", "Test Book", "John123Doe");
        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(invalidAuthorWithDigit), "Author name with digits should throw an exception.");

        Book invalidAuthorWithSpecialChar = new Book("9780132350884", "Test Book", "John@Doe");
        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(invalidAuthorWithSpecialChar), "Author name with special characters should throw an exception.");


        Book invalidAuthorWithConsecutiveHyphens = new Book("9780590353427", "Test Book", "John--Doe");
        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(invalidAuthorWithConsecutiveHyphens), "Author name with consecutive hyphens should throw an exception.");

        Book invalidAuthorWithConsecutiveApostrophes = new Book("9783161484100", "Test Book", "O'Neil''O");
        assertThrows(IllegalArgumentException.class,
                () -> library.addBook(invalidAuthorWithConsecutiveApostrophes), "Author name with consecutive apostrophes should throw an exception.");
    }

    @Test
    public void add_IllegalArgumentException_TO_LIBRARY() {
        Book null_isbn = new Book(null, "Test Book", "Test Author");
        assertThrows(IllegalArgumentException.class, () -> library.addBook(null_isbn), "Invalid ISBN.");
        assertThrows(IllegalArgumentException.class, () -> library.addBook(null), "Invalid book.");
        assertThrows(IllegalArgumentException.class, () -> library.addBook(book_null_title), "Invalid title.");
        assertThrows(IllegalArgumentException.class, () -> library.addBook(book_null_ISBN), "Invalid ISBN.");
        assertThrows(IllegalArgumentException.class, () -> library.addBook(book_null_author), "Invalid author.");
        assertThrows(IllegalArgumentException.class, () -> library.addBook(book_borrowed), "Book with invalid borrowed state.");
    }

    @Test
    public void testAddBook_BookAlreadyExists() {
        when(databaseService.getBookByISBN(book.getISBN())).thenReturn(book);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.addBook(book), "Book already exists.");

        assertEquals("Book already exists.", thrown.getMessage());

        verify(databaseService, never()).addBook(anyString(), any());
    }

    @Test
    public void testAddBook_Success() {
        when(databaseService.getBookByISBN(book.getISBN())).thenReturn(null);

        library.addBook(book);

        verify(databaseService, times(1)).addBook(book.getISBN(), book);
    }

    @Test
    public void testAddBook_InvalidBook() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> library.addBook(null), "Invalid book.");

        assertEquals("Invalid book.", thrown.getMessage());

    }
    @Test
    public void testGetISBN() {
        assertEquals("9780306406157", book.getISBN(), "ISBN should match the initialized value.");
    }

    @Test
    public void testGetTitle() {
        assertEquals("Test Book", book.getTitle(), "Title should match the initialized value.");
    }

    @Test
    public void testGetAuthor() {
        assertEquals("Test Author", book.getAuthor(), "Author should match the initialized value.");
    }

    @Test
    public void testIsBorrowedInitially() {
        assertFalse(book.isBorrowed(), "New book should not be borrowed initially.");
    }

    @Test
    public void testBorrow() {
        book.borrow();
        assertTrue(book.isBorrowed(), "Book should be marked as borrowed after calling borrow().");
    }

    @Test
    public void testBorrowAlreadyBorrowed() {
        book.borrow();
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> book.borrow(),
                "Borrowing an already borrowed book should throw an exception.");
        assertEquals("Book is already borrowed!", thrown.getMessage());
    }

    @Test
    public void testReturnBook() {
        book.borrow();
        book.returnBook();
        assertFalse(book.isBorrowed(), "Book should be marked as not borrowed after calling returnBook().");
    }

    @Test
    public void testReturnBookNotBorrowed() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> book.returnBook(),
                "Returning a book that wasn't borrowed should throw an exception.");
        assertEquals("ac.il.bgu.qa.Book wasn't borrowed!", thrown.getMessage());
    }
}