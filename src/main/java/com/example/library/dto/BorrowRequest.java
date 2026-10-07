package com.example.library.dto;

import jakarta.validation.constraints.NotNull;

/** Request body for POST /api/loans/borrow */
public record BorrowRequest(
        @NotNull(message = "memberId is required") Long memberId,
        @NotNull(message = "bookId is required") Long bookId) {
}
