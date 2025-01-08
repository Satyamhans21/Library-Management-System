package com.hexaware.librarymanagement.service;

import com.hexaware.librarymanagement.dto.BorrowedBookDTO;
import com.hexaware.librarymanagement.entity.BorrowedBook;

import java.util.List;

public interface IBorrowedBookService {
    BorrowedBookDTO borrowBook(BorrowedBookDTO borrowedBookDTO);
    List<BorrowedBookDTO> getBorrowedBooksByUser(int userId);
    List<BorrowedBookDTO> getAllBorrowedBooks();

    double returnBook(int borrowId);

    BorrowedBookDTO getBorrowedBookById(int borrowedBookId);
    double returnBook(int borrowId);
    double calculateFine(BorrowedBook borrowedBook);
}
