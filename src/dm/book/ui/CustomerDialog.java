package dm.book.ui;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import dm.book.data.Customer;
import dm.book.data.CustomerDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;

import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * @author Maksim Dudnik
 * @version 1.0
 *
 */
@SuppressWarnings("serial")
public class CustomerDialog extends JDialog {
	private static final String LOG4J_CONFIG_FILENAME = "log4j2.xml";
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
	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField textField_4;
	private JTextField textField_5;
	private JTextField textField_6;
	private JTextField textField_7;
	private JTextField textField_8;

	/**
	 * Create the dialog.
	 */
	public CustomerDialog(Customer customer, CustomerDao dao) {
		setBounds(100, 100, 477, 331);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[][19.00][grow]", "[][][][][][][][][]"));
		setLocationRelativeTo(null);
		setTitle("Customer Report");
		
		
		{
			JLabel lblId = new JLabel("ID");
			contentPanel.add(lblId, "cell 1 0,alignx trailing");
		}
		{
			textField = new JTextField(Long.toString(customer.getId()));
			textField.setEnabled(false);
			contentPanel.add(textField, "cell 2 0,growx");
			textField.setColumns(10);
		}
		{
			JLabel lblNewLabel = new JLabel("First Name");
			contentPanel.add(lblNewLabel, "cell 1 1,alignx trailing");
		}
		{
			textField_1 = new JTextField(customer.getFirstName());
			contentPanel.add(textField_1, "cell 2 1,growx");
			textField_1.setColumns(10);
		}
		{
			JLabel lblLastName = new JLabel("Last Name");
			contentPanel.add(lblLastName, "cell 1 2,alignx trailing");
		}
		{
			textField_2 = new JTextField(customer.getLastName());
			contentPanel.add(textField_2, "cell 2 2,growx");
			textField_2.setColumns(10);
		}
		{
			JLabel lblStreet = new JLabel("Street");
			contentPanel.add(lblStreet, "cell 1 3,alignx trailing");
		}
		{
			textField_3 = new JTextField(customer.getStreet());
			contentPanel.add(textField_3, "cell 2 3,growx");
			textField_3.setColumns(10);
		}
		{
			JLabel lblCity = new JLabel("City");
			contentPanel.add(lblCity, "cell 1 4,alignx trailing");
		}
		{
			textField_4 = new JTextField(customer.getCity());
			contentPanel.add(textField_4, "cell 2 4,growx");
			textField_4.setColumns(10);
		}
		{
			JLabel lblPostalCode = new JLabel("Postal Code");
			contentPanel.add(lblPostalCode, "cell 1 5,alignx trailing");
		}
		{
			textField_5 = new JTextField(customer.getPostalCode());
			contentPanel.add(textField_5, "cell 2 5,growx");
			textField_5.setColumns(10);
		}
		{
			JLabel lblPhone = new JLabel("Phone");
			contentPanel.add(lblPhone, "cell 1 6,alignx trailing");
		}
		{
			textField_6 = new JTextField(customer.getPhone());
			contentPanel.add(textField_6, "cell 2 6,growx");
			textField_6.setColumns(10);
		}
		{
			JLabel lblEmail = new JLabel("Email");
			contentPanel.add(lblEmail, "cell 1 7,alignx trailing");
		}
		{
			textField_7 = new JTextField(customer.getEmailAddress());
			contentPanel.add(textField_7, "cell 2 7,growx");
			textField_7.setColumns(10);
		}
		{
			JLabel lblJoin = new JLabel("Joined Date");
			contentPanel.add(lblJoin, "cell 1 8,alignx trailing");
		}
		{
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM uuuu");
			textField_8 = new JTextField(customer.getJoinedDate().format(formatter));
			contentPanel.add(textField_8, "cell 2 8,growx");
			textField_8.setColumns(10);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						customer.setFirstName(textField_1.getText());
						customer.setLastName(textField_2.getText());
						customer.setStreet(textField_3.getText());
						customer.setCity(textField_4.getText());
						customer.setPostalCode(textField_5.getText());
						customer.setPhone(textField_6.getText());
						customer.setEmailAddress(textField_7.getText());
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM uuuu");
						String text = textField_8.getText();
						LocalDate parsedDate = LocalDate.parse(text, formatter);
						customer.setJoinedDate(parsedDate);
						
						try {
							dao.update(customer);
						} catch (SQLException e1) {
							LOG.error(e1.getMessage());
						}
						dispose();
					}
				});
				
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				
				buttonPane.add(cancelButton);
			}
		}
	}
	
	public Customer getCustomer(CustomerDao customerDao) {
		try {
			List<Long> ids = customerDao.getCustomerIds();
			Random rand = new Random();
			int customerIndex = rand.nextInt(ids.size());
			return customerDao.getCustomer(ids.get(customerIndex));
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		return null;
	}

}
