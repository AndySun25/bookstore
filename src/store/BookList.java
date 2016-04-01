package store;

public interface BookList {
    Book[] list(String searchString);
    boolean add(Book book, int amount);
    int[] buy(Book... books);
}
