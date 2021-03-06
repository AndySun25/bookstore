package frontend;

import org.json.simple.JSONObject;
import store.Book;
import store.Store;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StoreFront {
    private JFrame frm_main;
    private JPanel pnl_main;
    private JTextField txt_title;
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
    private JLabel lbl_total;
    private JTextField txt_author;
    private JLabel lbl_Title;
    private JLabel lbl_author;
    private JLabel lbl_title;
    private JCheckBox chk_partial;
    private static Dimension def = new Dimension(600, 400);

    private static String buy_success = "Purchased";
    private static String buy_out_of_stock = "Out of stock";
    private static String buy_invalid = "Book not found";

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
        frm_main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm_main.add(pnl_main);
        frm_main.pack();
        frm_main.setVisible(true);
        frm_main.setExtendedState(frm_main.getExtendedState() | JFrame.MAXIMIZED_BOTH);
    }

    private void populateResults(Book[] books) {
        lst_result.setListData(books);
    }

    private void addToCart(Book book) {
        cart.add(book);
        refreshCart();
    }

    private void removeFromCart(Book book) {
        cart.remove(book);
        refreshCart();
    }

    private void buy() {
        /**
         * Buy books and show results in dialog.
         */

        int[] results = store.buy(cart.toArray(new Book[cart.size()]));
        BigDecimal total_final = new BigDecimal(0);
        StringBuilder msg = new StringBuilder();

        for (int i=0;i<results.length;i++) {
            msg.append(cart.get(i).toString()).append(" - ");
            switch(results[i]) {
                case 0: msg.append(buy_success);
                    total_final = total_final.add(cart.get(i).getPrice());
                    break;
                case 1: msg.append(buy_out_of_stock);
                    break;
                case 2: msg.append(buy_invalid);
            }
            msg.append("\n");
        }

        msg.append("Total charged: ").append(total_final.toString());
        cart.clear();
        JOptionPane.showMessageDialog(pnl_main, msg.toString());
        populateResults(store.list());
        refreshCart();
    }

    private void refreshCart() {
        lst_cart.setListData(cart.toArray(new Book[cart.size()]));

        BigDecimal total = new BigDecimal(0);
        for (Book book: cart) {
            total = total.add(book.getPrice());
        }
        lbl_total.setText("Total: " + total.toString());
    }

    private class searchListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String title = txt_title.getText();
            String author = txt_author.getText();
            Book[] results;

            if (title.length() == 0 && author.length() == 0) {
                results = store.list("");
            } else {
                JSONObject query = new JSONObject();
                query.put("title", txt_title.getText());
                query.put("author", txt_author.getText());
                query.put("partial", !chk_partial.isSelected());
                results = store.list(query.toJSONString());
            }

            populateResults(results);
        }
    }

    private class addListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (!lst_result.isSelectionEmpty()) {
                Book selectedBook = (Book) lst_result.getSelectedValue();
                addToCart(selectedBook);
            }
        }
    }

    private class removeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (!lst_cart.isSelectionEmpty()) {
                Book selectedBook = (Book) lst_cart.getSelectedValue();
                removeFromCart(selectedBook);
            }
        }
    }

    private class buyListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (cart.size() > 0) {
                buy();
            }
        }
    }
}
