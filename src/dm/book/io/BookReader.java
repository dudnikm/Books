/**
 * Project: Books
 * File: BookReader.java
 */

package dm.book.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import dm.book.data.Book;
import dm.book.data.BookDao;
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
public class BookReader extends Reader {

    private static final Logger LOG = LogManager.getLogger();
    public static final String FILENAME = "books500.csv";

    /**
     * private constructor to prevent instantiation
     */
    private BookReader() {
    }

    /**
     * Read the book input data.
     * 
     * @return A collection of books.
     * @throws ApplicationException
     * @throws IOException
     */
    public static Map<Long, Book> read(File bookDataFile, BookDao dao) throws ApplicationException {
        File file = bookDataFile;
        FileReader in;
        Iterable<CSVRecord> records;
        try {
            in = new FileReader(file);
            records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }

        Map<Long, Book> books = new HashMap<>();
        LOG.debug("Reading" + file.getAbsolutePath());

        
        for (CSVRecord record : records) {
            String id = record.get("book_id");
            String isbn = record.get("isbn");
            String authors = record.get("authors");
            String original_publication_year = record.get("original_publication_year");
            String original_title = record.get("original_title");
            String average_rating = record.get("average_rating");
            String ratings_count = record.get("ratings_count");
            String image_url = record.get("image_url");

            Book book = new Book.Builder(Long.parseLong(id)). //
                    setIsbn(isbn). //
                    setAuthors(authors). //
                    setYear(Integer.parseInt(original_publication_year)). //
                    setTitle(original_title). //
                    setRating(Float.parseFloat(average_rating)). // //
                    setRatingsCount(Integer.parseInt(ratings_count)). //
                    setImageUrl(image_url).//
                    build();
            try {
				dao.add(book);
			} catch (SQLException e) {
				throw new ApplicationException(e);
			}
            LOG.trace("Added " + book.toString() + " as " + id);
        }

        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                throw new ApplicationException(e);
            }
        }

        return books;
    }
    
    public static Map<Long, Book> read() throws ApplicationException {
        File file = new File(FILENAME);
        FileReader in;
        Iterable<CSVRecord> records;
        try {
            in = new FileReader(file);
            records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }

        Map<Long, Book> books = new HashMap<>();

        LOG.debug("Reading" + file.getAbsolutePath());
        for (CSVRecord record : records) {
            String id = record.get("book_id");
            String isbn = record.get("isbn");
            String authors = record.get("authors");
            String original_publication_year = record.get("original_publication_year");
            String original_title = record.get("original_title");
            String average_rating = record.get("average_rating");
            String ratings_count = record.get("ratings_count");
            String image_url = record.get("image_url");

            Book book = new Book.Builder(Long.parseLong(id)). //
                    setIsbn(isbn). //
                    setAuthors(authors). //
                    setYear(Integer.parseInt(original_publication_year)). //
                    setTitle(original_title). //
                    setRating(Float.parseFloat(average_rating)). // //
                    setRatingsCount(Integer.parseInt(ratings_count)). //
                    setImageUrl(image_url).//
                    build();

            books.put(book.getId(), book);
            LOG.trace("Added " + book.toString() + " as " + id);
        }

        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                throw new ApplicationException(e);
            }
        }

        return books;
    }
}
