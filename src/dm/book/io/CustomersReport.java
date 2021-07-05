/**
 * Project: Books
 * File: CustomersReport.java
 */

package dm.book.io;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dm.book.data.Customer;
import dm.book.data.CustomerDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Maksim Dudnik
 * @version 1.0
 *
 */
public class CustomersReport {

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd yyyy");
    public static final String REPORT_FILENAME = "customers_report.txt";

    public static final String HORIZONTAL_LINE = "----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
    public static final String HEADER_FORMAT = "%4s. %-6s %-12s %-12s";
    public static final String ROW_FORMAT = "%4d. %06d %-12s %-12s";

    private static final Logger LOG = LogManager.getLogger();

    /**
     * Print the report.
     *
     * @param customers
     *                  Collection of customers
     * @param byJoinDate
     *                  if true, sort collection bu join date
     */
    public static ArrayList<String> print(Collection<Customer> customers, boolean byJoinDate) {
        LOG.debug("Printing the Customers Report");
        ArrayList<String> text = new ArrayList<String>();
        text.add(String.format(HEADER_FORMAT, "#", "ID", "First name", "Last name"));


        if (byJoinDate) {
            LOG.debug("isByJoinDateOptionSet = true");
            List<Customer> list = new ArrayList<>(customers);
                LOG.debug("isDescendingOptionSet = false");
                Collections.sort(list, new CompareByJoinedDate());

            customers = list;
        }

        int i = 0;
        for (Customer customer : customers) {
            text.add(String.format(ROW_FORMAT, ++i, customer.getId(), customer.getFirstName(), customer.getLastName()));
        }
        return text;
    }
    
    public static Collection<Customer> getCollection(CustomerDao customerDao) {
    	LOG.debug("Getting customers from database");
    	Collection<Customer> customers = new ArrayList<Customer>();
        try {
			List<Long> ids= customerDao.getCustomerIds();
	        for(Long id : ids) {
	        	customers.add(customerDao.getCustomer(id));
	        }
        }catch (Exception e) {
				LOG.error(e.getMessage());
			}

        return customers;
    }


    public static class CompareByJoinedDate implements Comparator<Customer> {
        @Override
        public int compare(Customer customer1, Customer customer2) {
            return customer1.getJoinedDate().compareTo(customer2.getJoinedDate());
        }
    }

    public static class CompareByJoinedDateDescending implements Comparator<Customer> {
        @Override
        public int compare(Customer customer1, Customer customer2) {
            return customer2.getJoinedDate().compareTo(customer1.getJoinedDate());
        }
    }
}
