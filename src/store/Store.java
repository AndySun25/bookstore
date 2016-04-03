package store;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

public class Store implements BookList {
    // All books
    private HashMap<Integer, Book> books;

    // Book indices - Title and author indices are case insensitive.
    private HashMap<Integer, Integer> book_stock;
    private HashMap<String, Set<Integer>> title_index;
    private HashMap<String, Set<Integer>> author_index;

    // Unique id
    private int id = 1;

    // JSON parser for querystring parsing
    private static JSONParser queryparser = new JSONParser();


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

    private void getBooksFromUrl(String source_url){
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
                } catch (java.text.ParseException e) {
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
        /**
         * List all books.
         */
        Collection<Book> ret = books.values();
        return ret.toArray(new Book[ret.size()]);
    }

    @Override
    public Book[] list(String searchString) {
        /**
         * List books with JSON search parameters.
         */
        if (searchString.length()==0) {
            return list();
        }
        try {
            Object queryobj = queryparser.parse(searchString);
            JSONObject query = (JSONObject) queryobj;

            String title = (String) query.get("title");
            String author = (String) query.get("author");
            Boolean partial = (Boolean) query.get("partial");

            // Partial defaults to true if not provided.
            if (partial==null) {
                partial = true;
            }

            Set<Integer> aggregated_matches = new HashSet<>();

            // Find intersection between title and author filtering, use all books for either field if no value is provided.
            if (title==null || title.length()==0) {
                aggregated_matches.addAll(books.keySet());
            } else {
                // If partial match is requested, do full search through all books. Otherwise use indices.
                if (partial) {
                    Set<Integer> match_keys = new HashSet<>();
                    for (int key: books.keySet()) {
                        if (books.get(key).getTitle().toLowerCase().contains(title.toLowerCase())) {
                            match_keys.add(key);
                        }
                    }
                    aggregated_matches.addAll(match_keys);
                } else {
                    Set<Integer> match_keys = title_index.get(title.toLowerCase());
                    if (match_keys!=null) {
                        aggregated_matches.addAll(match_keys);
                    }
                }
            }

            if (author==null || author.length()==0) {
                aggregated_matches.retainAll(books.keySet());
            } else {
                if (partial) {
                    Set<Integer> match_keys = new HashSet<>();
                    for (int key: books.keySet()) {
                        if (books.get(key).getAuthor().toLowerCase().contains(author.toLowerCase())) {
                            match_keys.add(key);
                        }
                    }
                    aggregated_matches.retainAll(match_keys);
                } else {
                    Set<Integer> match_keys = author_index.get(author.toLowerCase());
                    if (match_keys!=null) {
                        aggregated_matches.retainAll(match_keys);
                    } else {
                        aggregated_matches.clear();
                    }
                }
            }

            Book[] ret = new Book[aggregated_matches.size()];

            int counter = 0;
            for (int id : aggregated_matches) {
                ret[counter++] = books.get(id);
            }

            return ret;
        } catch (ParseException e) {
            System.err.println("Malformed JSON querystring");
            return new Book[0];
        }
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

            if (intersect.size()>=1) {
                for (int id: intersect) {
                    if (books.get(id).getPrice().equals(book.getPrice())) {
                        exists = true;
                        break;
                    }
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
        }
        System.out.println();

        return ret;
    }

    private void AddToIndices(Book book) {
        /***
         * Adds book title and author to their respective indices.
         */
        int id = book.getId();
        String title = book.getTitle().toLowerCase();
        String author = book.getAuthor().toLowerCase();

        // Update title index
        if (title_index.containsKey(title)) {
            title_index.get(title).add(id);
        } else {
            HashSet<Integer> val = new HashSet<>();
            val.add(id);
            title_index.put(title, val);
        }

        // Update author index
        if (author_index.containsKey(author)) {
            author_index.get(author).add(id);
        } else {
            HashSet<Integer> val = new HashSet<>();
            val.add(id);
            author_index.put(author, val);
        }
    }
}
