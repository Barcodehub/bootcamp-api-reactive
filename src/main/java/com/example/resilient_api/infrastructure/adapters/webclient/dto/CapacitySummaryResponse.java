package com.example.resilient_api.infrastructure.adapters.webclient.dto;

import com.example.resilient_api.domain.model.TechnologySummary;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapacitySummaryResponse {
    private Long id;
    private String name;
    private List<TechnologySummary> technologies;
}

