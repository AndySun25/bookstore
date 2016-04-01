import frontend.StoreFront;
import store.Store;

public class main {
    static String source_url = "http://www.contribe.se/bookstoredata/bookstoredata.txt";

    public static void main(String[] args) {
        Store store = new Store(source_url);
        StoreFront front = new StoreFront(store);
    }
}
