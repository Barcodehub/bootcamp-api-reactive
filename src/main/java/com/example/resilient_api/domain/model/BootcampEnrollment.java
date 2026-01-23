package com.example.resilient_api.domain.model;

import java.time.LocalDateTime;

public record BootcampEnrollment(
        Long id,
        Long bootcampId,
        Long userId,
        LocalDateTime enrolledAt
) {
}
