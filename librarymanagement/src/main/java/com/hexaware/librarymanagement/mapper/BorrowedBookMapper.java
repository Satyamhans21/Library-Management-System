package com.hexaware.librarymanagement.mapper;

import com.hexaware.librarymanagement.dto.BorrowedBookDTO;
import com.hexaware.librarymanagement.entity.BorrowedBook;
import com.hexaware.librarymanagement.entity.Book;
import com.hexaware.librarymanagement.entity.User;
import com.hexaware.librarymanagement.exception.CRUDAPIException;
import org.springframework.http.HttpStatus;

public class BorrowedBookMapper {

    // Method to map BorrowedBookDTO to BorrowedBook entity
    public static BorrowedBook mapToBorrowedBook(BorrowedBookDTO dto) {
        BorrowedBook borrowedBook = new BorrowedBook();

        borrowedBook.setBorrowId(dto.getBorrowId());
        borrowedBook.setBorrowedDate(dto.getBorrowedDate());
        borrowedBook.setDueDate(dto.getDueDate());
        borrowedBook.setReturnDate(dto.getReturnDate());
        borrowedBook.setFine(dto.getFine());

        return borrowedBook;
    }

    // Method to map BorrowedBook entity to BorrowedBookDTO
    public static BorrowedBookDTO mapToBorrowedBookDTO(BorrowedBook borrowedBook) {
        BorrowedBookDTO dto = new BorrowedBookDTO();

        dto.setBorrowId(borrowedBook.getBorrowId());
        dto.setUserId(borrowedBook.getUser().getId());
        dto.setBookId(borrowedBook.getBook().getBookId());
        dto.setBorrowedDate(borrowedBook.getBorrowedDate());
        dto.setDueDate(borrowedBook.getDueDate());
        dto.setReturnDate(borrowedBook.getReturnDate());
        dto.setFine(borrowedBook.getFine());

        return dto;
    }
}
