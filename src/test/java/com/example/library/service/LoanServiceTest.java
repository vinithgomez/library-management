package com.example.library.service;

import com.example.library.exception.BusinessRuleException;
import com.example.library.model.Book;
import com.example.library.model.Loan;
import com.example.library.model.Member;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Unit tests for the business rules in the Service layer (repositories are mocked). */
@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private LoanService loanService;

    private Member member() {
        Member m = new Member("Arun Kumar", "arun@example.com");
        m.setId(1L);
        return m;
    }

    private Book book(int available) {
        Book b = new Book("Clean Code", "Robert C. Martin", "111", 3, available);
        b.setId(10L);
        return b;
    }

    @Test
    void borrowBook_success_reducesCopiesAndSetsDueDate() {
        Book book = book(3);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member()));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(loanRepository.countByMember_IdAndReturnDateIsNull(1L)).thenReturn(0L);
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        Loan loan = loanService.borrowBook(1L, 10L);

        assertEquals(2, book.getAvailableCopies());
        assertEquals(LocalDate.now().plusDays(LoanService.LOAN_PERIOD_DAYS), loan.getDueDate());
        assertEquals(book, loan.getBook());
    }

    @Test
    void borrowBook_noCopiesAvailable_throwsBusinessRuleException() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member()));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book(0)));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> loanService.borrowBook(1L, 10L));

        assertEquals("No copies of 'Clean Code' are currently available", ex.getMessage());
    }

    @Test
    void borrowBook_memberAlreadyHasThreeBooks_throwsBusinessRuleException() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member()));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book(2)));
        when(loanRepository.countByMember_IdAndReturnDateIsNull(1L)).thenReturn(3L);

        assertThrows(BusinessRuleException.class, () -> loanService.borrowBook(1L, 10L));
    }

    @Test
    void returnBook_fourDaysLate_chargesTwentyRupeesAndRestoresCopy() {
        Book book = book(1);
        Loan loan = new Loan();
        loan.setId(5L);
        loan.setBook(book);
        loan.setMember(member());
        loan.setBorrowDate(LocalDate.now().minusDays(18));
        loan.setDueDate(LocalDate.now().minusDays(4));   // due 4 days ago

        when(loanRepository.findById(5L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        Loan returned = loanService.returnBook(5L);

        assertEquals(20.0, returned.getFine(), 0.001);   // 4 days x Rs. 5
        assertEquals(LocalDate.now(), returned.getReturnDate());
        assertEquals(2, book.getAvailableCopies());
    }

    @Test
    void calculateFine_onTime_isZero() {
        LocalDate due = LocalDate.of(2026, 10, 20);
        assertEquals(0.0, loanService.calculateFine(due, due), 0.001);
        assertEquals(0.0, loanService.calculateFine(due, due.minusDays(2)), 0.001);
    }
}
