public class main {
    static String source_url = "http://www.contribe.se/bookstoredata/bookstoredata.txt";

    public static void main(String[] args) {
        Store store = new Store(source_url);
        Book[] list = store.list("First Author");
        for (int i=0;i<list.length;i++) {
            System.out.println(list[i]);
        }
    }
}
