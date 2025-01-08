package com.hexaware.librarymanagement.repository;

import com.hexaware.librarymanagement.entity.BorrowedBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowedBookRepository extends JpaRepository<BorrowedBook, Integer> {
    List<BorrowedBook> findByUser_Id(int userId);
<<<<<<< HEAD
    List<BorrowedBook> findByBook_BookId(int bookId);
    public Optional<BorrowedBook> findByUser_IdAndBook_BookId(int userId, int bookId);
=======
    List<BorrowedBook> findByBookBookId(int bookId);

    public Optional<BorrowedBook> findByUser_IdAndBook_BookId(int userId, int bookId);


>>>>>>> 1186ea151680a4f7b8a2ab06183fa4ee0f36d8cb
}