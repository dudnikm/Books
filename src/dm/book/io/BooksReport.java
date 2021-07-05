/**
 * Project: Books
 * File: BooksReport.java
 */

package dm.book.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dm.book.data.Book;
import dm.book.data.BookDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dm.book.data.util.Common;

/**
 * @author Maksim Dudnik
 * @version 1.0
 *
 */
public class BooksReport {

    public static final String REPORT_FILENAME = "book_report.txt";
    public static final String HORIZONTAL_LINE = "--------------------------------------------------------------------------------------------------------------------------------------------------------------------";
    public static final String HEADER_FORMAT = "%-8s %-12s %-40s %-40s %4s %-6s %-13s %-60s";
    public static final String ROW_FORMAT = "%08d %-12s %-40s %-40s %4d %6.3f %13d %-60s";
    private static final Logger LOG = LogManager.getLogger();

    /**
     * Print the report.
     * 
     * @param books
     *              Collection of books
     * @param byAuthor
     *              if true sort output by author's name
     * @param descending
     *              if true, sort in descending order
     */
    public static ArrayList<String> print(Collection<Book> books, boolean byAuthor, boolean descending) {
        LOG.debug("Printing the Books Report");
        ArrayList<String> text = new ArrayList<String>();
        text.add(String.format(HEADER_FORMAT, "ID", "ISBN", "Authors", "Title", "Year", "Rating", "Ratings Count", "Image URL"));

        if (byAuthor) {
            LOG.debug("isByAuthorOptionSet = true");
            List<Book> list = new ArrayList<>(books);
            if (descending) {
                LOG.debug("isDescendingOptionSet = true");
                Collections.sort(list, new CompareByAuthorDescending());
            } else {
                LOG.debug("isDescendingOptionSet = false");
                Collections.sort(list, new CompareByAuthor());
            }

            books = list;
        }

        for (Book book : books) {
            long id = book.getId();
            String isbn = book.getIsbn();
            String authors = Common.truncateIfRequired(book.getAuthors(), 40);
            int year = book.getYear();
            String title = Common.truncateIfRequired(book.getTitle(), 40);
            float rating = book.getRating();
            int ratingsCount = book.getRatingsCount();
            String imageUrl = Common.truncateIfRequired(book.getImageUrl(), 60);
            text.add(String.format(ROW_FORMAT, id, isbn, authors, title, year, rating, ratingsCount, imageUrl));
            LOG.trace(text);
        }
        
        return text;
    }
    
    public static ArrayList<Book> getCollection(BookDao bookDao) {
        LOG.debug("Printing the Books Report");

        ArrayList<Book> books = new ArrayList<Book>();
        try {
			List<Long> ids= bookDao.getBookIds();
	        for(Long id : ids) {
	        	books.add(bookDao.getBook(id));
	        }
	        
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
        
        return books;
    }

    public static class CompareByAuthor implements Comparator<Book> {
        @Override
        public int compare(Book book1, Book book2) {
            return book1.getAuthors().compareToIgnoreCase(book2.getAuthors());
        }
    }

    public static class CompareByAuthorDescending implements Comparator<Book> {
        @Override
        public int compare(Book book1, Book book2) {
            return -book1.getAuthors().compareToIgnoreCase(book2.getAuthors());
        }
    }

}
