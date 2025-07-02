package com.StudentLibrary.Studentlibrary.Controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.StudentLibrary.Studentlibrary.Model.Student;
import com.StudentLibrary.Studentlibrary.Repositories.StudentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.StudentLibrary.Studentlibrary.Model.Transaction;
import com.StudentLibrary.Studentlibrary.Repositories.TransactionRepository;
import com.StudentLibrary.Studentlibrary.Services.TransactionService;

@RestController
public class TransactionController {

    @Autowired
    TransactionService transactionService;

    @Autowired
    StudentRepository studentRepository;
    
    @Autowired
    TransactionRepository transactionRepository;

    @GetMapping("/transaction/all")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        try {
            System.out.println("Controller: Fetching all transactions from database");
            List<Transaction> transactions = transactionService.getAllTransactions();
            System.out.println("Controller: Successfully retrieved " + transactions.size() + " transactions");
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            System.err.println("Controller: Error fetching all transactions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/transaction/basic")
    public ResponseEntity<List<Map<String, Object>>> getBasicTransactions() {
        try {
            System.out.println("Controller: Fetching basic transaction data");
            List<Map<String, Object>> transactions = transactionService.getBasicTransactionData();
            System.out.println("Controller: Successfully retrieved " + transactions.size() + " basic transactions");
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            System.err.println("Controller: Error fetching basic transactions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/transaction/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable String id) {
        try {
            Transaction transaction = transactionService.getTransactionById(id);
            if (transaction == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/transaction/student")
    public ResponseEntity<List<Transaction>> getStudentTransactions(@RequestParam("cardId") int cardId) {
        try {
            List<Transaction> transactions = transactionService.getTransactionsByCardId(cardId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/transaction/book")
    public ResponseEntity<List<Transaction>> getBookTransactions(@RequestParam("bookId") int bookId) {
        try {
            System.out.println("Controller: Fetching transactions for book ID: " + bookId);
            List<Transaction> transactions = transactionService.getTransactionsByBookId(bookId);
            System.out.println("Controller: Found " + transactions.size() + " transactions");
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            System.err.println("Controller: Error fetching book transactions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/transaction/overdue")
    public ResponseEntity<List<Transaction>> getOverdueTransactions() {
        try {
            List<Transaction> transactions = transactionService.getOverdueTransactions();
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/transaction/issueBook")
    public ResponseEntity<?> issueBook(@RequestParam(value = "studentId") int studentId,
                                    @RequestParam("bookId")int bookId) throws Exception {
        try {
            String transaction_id = transactionService.issueBooks(studentId, bookId);
            return ResponseEntity.ok(Map.of(
                "message", "Book issued successfully",
                "transactionId", transaction_id
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/transaction/setfine")
    public ResponseEntity<?> setOverdue(@RequestBody JsonNode node){
        double fine = node.get("fine").asDouble();
        int studentId = node.get("studentId").asInt();

        Optional<Student> student = studentRepository.findById(studentId);
        if (student.isPresent()){
            student.get().setFine(String.valueOf(fine));
        }

        Student savedStudent = studentRepository.save(student.get());
        studentRepository.flush();

        return ResponseEntity.ok(savedStudent);
    }
    
    @PostMapping("/transaction/returnBook")
    public ResponseEntity<?> returnBook(@RequestParam("studentId") int studentId,
                                     @RequestParam("bookId") int bookId) throws Exception {
        try {
            String transaction_id = transactionService.returnBooks(studentId, bookId);
            return ResponseEntity.ok(Map.of(
                "message", "Book returned successfully",
                "transactionId", transaction_id
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
