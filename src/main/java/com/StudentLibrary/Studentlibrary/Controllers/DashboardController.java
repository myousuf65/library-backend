package com.StudentLibrary.Studentlibrary.Controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

import com.StudentLibrary.Studentlibrary.Model.Book;
import com.StudentLibrary.Studentlibrary.Model.Transaction;
import com.StudentLibrary.Studentlibrary.Services.AuthorService;
import com.StudentLibrary.Studentlibrary.Services.BookService;
import com.StudentLibrary.Studentlibrary.Services.StudentService;
import com.StudentLibrary.Studentlibrary.Services.TransactionService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private BookService bookService;
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private AuthorService authorService;
    
    @Autowired
    private TransactionService transactionService;
    
    @Value("${books.max_allowed_days}")
    private int maxAllowedDays;
    
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Get counts
            int totalBooks = bookService.getTotalBooksCount();
            int totalStudents = studentService.getTotalStudentsCount();
            int totalAuthors = authorService.getTotalAuthorsCount();
            int totalTransactions = transactionService.getTotalTransactionsCount();
            
            // Get percentages for statistics
            double borrowedBooksPercentage = bookService.getBorrowedBooksPercentage();
            double studentEngagementPercentage = studentService.getStudentEngagementPercentage();
            double onTimeReturnsPercentage = transactionService.getOnTimeReturnsPercentage();
            
            // Build response
            stats.put("totalBooks", totalBooks);
            stats.put("totalStudents", totalStudents);
            stats.put("totalAuthors", totalAuthors);
            stats.put("totalTransactions", totalTransactions);
            
            stats.put("borrowedBooksPercentage", borrowedBooksPercentage);
            stats.put("studentEngagementPercentage", studentEngagementPercentage);
            stats.put("onTimeReturnsPercentage", onTimeReturnsPercentage);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch dashboard stats: " + e.getMessage()));
        }
    }
    
    @GetMapping("/recent-transactions")
    public ResponseEntity<?> getRecentTransactions(@RequestParam(defaultValue = "5") int limit) {
        try {
            List<Transaction> transactions = transactionService.getRecentTransactions(limit);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch recent transactions: " + e.getMessage()));
        }
    }
    
    @GetMapping("/popular-books")
    public ResponseEntity<?> getPopularBooks(@RequestParam(defaultValue = "5") int limit) {
        try {
            List<Book> books = bookService.getPopularBooks(limit);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch popular books: " + e.getMessage()));
        }
    }
    
    @GetMapping("/overdue-books")
    public ResponseEntity<?> getOverdueBooks(@RequestParam(defaultValue = "5") int limit) {
        try {
            List<Transaction> overdueTransactions = transactionService.getOverdueTransactions();
            
            // Limit the results
            if (overdueTransactions.size() > limit) {
                overdueTransactions = overdueTransactions.subList(0, limit);
            }
            
            // Transform to the format expected by the frontend
            List<Map<String, Object>> result = overdueTransactions.stream().map(transaction -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", transaction.getId());
                
                // Book info
                if (transaction.getBook() != null) {
                    Map<String, Object> book = new HashMap<>();
                    book.put("name", transaction.getBook().getName());
                    item.put("book", book);
                }
                
                // Student info
                if (transaction.getStudent() != null) {
                    Map<String, Object> student = new HashMap<>();
                    student.put("name", transaction.getStudent().getName());
                    item.put("student", student);
                }
                
                // Calculate days overdue
                Date issueDate = transaction.getTransactionDate();
                Date currentDate = new Date();
                long diffInMillies = currentDate.getTime() - issueDate.getTime();
                long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                int daysOverdue = (int) (diffInDays - maxAllowedDays); // Use configured borrow period
                if (daysOverdue < 0) daysOverdue = 0;
                
                item.put("dueDate", new Date(issueDate.getTime() + TimeUnit.DAYS.toMillis(maxAllowedDays)));
                item.put("daysOverdue", daysOverdue);
                
                return item;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch overdue books: " + e.getMessage()));
        }
    }
}
