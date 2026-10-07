package com.example.library.service;

import com.example.library.exception.BusinessRuleException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.model.Book;
import com.example.library.model.Loan;
import com.example.library.model.Member;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Business Layer: all library rules live here (NOT in the controller).
 *
 *  Rule 1: a book can only be borrowed if at least one copy is available.
 *  Rule 2: a member may hold at most 3 books at the same time.
 *  Rule 3: (MemberService) email must be unique.
 *  Rule 4: books are due after 14 days; late returns pay Rs. 5 per day overdue.
 */
@Service
public class LoanService {

    public static final int MAX_ACTIVE_LOANS = 3;
    public static final int LOAN_PERIOD_DAYS = 14;
    public static final double FINE_PER_DAY = 5.0;

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public LoanService(LoanRepository loanRepository,
                       BookRepository bookRepository,
                       MemberRepository memberRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public List<Loan> getLoansForMember(Long memberId) {
        return loanRepository.findByMember_Id(memberId);
    }

    @Transactional
    public Loan borrowBook(Long memberId, Long bookId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id " + memberId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id " + bookId));

        // Rule 1: copy must be available
        if (book.getAvailableCopies() <= 0) {
            throw new BusinessRuleException("No copies of '" + book.getTitle() + "' are currently available");
        }

        // Rule 2: borrowing limit
        long activeLoans = loanRepository.countByMember_IdAndReturnDateIsNull(memberId);
        if (activeLoans >= MAX_ACTIVE_LOANS) {
            throw new BusinessRuleException("Member " + member.getName()
                    + " has already borrowed the maximum of " + MAX_ACTIVE_LOANS + " books");
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setMember(member);
        loan.setBorrowDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(LOAN_PERIOD_DAYS));
        return loanRepository.save(loan);
    }

    @Transactional
    public Loan returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id " + loanId));

        if (loan.getReturnDate() != null) {
            throw new BusinessRuleException("Loan " + loanId + " was already returned");
        }

        LocalDate today = LocalDate.now();
        loan.setReturnDate(today);
        // Rule 4: late fee
        loan.setFine(calculateFine(loan.getDueDate(), today));

        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        return loanRepository.save(loan);
    }

    /** Rs. 5 for every day the book is returned after its due date; 0 if on time. */
    public double calculateFine(LocalDate dueDate, LocalDate returnDate) {
        long daysLate = ChronoUnit.DAYS.between(dueDate, returnDate);
        return daysLate > 0 ? daysLate * FINE_PER_DAY : 0.0;
    }
}
