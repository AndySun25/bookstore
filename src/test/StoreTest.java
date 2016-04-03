package test;

import junit.framework.*;
import org.json.simple.JSONObject;
import org.junit.Assert;
import store.Book;
import store.Store;

import java.math.BigDecimal;

public class StoreTest extends TestCase {
    protected Store store;
    protected Book book1, book2, book3;

    protected void setUp() {
        store = new Store();
        book1 = new Book("title_1", "author_1", new BigDecimal(10.0));
        book1.setId(1);
        book2 = new Book("title_2", "author_2", new BigDecimal(15.0));
        book2.setId(2);
        book3 = new Book("title_2", "author_1", new BigDecimal(5.0));
        book3.setId(3);
        store.add(book1, 1);
        store.add(book2, 2);
        store.add(book3, 0);
    }

    public void testListAll() {
        /**
         * Test listing all books.
         */
        Book[] expectedResult = {book1, book2, book3};
        Book[] result = store.list("");
        Assert.assertArrayEquals(expectedResult, result);
    }

    public void testListTitle() {
        /**
         * Test listing books by title search.
         */
        Book[] expectedResult = {book2, book3};
        JSONObject query = new JSONObject();
        query.put("title", book2.getTitle());
        query.put("partial", false);
        Book[] result = store.list(query.toJSONString());
        Assert.assertArrayEquals(expectedResult, result);
    }

    public void testListTitleCaseInsensitive() {
        /**
         * Test listing books by title search can be done case insensitively.
         */
        Book[] expectedResult = {book2, book3};
        JSONObject query = new JSONObject();
        query.put("title", book2.getTitle().toUpperCase());
        query.put("partial", false);
        Book[] result = store.list(query.toJSONString());
        Assert.assertArrayEquals(expectedResult, result);
    }

    public void testListTitlePartial() {
        /**
         * Test listing books by title search.
         */
        Book[] expectedResult = {book2, book3};
        JSONObject query = new JSONObject();
        query.put("title", "_2");
        query.put("partial", true);
        Book[] result = store.list(query.toJSONString());
        Assert.assertArrayEquals(expectedResult, result);
    }

    public void testListAuthor() {
        /**
         * Test listing books by author search.
         */
        Book[] expectedResult = {book1, book3};
        JSONObject query = new JSONObject();
        query.put("author", book1.getAuthor());
        query.put("partial", false);
        Book[] result = store.list(query.toJSONString());
        Assert.assertArrayEquals(expectedResult, result);
    }

    public void testListAuthorCaseInsensitive() {
        /**
         * Test listing books by author search can be done case insensitively.
         */
        Book[] expectedResult = {book1, book3};
        JSONObject query = new JSONObject();
        query.put("author", book1.getAuthor().toUpperCase());
        query.put("partial", false);
        Book[] result = store.list(query.toJSONString());
        Assert.assertArrayEquals(expectedResult, result);
    }

    public void testListAuthorPartial() {
        /**
         * Test listing books by author search.
         */
        Book[] expectedResult = {book1, book3};
        JSONObject query = new JSONObject();
        query.put("author", "_1");
        query.put("partial", true);
        Book[] result = store.list(query.toJSONString());
        Assert.assertArrayEquals(expectedResult, result);
    }

    public void testAddBook() {
        /**
         * Test adding new book.
         */
        int initial_length = store.list().length;

        Book newBook = new Book("new_title", "new_author", new BigDecimal(25.0));
        store.add(newBook, 5);

        Assert.assertEquals(store.list().length, initial_length+1);

    }

    public void testBuyBook() {
        /**
         * Test buying books.
         */
        Book non_existent_book = new Book("title", "author", new BigDecimal(4.0));
        int[] expected_result = {0, 0, 1, 2};
        int[] result = store.buy(book1, book2, book3, non_existent_book);

        Assert.assertArrayEquals(expected_result, result);
    }
}
