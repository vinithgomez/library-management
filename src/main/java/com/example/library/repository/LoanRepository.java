package com.example.library.repository;

import com.example.library.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    /** Number of books a member currently holds (loans not yet returned). */
    long countByMember_IdAndReturnDateIsNull(Long memberId);

    List<Loan> findByMember_Id(Long memberId);
}
