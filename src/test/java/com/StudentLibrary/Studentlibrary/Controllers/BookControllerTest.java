package com.StudentLibrary.Studentlibrary.Controllers;

import com.StudentLibrary.Studentlibrary.Model.Author;
import com.StudentLibrary.Studentlibrary.Model.Book;
import com.StudentLibrary.Studentlibrary.Model.Genre;
import com.StudentLibrary.Studentlibrary.Model.Student;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void testAddBook() throws Exception {
        Book testBook = new Book();
        testBook.setAuthor(new Author("yousuf", "email@yousuf.com", 12, "Hong Kong"));
        testBook.setName("my first book");
        testBook.setAvailable(true);
        testBook.setGenre(Genre.FICTIONAL);
        testBook.setStudent(new Student("yousuf", "email@gmail.com", 10, "hk", "STU123"));

        MvcResult result = mockMvc.perform(
                post("/api/books/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(testBook))

                )
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
    }



}