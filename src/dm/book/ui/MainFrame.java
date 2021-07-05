package dm.book.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;

import dm.book.data.Book;
import dm.book.data.BookDao;
import dm.book.data.Customer;
import dm.book.data.CustomerDao;
import dm.book.data.Purchase;
import dm.book.data.PurchaseDao;
import dm.book.io.BooksReport;
import dm.book.io.CustomersReport;
import dm.book.io.PurchasesReport;
import net.miginfocom.swing.MigLayout;

/**
 * @author Maksim Dudnik
 * @version 1.0
 *
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame {

    private static final String LOG4J_CONFIG_FILENAME = "log4j2.xml";
    private ArrayList<Book> books;
    private ArrayList<Purchase> purchases;
	static {
        configureLogging();
    }
    private static final Logger LOG = LogManager.getLogger();
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
	private JPanel contentPane;

	/**
	 * Create the frame.
	 */
	public MainFrame(CustomerDao customerDao, PurchaseDao purchaseDao, BookDao bookDao) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[]", "[]"));
		setLocationRelativeTo(null);
		setTitle("Bookstore Application");

		JMenuBar mainMenuBar = new JMenuBar();
		setJMenuBar(mainMenuBar);
		books = BooksReport.getCollection(bookDao);
		purchases = PurchasesReport.getCollection(purchaseDao);

		// JMenu object
		JMenu fileMenu = new JMenu("File");
		JMenu bookMenu = new JMenu("Books");
		JMenu customerMenu = new JMenu("Customers");
		JMenu purchaseMenu = new JMenu("Purchases");
		JMenu helpMenu = new JMenu("Help");
		// File menu options
		JMenuItem drop = new JMenuItem("Drop");
		JMenuItem quit = new JMenuItem("Quit");
		// Book menu options
		JMenuItem bookCount = new JMenuItem("Count");
		JCheckBoxMenuItem bookByAuthor = new JCheckBoxMenuItem("By Author");
		JCheckBoxMenuItem bookDescending = new JCheckBoxMenuItem("Descending");
		JMenuItem bookList = new JMenuItem("List");
		// Customer menu options
		JMenuItem customerCount = new JMenuItem("Count");
		JCheckBoxMenuItem bookByJoinDate = new JCheckBoxMenuItem("By Join Date");
		JMenuItem customerList = new JMenuItem("List");
		// Purchase menu options
		JMenuItem purchaseTotal = new JMenuItem("Total");
		JCheckBoxMenuItem purchaseByLastName = new JCheckBoxMenuItem("By Last Name");
		JCheckBoxMenuItem purchaseByTitle = new JCheckBoxMenuItem("By Title");
		JCheckBoxMenuItem purchaseDescending = new JCheckBoxMenuItem("Descending");
		JMenuItem purchaseByCustomerId = new JMenuItem("Filter by Customer ID");
		JMenuItem purchaseList = new JMenuItem("List");
		// Help menu options
		JMenuItem about = new JMenuItem("About");

		fileMenu.add(drop);
		fileMenu.add(quit);

		bookMenu.add(bookCount);
		bookMenu.add(bookByAuthor);
		bookMenu.add(bookDescending);
		bookMenu.add(bookList);

		customerMenu.add(customerCount);
		customerMenu.add(bookByJoinDate);
		customerMenu.add(customerList);

		purchaseMenu.add(purchaseTotal);
		purchaseMenu.add(purchaseByLastName);
		purchaseMenu.add(purchaseByTitle);
		purchaseMenu.add(purchaseDescending);
		purchaseMenu.add(purchaseByCustomerId);
		purchaseMenu.add(purchaseList);

		helpMenu.add(about);

		mainMenuBar.add(fileMenu);
		mainMenuBar.add(bookMenu);
		mainMenuBar.add(customerMenu);
		mainMenuBar.add(purchaseMenu);
		mainMenuBar.add(helpMenu);

		// File menu actions
		drop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int input = JOptionPane.showConfirmDialog(null, "Do you want to drop all tables?");
				if (input == 0) {
					try {
						customerDao.drop();
						bookDao.drop();
						purchaseDao.drop();
					} catch (SQLException e1) {
						LOG.error(e1.getMessage());
					}
					System.exit(0);
				}
			}
		});
		quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		// Book menu actions
		bookCount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					int count = bookDao.countAllBooks();
					JOptionPane.showMessageDialog(MainFrame.this, "There are " + count + " books in the database",
							"The total book count", JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e1) {
					LOG.error(e1.getMessage());
				}
			}
		});
		bookList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> booksList = BooksReport.print(books,bookByAuthor.isSelected(), bookDescending.isSelected());
				DefaultListModel<String> model = new DefaultListModel<>();
				JList<String> list = new JList<>(model);
				list.setFont(new Font("monospaced",Font.PLAIN,14));

				for (String book : booksList) {
					model.addElement(book);
				}

				JScrollPane scrollPane = new JScrollPane(list);
				scrollPane.setPreferredSize(new Dimension(1200, 700));

				JOptionPane.showMessageDialog(MainFrame.this, scrollPane, "Books List",
						JOptionPane.INFORMATION_MESSAGE);
			}

		});
		// Customer menu options
		customerCount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					int count = customerDao.countAllCustomers();
					JOptionPane.showMessageDialog(MainFrame.this, "There are " + count + " customers in the database",
							"The total customer count", JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e1) {
					LOG.error(e1.getMessage());
				}
			}
		});
		customerList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Collection<Customer> customers = CustomersReport.getCollection(customerDao);
				ArrayList<String> customersList = CustomersReport.print(customers,bookByJoinDate.isSelected());
				DefaultListModel<String> model = new DefaultListModel<>();
				JList<String> list = new JList<>(model);
				list.setFont(new Font("monospaced",Font.PLAIN,14));

				list.addMouseListener(new MouseAdapter() {
					@SuppressWarnings("rawtypes")
					public void mouseClicked(MouseEvent evt) {
						JList list = (JList) evt.getSource();
						if (evt.getClickCount() == 2) {

							// Double-click detected
							int index = list.locationToIndex(evt.getPoint());
							createDialog((Customer) customers.toArray()[index - 1], customerDao);
						}
					}
				});

				for (String customer : customersList) {
					model.addElement(customer);
				}

				JScrollPane scrollPane = new JScrollPane(list);
				scrollPane.setPreferredSize(new Dimension(1100, 700));

				try {
					CustomerList dialog = new CustomerList(scrollPane);
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
				} catch (Exception e1) {
					LOG.error(e1.getMessage());
				}
			}

		});
		// Purchase menu options
		purchaseTotal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					int count = purchaseDao.countAllPurchases();
					JOptionPane.showMessageDialog(MainFrame.this, "There are " + count + " purchases in the database",
							"The total purchase count", JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e1) {
					LOG.error(e1.getMessage());
				}
			}
		});
		purchaseList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> purchasesList = PurchasesReport.print(purchases,books,purchaseByTitle.isSelected(),
						purchaseByLastName.isSelected(), purchaseDescending.isSelected(), customerDao);
				DefaultListModel<String> model = new DefaultListModel<>();
				JList<String> list = new JList<>(model);
				list.setFont(new Font("monospaced",Font.PLAIN,14));

				for (String purchase : purchasesList) {
					model.addElement(purchase);
				}

				JScrollPane scrollPane = new JScrollPane(list);
				scrollPane.setPreferredSize(new Dimension(1000, 700));

				JOptionPane.showMessageDialog(MainFrame.this, scrollPane, "Books List",
						JOptionPane.INFORMATION_MESSAGE);
			}

		});
		purchaseByCustomerId.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String id = JOptionPane.showInputDialog(MainFrame.this, "Customer ID:");
				ArrayList<String> purchasesList = PurchasesReport.print(purchases,books,purchaseByTitle.isSelected(),
						purchaseByLastName.isSelected(), purchaseDescending.isSelected(),customerDao, id);
				DefaultListModel<String> model = new DefaultListModel<>();
				JList<String> list = new JList<>(model);
				list.setFont(new Font("monospaced",Font.PLAIN,14));

				for (String purchase : purchasesList) {
					model.addElement(purchase);
				}

				JScrollPane scrollPane = new JScrollPane(list);
				scrollPane.setPreferredSize(new Dimension(1100, 700));

				JOptionPane.showMessageDialog(MainFrame.this, scrollPane, "Books List",
						JOptionPane.INFORMATION_MESSAGE);
			}

		});

		about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		about.setMnemonic(KeyEvent.VK_F1);
		about.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(MainFrame.this, "Bookstore application \n By Maksim Dudnik",
						"About app", JOptionPane.INFORMATION_MESSAGE);
			}
		});

	}

	public void createDialog(Customer customer, CustomerDao customerDao) {
		try {
			CustomerDialog dialog = new CustomerDialog(customer, customerDao);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}

}
