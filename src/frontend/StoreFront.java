package frontend;

import store.Book;
import store.Store;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class StoreFront {
    private JFrame frm_main;
    private JPanel pnl_main;
    private JTextField txt_search;
    private JButton btn_search;
    private JList lst_result;
    private JList lst_cart;
    private JButton btn_buy;
    private JLabel lbl_cart;
    private JLabel lbl_store;
    private JButton btn_add;
    private JButton btn_remove;
    private JScrollPane scr_result;
    private JScrollPane scr_cart;
    private static Dimension def = new Dimension(500, 400);

    private List<Book> cart;
    private Store store;

    public StoreFront(Store store) {
        this.store = store;
        cart = new ArrayList<>();
        populateResults(store.list());

        btn_search.addActionListener(new searchListener());
        btn_add.addActionListener(new addListener());
        btn_remove.addActionListener(new removeListener());
        btn_buy.addActionListener(new buyListener());

        frm_main = new JFrame("Storefront");
        frm_main.setPreferredSize(def);
        frm_main.add(pnl_main);
        frm_main.pack();
        frm_main.setVisible(true);
    }

    public void populateResults(Book[] books) {
        lst_result.setListData(books);
    }

    public void addToCart(Book book) {
        cart.add(book);
        refreshCart();
    }

    public void removeFromCart(Book book) {
        cart.remove(book);
        refreshCart();
    }

    public void buy() {
        store.buy(cart.toArray(new Book[cart.size()]));
        cart.clear();
        populateResults(store.list());
        refreshCart();
    }

    public void refreshCart() {
        lst_cart.setListData(cart.toArray(new Book[cart.size()]));
    }

    public class searchListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String queryString = txt_search.getText();
            Book[] results;
            if (queryString.length()==0) {
                results = store.list();
            } else {
                results = store.list(queryString);
            }
            populateResults(results);
        }
    }

    public class addListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (!lst_result.isSelectionEmpty()) {
                Book selectedBook = (Book) lst_result.getSelectedValue();
                addToCart(selectedBook);
            }
        }
    }

    public class removeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (!lst_cart.isSelectionEmpty()) {
                Book selectedBook = (Book) lst_cart.getSelectedValue();
                removeFromCart(selectedBook);
            }
        }
    }

    public class buyListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (cart.size() > 0) {
                buy();
            }
        }
    }
}
