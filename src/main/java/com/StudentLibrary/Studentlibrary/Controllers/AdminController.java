package com.StudentLibrary.Studentlibrary.Controllers;

import com.StudentLibrary.Studentlibrary.Model.Student;
import com.StudentLibrary.Studentlibrary.Model.User;
import com.StudentLibrary.Studentlibrary.Services.StudentService;
import com.StudentLibrary.Studentlibrary.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private StudentService studentService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/students")
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @PostMapping("/users/create")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> userRequest) {
        try {
            String username = (String) userRequest.get("username");
            String password = (String) userRequest.get("password");
            List<String> roles = (List<String>) userRequest.get("roles");

            if (username == null || password == null || roles == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Username, password, and roles are required"));
            }

            if (userService.existsByUsername(username)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Username already exists"));
            }

            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
//            user.setRoles(roles);

            User createdUser = userService.createUser(user);
            
            // Remove password from response
            createdUser.setPassword(null);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to create user: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable int id, @RequestBody Map<String, Object> userRequest) {
        try {
            User existingUser = userService.findById(id);
            
            String username = (String) userRequest.get("username");
            String password = (String) userRequest.get("password");
            List<String> roles = (List<String>) userRequest.get("roles");
            
            if (username != null) {
                // Check if username is being changed and if it already exists
                if (!existingUser.getUsername().equals(username) && userService.existsByUsername(username)) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Username already exists"));
                }
                existingUser.setUsername(username);
            }
            
            if (password != null) {
                existingUser.setPassword(password);
            }
            
            if (roles != null) {
                existingUser.setRoles(roles);
            }
            
            User updatedUser = userService.updateUser(existingUser);
            
            // Remove password from response
            updatedUser.setPassword(null);
            
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to update user: " + e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to delete user: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/students/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable int id) {
        try {
            studentService.deleteStudent(id);
            return ResponseEntity.ok(Map.of("message", "Student deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to delete student: " + e.getMessage()));
        }
    }
}
