package ac.il.bgu.qa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestLibrary {

    private Book book;

    @BeforeEach
    public void setUp() {
        book = new Book("123-4567890123", "Test Book", "Test Author");
    }

    @Test
    public void testGetISBN() {
        assertEquals("123-4567890123", book.getISBN(), "ISBN should match the initialized value.");
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
