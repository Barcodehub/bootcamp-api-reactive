package com.example.resilient_api.domain.model;

import java.util.List;

public record CapacitySummary(
        Long id,
        String name,
        List<TechnologySummary> technologies
) {
}

