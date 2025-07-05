package com.StudentLibrary.Studentlibrary.Services;

import com.StudentLibrary.Studentlibrary.Model.Book;
import com.StudentLibrary.Studentlibrary.Model.Student;
import com.StudentLibrary.Studentlibrary.Model.Transaction;
import com.StudentLibrary.Studentlibrary.Model.TransactionStatus;
import com.StudentLibrary.Studentlibrary.Repositories.BookRepository;
import com.StudentLibrary.Studentlibrary.Repositories.StudentRepository;
import com.StudentLibrary.Studentlibrary.Repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class TransactionService {

    private static final int FINE_PER_DAY = 5;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private StudentRepository studentRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Value("${books.max_allowed}")
    private int maxAllowedBooks;
    @Value("${books.max_allowed_days}")
    private int maxAllowedDays;

    @Transactional
    public String issueBooks(int studentId, int bookId) throws Exception {
        // Check if book is available
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new Exception("Book not found with id: " + bookId));

        if (!book.isAvailable()) {
            throw new Exception("Book is not available for issue");
        }

        // Check if student exists
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new Exception("Student not found with id: " + studentId));

        // Count only currently borrowed books (available == false)
        long currentlyBorrowed = student.getBooks() == null ? 0 : student.getBooks().stream()
                .filter(b -> !b.isAvailable())
                .count();
        if (currentlyBorrowed >= maxAllowedBooks) {
            throw new Exception("Student has already issued maximum allowed books");
        }

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setBook(book);
        transaction.setStudent(student);
        transaction.setIsIssueOperation(true);
        transaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        transaction.setTransactionId(UUID.randomUUID().toString());

        // Update book status
        book.setAvailable(false);
        book.setStudent(student);
        bookRepository.save(book);

        // Save transaction
        transactionRepository.save(transaction);

        return transaction.getTransactionId();
    }

    @Transactional
    public String issueScannedBooks(String studentId, int bookId) throws Exception {
        // Check if book is available
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new Exception("Book not found with id: " + bookId));

        if (!book.isAvailable()) {
            throw new Exception("Book is not available for issue");
        }

        // Check if student exists
        Student student = studentRepository.findByStudentId(studentId);


        // Count only currently borrowed books (available == false)
        long currentlyBorrowed = student.getBooks() == null ? 0 : student.getBooks().stream()
                .filter(b -> !b.isAvailable())
                .count();
        if (currentlyBorrowed >= maxAllowedBooks) {
            throw new Exception("Student has already issued maximum allowed books");
        }

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setBook(book);
        transaction.setStudent(student);
        transaction.setIsIssueOperation(true);
        transaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        transaction.setTransactionId(UUID.randomUUID().toString());

        // Update book status
        book.setAvailable(false);
        book.setStudent(student);
        bookRepository.save(book);

        // Save transaction
        transactionRepository.save(transaction);

        return transaction.getTransactionId();
    }

    @Transactional
    public String returnBooks(int studentId, int bookId) throws Exception {
        // Check if book exists
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new Exception("Book not found with id: " + bookId));

        // Check if student exists
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new Exception("Student not found with id: " + studentId));

        // Check if book was issued to this student
        if (book.getStudent() == null || book.getStudent().getId() != studentId) {
            throw new Exception("This book was not issued to this student");
        }


        Optional<Transaction> transaction = transactionRepository.findByStudentAndBookAndIsIssueOperationIsTrue(student, book);

        if (!transaction.isPresent()) {
            throw new Exception("No issue transaction found for this book and student");
        }

        transaction.get().setIsIssueOperation(false);

        Transaction savedTransaction = transactionRepository.save(transaction.get());
        transactionRepository.flush();

        // Update book status
        book.setAvailable(true);
        book.setStudent(null);
        bookRepository.save(book);

        return savedTransaction.getTransactionId();
    }

    public List<Transaction> getAllTransactions() {
        try {
            System.out.println("Service: Getting all transactions from database with detailed logging");
            List<Transaction> transactions = transactionRepository.findAll(
                    Sort.by(Sort.Direction.DESC, "transactionDate")
            );
            System.out.println("Service: Raw transaction count from repository: " + transactions.size());

            // Debug each transaction
            if (transactions.isEmpty()) {
                System.out.println("Service: No transactions found in database");
            } else {
                System.out.println("Service: Transaction details:");
                for (Transaction t : transactions) {
                    System.out.println("  ID: " + t.getId() +
                            ", UUID: " + t.getTransactionId() +
                            ", Type: " + (t.getIsIssueOperation() ? "ISSUE" : "RETURN") +
                            ", Status: " + t.getTransactionStatus() +
                            ", Student: " + (t.getStudent() != null ? t.getStudent().getId() : "null") +
                            ", Book: " + (t.getBook() != null ? t.getBook().getId() : "null"));
                }
            }

            return transactions;
        } catch (Exception e) {
            System.err.println("Service: Error fetching all transactions: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getBasicTransactionData() {
        try {
            System.out.println("Service: Getting basic transaction data");
            // Updated SQL to include author name
            String sql = "SELECT t.id, t.transaction_id, t.is_issue_operation, t.transaction_status, " +
                    "t.transaction_date, t.fine_amount, s.id as student_id, s.name as student_name, " +
                    "b.id as book_id, b.name as book_name, a.id as author_id, a.name as author_name " +
                    "FROM transaction t " +
                    "LEFT JOIN student s ON t.student_id = s.id " +
                    "LEFT JOIN book b ON t.book_id = b.id " +
                    "LEFT JOIN author a ON b.author_id = a.id " +
                    "ORDER BY t.transaction_date DESC";
            List<Object[]> results = entityManager.createNativeQuery(sql).getResultList();
            List<Map<String, Object>> transactions = new ArrayList<>();
            for (Object[] row : results) {
                Map<String, Object> transaction = new HashMap<>();
                transaction.put("id", row[0]);
                transaction.put("transactionId", row[1]);
                transaction.put("isIssueOperation", row[2]);
                transaction.put("transactionStatus", row[3]);
                transaction.put("transactionDate", row[4]);
                transaction.put("fineAmount", row[5]);
                // Handle student and book data
                Map<String, Object> student = new HashMap<>();
                student.put("id", row[6]);
                student.put("name", row[7]);
                transaction.put("student", student);
                Map<String, Object> book = new HashMap<>();
                book.put("id", row[8]);
                book.put("name", row[9]);
                Map<String, Object> author = new HashMap<>();
                author.put("id", row[10]);
                author.put("name", row[11]);
                book.put("author", author);
                transaction.put("book", book);
                transactions.add(transaction);
            }
            System.out.println("Service: Successfully retrieved " + transactions.size() + " basic transactions");
            return transactions;
        } catch (Exception e) {
            System.err.println("Service: Error fetching basic transaction data: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Transaction getTransactionById(String transactionId) {
        try {
            System.out.println("Service: Getting transaction by ID: " + transactionId);
            Optional<Transaction> transaction = transactionRepository.findByTransactionId(transactionId);
            if (transaction.isPresent()) {
                System.out.println("Service: Found transaction with ID: " + transactionId);
                return transaction.get();
            } else {
                System.out.println("Service: No transaction found with ID: " + transactionId);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Service: Error fetching transaction by ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<Transaction> getTransactionsByCardId(int cardId) {
        try {
            System.out.println("Service: Getting transactions for card ID: " + cardId);
            // In a real implementation, you would query by card ID
            // For now, we'll get transactions by student ID (assuming card ID = student ID)
            Optional<Student> student = studentRepository.findById(cardId);
            if (student.isPresent()) {
                List<Transaction> transactions = transactionRepository.findByStudent(
                        student.get(),
                        Sort.by(Sort.Direction.DESC, "transactionDate")
                );
                System.out.println("Service: Found " + transactions.size() + " transactions for card ID: " + cardId);
                return transactions;
            } else {
                System.out.println("Service: No student found with ID: " + cardId);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Service: Error fetching transactions by card ID: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Transaction> getOverdueTransactions() {
        List<Transaction> overdueTransactions = new ArrayList<>();
        List<Transaction> allTransactions = transactionRepository.findAll();

        Date currentDate = new Date();
        for (Transaction t : allTransactions) {
            if(t.getIsIssueOperation()){
                System.out.println(t);
                long diffInMillis = currentDate.getTime() - t.getTransactionDate().getTime();
                long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

                if (diffInDays > 10) {
                    overdueTransactions.add(t);
                }
            }
        }
        return overdueTransactions;
    }

    public List<Transaction> getTransactionsByBookId(int bookId) {
        try {
            System.out.println("Fetching transactions for book ID: " + bookId);
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));

            List<Transaction> transactions = transactionRepository.findByBook(book,
                    Sort.by(Sort.Direction.DESC, "transactionDate"));

            System.out.println("Found " + transactions.size() + " transactions for book ID: " + bookId);
            return transactions;
        } catch (Exception e) {
            System.err.println("Error fetching transactions for book: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public int getTotalTransactionsCount() {
        try {
            System.out.println("Service: Getting total transactions count");
            long count = transactionRepository.count();
            System.out.println("Service: Total transactions count: " + count);
            return (int) count;
        } catch (Exception e) {
            System.err.println("Service: Error getting total transactions count: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public List<Transaction> getRecentTransactions(int limit) {
        try {
            System.out.println("Service: Getting recent transactions from database, limit: " + limit);
            List<Transaction> transactions = transactionRepository.findAll(
                    PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "transactionDate"))
            ).getContent();
            System.out.println("Service: Found " + transactions.size() + " recent transactions");
            return transactions;
        } catch (Exception e) {
            System.err.println("Service: Error fetching recent transactions: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    public double getOnTimeReturnsPercentage() {
        try {
            System.out.println("Service: Calculating on-time returns percentage from actual data");

            // Get all return transactions
            List<Transaction> returnTransactions = transactionRepository.findByIsIssueOperationFalse(
                    Sort.by(Sort.Direction.DESC, "transactionDate")
            );

            if (returnTransactions.isEmpty()) {
                return 0.0;
            }

            int onTimeReturns = 0;
            int totalReturns = returnTransactions.size();

            for (Transaction returnTransaction : returnTransactions) {
                // If fine amount is 0, it was returned on time
                if (returnTransaction.getFineAmount() == 0) {
                    onTimeReturns++;
                }
            }

            double percentage = (double) onTimeReturns / totalReturns * 100;
            System.out.println("Service: On-time returns percentage: " + percentage + "%");
            return percentage;
        } catch (Exception e) {
            System.err.println("Service: Error calculating on-time returns: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}
