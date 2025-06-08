package com.StudentLibrary.Studentlibrary.Repositories;

import com.StudentLibrary.Studentlibrary.Model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Integer> {

    @Modifying
    @Transactional
    @Query("update Author a set a.name = :#{#author.name}, a.email = :#{#author.email}, a.age = :#{#author.age}, a.country = :#{#author.country} where a.id = :#{#author.id}")
    int updateAuthorDetails(Author author);

    @Modifying
    @Transactional
    @Query("delete from Author a where a.id = :id")
    void deleteCustom(int id);
    
    Optional<Author> findByEmail(String email);
}
