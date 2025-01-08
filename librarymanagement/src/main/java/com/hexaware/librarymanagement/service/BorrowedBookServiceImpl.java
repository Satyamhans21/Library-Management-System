package com.hexaware.librarymanagement.service;

import com.hexaware.librarymanagement.dto.BorrowedBookDTO;
import com.hexaware.librarymanagement.entity.Book;
import com.hexaware.librarymanagement.entity.BorrowedBook;
import com.hexaware.librarymanagement.entity.User;
import com.hexaware.librarymanagement.exception.CRUDAPIException;
import com.hexaware.librarymanagement.mapper.BorrowedBookMapper;
import com.hexaware.librarymanagement.repository.BorrowedBookRepository;
import com.hexaware.librarymanagement.repository.UserRepository;
import com.hexaware.librarymanagement.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BorrowedBookServiceImpl implements IBorrowedBookService {

    @Autowired
    private BorrowedBookRepository borrowedBookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Override
    public BorrowedBookDTO borrowBook(BorrowedBookDTO borrowedBookDTO) {
        User user = userRepository.findById(borrowedBookDTO.getUserId())
                .orElseThrow(() -> new CRUDAPIException(HttpStatus.NOT_FOUND, "User not found", "User with ID " + borrowedBookDTO.getUserId() + " not found"));

        // Fetch Book entity by ID
        Book book = bookRepository.findById(borrowedBookDTO.getBookId())
                .orElseThrow(() -> new CRUDAPIException(HttpStatus.NOT_FOUND, "Book not found", "Book with ID " + borrowedBookDTO.getBookId() + " not found"));

        // Map BorrowedBookDTO to BorrowedBook entity (without fetching User and Book)

    // Method to borrow a book and save the borrowed record
    @Override
    public BorrowedBookDTO borrowBook(BorrowedBookDTO borrowedBookDTO) {
        // Check if the due date is provided for borrowing
        if (borrowedBookDTO.getDueDate() == null) {
            throw new CRUDAPIException(HttpStatus.BAD_REQUEST, "Validation Error", "Due date is required for borrowing a book.");
        }

        // Map the DTO to Entity and save the record

        BorrowedBook borrowedBook = BorrowedBookMapper.mapToBorrowedBook(borrowedBookDTO);

        // Set User and Book entities to BorrowedBook
        borrowedBook.setUser(user);
        borrowedBook.setBook(book);

        // Save the BorrowedBook entity
        BorrowedBook savedBorrowedBook = borrowedBookRepository.save(borrowedBook);


        // Return the saved borrowed record as a DTO

        return BorrowedBookMapper.mapToBorrowedBookDTO(savedBorrowedBook);
    }

    // Method to fetch all borrowed books by a specific user
    @Override
    public List<BorrowedBookDTO> getBorrowedBooksByUser(int userId) {
        // Fetch borrowed books by user ID
        List<BorrowedBook> borrowedBooks = borrowedBookRepository.findByUser_Id(userId);

        // If no records found, throw an exception
        if (borrowedBooks.isEmpty()) {
            throw new CRUDAPIException(HttpStatus.NOT_FOUND, "No Records Found", "No borrowed books found for User ID: " + userId);
        }

        // Calculate fine for each borrowed book and return as DTOs
        return borrowedBooks.stream()
                .peek(this::calculateFine) // Calculate fine for each borrowed book
                .map(BorrowedBookMapper::mapToBorrowedBookDTO)
                .collect(Collectors.toList());
    }

    // Method to fetch all borrowed books in the system
    @Override
    public List<BorrowedBookDTO> getAllBorrowedBooks() {
        // Fetch all borrowed books
        List<BorrowedBook> borrowedBooks = borrowedBookRepository.findAll();

        // Calculate fine for each borrowed book and return as DTOs
        return borrowedBooks.stream()
                .peek(this::calculateFine) // Calculate fine for each borrowed book
                .map(BorrowedBookMapper::mapToBorrowedBookDTO)
                .collect(Collectors.toList());
    }

    // Method to return a borrowed book and calculate the fine
    @Override
    public double returnBook(int borrowId) {
        // Find the borrowed record based on borrowId
        BorrowedBook borrowedBook = borrowedBookRepository.findById(borrowId)
                .orElseThrow(() -> new CRUDAPIException(HttpStatus.NOT_FOUND,
                        "Borrowed record not found for Borrow ID: " + borrowId));

        // Set the return date to the current date
        borrowedBook.setReturnDate(new Date());

        // Calculate the fine based on the return date
        double fine = calculateFine(borrowedBook);

        // Set the calculated fine in the borrowed record
        borrowedBook.setFine(fine);

        // Save the updated borrowed book record
        borrowedBookRepository.save(borrowedBook);

        // Now delete the record as per your original requirement
        borrowedBookRepository.delete(borrowedBook);

        // Return the calculated fine
        return fine;
    }

    // Method to fetch a borrowed book by its ID
    @Override
    public BorrowedBookDTO getBorrowedBookById(int borrowedBookId) {
        // Find the borrowed book record based on ID
        BorrowedBook borrowedBook = borrowedBookRepository.findById(borrowedBookId)
                .orElseThrow(() -> new CRUDAPIException(HttpStatus.NOT_FOUND, "Record Not Found", "Borrowed Book not found with ID: " + borrowedBookId));

        // Calculate fine for the borrowed book and return as DTO
        calculateFine(borrowedBook);
        return BorrowedBookMapper.mapToBorrowedBookDTO(borrowedBook);
    }

    @Override
    public double returnBook(int borrowId) {
        // Find the borrowed record based on borrowId
        BorrowedBook borrowedBook = borrowedBookRepository.findById(borrowId)
                .orElseThrow(() -> new CRUDAPIException(HttpStatus.NOT_FOUND,
                        "Borrowed record not found for Borrow ID: " + borrowId));

        // Set the return date to the current date
        borrowedBook.setReturnDate(new Date());

        // Calculate the fine based on the return date
        double fine = calculateFine(borrowedBook);

        // Set the calculated fine
        borrowedBook.setFine(fine);

        // Save the updated record
        borrowedBookRepository.save(borrowedBook);

        // Do NOT delete the record from the database
        // The record remains with updated fine and return date

        // Return the calculated fine
        return fine;
    }

    public double calculateFine(BorrowedBook borrowedBook) {
        // Ensure the due date is present
    // Method to calculate the fine for overdue borrowed books
    public double calculateFine(BorrowedBook borrowedBook) {
        // Validate that the due date is present
        if (borrowedBook.getDueDate() == null) {
            throw new CRUDAPIException(
                    HttpStatus.BAD_REQUEST,
                    "Validation Error",
                    "Due date is missing for Borrowed Book ID: " + borrowedBook.getBorrowId()
            );
        }

        // Convert the due date from Date to LocalDate
        LocalDate dueDate = borrowedBook.getDueDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate returnDate = borrowedBook.getReturnDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // Calculate the days overdue
        long overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);

        // Fine logic: Rs. 5 per day after 10 days overdue
        double fine = overdueDays > 10 ? (overdueDays - 10) * 5 : 0;

        // Return the calculated fine
        return fine;
    }

}

        // Get the current return date
        LocalDate returnDate = LocalDate.now();

        // Calculate the number of days overdue
        long overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);

        // Calculate the fine: Rs. 5 per day for overdue days (only if overdueDays > 0)
        double fine = overdueDays > 0 ? overdueDays * 5 : 0;

        // Return the calculated fine
        return fine;
    }
}

