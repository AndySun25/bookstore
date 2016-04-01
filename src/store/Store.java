package store;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;

public class Store implements BookList {
    // All books
    private HashMap<Integer, Book> books;

    // Book indices
    private HashMap<Integer, Integer> book_stock;
    private HashMap<String, List<Integer>> title_index;
    private HashMap<String, List<Integer>> author_index;

    // Unique id
    private int id = 1;


    public Store() {
        books = new HashMap<>();
        book_stock = new HashMap<>();
        title_index = new HashMap<>();
        author_index = new HashMap<>();
    }

    public Store(String source_url) {
        /**
         * Initialize and populate from url.
         */

        books = new HashMap<>();
        book_stock = new HashMap<>();
        title_index = new HashMap<>();
        author_index = new HashMap<>();
        getBooksFromUrl(source_url);
    }

    public void getBooksFromUrl(String source_url){
        /**
         * Populates list of books from url.
         */
        DecimalFormat df = new DecimalFormat();
        df.setParseBigDecimal(true);
        try {
            URL url = new URL(source_url);
            Scanner s = new Scanner(url.openStream());

            while (s.hasNextLine()) {
                try {
                    String[] values = s.nextLine().split(";");
                    String title = values[0];
                    String author = values[1];
                    BigDecimal price = (BigDecimal) df.parse(values[2]);
                    int stock = Integer.parseInt(values[3]);

                    Book book = new Book(title, author, price);
                    book.setId(id);

                    add(book, stock);
                } catch (ParseException e) {
                    System.err.println("Error in parsing record");
                }
            }
        } catch (MalformedURLException e) {
            System.err.println("Malformed URL, please ensure the URL provided is valid.");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Store initialized");
        }
    }

    public Book[] list() {
        Collection<Book> ret = books.values();
        return ret.toArray(new Book[ret.size()]);
    }

    @Override
    public Book[] list(String searchString) {
        List<Integer> title_matches = title_index.get(searchString);
        List<Integer> author_matches = author_index.get(searchString);

        Set<Integer> aggregated = new HashSet<>();
        if (title_matches != null) {
            aggregated.addAll(title_matches);
        }
        if (author_matches != null) {
            aggregated.addAll(author_matches);
        }

        Book[] ret = new Book[aggregated.size()];

        int counter = 0;
        for (int id : aggregated) {
            ret[counter++] = books.get(id);
        }

        return ret;
    }

    @Override
    public boolean add(Book book, int amount) {
        /**
         * Add new book to store or add stock to existing book.
         */
        boolean exists = false;
        // Check if book exists.
        if (author_index.containsKey(book.getAuthor()) && title_index.containsKey(book.getTitle())) {

            // Create copy of list as to not overwrite index entry.
            List<Integer> intersect = new ArrayList<>(author_index.get(book.getAuthor()));
            intersect.retainAll(title_index.get(book.getTitle()));

            if (intersect.size()==1) {
                // If book with same title and same author but different price exists, return false.
                if (books.get(intersect.get(0)).getPrice() != book.getPrice()) {
                    return false;
                } else {
                    exists = true;
                }
            }
        }

        // If book exists, add to stock, otherwise add new book.
        if (exists) {
            int new_stock = book_stock.get(book.getId()) + amount;
            book_stock.put(book.getId(), new_stock);
        } else {
            book.setId(id);
            books.put(id, book);
            book_stock.put(id, amount);
            AddToIndices(book);
            id++;
        }
        return true;
    }

    @Override
    public int[] buy(Book... books) {
        /**
         * Decrements stock values where applicable and return array of result status codes.
         */

        int[] ret = new int[books.length];

        for (int i=0;i<books.length;i++) {
            int cur_id = books[i].getId();

            // If book exists
            if (book_stock.containsKey(cur_id)) {
                int cur_stock = book_stock.get(cur_id);

                // If book has sufficient stock
                if (cur_stock >= 1) {
                    int new_stock = cur_stock - 1;
                    book_stock.put(cur_id, new_stock);
                    ret[i] = 0;
                } else {
                    ret[i] = 1;
                }
            } else {
                ret[i] = 2;
            }
            System.out.print(ret[i] + ", ");
        }
        System.out.println();

        return ret;
    }

    public void AddToIndices(Book book) {
        /***
         * Adds book title and author to their respective indices.
         */
        int id = book.getId();
        String title = book.getTitle();
        String author = book.getAuthor();

        // Update title index
        if (title_index.containsKey(title)) {
            title_index.get(title).add(id);
        } else {
            List<Integer> val = new ArrayList<>();
            val.add(id);
            title_index.put(title, val);
        }

        // Update author index
        if (author_index.containsKey(author)) {
            author_index.get(author).add(id);
        } else {
            List<Integer> val = new ArrayList<>();
            val.add(id);
            author_index.put(author, val);
        }
    }
}
