package com.example.resilient_api.infrastructure.adapters.webclient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapacityExistsResponse {
    private Map<Long, Boolean> exists;
}
