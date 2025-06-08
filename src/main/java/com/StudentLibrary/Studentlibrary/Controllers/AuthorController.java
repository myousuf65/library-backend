package com.StudentLibrary.Studentlibrary.Controllers;

import com.StudentLibrary.Studentlibrary.Model.Author;
import com.StudentLibrary.Studentlibrary.Services.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class AuthorController {

    @Autowired
    AuthorService authorService;

    @PostMapping("/createAuthor")
    public ResponseEntity createAuthor(@RequestBody Author author){
        authorService.createAuthor(author);
        return new ResponseEntity("Author created", HttpStatus.CREATED);
    }
    
    @GetMapping("/public/authors/all")
    public ResponseEntity<List<Author>> getAllAuthors() {
        List<Author> authors = authorService.getAllAuthors();
        return ResponseEntity.ok(authors);
    }
    
    @GetMapping("/public/authors/findById")
    public ResponseEntity<Author> getAuthorById(@RequestParam int id) {
        try {
            Author author = authorService.getAuthorById(id);
            return ResponseEntity.ok(author);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/updateAuthor")
    public ResponseEntity updateAuthor(@RequestBody Author author){
        authorService.updateAuthor(author);
        return new ResponseEntity("Auhtor upadted!!",HttpStatus.ACCEPTED);
    }
    
    @PutMapping("/public/authors/updateAuthor")
    public ResponseEntity<?> updateAuthorPublic(@RequestBody Author author) {
        try {
            authorService.updateAuthor(author);
            return ResponseEntity.ok(Map.of("message", "Author updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to update author: " + e.getMessage()));
        }
    }

    @DeleteMapping("/deleteAuthor")
    public ResponseEntity deleteAuthor(@RequestParam("id") int id){
        authorService.deleteAuthor(id);
        return new ResponseEntity("Author deleted!!",HttpStatus.ACCEPTED);
    }
    
    @DeleteMapping("/public/authors/deleteAuthor")
    public ResponseEntity<?> deleteAuthorPublic(@RequestParam int id) {
        try {
            authorService.deleteAuthor(id);
            return ResponseEntity.ok(Map.of("message", "Author deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to delete author: " + e.getMessage()));
        }
    }
}
