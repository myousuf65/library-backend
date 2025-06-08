package com.StudentLibrary.Studentlibrary.Repositories;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.StudentLibrary.Studentlibrary.Model.Book;

@Repository
@Transactional
public interface BookRepository extends JpaRepository<Book, Integer> {

    @Modifying
    @Query("update Book b set b.name = :#{#book.name}, b.genre = :#{#book.genre}, b.description = :#{#book.description}, b.publishedYear = :#{#book.publishedYear}, b.available = :#{#book.available}, b.author.id = :#{#book.author.id} where b.id = :#{#book.id}")
    int updateBook(@Param("book") Book book);
    
    @Modifying
    @Query("update Book b set b.coverImage = :imageData where b.id = :bookId")
    int updateBookImage(@Param("bookId") int bookId, @Param("imageData") byte[] imageData);

    @Query("select b from Book b where b.genre = :genre and b.available = :isAvailable and b.author.name = :author")
    List<Book> findBooksByGenre_Author(@Param("genre") String genre, @Param("author") String author, @Param("isAvailable") boolean isAvailable);

    @Query("select b from Book b where b.genre = :genre and b.available = :isAvailable")
    List<Book> findBooksByGenre(@Param("genre") String genre, @Param("isAvailable") boolean isAvailable);

    @Query("select b from Book b where b.available = :isAvailable and b.author.name = :author")
    List<Book> findBooksByAuthor(@Param("author") String author, @Param("isAvailable") boolean isAvailable);

    @Query("select b from Book b where b.available = :isAvailable")
    List<Book> findBooksByAvailability(@Param("isAvailable") boolean isAvailable);
    
    long countByAvailable(boolean available);
    
    List<Book> findTop5ByOrderByIdDesc();
}
