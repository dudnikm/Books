/**
 * Project: A00123456Lab8
 * File: CustomerDao.java
 */

package dm.book.data;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dm.book.ApplicationException;
import dm.book.db.Dao;
import dm.book.db.Database;
import dm.book.db.DbConstants;
import dm.book.io.CustomerReader;

/**
 * @author Maksim Dudnik
 * @version 1.0
 *
 */
public class CustomerDao extends Dao {

	public static final String TABLE_NAME = DbConstants.TABLE_ROOT + "Customers";

	private static final String CUSTOMERS_DATA_FILENAME = "customers.dat";
	private static Logger LOG = LogManager.getLogger();

	public CustomerDao(Database database) throws ApplicationException {
		super(database, TABLE_NAME);
		load();
	}

	/**
	 * Loads customers into db
	 * @throws ApplicationException
	 * @throws SQLException
	 */
	public void load() throws ApplicationException {
		File customerDataFile = new File(CUSTOMERS_DATA_FILENAME);
		try {
			if (!Database.tableExists(CustomerDao.TABLE_NAME) || Database.dbTableDropRequested()) {
				if (Database.tableExists(CustomerDao.TABLE_NAME) && Database.dbTableDropRequested()) {
					drop();
				}

				create();

				LOG.debug("Inserting the customers");

				if (!customerDataFile.exists()) {
					throw new ApplicationException(String.format("Required '%s' is missing.", CUSTOMERS_DATA_FILENAME));
				}

				CustomerReader.read(customerDataFile, this);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ApplicationException(e);
		}
	}

	@Override
	public void create() throws SQLException {
		LOG.debug("Creating database table " + TABLE_NAME);

		// With MS SQL Server, JOINED_DATE needs to be a DATETIME type.
		String sqlString = String.format("CREATE TABLE %s(" //
				+ "%s BIGINT, " // ID
				+ "%s VARCHAR(%d), " // FIRST_NAME
				+ "%s VARCHAR(%d), " // LAST_NAME
				+ "%s VARCHAR(%d), " // STREET
				+ "%s VARCHAR(%d), " // CITY
				+ "%s VARCHAR(%d), " // POSTAL_CODE
				+ "%s VARCHAR(%d), " // PHONE
				+ "%s VARCHAR(%d), " // EMAIL_ADDRESS
				+ "%s DATETIME, " // JOINED_DATE
				+ "PRIMARY KEY (%s))", // ID
				TABLE_NAME, //
				Column.ID.name, //
				Column.FIRST_NAME.name, Column.FIRST_NAME.length, //
				Column.LAST_NAME.name, Column.LAST_NAME.length, //
				Column.STREET.name, Column.STREET.length, //
				Column.CITY.name, Column.CITY.length, //
				Column.POSTAL_CODE.name, Column.POSTAL_CODE.length, //
				Column.PHONE.name, Column.PHONE.length, //
				Column.EMAIL_ADDRESS.name, Column.EMAIL_ADDRESS.length, //
				Column.JOINED_DATE.name, //
				Column.ID.name);

		super.create(sqlString);
	}

	public void add(Customer customer) throws SQLException {
		String sqlString = String.format("INSERT INTO %s values(?, ?, ?, ?, ?, ?, ?, ?, ?)", TABLE_NAME);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM uuuu");
		  String date = customer.getJoinedDate().format(formatter);
		boolean result = execute(sqlString, //
				customer.getId(), //
				customer.getFirstName(), //
				customer.getLastName(), //
				customer.getStreet(), //
				customer.getCity(), //
				customer.getPostalCode(), //
				customer.getPhone(), //
				customer.getEmailAddress(), //
				date);
		LOG.debug(String.format("Adding %s was %s", customer, result ? "unsuccessful" : "successful"));
	}

	/**
	 * Update the customer.
	 * 
	 * @param customer
	 * @throws SQLException
	 */
	public void update(Customer customer) throws SQLException {
		String sqlString = String.format("UPDATE %s SET %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=? WHERE %s=?", TABLE_NAME, //
				Column.FIRST_NAME.name, //
				Column.LAST_NAME.name, //
				Column.STREET.name, //
				Column.CITY.name, //
				Column.POSTAL_CODE.name, //
				Column.PHONE.name, //
				Column.EMAIL_ADDRESS.name, //
				Column.JOINED_DATE.name, //
				Column.ID.name);
		LOG.debug("Update statment: " + sqlString);
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM uuuu");
		  String date = customer.getJoinedDate().format(formatter);

		  boolean result = execute(sqlString, customer.getFirstName(), customer.getLastName(), customer.getStreet(),
				customer.getCity(), customer.getPostalCode(), customer.getPhone(), customer.getEmailAddress(), date, customer.getId());
		LOG.debug(String.format("Updating %s was %s", customer, result ? "unsuccessful" : "successful"));
	}

	/**
	 * Delete the customer from the database.
	 * 
	 * @param customer
	 * @throws SQLException
	 */
	public void delete(Customer customer) throws SQLException {
		Connection connection;
		Statement statement = null;
		try {
			connection = Database.getConnection();
			statement = connection.createStatement();

			String sqlString = String.format("DELETE FROM %s WHERE %s='%s'", TABLE_NAME, Column.ID.name, customer.getId());
			LOG.debug(sqlString);
			int rowcount = statement.executeUpdate(sqlString);
			LOG.debug(String.format("Deleted %d rows", rowcount));
		} finally {
			close(statement);
		}
	}

	/**
	 * Retrieve all the customer IDs from the database
	 * 
	 * @return the list of customer IDs
	 * @throws SQLException
	 */
	public List<Long> getCustomerIds() throws SQLException {
		List<Long> ids = new ArrayList<>();

		String selectString = String.format("SELECT %s FROM %s", Column.ID.name, TABLE_NAME);
		LOG.debug(selectString);

		Statement statement = null;
		ResultSet resultSet = null;
		try {
			Connection connection = Database.getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(selectString);

			while (resultSet.next()) {
				ids.add(resultSet.getLong(Column.ID.name));
			}

		} finally {
			close(statement);
		}

		LOG.debug(String.format("Loaded %d customer IDs from the database", ids.size()));

		return ids;
	}

	/**
	 * @param customerId
	 * @return
	 * @throws Exception
	 */
	public Customer getCustomer(Long customerId) throws Exception {
		String sqlString = String.format("SELECT * FROM %s WHERE %s = %d", TABLE_NAME, Column.ID.name, customerId);
		LOG.debug(sqlString);

		Statement statement = null;
		ResultSet resultSet = null;
		try {
			Connection connection = Database.getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlString);

			int count = 0;
			while (resultSet.next()) {
				count++;
				if (count > 1) {
					throw new ApplicationException(String.format("Expected one result, got %d", count));
				}

				Timestamp timestamp = resultSet.getTimestamp(Column.JOINED_DATE.name);
				LocalDate date = timestamp.toLocalDateTime().toLocalDate();

				Customer customer = new Customer.Builder(resultSet.getInt(Column.ID.name), resultSet.getString(Column.PHONE.name)) //
						.setFirstName(resultSet.getString(Column.FIRST_NAME.name)) //
						.setLastName(resultSet.getString(Column.LAST_NAME.name)) //
						.setStreet(resultSet.getString(Column.STREET.name)) //
						.setCity(resultSet.getString(Column.CITY.name)) //
						.setPostalCode(resultSet.getString(Column.POSTAL_CODE.name)) //
						.setEmailAddress(resultSet.getString(Column.EMAIL_ADDRESS.name)) //
						.setJoinedDate(date).build();

				return customer;
			}
		} finally {
			close(statement);
		}

		return null;
	}

	public int countAllCustomers() throws Exception {
		Statement statement = null;
		int count = 0;
		try {
			Connection connection = Database.getConnection();
			statement = connection.createStatement();
			// Execute a statement
			String sqlString = String.format("SELECT COUNT(*) AS total FROM %s", tableName);
			ResultSet resultSet = statement.executeQuery(sqlString);
			if (resultSet.next()) {
				count = resultSet.getInt("total");
			}
		} finally {
			close(statement);
		}
		return count;
	}

	public enum Column {
		ID("id", 16), //
		FIRST_NAME("firstName", 50), //
		LAST_NAME("lastName", 50), //
		STREET("street", 50), //
		CITY("city", 50), //
		POSTAL_CODE("postalCode", 10), //
		PHONE("phone", 20), //
		EMAIL_ADDRESS("emailAddress", 50), //
		JOINED_DATE("joinedDate", 20); //

		String name;
		int length;

		private Column(String name, int length) {
			this.name = name;
			this.length = length;
		}

	}

}
