package com.StudentLibrary.Studentlibrary.Controllers;

import com.StudentLibrary.Studentlibrary.Model.User;
import com.StudentLibrary.Studentlibrary.Repositories.UserRepository;
import com.StudentLibrary.Studentlibrary.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        try {
            // For debugging
            System.out.println("Login attempt: " + username);
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            User user = userService.findByUsername(userDetails.getUsername());
            
            Map<String, Object> response = new HashMap<>();
            response.put("username", user.getUsername());
            response.put("roles", user.getRoles());
            
            // For debugging
            System.out.println("Login successful for: " + username);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // For debugging
            System.out.println("Login failed for: " + username + " - " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }
    }

    @GetMapping("/currentUser/{username}")
    public ResponseEntity<?> getCurrentUser(@PathVariable String username) {
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return ResponseEntity.status(401).body("User not authenticated");
//        }

//        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(username);

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.get().getUsername());
        response.put("roles", user.get().getRoles());
        
        if (user.get().getStudent() != null) {
            response.put("studentId", user.get().getStudent().getId());
            response.put("studentCode", user.get().getStudent().getStudentId());
            response.put("name", user.get().getStudent().getName());
        }
        
        return ResponseEntity.ok(response);
    }
}
