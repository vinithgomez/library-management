package com.example.library.config;

import com.example.library.model.Book;
import com.example.library.model.Member;
import com.example.library.repository.BookRepository;
import com.example.library.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/** Inserts sample data into the in-memory H2 database every time the app starts. */
@Component
public class DataLoader implements CommandLineRunner {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public DataLoader(BookRepository bookRepository, MemberRepository memberRepository) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public void run(String... args) {
        if (bookRepository.count() > 0) {
            return;
        }
        bookRepository.save(new Book("Clean Code", "Robert C. Martin", "9780132350884", 3, 3));
        bookRepository.save(new Book("Effective Java", "Joshua Bloch", "9780134685991", 2, 2));
        bookRepository.save(new Book("Head First Java", "Kathy Sierra", "9781491910771", 2, 2));
        bookRepository.save(new Book("Spring in Action", "Craig Walls", "9781617297571", 1, 1));
        // Out-of-stock book: used to demonstrate Rule 1
        bookRepository.save(new Book("Introduction to Algorithms", "Thomas H. Cormen", "9780262046305", 1, 0));

        Member m1 = new Member("Arun Kumar", "arun@example.com");
        m1.setJoinDate(LocalDate.now());
        Member m2 = new Member("Priya Sharma", "priya@example.com");
        m2.setJoinDate(LocalDate.now());
        memberRepository.save(m1);
        memberRepository.save(m2);
    }
}
