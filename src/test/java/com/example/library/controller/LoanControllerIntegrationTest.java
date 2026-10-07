package com.example.library.controller;

import com.example.library.model.Book;
import com.example.library.model.Member;
import com.example.library.repository.BookRepository;
import com.example.library.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test: exercises Controller -> Service -> Repository -> H2 database.
 * @Transactional rolls back whatever each test inserts.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoanControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    void getAllBooks_returnsSeededBooks() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void borrowBook_endToEnd_returns201AndReducesAvailableCopies() throws Exception {
        Book book = bookRepository.save(new Book("Test Book", "Test Author", "000", 5, 5));
        Member member = memberRepository.save(new Member("Test User", "test.user@example.com"));

        mockMvc.perform(post("/api/loans/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":" + member.getId() + ",\"bookId\":" + book.getId() + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.book.availableCopies").value(4))
                .andExpect(jsonPath("$.member.name").value("Test User"));
    }

    @Test
    void borrowBook_outOfStock_returns409() throws Exception {
        Book book = bookRepository.save(new Book("Empty Shelf", "Nobody", "001", 1, 0));
        Member member = memberRepository.save(new Member("Test User 2", "test.user2@example.com"));

        mockMvc.perform(post("/api/loans/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":" + member.getId() + ",\"bookId\":" + book.getId() + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("No copies of 'Empty Shelf' are currently available"));
    }
}
