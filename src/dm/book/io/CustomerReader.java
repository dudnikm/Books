/**
 * Project: A00123456Lab3
 * File: CustomerReader.java
 */

package dm.book.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import dm.book.data.Customer;
import dm.book.data.CustomerDao;
import dm.book.data.util.Validator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dm.book.ApplicationException;

/**
 * @author Maksim Dudnik
 * @version 1.0
 *
 */
public class CustomerReader {
	

    public static final String FILENAME = "customers.dat";

	public static final String RECORD_DELIMITER = ":";
	public static final String FIELD_DELIMITER = "\\|";
	
	private static final Logger LOG = LogManager.getLogger();

	/**
	 * private constructor to prevent instantiation
	 */
	private CustomerReader() {
	}

	/**
	 * Read the customer input data.
	 * 
	 * @param customerDataFile
	 *            The source file for customers' data.
	 * @param dao
	 * 			  Database connector class
	 * @return A list of customers.
	 * @throws ApplicationException
	 */
	public static void read(File customerDataFile, CustomerDao dao) throws ApplicationException {
		BufferedReader customerReader = null;
		try {
			customerReader = new BufferedReader(new FileReader(customerDataFile));

			String line = null;
			line = customerReader.readLine(); // skip the header line
			while ((line = customerReader.readLine()) != null) {
				Customer customer = readCustomerString(line);
				try {
					dao.add(customer);
				} catch (SQLException e) {
					throw new ApplicationException(e);
				}
			}
		} catch (IOException e) {
			throw new ApplicationException(e.getMessage());
		} finally {
			try {
				if (customerReader != null) {
					customerReader.close();
				}
			} catch (IOException e) {
				throw new ApplicationException(e.getMessage());
			}
		}
	}
	
	public static Map<Long, Customer> read() throws ApplicationException {
        File customerDataFile = new File(FILENAME);
        BufferedReader customerReader = null;
        LOG.debug("Reading" + customerDataFile.getAbsolutePath());

        Map<Long, Customer> customers = new HashMap<>();
        int i = 0;
        long customerId;
        try {
            customerReader = new BufferedReader(new FileReader(customerDataFile));

            String line = null;
            line = customerReader.readLine(); // skip the header line
            while ((line = customerReader.readLine()) != null) {
                LOG.debug("line: " + line);
                try {
                    Customer customer = readCustomerString(line);
                    customerId = customer.getId();
                    if (customers.get(customerId) != null) {
                        LOG.warn("Customer exists: " + customer);
                    }
                    customers.put(customer.getId(), customer);
                    LOG.debug("Added " + customer.toString() + " as " + customer.getId());
                } catch (ApplicationException e) {
                    LOG.error(e.getMessage());
                }
                LOG.debug("customer " + ++i);
            }
        } catch (IOException e) {
            throw new ApplicationException(e.getMessage());
        } finally {
            try {
                if (customerReader != null) {
                    customerReader.close();
                }
            } catch (IOException e) {
                throw new ApplicationException(e.getMessage());
            }
        }

        return customers;
    }

	/**
	 * Parse a Customer data string into a CUstomer object;
	 * 
	 * @param data
	 * 				String containing customer's information
	 * @throws ApplicationException
	 */
	private static Customer readCustomerString(String data) throws ApplicationException {
		String[] elements = data.split(FIELD_DELIMITER);
		if (elements.length != Customer.ATTRIBUTE_COUNT) {
			throw new ApplicationException(String.format("Expected %d but got %d: %s", Customer.ATTRIBUTE_COUNT, elements.length, Arrays.toString(elements)));
		}

		int index = 0;
		long id = Integer.parseInt(elements[index++]);
		String firstName = elements[index++];
		String lastName = elements[index++];
		String street = elements[index++];
		String city = elements[index++];
		String postalCode = elements[index++];
		String phone = elements[index++];
		// should the email validation be performed here or in the customer class? I'm leaning towards putting it here.
		String emailAddress = elements[index++];
		if (!Validator.validateEmail(emailAddress)) {
			throw new ApplicationException(String.format("Invalid email: %s", emailAddress));
		}
		String yyyymmdd = elements[index];
		if (!Validator.validateJoinedDate(yyyymmdd)) {
			throw new ApplicationException(String.format("Invalid joined date: %s for customer %d", yyyymmdd, id));
		}
		
		DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
		LocalDate parsedDate = LocalDate.parse(yyyymmdd, formatter);

		return new Customer.Builder(id, phone).//
				setFirstName(firstName).//
				setLastName(lastName).//
				setStreet(street).//
				setCity(city).//
				setPostalCode(postalCode).//
				setEmailAddress(emailAddress).//
				setJoinedDate(parsedDate).build();
	}

}
