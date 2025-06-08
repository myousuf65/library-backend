package com.StudentLibrary.Studentlibrary.Controllers;

import com.StudentLibrary.Studentlibrary.Model.Student;
import com.StudentLibrary.Studentlibrary.Model.User;
import com.StudentLibrary.Studentlibrary.Services.StudentService;
import com.StudentLibrary.Studentlibrary.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }
    
    @GetMapping("/public/all")
    public ResponseEntity<List<Student>> getAllStudentsPublic() {
        try {
            List<Student> students = studentService.getAllStudents();
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable int id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        // Check if the user is an admin or the student themselves
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            Student student = studentService.getStudentById(id);
            if (!student.getEmailId().equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        return ResponseEntity.ok(studentService.getStudentById(id));
    }
    
    @GetMapping("/public/findById")
    public ResponseEntity<Student> getStudentByIdPublic(@RequestParam int id) {
        try {
            Student student = studentService.getStudentById(id);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/createStudent")
    public ResponseEntity<?> createStudent(@RequestBody Map<String, Object> request) {
        try {
            // Log the incoming request for debugging
            System.out.println("Received registration request: " + request);
            
            // Extract student data
            Student student = new Student();
            student.setName((String) request.get("name"));
            student.setEmailId((String) request.get("emailId"));
            
            // Make sure studentId is not null
            String studentId = (String) request.get("studentId");
            if (studentId == null || studentId.isEmpty()) {
                // Generate a student ID automatically
                studentId = "STU" + System.currentTimeMillis();
                System.out.println("Generated student ID: " + studentId);
            }
            student.setStudentId(studentId);
            System.out.println("Setting student ID: " + studentId);
            
            // Handle age conversion properly
            Object ageObj = request.get("age");
            int age;
            if (ageObj instanceof Integer) {
                age = (Integer) ageObj;
            } else if (ageObj instanceof String) {
                age = Integer.parseInt((String) ageObj);
            } else {
                throw new IllegalArgumentException("Invalid age format");
            }
            student.setAge(age);
            
            student.setCountry((String) request.get("country"));
            
            // Get password from request or use default
            String password = request.containsKey("password") ? 
                (String) request.get("password") : "pass123";
            
            // Create student with user account
            Student createdStudent = studentService.createStudent(student, password);
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("id", createdStudent.getId());
            response.put("studentId", createdStudent.getStudentId());
            response.put("name", createdStudent.getName());
            response.put("emailId", createdStudent.getEmailId());
            response.put("message", "Student created successfully");
            
            System.out.println("Student created successfully: " + createdStudent.getId() + ", Student ID: " + createdStudent.getStudentId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.err.println("Error creating student: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to create student: " + e.getMessage()));
        }
    }

    @PutMapping("/updateStudent")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> updateStudent(@RequestBody Student student) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        // Ensure the student can only update their own profile
        Student existingStudent = studentService.getStudentById(student.getId());
        if (!existingStudent.getEmailId().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only update your own profile"));
        }
        
        // Don't allow changing the email as it's used for authentication
        student.setEmailId(existingStudent.getEmailId());
        
        Student updatedStudent = studentService.updateStudent(student);
        return ResponseEntity.ok(updatedStudent);
    }
    
    @PutMapping("/public/updateStudent")
    public ResponseEntity<?> updateStudentPublic(@RequestBody Student student) {
        try {
            Student updatedStudent = studentService.updateStudent(student);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to update student: " + e.getMessage()));
        }
    }

    @PostMapping("/changePassword")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");
        
        try {
            User user = userService.findByUsername(username);
            
            // Verify old password
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Current password is incorrect"));
            }
            
            // Update password
            user.setPassword(newPassword);
            userService.updateUser(user);
            
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to change password: " + e.getMessage()));
        }
    }

    @DeleteMapping("/public/deleteStudent")
    public ResponseEntity<?> deleteStudentPublic(@RequestParam int id) {
        try {
            studentService.deleteStudent(id);
            return ResponseEntity.ok(Map.of("message", "Student deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to delete student: " + e.getMessage()));
        }
    }
}
