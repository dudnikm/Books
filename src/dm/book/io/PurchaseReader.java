/**
 * Project: Books
 * File: PurchaseReader.java
 */

package dm.book.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dm.book.data.AllData;
import dm.book.data.Purchase;
import dm.book.data.PurchaseDao;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dm.book.ApplicationException;

/**
 * @author Maksim Dudnik
 * @version 1.0
 *
 */
public class PurchaseReader extends Reader {

    public static final String FILENAME = "purchases.csv";

    private static final Logger LOG = LogManager.getLogger();
    private static Set<Long> customerIds = AllData.getCustomers().keySet();
    private static Long[] customerIdArray = customerIds.toArray(new Long[0]);
    private static int customerIdCount = customerIdArray.length;
    private static Set<Long> bookIds = AllData.getBooks().keySet();
    private static Long[] bookIdArray = bookIds.toArray(new Long[0]);
    private static int bookIdCount = bookIdArray.length;

    /**
     * private constructor to prevent instantiation
     */
    private PurchaseReader() {
    }

    /**
     * Read the inventory input data.
     * 
     * @return the inventory.
     * @throws ApplicationException
     */
    public static Map<Long, Purchase> read(File purchaseDataFile, PurchaseDao dao) throws ApplicationException {
        File file = purchaseDataFile;
        FileReader in;
        Iterable<CSVRecord> records;
        try {
            in = new FileReader(file);
            records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }

        Map<Long, Purchase> purchases = new HashMap<>();
        LOG.debug("Reading" + file.getAbsolutePath());
        for (CSVRecord record : records) {
            long id = Long.parseLong(record.get("id"));
            long customerId = Long.parseLong(record.get("customer_id"));
            long bookId = Long.parseLong(record.get("book_id"));
            float price = Float.parseFloat(record.get("price"));

            Purchase purchase = new Purchase.Builder(id, customerId, bookId, price).build();
            try {
				dao.add(purchase);
			} catch (SQLException e) {
				throw new ApplicationException(e);
			}
            LOG.debug("Added " + purchase.toString() + " as " + purchase.getId());

            if (!customerIds.contains(customerId)) {
                customerId = customerIdArray[(int) (Math.random() * customerIdCount)];
            }
            if (!bookIds.contains(bookId)) {
                bookId = bookIdArray[(int) (Math.random() * bookIdCount)];
            }
        }

        return purchases;
    }
    
    public static Map<Long, Purchase> read() throws ApplicationException {
        File file = new File(FILENAME);
        FileReader in;
        Iterable<CSVRecord> records;
        try {
            in = new FileReader(file);
            records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }

        Map<Long, Purchase> purchases = new HashMap<>();
        LOG.debug("Reading" + file.getAbsolutePath());
        for (CSVRecord record : records) {
            long id = Long.parseLong(record.get("id"));
            long customerId = Long.parseLong(record.get("customer_id"));
            long bookId = Long.parseLong(record.get("book_id"));
            float price = Float.parseFloat(record.get("price"));

            Purchase purchase = new Purchase.Builder(id, customerId, bookId, price).build();
            purchases.put(purchase.getId(), purchase);
            LOG.debug("Added " + purchase.toString() + " as " + purchase.getId());

            if (!customerIds.contains(customerId)) {
                customerId = customerIdArray[(int) (Math.random() * customerIdCount)];
            }
            if (!bookIds.contains(bookId)) {
                bookId = bookIdArray[(int) (Math.random() * bookIdCount)];
            }
        }

        return purchases;
    }


}
