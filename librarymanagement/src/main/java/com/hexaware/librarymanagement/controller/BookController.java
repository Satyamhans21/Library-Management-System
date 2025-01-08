package com.hexaware.librarymanagement.controller;

import com.hexaware.librarymanagement.dto.BookDTO;
import com.hexaware.librarymanagement.exception.CRUDAPIException;
import com.hexaware.librarymanagement.service.IBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@RequestMapping("/api/v1/books")
@CrossOrigin("http://localhost:3000")
//@CrossOrigin("http://localhost:3000")

public class BookController {

    @Autowired
    private IBookService bookService;


    @GetMapping
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        // Fetch all books as a list of BookDTO
        List<BookDTO> books = bookService.getAllBooks();
        if (books.isEmpty()) {
            throw new CRUDAPIException(HttpStatus.NOT_FOUND, "No books found in the system.");
        }
        return ResponseEntity.ok(books);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @PostMapping("/add")
    public ResponseEntity<BookDTO> addBook(@RequestBody BookDTO bookDTO) {
        if (bookDTO.getTitle() == null || bookDTO.getTitle().trim().isEmpty()) {
            throw new CRUDAPIException(HttpStatus.BAD_REQUEST, "Title is required. Please provide a valid title.");
        }
        // Add the book and return the saved BookDTO
        BookDTO savedBook = bookService.addBook(bookDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
    }



    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @GetMapping("/{bookId}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable int bookId) {
        if (bookId <= 0) {
            throw new CRUDAPIException(HttpStatus.BAD_REQUEST, "Invalid Book ID. ID must be greater than 0.");
        }
        // Fetch the book by ID and return the corresponding BookDTO
        BookDTO bookDTO = bookService.getBookById(bookId);
        return ResponseEntity.ok(bookDTO);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @GetMapping("/search/title")
    public ResponseEntity<List<BookDTO>> searchBooksByTitle(@RequestParam String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new CRUDAPIException(HttpStatus.BAD_REQUEST, "Search title is required. Please provide a valid title.");
        }
        // Fetch books by title and return as a list of BookDTO
        List<BookDTO> books = bookService.searchBooksByTitle(title);
        return ResponseEntity.ok(books);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @GetMapping("/genre")
    public ResponseEntity<List<BookDTO>> getBooksByGenre(@RequestParam String genre) {
        if (genre == null || genre.trim().isEmpty()) {
            throw new CRUDAPIException(HttpStatus.BAD_REQUEST, "Genre is required. Please provide a valid genre.");
        }
        // Fetch books by genre and return as a list of BookDTO
        List<BookDTO> books = bookService.getBooksByGenre(genre);
        return ResponseEntity.ok(books);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @GetMapping("/author")
    public ResponseEntity<List<BookDTO>> getBooksByAuthor(@RequestParam String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new CRUDAPIException(HttpStatus.BAD_REQUEST, "Author is required. Please provide a valid author.");
        }
        // Fetch books by author and return as a list of BookDTO
        List<BookDTO> books = bookService.getBooksByAuthor(author);
        return ResponseEntity.ok(books);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @PutMapping("/update/{bookId}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable int bookId, @RequestBody BookDTO bookDTO) {
        if (bookId <= 0) {
            throw new CRUDAPIException(HttpStatus.BAD_REQUEST, "Invalid Book ID. ID must be greater than 0.");
        }
        if (bookDTO.getTitle() == null || bookDTO.getTitle().trim().isEmpty()) {
            throw new CRUDAPIException(HttpStatus.BAD_REQUEST, "Title is required. Please provide a valid title.");
        }
        // Update the book and return the updated BookDTO
        BookDTO updatedBook = bookService.updateBook(bookId, bookDTO);
        return ResponseEntity.ok(updatedBook);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @DeleteMapping("/delete/{bookId}")
    public ResponseEntity<String> deleteBook(@PathVariable int bookId) {
        if (bookId <= 0) {
            throw new CRUDAPIException(HttpStatus.BAD_REQUEST, "Invalid Book ID. ID must be greater than 0.");
        }
        // Delete the book by ID
        bookService.deleteBook(bookId);
        return ResponseEntity.ok("Book with ID " + bookId + " has been deleted successfully.");
    }
}
