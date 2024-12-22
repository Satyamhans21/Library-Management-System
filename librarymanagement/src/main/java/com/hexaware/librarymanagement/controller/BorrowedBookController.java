package com.hexaware.librarymanagement.controller;

import com.hexaware.librarymanagement.dto.BorrowedBookDTO;
import com.hexaware.librarymanagement.entity.Book;
import com.hexaware.librarymanagement.entity.BorrowedBook;
import com.hexaware.librarymanagement.entity.User;
import com.hexaware.librarymanagement.exception.CRUDAPIException;
import com.hexaware.librarymanagement.repository.UserRepository;
import com.hexaware.librarymanagement.repository.BorrowedBookRepository;
import com.hexaware.librarymanagement.repository.BookRepository;
import com.hexaware.librarymanagement.service.IBorrowedBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/borrowed-books")
public class BorrowedBookController {

    @Autowired
    private BorrowedBookRepository borrowedBookRepository;

    @Autowired
    private IBorrowedBookService borrowedBookService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @PostMapping("/borrow")
    public ResponseEntity<BorrowedBookDTO> borrowBook(@RequestBody BorrowedBookDTO borrowedBookDTO) {
        // Validate User
        userRepository.findById(borrowedBookDTO.getUserId())
                .orElseThrow(() -> new CRUDAPIException(HttpStatus.NOT_FOUND, "User with ID " + borrowedBookDTO.getUserId() + " not found"));

        // Validate Book
        bookRepository.findById(borrowedBookDTO.getBookId())
                .orElseThrow(() -> new CRUDAPIException(HttpStatus.NOT_FOUND, "Book with ID " + borrowedBookDTO.getBookId() + " not found"));

        if (borrowedBookDTO.getBorrowedDate() == null) {
            borrowedBookDTO.setBorrowedDate(new Date());
        }

        if (borrowedBookDTO.getFine() == 0.0) {
            borrowedBookDTO.setFine(0.0);
        }

        BorrowedBookDTO savedBorrowedBook = borrowedBookService.borrowBook(borrowedBookDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBorrowedBook);
    }

    @PostMapping("/return")
    public ResponseEntity<String> returnBook(@RequestBody Map<String, Integer> requestBody) {
        int borrowId = requestBody.get("borrowId");
        // Call the service method to return the book
        double fine = borrowedBookService.returnBook(borrowId);

        // Prepare the response message
        String message = fine > 0
                ? "Book returned successfully. Fine applied: Rs. " + fine
                : "Book returned successfully. No fine applied.";

        // Return the response
        return ResponseEntity.ok(message);
    }

    @GetMapping
    public ResponseEntity<List<BorrowedBookDTO>> getAllBorrowedBooks() {
        List<BorrowedBookDTO> borrowedBooks = borrowedBookService.getAllBorrowedBooks();
        return ResponseEntity.ok(borrowedBooks);
    }
    @GetMapping("/{borrowedBookId}")
    public ResponseEntity<BorrowedBookDTO> getBorrowedBookById(@PathVariable int borrowedBookId) {
        BorrowedBookDTO borrowedBook = borrowedBookService.getBorrowedBookById(borrowedBookId);
        if (borrowedBook == null) {
            throw new CRUDAPIException(HttpStatus.NOT_FOUND, "Borrowed Book with ID " + borrowedBookId + " not found");
        }
        return ResponseEntity.ok(borrowedBook);
    }
}
