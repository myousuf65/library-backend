package com.StudentLibrary.Studentlibrary.Services;

import com.StudentLibrary.Studentlibrary.Model.Author;
import com.StudentLibrary.Studentlibrary.Repositories.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthorService {
    @Autowired
    AuthorRepository authorRepository;

    public void createAuthor(Author author){
        authorRepository.save(author);
    }
    
    public Author createOrUpdateAuthor(Author author) {
        // Check if author with same email exists
        Optional<Author> existingAuthor = authorRepository.findByEmail(author.getEmail());
        
        if (existingAuthor.isPresent()) {
            // Update existing author
            Author authorToUpdate = existingAuthor.get();
            authorToUpdate.setName(author.getName());
            authorToUpdate.setAge(author.getAge());
            authorToUpdate.setCountry(author.getCountry());
            return authorRepository.save(authorToUpdate);
        } else {
            // Create new author
            return authorRepository.save(author);
        }
    }
    
    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }
    
    public Author getAuthorById(int id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + id));
    }
    
    public void updateAuthor(Author author){
        authorRepository.updateAuthorDetails(author);
    }
    
    public void deleteAuthor(int id){
        authorRepository.deleteCustom(id);
    }
    
    public int getTotalAuthorsCount() {
        try {
            System.out.println("Service: Getting total authors count");
            long count = authorRepository.count();
            System.out.println("Service: Total authors count: " + count);
            return (int) count;
        } catch (Exception e) {
            System.err.println("Service: Error getting total authors count: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}
