package com.StudentLibrary.Studentlibrary.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.StudentLibrary.Studentlibrary.Model.Book;
import com.StudentLibrary.Studentlibrary.Model.Student;
import com.StudentLibrary.Studentlibrary.Model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findByBookAndStudentAndIsIssueOperationTrueOrderByTransactionDateDesc(
            Book book, Student student, Pageable pageable);
            
    Optional<Transaction> findByTransactionId(String transactionId);
    
    List<Transaction> findByStudent(Student student, Sort sort);
    
    List<Transaction> findByBook(Book book, Sort sort);
    
    List<Transaction> findByIsIssueOperationTrue(Sort sort);
    
    List<Transaction> findByIsIssueOperationFalse(Sort sort);
    
    boolean existsByBookAndStudentAndIsIssueOperationFalse(Book book, Student student);

    List<Transaction> findByStudentAndBook(Student student, Book book);

    Optional<Transaction> findByStudentAndBookAndIsIssueOperationIsTrue(Student student, Book book);
}
