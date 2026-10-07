package com.example.library.service;

import com.example.library.exception.BusinessRuleException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.model.Member;
import com.example.library.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id " + id));
    }

    /** Business Rule 3: two members cannot register with the same email. */
    public Member registerMember(Member member) {
        if (memberRepository.existsByEmailIgnoreCase(member.getEmail())) {
            throw new BusinessRuleException("A member with email " + member.getEmail() + " already exists");
        }
        member.setId(null);
        member.setJoinDate(LocalDate.now());
        return memberRepository.save(member);
    }
}
