/**
 * Project: Books
 * File: PurchasesReport.java
 */

package dm.book.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dm.book.data.Book;
import dm.book.data.Customer;
import dm.book.data.CustomerDao;
import dm.book.data.Purchase;
import dm.book.data.PurchaseDao;
import dm.book.data.util.Common;

/**
 * @author Maksim Dudnik
 * @version 1.0
 *
 */
public class PurchasesReport {

    public static final String REPORT_FILENAME = "purchases_report.txt";
    public static final String HORIZONTAL_LINE = "-------------------------------------------------------------------------------------------------------------------";
    public static final String HEADER_FORMAT = "%-24s %-80s %-8s";
    public static final String ROW_FORMAT = "%-24s %-80s $%.2f";

    private static final Logger LOG = LogManager.getLogger();

    private static List<Item> items;

    /**
     * Print the report.
     *
     * @param purchases
     *                  List of purchases
     * @param books
     *                  List of books
     */
    public static ArrayList<String> print(ArrayList<Purchase> purchases, ArrayList<Book> books, boolean byTitle, boolean byLastName, boolean descending, CustomerDao customerDao) {
        LOG.debug("Printing the Purchases Report");

        ArrayList<String> text = new ArrayList<String>();
        text.add(String.format(HEADER_FORMAT, "Name", "Title", "Price"));


       // Collection<Purchase> purchases = new ArrayList<Purchase>();
       // Map<Long, Book> books = new HashMap<Long, Book>();
        Map<Long, Customer> customers = new HashMap<Long, Customer>();
        try {
			List<Long> ids= customerDao.getCustomerIds();
	        for(Long id : ids) {
	        	customers.put(id, customerDao.getCustomer(id));
	        }
	        
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}

        items = new ArrayList<>();
        for (Purchase purchase : purchases) {
            long customerId = purchase.getCustomerId();

            long bookId = purchase.getBookId();
            Book book = books.get((int)bookId);
            Customer customer = customers.get(customerId);
            float price = purchase.getPrice();
            Item item = new Item(customer.getFirstName(), customer.getLastName(), book.getTitle(), price);
            items.add(item);
        }

        if (byLastName) {
            LOG.debug("isByLastnameOptionSet = true");
            if (descending) {
                Collections.sort(items, new CompareByLastNameDescending());
            } else {
                Collections.sort(items, new CompareByLastName());
            }
        }

        if (byTitle) {
            LOG.debug("isByTitleOptionSet = true");
            if (descending) {
                Collections.sort(items, new CompareByTitleDescending());
            } else {
                Collections.sort(items, new CompareByTitle());
            }
        }

        for (Item item : items) {

            text.add(String.format(ROW_FORMAT, item.firstName + " " + item.lastName, item.title, item.price));
            LOG.trace(text);
        

        }
        return text;
    }
    
    /**
     * Print the report.
     *
     * @param purchases
     *                  List of purchases
     * @param books
     *                  List of books
     */
    public static ArrayList<String> print(ArrayList<Purchase> purchases, ArrayList<Book> books,boolean byTitle, boolean byLastName,boolean descending,CustomerDao customerDao,String custId) {
        LOG.debug("Printing the Purchases Report");

        ArrayList<String> text = new ArrayList<String>();
        text.add(String.format(HEADER_FORMAT, "Name", "Title", "Price"));

        Map<Long, Customer> customers = new HashMap<Long, Customer>();
        try {
			List<Long> ids= customerDao.getCustomerIds();
	        for(Long id : ids) {
	        	customers.put(id, customerDao.getCustomer(id));
	        }
	        
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}

        items = new ArrayList<>();
        for (Purchase purchase : purchases) {
            long customerId = purchase.getCustomerId();
            /**
            if (customerIdString != null && customerId != optionsCustomerId) {
                continue;
            }
            */
            if(custId.equals(Long.toString(customerId))) {
	            long bookId = purchase.getBookId();
	            Book book = books.get((int)bookId);
	            Customer customer = customers.get(customerId);
	            float price = purchase.getPrice();
	            Item item = new Item(customer.getFirstName(), customer.getLastName(), book.getTitle(), price);
	            items.add(item);
            }
        }

        if (byLastName) {
            LOG.debug("isByLastnameOptionSet = true");
            if (descending) {
                Collections.sort(items, new CompareByLastNameDescending());
            } else {
                Collections.sort(items, new CompareByLastName());
            }
        }

        if (byTitle) {
            LOG.debug("isByTitleOptionSet = true");
            if (descending) {
                Collections.sort(items, new CompareByTitleDescending());
            } else {
                Collections.sort(items, new CompareByTitle());
            }
        }

        for (Item item : items) {

            text.add(String.format(ROW_FORMAT, item.firstName + " " + item.lastName, item.title, item.price));
            LOG.trace(text);
        

        }
        return text;
    }
    
    public static ArrayList<Purchase> getCollection(PurchaseDao purchaseDao) {
        LOG.debug("Printing the Purchases Report");


        ArrayList<Purchase> purchases = new ArrayList<Purchase>();
        try {
			List<Long> ids= purchaseDao.getPurchaseIds();
	        for(Long id : ids) {
	        	purchases.add(purchaseDao.getPurchase(id));
	        }
	        
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}

        
        return purchases;
    }

    public static class CompareByLastName implements Comparator<Item> {
        @Override
        public int compare(Item item1, Item item2) {
            return item1.lastName.compareTo(item2.lastName);
        }
    }

    public static class CompareByLastNameDescending implements Comparator<Item> {
        @Override
        public int compare(Item item1, Item item2) {
            return item2.lastName.compareTo(item1.lastName);
        }
    }

    public static class CompareByTitle implements Comparator<Item> {
        @Override
        public int compare(Item item1, Item item2) {
            return item1.title.compareToIgnoreCase(item2.title);
        }
    }

    public static class CompareByTitleDescending implements Comparator<Item> {
        @Override
        public int compare(Item item1, Item item2) {
            return item2.title.compareToIgnoreCase(item1.title);
        }
    }

    private static class Item {
        private String firstName;
        private String lastName;
        private String title;
        private float price;

        /**
         * @param firstName
         * @param lastName
         * @param title
         * @param price
         */
        public Item(String firstName, String lastName, String title, float price) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.title = Common.truncateIfRequired(title, 80);
            this.price = price;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "Item [firstName=" + firstName + ", lastName=" + lastName + ", title=" + title + ", price=" + price + "]";
        }

    }
}
