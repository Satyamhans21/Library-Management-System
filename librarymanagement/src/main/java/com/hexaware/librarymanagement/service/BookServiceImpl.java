package com.hexaware.librarymanagement.service;

import com.hexaware.librarymanagement.dto.BookDTO;
import com.hexaware.librarymanagement.entity.Book;
import com.hexaware.librarymanagement.exception.CRUDAPIException;
import com.hexaware.librarymanagement.mapper.BookMapper;
import com.hexaware.librarymanagement.repository.BookRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements IBookService {

    // Autowired BookRepository for interacting with the book data in the database
    @Autowired
    private BookRepository bookRepository;

    @Override
    public BookDTO addBook(BookDTO bookDTO) {
        // Validate if the book title is not null or empty
        if (bookDTO.getTitle() == null || bookDTO.getTitle().trim().isEmpty()) {
            throw new CRUDAPIException(HttpStatus.BAD_REQUEST, "Invalid Book Data", "Book title cannot be null or empty.");
        }
        // Map the BookDTO to a Book entity and save it to the database
        Book book = BookMapper.mapToBook(bookDTO);
        Book savedBook = bookRepository.save(book);

        // Return the saved Book entity as a BookDTO
        return BookMapper.mapToBookDTO(savedBook);
    }

    @Override
    public List<BookDTO> searchBooksByTitle(String title) {
        // Validate the search query for title
        if (title == null || title.trim().isEmpty()) {
            throw new CRUDAPIException(HttpStatus.BAD_REQUEST, "Invalid Search Query", "Search title cannot be null or empty.");
        }
        // Find books containing the title and map to DTOs
        List<Book> books = bookRepository.findByTitleContaining(title);
        return books.stream()
                .map(BookMapper::mapToBookDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookDTO> getBooksByGenre(String genre) {
        // Validate if the genre is not null or empty
        if (genre == null || genre.trim().isEmpty()) {
            throw new CRUDAPIException(HttpStatus.BAD_REQUEST, "Invalid Genre", "Genre cannot be null or empty.");
        }
        // Find books by genre and map to DTOs
        List<Book> books = bookRepository.findByGenre(genre);
        return books.stream()
                .map(BookMapper::mapToBookDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookDTO> getBooksByAuthor(String author) {
        // Validate if the author is not null or empty
        if (author == null || author.trim().isEmpty()) {
            throw new CRUDAPIException(HttpStatus.BAD_REQUEST, "Invalid Author", "Author cannot be null or empty.");
        }
        // Find books by author and map to DTOs
        List<Book> books = bookRepository.findByAuthor(author);
        return books.stream()
                .map(BookMapper::mapToBookDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookDTO> getAllBooks() {
        // Retrieve all books and map them to DTOs
        List<Book> books = bookRepository.findAll();
        // If no books are found, throw an exception
        if (books.isEmpty()) {
            throw new CRUDAPIException(HttpStatus.NOT_FOUND, "No Books Found", "There are no books available in the system.");
        }
        return books.stream()
                .map(BookMapper::mapToBookDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookDTO getBookById(int bookId) {
        // Validate if the book ID is greater than 0
        if (bookId <= 0) {
            throw new CRUDAPIException(HttpStatus.BAD_REQUEST, "Invalid Book ID", "Book ID must be greater than 0.");
        }
        // Find the book by its ID, or throw an exception if not found
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CRUDAPIException(HttpStatus.NOT_FOUND, "Book Not Found", "No book found with ID: " + bookId));
        return BookMapper.mapToBookDTO(book);
    }

    @Transactional
    @Override
    public BookDTO updateBook(int bookId, BookDTO bookDTO) {
        // Validate the book ID and title
        if (bookId <= 0) {
            throw new CRUDAPIException(HttpStatus.BAD_REQUEST, "Invalid Book ID", "Book ID must be greater than 0.");
        }
        if (bookDTO.getTitle() == null || bookDTO.getTitle().trim().isEmpty()) {
            throw new CRUDAPIException(HttpStatus.BAD_REQUEST, "Invalid Book Data", "Book title cannot be null or empty.");
        }

        // Fetch the existing book from the repository
        Book existingBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new CRUDAPIException(HttpStatus.NOT_FOUND, "Book Not Found", "No book found with ID: " + bookId));

        // Update the book's properties with the new data from the DTO
        existingBook.setTitle(bookDTO.getTitle());
        existingBook.setAuthor(bookDTO.getAuthor());
        existingBook.setIsbn(bookDTO.getIsbn());
        existingBook.setPublisher(bookDTO.getPublisher());
        existingBook.setPublicationDate(bookDTO.getPublicationDate());
        existingBook.setEdition(bookDTO.getEdition());
        existingBook.setGenre(bookDTO.getGenre());
        existingBook.setDescription(bookDTO.getDescription());
        existingBook.setLanguage(bookDTO.getLanguage());
        existingBook.setNumberOfPages(bookDTO.getNumberOfPages());
        existingBook.setCost(bookDTO.getCost());
        existingBook.setAvailable(bookDTO.isAvailable());

        // Save the updated book and return the updated DTO
        Book updatedBook = bookRepository.save(existingBook);
        return BookMapper.mapToBookDTO(updatedBook);
    }

    @Override
    public void deleteBook(int bookId) {
        // Validate if the book ID is valid
        if (bookId <= 0) {
            throw new CRUDAPIException(HttpStatus.BAD_REQUEST, "Invalid Book ID", "Book ID must be greater than 0.");
        }

        // Check if the book exists before attempting to delete
        Book existingBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new CRUDAPIException(HttpStatus.NOT_FOUND, "Book Not Found", "No book found with ID: " + bookId));

        // Delete the book from the repository
        bookRepository.delete(existingBook);
    }
}
