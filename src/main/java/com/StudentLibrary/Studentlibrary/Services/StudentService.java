package com.StudentLibrary.Studentlibrary.Services;

import com.StudentLibrary.Studentlibrary.Model.Student;
import com.StudentLibrary.Studentlibrary.Model.User;
import com.StudentLibrary.Studentlibrary.Repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private UserService userService;

    @Transactional
    public Student createStudent(Student student, String password) {
        try {
            System.out.println("Creating student with email: " + student.getEmailId());
            System.out.println("Student ID before saving: " + student.getStudentId());
            
            // Ensure studentId is not null
            if (student.getStudentId() == null || student.getStudentId().isEmpty()) {
                student.setStudentId("STU" + System.currentTimeMillis());
                System.out.println("Generated default Student ID: " + student.getStudentId());
            }
            
            // Create a user account for the student
            User user = new User();
            user.setUsername(student.getEmailId());
            user.setPassword(password);
            user.setRoles(new ArrayList<>(List.of("STUDENT")));
            
            // Save the user first
            User savedUser = userService.createUser(user);
            System.out.println("Created user with ID: " + savedUser.getId());
            
            // Associate the user with the student
            student.setUser(savedUser);
            savedUser.setStudent(student);
            
            // Save the student
            Student savedStudent = studentRepository.save(student);
            System.out.println("Created student with ID: " + savedStudent.getId() + ", Student ID: " + savedStudent.getStudentId());
            
            return savedStudent;
        } catch (Exception e) {
            System.err.println("Error in createStudent: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public Student getStudentById(int id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
    }

    public List<Student> getAllStudents() {
        try {
            System.out.println("Fetching all students");
            List<Student> students = studentRepository.findAll();
            System.out.println("Found " + students.size() + " students");
            
            // Debug each student's fine
            for (Student student : students) {
                System.out.println("Student: " + student.getName() + " (ID: " + student.getId() + "), Fine: " + student.getFine() + ", Type: " + (student.getFine() != null ? student.getFine().getClass().getSimpleName() : "null"));
            }
            
            return students;
        } catch (Exception e) {
            System.err.println("Error fetching students: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public Student updateStudent(Student student) {
        try {
            System.out.println("Updating student with ID: " + student.getId() + ", Student ID: " + student.getStudentId());

            // Always fetch the existing student from the DB
            Student existing = studentRepository.findById(student.getId())
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + student.getId()));

            // Always preserve the user relationship
            if (student.getUser() != null && student.getUser().getId() != 0) {
                // Fetch the managed user entity from the database
                User user = userService.findById(student.getUser().getId());
                student.setUser(user);
            } else if (existing.getUser() != null) {
                student.setUser(existing.getUser());
            } else {
                student.setUser(null);
            }

            // Optionally, preserve other fields that should not be changed by the update
            // For example, if you don't want to allow changing studentId, etc.

            return studentRepository.save(student);
        } catch (Exception e) {
            System.err.println("Error updating student: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public void deleteStudent(int id) {
        try {
            System.out.println("Deleting student with ID: " + id);
            
            // Get the student first to access the associated user
            Student student = studentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
            
            // Get the associated user
            User user = student.getUser();
            
            if (user != null) {
                int userId = user.getId();
                System.out.println("Found associated user with ID: " + userId);
                
                // First, remove the association between student and user
                student.setUser(null);
                user.setStudent(null);
                studentRepository.save(student);
                
                // Delete the student
                studentRepository.deleteById(id);
                System.out.println("Student deleted successfully");
                
                // Delete the user (this will cascade to user_roles due to JPA configuration)
                userService.deleteUser(userId);
                System.out.println("Associated user and roles deleted successfully");
            } else {
                // If no user is associated, just delete the student
                studentRepository.deleteById(id);
                System.out.println("Student deleted successfully (no associated user)");
            }
        } catch (Exception e) {
            System.err.println("Error deleting student: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    public Student findByEmailId(String emailId) {
        return studentRepository.findByEmailId(emailId)
                .orElseThrow(() -> new RuntimeException("Student not found with email: " + emailId));
    }
    
    public int getTotalStudentsCount() {
        try {
            System.out.println("Service: Getting total students count");
            long count = studentRepository.count();
            System.out.println("Service: Total students count: " + count);
            return (int) count;
        } catch (Exception e) {
            System.err.println("Service: Error getting total students count: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    public double getStudentEngagementPercentage() {
        try {
            System.out.println("Service: Calculating student engagement percentage");
            // This is a simplified implementation
            // In a real application, you would calculate engagement based on active borrowing
            
            // Count students with at least one book
            long totalStudents = studentRepository.count();
            if (totalStudents == 0) return 0;
            
            // For now, return a fixed value
            // In a real implementation, you would query the database to get this information
            double percentage = 65.0;
            System.out.println("Service: Student engagement percentage: " + percentage + "%");
            return percentage;
        } catch (Exception e) {
            System.err.println("Service: Error calculating student engagement: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}
