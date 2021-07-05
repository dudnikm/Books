package dm.book;

import java.awt.EventQueue;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

import dm.book.data.AllData;
import dm.book.data.BookDao;
import dm.book.data.CustomerDao;
import dm.book.data.PurchaseDao;
import dm.book.db.Database;
import dm.book.ui.MainFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;

/**
 * Project: Books
 * File: BookStore.java
 */

/**
 * @author Maksim Dudnik
 * @version 1.0
 *
 */
public class BookStore {

	private Database db;
	private CustomerDao customerDao;
	private BookDao bookDao;
	private PurchaseDao purchaseDao;
    private static final String LOG4J_CONFIG_FILENAME = "log4j2.xml";
	private static final String DB_PROPERTIES_FILENAME = "db.properties";
    static {
        configureLogging();
    }
    private static final Logger LOG = LogManager.getLogger();


    /**
     * Entry point to GIS
     * 
     * @param args
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void main(String[] args) {
        LOG.info("Starting Books");
        Instant startTime = Instant.now();
        LOG.info(startTime);
        // start the Bookstore System
        try {
        	BookStore bookStore = new BookStore();

            AllData.loadData();

            bookStore.run();
            bookStore.createFrame();
        } catch (ApplicationException | FileNotFoundException e) {
            // e.printStackTrace();
            LOG.debug(e.getMessage());
        }

        Instant endTime = Instant.now();
        LOG.info(endTime);
        LOG.info(String.format("Duration: %d ms", Duration.between(startTime, endTime).toMillis()));
        LOG.info("Books has stopped");
    }

    /**
     * Configures log4j2 from the external configuration file specified in LOG4J_CONFIG_FILENAME.
     * If the configuration file isn't found then log4j2's DefaultConfiguration is used.
     */
    private static void configureLogging() {
        ConfigurationSource source;
        try {
            source = new ConfigurationSource(new FileInputStream(LOG4J_CONFIG_FILENAME));
            Configurator.initialize(null, source);
        } catch (IOException e) {
            System.out.println(String.format("WARNING! Can't find the log4j logging configuration file %s; using DefaultConfiguration for logging.",
                    LOG4J_CONFIG_FILENAME));
            Configurator.initialize(new DefaultConfiguration());
        }
    }

    /**
     * @throws ApplicationException
     * @throws FileNotFoundException
     * 
     */
    private void run() throws ApplicationException, FileNotFoundException {
        LOG.debug("run()");
        
        try {
        	
			db = connect();
			customerDao = loadCustomers(db);
			bookDao = loadBooks(db);
			purchaseDao = loadPurchases(db);

		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
    }
    
    private CustomerDao loadCustomers(Database db) throws ApplicationException {
		CustomerDao customerDao = new CustomerDao(db);
		return customerDao;
	}
    
    private BookDao loadBooks(Database db) throws ApplicationException {
    	BookDao bookDao = new BookDao(db);
		return bookDao;
	}
    
    private PurchaseDao loadPurchases(Database db) throws ApplicationException {
    	PurchaseDao purchaseDao = new PurchaseDao(db);
		return purchaseDao;
	}

	private Database connect() throws IOException, SQLException, ApplicationException {
		Properties dbProperties = new Properties();
		dbProperties.load(new FileInputStream(DB_PROPERTIES_FILENAME));
		Database db = new Database(dbProperties);

		return db;
	}
	
	private void createFrame() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame(customerDao,purchaseDao,bookDao);
					frame.setVisible(true);
				} catch (Exception e) {
					LOG.error(e.getMessage());
				}
			}
		});
	}
    
}
