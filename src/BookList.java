public interface BookList {
    public Book[] list(String searchString);
    public boolean add(Book book, int amount);
    public int[] buy(Book... books);
}
