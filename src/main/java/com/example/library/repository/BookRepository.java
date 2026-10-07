package com.example.library.repository;

import com.example.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Data Access Layer. Spring Data generates the implementation at runtime. */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
}
