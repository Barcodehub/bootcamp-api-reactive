package com.example.resilient_api.domain.model;

public record EnrollmentRequest(
        Long bootcampId,
        Long userId
) {
}
