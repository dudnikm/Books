package dm.book.data;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dm.book.ApplicationException;
import dm.book.db.Dao;
import dm.book.db.Database;
import dm.book.db.DbConstants;
import dm.book.io.PurchaseReader;

/**
 * @author Maksim Dudnik
 * @version 1.0
 *
 */
public class PurchaseDao extends Dao {

	public static final String TABLE_NAME = DbConstants.TABLE_ROOT + "Purchases";

	private static final String PURCHASES_DATA_FILENAME = "purchases.csv";
	private static Logger LOG = LogManager.getLogger();

	public PurchaseDao(Database database) throws ApplicationException {
		super(database, TABLE_NAME);

		load();
	}

	/**
	 *
	 * @throws ApplicationException
	 * @throws SQLException
	 */
	public void load() throws ApplicationException {
		File purchaseDataFile = new File(PURCHASES_DATA_FILENAME);
		try {
			if (!Database.tableExists(PurchaseDao.TABLE_NAME) || Database.dbTableDropRequested()) {
				if (Database.tableExists(PurchaseDao.TABLE_NAME) && Database.dbTableDropRequested()) {
					drop();
				}
			
				create();

				LOG.debug("Inserting the purchases");

				if (!purchaseDataFile.exists()) {
					throw new ApplicationException(String.format("Required '%s' is missing.", PURCHASES_DATA_FILENAME));
				}

				PurchaseReader.read(purchaseDataFile, this);
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
				+ "%s VARCHAR(%d), " // CUSTOMER_ID
				+ "%s VARCHAR(%d), " // BOOK_ID
				+ "%s VARCHAR(%d), " // PRICE
				+ "PRIMARY KEY (%s))", // ID
				TABLE_NAME, //
				Column.ID.name, //
				Column.CUSTOMER_ID.name, Column.CUSTOMER_ID.length, //
				Column.BOOK_ID.name, Column.BOOK_ID.length, //
				Column.PRICE.name, Column.PRICE.length, //
				Column.ID.name);

		super.create(sqlString);
	}

	public void add(Purchase purchase) throws SQLException {
		String sqlString = String.format("INSERT INTO %s values(?, ?, ?, ?)", TABLE_NAME);
		boolean result = execute(sqlString, //
				purchase.getId(), //
				purchase.getCustomerId(), //
				purchase.getBookId(), //
				purchase.getPrice());
		LOG.debug(String.format("Adding %s was %s", purchase, result ? "unsuccessful" : "successful"));
	}

	/**
	 * Update the purchase.
	 * 
	 * @param purchase
	 * @throws SQLException
	 */
	public void update(Purchase purchase) throws SQLException {
		String sqlString = String.format("UPDATE %s SET %s=?, %s=?, %s=?, WHERE %s=?", TABLE_NAME, //
				Column.CUSTOMER_ID.name, //
				Column.BOOK_ID.name, //
				Column.PRICE.name, //
				Column.ID.name);
		LOG.debug("Update statment: " + sqlString);

		boolean result = execute(sqlString, purchase.getId(), sqlString, purchase.getCustomerId(), purchase.getBookId(),
				purchase.getPrice());
		LOG.debug(String.format("Updating %s was %s", purchase, result ? "unsuccessful" : "successful"));
	}

	/**
	 * Delete the purchase from the database.
	 * 
	 * @param purchase
	 * @throws SQLException
	 */
	public void delete(Purchase purchase) throws SQLException {
		Connection connection;
		Statement statement = null;
		try {
			connection = Database.getConnection();
			statement = connection.createStatement();

			String sqlString = String.format("DELETE FROM %s WHERE %s='%s'", TABLE_NAME, Column.ID.name,
					purchase.getId());
			LOG.debug(sqlString);
			int rowcount = statement.executeUpdate(sqlString);
			LOG.debug(String.format("Deleted %d rows", rowcount));
		} finally {
			close(statement);
		}
	}

	/**
	 * Retrieve all the purchase IDs from the database
	 * 
	 * @return the list of purchase IDs
	 * @throws SQLException
	 */
	public List<Long> getPurchaseIds() throws SQLException {
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

		LOG.debug(String.format("Loaded %d purchase IDs from the database", ids.size()));

		return ids;
	}

	/**
	 * @param purchaseId
	 * @return
	 * @throws Exception
	 */
	public Purchase getPurchase(Long purchaseId) throws Exception {
		String sqlString = String.format("SELECT * FROM %s WHERE %s = %d", TABLE_NAME, Column.ID.name, purchaseId);
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

				Purchase purchase = new Purchase.Builder(resultSet.getLong(Column.ID.name), resultSet.getLong(Column.CUSTOMER_ID.name),
						resultSet.getLong(Column.BOOK_ID.name), resultSet.getFloat(Column.PRICE.name)).build();

				return purchase;
			}
		} finally {
			close(statement);
		}

		return null;
	}

	public int countAllPurchases() throws Exception {
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
		CUSTOMER_ID("customerId", 16), //
		BOOK_ID("bookId", 16), //
		PRICE("price", 8); //

		String name;
		int length;

		private Column(String name, int length) {
			this.name = name;
			this.length = length;
		}

	}

}
